package kvstore;

import static kvstore.KVConstants.DEL_REQ;
import static kvstore.KVConstants.GET_REQ;
import static kvstore.KVConstants.ERROR_INVALID_FORMAT;
import static kvstore.KVConstants.PUT_REQ;
import static kvstore.KVConstants.RESP;
import static kvstore.KVConstants.SUCCESS;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * Uses a thread pool to ensure that none of its methods are blocking.
 */
public class ServerClientHandler implements NetworkHandler {

    public KVServer kvServer;
    public ThreadPool threadPool;

    /**
     * Constructs a ServerClientHandler with ThreadPool of a single thread.
     *
     * @param kvServer KVServer to carry out requests
     */
    public ServerClientHandler(KVServer kvServer) {
        this(kvServer, 1);
    }

    /**
     * Constructs a ServerClientHandler with ThreadPool of thread equal to
     * the number passed in as connections.
     *
     * @param kvServer KVServer to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public ServerClientHandler(KVServer kvServer, int connections) {
        /* begin */
        this.kvServer = kvServer;
        this.threadPool = new ThreadPool(connections);
        /* end */
    }
    
    /**
     * Stops all thread execution in the current client's ThreadPool.
     */
    public void stop() {
    	this.threadPool.stop();
    }

    /**
     * Creates a job to service the request for a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
       /* begin */
        final Socket final_client = client;
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    KVMessage message;
                    KVMessage reply = new KVMessage(RESP);
                    try {
                        message = new KVMessage(final_client);
                        String msg_type = message.getMsgType();
                        switch(msg_type){
                            case DEL_REQ:
                                reply.setMessage(SUCCESS);
                                kvServer.del(message.getKey());  
                                break;
                            case PUT_REQ:
                                reply.setMessage(SUCCESS);
                                kvServer.put(message.getKey(), message.getValue());
                                break;
                            case GET_REQ:
                                reply.setKey(message.getKey());
                                String value = kvServer.get(message.getKey());
                                reply.setValue(value);
                                break;
                            default:
                                throw new KVException(ERROR_INVALID_FORMAT);
                        }
                    } catch (KVException e) {
                        reply = e.getKVMessage();
                        
                    }
                    try {
                        reply.sendMessage(final_client);
                    } catch (KVException e) {
                        // TODO: ???
                    }
                }
            };
            threadPool.addJob(runnable);
        } catch (InterruptedException e) {
           // TODO: ignore?
        }
    }
}
