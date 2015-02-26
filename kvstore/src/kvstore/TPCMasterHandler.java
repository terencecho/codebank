package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * Implements NetworkHandler to handle 2PC operation requests from the Master/
 * Coordinator Server
 */
public class TPCMasterHandler implements NetworkHandler {

    public long slaveID;
    public KVServer kvServer;
    public TPCLog tpcLog;
    public ThreadPool threadpool;

    // implement me

    /**
     * Constructs a TPCMasterHandler with one connection in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log) {
        this(slaveID, kvServer, log, 1);
    }

    /**
     * Constructs a TPCMasterHandler with a variable number of connections
     * in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     * @param connections the number of connections in this slave's ThreadPool
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log, int connections) {
        this.slaveID = slaveID;
        this.kvServer = kvServer;
        this.tpcLog = log;
        this.threadpool = new ThreadPool(connections);
    }
    
    /**
     * Stops all thread execution in the current client's ThreadPool.
     */
    public void stop() {
    	this.threadpool.stop();
    }

    /**
     * Registers this slave server with the master.
     *
     * @param masterHostname
     * @param server SocketServer used by this slave server (which contains the
     *               hostname and port this slave is listening for requests on
     * @throws KVException with ERROR_INVALID_FORMAT if the response from the
     *         master is received and parsed but does not correspond to a
     *         success as defined in the spec OR any other KVException such
     *         as those expected in KVClient in project 3 if unable to receive
     *         and/or parse message
     */
    public void registerWithMaster(String masterHostname, SocketServer server)
            throws KVException {
        Socket socket;
        String slaveInfo = Long.toString(slaveID) + "@" + server.getHostname() + ":" + Integer.toString(server.getPort());
        
        try {
            socket = new Socket(masterHostname, 9090);
        } catch (UnknownHostException e) {
            throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);
        } catch (IOException e) {
            throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);
        }
        
        try {
            
            KVMessage request = new KVMessage(REGISTER);
            KVMessage reply;
            
            request.setMessage(slaveInfo);
            request.sendMessage(socket);
            
            reply = new KVMessage(socket);
            
            String type = reply.getMsgType();
            String message = reply.getMessage();
            
            if(!type.equals(RESP) || !message.equals("Successfully registered " + slaveInfo)) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
            
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                
            }
        }
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param master Socket connected to the master with the request
     */
    @Override
    public void handle(Socket master) {
        // implement me
        final Socket finalMaster = master;
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    KVMessage message;
                    KVMessage reply = new KVMessage(RESP);
                    String key;
                    String value;
                    try {
                        message = new KVMessage(finalMaster);
                        String type = message.getMsgType();
                        switch (type) {
                            case GET_REQ:
                                key = message.getKey();
                                value = kvServer.get(key);
                                if (value == null) {
                                    throw new KVException(ERROR_NO_SUCH_KEY);
                                } else {
                                	reply.setKey(key);
                                    reply.setValue(value);
                                }
                                break;
                            case PUT_REQ:
                            	//check valid params and prepare votes
                            	key = message.getKey();
                                value = message.getValue();
								if (key == null) {
								    reply = new KVMessage(ABORT, ERROR_INVALID_KEY);
								}
								else if (value == null) {
								    reply = new KVMessage(ABORT, ERROR_INVALID_VALUE);
								}
								else if (key.length() > 256) {
								    reply = new KVMessage(ABORT, ERROR_OVERSIZED_KEY);
								}
								else if(value.length() > 256*1024) {
								    reply = new KVMessage(ABORT, ERROR_OVERSIZED_KEY);
								}
								else {
								    reply = new KVMessage(READY);
								}
								
								//log
								tpcLog.appendAndFlush(message);
								break;
                            case DEL_REQ:
								//check valid and prepare votes
								key = message.getKey();
								if (key == null) {
								    reply = new KVMessage(ABORT, ERROR_INVALID_KEY);
								}
								else if (!kvServer.hasKey(key)) {
								    reply = new KVMessage(ABORT, ERROR_NO_SUCH_KEY);
								}
								else {
								    reply = new KVMessage(READY);
								}
								//log
								tpcLog.appendAndFlush(message);
								break;
                            case COMMIT:
                                
								//execute last action 
								KVMessage lastAct = tpcLog.getLastEntry();
								
								//log
                                tpcLog.appendAndFlush(message);
								switch (lastAct.getMsgType()) {
									case PUT_REQ:
										kvServer.put(lastAct.getKey(), lastAct.getValue());
										break;
									case DEL_REQ:
										kvServer.del(lastAct.getKey());
										break;
									default:
										break;
								}
								
								//prepare ack
								reply = new KVMessage(ACK);
								break;
                            case ABORT:
                            	//log abort
                            	tpcLog.appendAndFlush(message);
                            	//prepare ack
                            	reply = new KVMessage(ACK);
                            	break;
                            default:
                                throw new KVException(ERROR_INVALID_FORMAT);
                        }
                    } catch (KVException e) {
                        reply = e.getKVMessage();
                    }
                    
                    try {
                        reply.sendMessage(finalMaster);
                    } catch (KVException e) {
                        //TODO: ignore?
                    }
                }
            };
            threadpool.addJob(runnable);
        } catch (InterruptedException e) {
            //TODO: ignore??
        }
    }
}
