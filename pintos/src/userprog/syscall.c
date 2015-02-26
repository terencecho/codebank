#include "userprog/syscall.h"
#include <stdio.h>
#include <syscall-nr.h>
#include "threads/interrupt.h"
#include "threads/thread.h"
#include "devices/shutdown.h"
#include "threads/vaddr.h"
#include "threads/synch.h"
#include "filesys/file.h"
#include <stdbool.h>

static void syscall_handler (struct intr_frame *);
bool check_exec_address(const void* address);
void sys_exit(int status, struct thread *t, struct intr_frame *f);

//Lock to prevent race conditions during syscalls/file accesses
struct lock file_lock;

/*
Before executing any syscall, we must validate the addresses of all args before dereferencing them
In the event the arg is a pointer, we must also dereference the pointer and check that the derferenced
pointer points to a valid address
*/

void
syscall_init (void) 
{
  lock_init(&file_lock);
  intr_register_int (0x30, 3, INTR_ON, syscall_handler, "syscall");
}

static void
syscall_handler (struct intr_frame *f UNUSED) 
{
  uint32_t* args = ((uint32_t*) f->esp);
  struct thread *t = thread_current();

  //Check to make sure args address is valid
  if(!check_exec_address(args)){
    sys_exit(-1, t, f);
  }

  //Check to make sure esp is valid
  if(!check_exec_address(f->esp)){
    sys_exit(-1, t, f);
  }
  
  switch(args[0])
    {
      case SYS_EXIT:
        {
          if (!check_exec_address(args + 1)) {
            sys_exit(-1, t, f);
          } else {
            sys_exit(args[1], t, f);
          }
          break;
        }
      case SYS_NULL:
        {
          f->eax = args[1] + 1;
          break;
        }
      case SYS_WRITE:
        {
          if (args[1] == 0 || args[1] < 0 || args[1] > 127 ||
            !check_exec_address(args) ||
            !check_exec_address(args + 1) ||
            !check_exec_address(args + 2) ||
            !check_exec_address(args + 3) ||
            !check_exec_address(args[2])) {
            sys_exit(-1, t, f);
            break;            
          } else {
            lock_acquire (&file_lock);
            if (args[1] == 1) {
              putbuf(args[2], args[3]);
              f->eax = args[3];
            } else {
              struct file_descriptor *file_descriptor = t->file_descriptors[args[1]];
              if(file_descriptor == NULL){
                f->eax = -1;
                lock_release(&file_lock);
                break;
              }
              struct file *file = file_descriptor->file;
              if (file){
                int write = file_write (file, args[2], args[3]);
                f->eax = write;
              } else {
                f->eax = -1;
              }
            }
            lock_release (&file_lock);
          }
          break;
        }
      case SYS_HALT:
        {
          shutdown_power_off();
          break; 
        }
      case SYS_EXEC:
        {
          if(check_exec_address(args[1])){
            f->eax = process_execute(args[1]);
            break; 
          } else {
            sys_exit(-1, t, f);
            break; 
          }    
        }
      case SYS_WAIT:
        { 
          f->eax = process_wait(args[1]);
          break;
        }
      case SYS_CREATE:
        {
          if(!check_exec_address(args) ||
             !check_exec_address(args + 1) ||
             !check_exec_address(args + 2) ||
             !check_exec_address(args[1])){
            sys_exit(-1, t, f);
          } else {
            lock_acquire(&file_lock);
            bool success = filesys_create(args[1], args[2]);
            lock_release(&file_lock);
            f->eax = success;
          }
          break;
        }
      case SYS_REMOVE:
        {
          if(!check_exec_address(args) ||
             !check_exec_address(args + 1) ||
             !check_exec_address(args[1])){
            sys_exit(-1, t, f);
          } else {
            lock_acquire(&file_lock);
            bool success = filesys_remove(args[1]);
            lock_release(&file_lock);
            f->eax = success;
          }
          break;
        }
      case SYS_OPEN:
        {
          if(!check_exec_address(args) ||
             !check_exec_address(args + 1) ||
             !check_exec_address(args[1])){
            sys_exit(-1, t, f);
          } else {
            lock_acquire(&file_lock);
            struct file *file = filesys_open(args[1]);
            if(file){
              //Each new file must have a file descriptor
              struct file_descriptor *fd;
              fd = malloc(sizeof(struct file_descriptor));

              int i;
              int fd_num = -1;

              //Loop through to find an open file descriptor slot
              for(i = 2; i < t->file_descriptor_num; i++){
                if(t->file_descriptors[i] == NULL){
                  fd_num = i;
                  break;
                }
              }

              if(fd_num == -1){
                fd_num = t->file_descriptor_num;
                t->file_descriptor_num++;
              }

              fd->file = file;
              fd->id = fd_num;
              t->file_descriptors[fd_num] = fd;
              f->eax = fd_num;
            } else { 
              f->eax = -1;
            }
            lock_release(&file_lock);
          }
          break;
        }
      case SYS_FILESIZE:
        {
          int size;
          lock_acquire(&file_lock);
          struct file *file = t->file_descriptors[args[1]]->file;
          if(file){
            size = file_length(file);
            f->eax = size;
          } else {
            f->eax = -1;
          }
          lock_release(&file_lock);
          break;
        }
      case SYS_READ:
        {
          if(args[1] == 1 || args[1] < 0 || args[1] > 127 ||
            !check_exec_address(args) ||
            !check_exec_address(args + 1) ||
            !check_exec_address(args + 2) ||
            !check_exec_address(args + 3) ||
            !check_exec_address(args[2])){
            sys_exit(-1, t, f);
          } else {
            lock_acquire(&file_lock);
            if(args[1] == 0){
              int i;
              uint8_t *buffer = (uint8_t)args[2];
              for(i = 0; i < args[3]; i++){
                buffer[i] = input_getc();
              }
              f->eax = args[3];
            } else {
              struct file_descriptor *file_descriptor = t->file_descriptors[args[1]];
              if(file_descriptor == NULL){
                f->eax = -1;
                lock_release(&file_lock);
                break;
              }
              struct file *file = file_descriptor->file;
              if(file){
                int read = file_read(file, args[2], args[3]);
                f->eax = read;
              } else {
                f->eax = -1;
              }
            }
            lock_release(&file_lock);
          }
          break;
        }
      case SYS_SEEK:
        {
          if(args[1] < 0 || args[1] > 127){
            break;
          }
          lock_acquire(&file_lock);
          struct file *file = t->file_descriptors[args[1]]->file;
          if (file) {
            file_seek(file, (off_t) args[2]);
          } else {
            f->eax = -1;
          }
          lock_release(&file_lock);
          break;
        }
      case SYS_TELL:
        {
          if(args[1] < 0 || args[1] > 127){
            break;
          }
          lock_acquire(&file_lock);
          struct file *file = t->file_descriptors[args[1]]->file;
          if (file) {
            f->eax = file_tell(file);
          } else {
            f->eax = -1;
          }
          lock_release(&file_lock);
          break;
        }
      case SYS_CLOSE:
        {
          if(args[1] < 0 || args[1] > 127){
            break;
          }
          lock_acquire(&file_lock);
          struct file_descriptor *file_descriptor = t->file_descriptors[args[1]];
          if(file_descriptor){
            struct file *file = file_descriptor->file;
            if(file){
              file_close(file);
            }
            free(file_descriptor);
            t->file_descriptors[args[1]] = NULL;
          }
          lock_release(&file_lock);
          break;
        }
      default:
        break;
    }
}

//Wrapper function for checkpoint 2, calls everything needed to exit safely
void sys_exit(int status, struct thread *t, struct intr_frame *f){
  if (t->wait_status != NULL) {
      t->wait_status->exit_code = status;
  }
  printf("%s: exit(%d)\n", t->name, status);
  f->eax = status;
  thread_exit();
}

//Function for validating an address. Ensures the address is within the bounds of user addresses
//and the address is non-NULL
bool check_exec_address(const void* address){
  struct thread *t = thread_current();
  void *page;

  if(!is_user_vaddr(address) || address + 1 > PHYS_BASE || address == NULL){
    return false; 
  } else {
    page = pagedir_get_page(t->pagedir, address);
    if(!page){
      return false;
    }
    return true;
  }
}


