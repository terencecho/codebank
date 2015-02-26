package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * It uses a threadPool to ensure that none of it's methods are blocking.
 */
public class TPCClientHandler implements NetworkHandler {

    public TPCMaster tpcMaster;
    public ThreadPool threadPool;

    /**
     * Constructs a TPCClientHandler with ThreadPool of a single thread.
     *
     * @param tpcMaster TPCMaster to carry out requests
     */
    public TPCClientHandler(TPCMaster tpcMaster) {
        this(tpcMaster, 1);
    }

    /**
     * Constructs a TPCClientHandler with ThreadPool of a single thread.
     *
     * @param tpcMaster TPCMaster to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public TPCClientHandler(TPCMaster tpcMaster, int connections) {
        // implement me
        this.tpcMaster = tpcMaster;
        this.threadPool = new ThreadPool(connections);
    }
    
    /**
     * Stops all thread execution in the current client's ThreadPool.
     */
    public void stop() {
    	this.threadPool.stop();
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
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
                                tpcMaster.handleTPCRequest(message, false);  
                                break;
                            case PUT_REQ:
                                reply.setMessage(SUCCESS);
                                tpcMaster.handleTPCRequest(message, true);
                                break;
                            case GET_REQ:
                                reply.setKey(message.getKey());
                                String value = tpcMaster.handleGet(message);
                                if (value == null) {
                                	reply.setMessage(ERROR_NO_SUCH_KEY);
                                } else {
                                    reply.setValue(value);

                                }
                                
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
    
    // implement me

}
