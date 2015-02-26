package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * Uses a thread pool to ensure that none of its methods are blocking.
 */
public class TPCRegistrationHandler implements NetworkHandler {

    private ThreadPool threadpool;
    private TPCMaster master;

    /**
     * Constructs a TPCRegistrationHandler with a ThreadPool of a single thread.
     *
     * @param master TPCMaster to register slave with
     */
    public TPCRegistrationHandler(TPCMaster master) {
        this(master, 1);
    }

    /**
     * Constructs a TPCRegistrationHandler with ThreadPool of thread equal to the
     * number given as connections.
     *
     * @param master TPCMaster to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public TPCRegistrationHandler(TPCMaster master, int connections) {
        this.threadpool = new ThreadPool(connections);
        this.master = master;
    }
    
    /**
     * Stops all thread execution in the current client's ThreadPool.
     */
    public void stop() {
    	this.threadpool.stop();
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param slave Socket connected to the slave with the request
     */
    @Override
    public void handle(Socket slave) {
        final Socket final_slave = slave;
        
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    KVMessage message = null;
                    KVMessage reply = new KVMessage(RESP);
                    
                    try {
                        message = new KVMessage(final_slave);
                    } catch (KVException e) {
                        reply = e.getKVMessage();
                    }
                    
                    if(message.getMsgType().equals(REGISTER)) {
                        try {
                            master.registerSlave(new TPCSlaveInfo(message.getMessage()));
                            reply.setMessage("Successfully registered "+ message.getMessage());  
                        } catch (KVException e) {
                            reply = e.getKVMessage();
                        }
                    }
                    
                    try {
                        reply.sendMessage(final_slave);
                    } catch (KVException e) {
                        //TODO: ignore?
                    }
                }
            };
            
            threadpool.addJob(runnable);
            
        } catch (InterruptedException e) {
         // TODO: ignore?
        }
    }

}
