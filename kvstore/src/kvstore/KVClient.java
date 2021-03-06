package kvstore;

import static kvstore.KVConstants.DEL_REQ;
import static kvstore.KVConstants.ERROR_COULD_NOT_CONNECT;
import static kvstore.KVConstants.ERROR_COULD_NOT_CREATE_SOCKET;
import static kvstore.KVConstants.ERROR_INVALID_KEY;
import static kvstore.KVConstants.ERROR_INVALID_VALUE;
import static kvstore.KVConstants.GET_REQ;
import static kvstore.KVConstants.PUT_REQ;
import static kvstore.KVConstants.RESP;
import static kvstore.KVConstants.SUCCESS;
import static kvstore.KVConstants.ERROR_NO_SUCH_KEY;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client API used to issue requests to key-value server.
 */
public class KVClient implements KeyValueInterface {

    public String server;
    public int port;

    /**
     * Constructs a KVClient connected to a server.
     *
     * @param server is the DNS reference to the server
     * @param port is the port on which the server is listening
     */
    public KVClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    /**
     * Creates a socket connected to the server to make a request.
     *
     * @return Socket connected to server
     * @throws KVException if unable to create or connect socket
     */
    public Socket connectHost() throws KVException {
        /* begin */
        Socket socket;
        try {
            socket = new Socket(server, port);
            return socket;
        } catch (IOException e) {
            throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);
        }
        /* end */
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     * @param  sock Socket to be closed
     */
    public void closeHost(Socket sock) {
        /* begin */
        try {
            sock.close();
        } catch (IOException e) {
        	// BEST EFFORT: IGNORE
            return;
        }
        /* end */
    }

    /**
     * Issues a PUT request to the server.
     *
     * @param  key String to put in server as key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void put(String key, String value) throws KVException {
        /* begin */
        Socket socket = connectHost();
        try {
            KVMessage message = new KVMessage(PUT_REQ);
            if (key == null) {
            	throw new KVException(ERROR_INVALID_KEY);
            }
            message.setKey(key);
            if (value == null) {
            	throw new KVException(ERROR_INVALID_VALUE);
            }
            message.setValue(value);
            message.sendMessage(socket);
            
            KVMessage response = new KVMessage(socket);
            
            String msg = response.getMessage();
            if (msg == null) {
                throw new KVException(ERROR_NO_SUCH_KEY);
            } else if (!msg.equals(SUCCESS)) {
                throw new KVException(msg);
            }
        } finally {
            closeHost(socket);
        }
        /* end */
    }

    /**
     * Issues a GET request to the server.
     *
     * @param  key String to get value for in server
     * @return String value associated with key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public String get(String key) throws KVException {
        /* begin */
        Socket socket = connectHost();
        try {
            KVMessage message = new KVMessage(GET_REQ);
            if (key == null) {
            	throw new KVException(ERROR_INVALID_KEY);
            }
            
            message.setKey(key);
            message.sendMessage(socket);
            
            KVMessage response = new KVMessage(socket);
            
            String val = response.getValue();
            String msg = response.getMessage();
            if (val == null) {
                if (msg == null) {
                    throw new KVException(ERROR_NO_SUCH_KEY);
                } else {
                    throw new KVException(msg);
                }
            } 
            return val;
        } finally {
            closeHost(socket);
        }
        /* end */
    }

    /**
     * Issues a DEL request to the server.
     *
     * @param  key String to delete value for in server
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void del(String key) throws KVException {
        /* begin */
        Socket socket = connectHost();
        try {
            KVMessage message = new KVMessage(DEL_REQ);
            if (key == null) {
            	throw new KVException(ERROR_INVALID_KEY);
            }
            
            message.setKey(key);
            message.sendMessage(socket);
            
            KVMessage response = new KVMessage(socket);
            
            String msg = response.getMessage();
            if (msg == null) {
                throw new KVException(ERROR_NO_SUCH_KEY);
            } else if (!msg.equals(SUCCESS)) {
                throw new KVException(msg);
            }
        } finally {
            closeHost(socket);
        }
        /* end */
    }

}
