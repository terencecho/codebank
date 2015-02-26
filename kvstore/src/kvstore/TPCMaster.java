package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TPCMaster {

    public int numSlaves;
    public KVCache masterCache;
    
    public LinkedList<TPCSlaveInfo> slaves;
    public HashMap<Long, TPCSlaveInfo> mappings;

    public static final int TIMEOUT = 3000;
    
    public Lock slaveLock;
    public int registeredSlaves;

    /**
     * Creates TPCMaster, expecting numSlaves slave servers to eventually register
     *
     * @param numSlaves number of slave servers expected to register
     * @param cache KVCache to cache results on master
     */
    public TPCMaster(int numSlaves, KVCache cache) {
        this.numSlaves = numSlaves;
        this.masterCache = cache;
        // implement me
        this.slaves = new LinkedList<>();
        this.mappings = new HashMap<>();
        
        this.slaveLock = new ReentrantLock();
        this.registeredSlaves = 0;
        
    }

    /**
     * Registers a slave. Drop registration request if numSlaves already
     * registered. Note that a slave re-registers under the same slaveID when
     * it comes back online.
     *
     * @param slave the slaveInfo to be registered
     */
    public void registerSlave(TPCSlaveInfo slave) {
        
        //System.out.println("in master: registering slave");
        //System.out.println("PORT: " + Integer.toString(slave.getPort()));
        
        if (mappings.containsKey(slave.getSlaveID())) {
            mappings.put(slave.getSlaveID(), slave);
            TPCSlaveInfo current;
            for (int i = 0; i < slaves.size(); i++) {
                current = slaves.get(i);
                if (current.getSlaveID() == slave.getSlaveID()) {
                    slaves.remove(i);
                    slaves.add(i, slave);
                    break;
                }
            }
            return;
        }
        
        if (slaves.size() >= numSlaves) {
            return;
        } else if (slaves.size() == 0) {
            registeredSlaves += 1;
            slaves.add(slave);
            mappings.put(slave.getSlaveID(), slave);
            synchronized (slaveLock) {
                if (registeredSlaves >= numSlaves) {
                    slaveLock.notifyAll();
                }
            }
            return;
        } else {
            registeredSlaves += 1;
            for (int i = 0; i < slaves.size(); i++) {
                if (!isLessThanUnsigned(slaves.get(i).getSlaveID(), slave.getSlaveID())) {
                    slaves.add(i, slave);
                    mappings.put(slave.getSlaveID(), slave);
                    synchronized (slaveLock) {
                        if (registeredSlaves >= numSlaves) {
                            slaveLock.notifyAll();
                        }
                    }
                    return;
                }
            }
            slaves.addLast(slave);
            mappings.put(slave.getSlaveID(), slave);
            synchronized (slaveLock) {
                if (registeredSlaves >= numSlaves) {
                    slaveLock.notifyAll();
                }
            }
            return;
        }
    }

    /**
     * Converts Strings to 64-bit longs. Borrowed from http://goo.gl/le1o0W,
     * adapted from String.hashCode().
     *
     * @param string String to hash to 64-bit
     * @return long hashcode
     */
    public static long hashTo64bit(String string) {
        long h = 1125899906842597L;
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = (31 * h) + string.charAt(i);
        }
        return h;
    }

    /**
     * Compares two longs as if they were unsigned (Java doesn't have unsigned
     * data types except for char). Borrowed from http://goo.gl/QyuI0V
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than unsigned n2
     */
    public static boolean isLessThanUnsigned(long n1, long n2) {
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    /**
     * Compares two longs as if they were unsigned, uses isLessThanUnsigned
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than or equal to unsigned n2
     */
    public static boolean isLessThanEqualUnsigned(long n1, long n2) {
        return isLessThanUnsigned(n1, n2) || (n1 == n2);
    }

    /**
     * Find primary replica for a given key.
     *
     * @param key String to map to a slave server replica
     * @return SlaveInfo of first replica
     */
    public TPCSlaveInfo findFirstReplica(String key) {
        
        Long keyLong = hashTo64bit(key);

        if (slaves.size() == 0) {
            return null;
        }
        
        if (slaves.size() == 1) {
            return slaves.get(0);
        }
        
        for (int i = 0; i < slaves.size(); i++) {
            if (isLessThanEqualUnsigned(keyLong, slaves.get(i).getSlaveID())) {
                return slaves.get(i);
            }
        }
        
        return slaves.get(0);
    }

    /**
     * Find the successor of firstReplica.
     *
     * @param firstReplica SlaveInfo of primary replica
     * @return SlaveInfo of successor replica
     */
    public TPCSlaveInfo findSuccessor(TPCSlaveInfo firstReplica) {

        if (slaves.size() == 0) {
            return null;
        }
        
        if (slaves.size() == 1) {
            return slaves.get(0);
        }
        
        Long slaveId = firstReplica.getSlaveID();
        
        for (int i = 0; i < slaves.size(); i++) {
            if (isLessThanUnsigned(slaveId, slaves.get(i).getSlaveID())) {
                return slaves.get(i);
            }
        }
        
        return slaves.get(0);

    }

    /**
     * @return The number of slaves currently registered.
     */
    public int getNumRegisteredSlaves() {
        return slaves.size();
    }

    /**
     * (For testing only) Attempt to get a registered slave's info by ID.
     * @return The requested TPCSlaveInfo if present, otherwise null.
     */
    public TPCSlaveInfo getSlave(long slaveId) {
        return mappings.get(slaveId);
    }

    /**
     * Perform 2PC operations from the master node perspective. This method
     * contains the bulk of the two-phase commit logic. It performs phase 1
     * and phase 2 with appropriate timeouts and retries.
     *
     * See the spec for details on the expected behavior.
     *
     * @param msg KVMessage corresponding to the transaction for this TPC request
     * @param isPutReq boolean to distinguish put and del requests
     * @throws KVException if the operation cannot be carried out for any reason
     */
    public synchronized void handleTPCRequest(KVMessage msg, boolean isPutReq)
            throws KVException {
        // implement me
        synchronized (slaveLock) {
            while (registeredSlaves < numSlaves) {
                try {
                    slaveLock.wait();
                } catch (InterruptedException e) {
                   
                }
            }
        }
        
        boolean abort = false;
        LinkedList<TPCSlaveInfo> workingSlaves = new LinkedList<>();
        TPCSlaveInfo firstReplica = findFirstReplica(msg.getKey());
        TPCSlaveInfo successor = null;
        workingSlaves.add(firstReplica);
        workingSlaves.add(findSuccessor(firstReplica));
        
        //Sequentially go through slaves and see if any of them abort
        for (TPCSlaveInfo slaveInfo : workingSlaves) {
            KVMessage reply;
            KVMessage msgCopy = new KVMessage(msg);
            Socket socket = slaveInfo.connectHost(TIMEOUT);
            msgCopy.sendMessage(socket);
            try {                
                reply = new KVMessage(socket, TIMEOUT);
                if (reply.getMsgType().equals(ABORT)) {
                    abort = true;
                } 
            //timed out slave interpreted as an abort
            } catch (KVException e) {
                abort = true;
                continue;
            } finally {
                slaveInfo.closeHost(socket);
            }
        }
        
        
        //abort
        if (abort) { 
            Socket socket = null;
            while (true) {
                try {
                    firstReplica = findFirstReplica(msg.getKey());
                    //System.out.println(Long.toString(firstReplica.getSlaveID()) + "@" + firstReplica.getHostname() + ":" + Integer.toString(firstReplica.getPort()));
                    KVMessage abortMsg = new KVMessage(ABORT);
                    socket = firstReplica.connectHost(TIMEOUT);
                    abortMsg.sendMessage(socket);
                    KVMessage reply = new KVMessage(socket, TIMEOUT);
                    if (!reply.getMsgType().equals(ACK)) {
                        throw new KVException(ERROR_INVALID_FORMAT);
                    }
                    break;
                } catch (KVException e) {
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e1) {
                        
                    }
                    continue;
                } finally {
                    firstReplica.closeHost(socket);
                }
            }
            Socket socket2 = null;
            while (true) {
                try {
                    successor = findSuccessor(firstReplica);
                    KVMessage abortMsg2 = new KVMessage(ABORT);
                    socket2 = successor.connectHost(TIMEOUT);
                    abortMsg2.sendMessage(socket2);
                    KVMessage reply2 = new KVMessage(socket2, TIMEOUT);
                    if (!reply2.getMsgType().equals(ACK)) {
                        throw new KVException(ERROR_INVALID_FORMAT);
                    }
                    break;
                    
                }
                catch (KVException e) {
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e1) {
                        
                    }
                    continue;
                } finally {
                    successor.closeHost(socket2);
                }
            }
            
            
            
        } else {
            Socket socket = null;
            while (true) {
                try {
                    firstReplica = findFirstReplica(msg.getKey());
                    KVMessage abortMsg = new KVMessage(COMMIT);
                    socket = firstReplica.connectHost(TIMEOUT);
                    abortMsg.sendMessage(socket);
                    KVMessage reply = new KVMessage(socket, TIMEOUT);
                    if (!reply.getMsgType().equals(ACK)) {
                        throw new KVException(ERROR_INVALID_FORMAT);
                    }
                    break;
                } catch (KVException e) {
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e1) {
                        
                    }
                    continue;
                } finally {
                    firstReplica.closeHost(socket);
                }
            }
            Socket socket2 = null;
            while (true) {
                try {
                    successor = findSuccessor(firstReplica);
                    KVMessage abortMsg2 = new KVMessage(COMMIT);
                    socket2 = successor.connectHost(TIMEOUT);
                    abortMsg2.sendMessage(socket2);
                    KVMessage reply2 = new KVMessage(socket2, TIMEOUT);
                    if (!reply2.getMsgType().equals(ACK)) {
                        throw new KVException(ERROR_INVALID_FORMAT);
                    }
                    break;
                    
                }
                catch (KVException e) {
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e1) {
                        
                    }
                    continue;
                } finally {
                    successor.closeHost(socket2);
                }
            }
            
            if (isPutReq) {
                masterCache.put(msg.getKey(), msg.getValue());
            } else {
                masterCache.del(msg.getKey());
            }
        }
        
        if (abort) {
            throw new KVException(ERROR_INVALID_FORMAT);
        }
        
        
        
    }

    /**
     * Perform GET operation in the following manner:
     * - Try to GET from cache, return immediately if found
     * - Try to GET from first/primary replica
     * - If primary succeeded, return value
     * - If primary failed, try to GET from the other replica
     * - If secondary succeeded, return value
     * - If secondary failed, return KVExceptions from both replicas
     *
     * @param msg KVMessage containing key to get
     * @return value corresponding to the Key
     * @throws KVException with ERROR_NO_SUCH_KEY if unable to get
     *         the value from either slave for any reason
     */
    public String handleGet(KVMessage msg) throws KVException {
        
        synchronized (slaveLock) {
            while (registeredSlaves < numSlaves) {
                try {
                    slaveLock.wait();
                } catch (InterruptedException e) {
                   
                }
            }
        }
                
        if (masterCache.get(msg.getKey()) != null) {
            return masterCache.get(msg.getKey());
        }
                
        TPCSlaveInfo slaveInfo = findFirstReplica(msg.getKey());
        Socket socket = null;
        KVMessage message = null;
        KVMessage reply = null;
        
        try {
            socket = slaveInfo.connectHost(TIMEOUT);
            message = new KVMessage(GET_REQ);
            message.setKey(msg.getKey());
            message.sendMessage(socket);
            
            reply = new KVMessage(socket, TIMEOUT);
            
            if (reply != null) {
                String value = reply.getValue();
                socket.close();
                masterCache.put(msg.getKey(), value);
                return value;
            }
            
        } catch (IOException e) {
            
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                
            }
        }
                
        TPCSlaveInfo slaveInfo2 = findSuccessor(slaveInfo);
        Socket socket2 = null;
        KVMessage message2 = null;
        KVMessage reply2 = null;
        
        try {
            socket2 = slaveInfo2.connectHost(TIMEOUT);
            message2 = new KVMessage(GET_REQ);
            message2.setKey(msg.getKey());
            message2.sendMessage(socket2);
            
            reply2 = new KVMessage(socket2, TIMEOUT);
            
            if (reply2 != null) {
                String value2 = reply2.getValue();
                socket2.close();
                masterCache.put(msg.getKey(), value2);
                return value2;
            }
            
        } catch (IOException e) {
            
        } finally {
            try {
                if (socket2 != null) {
                    socket2.close();
                }
            } catch (IOException e) {
                
            }
        }
        
        throw new KVException(ERROR_NO_SUCH_KEY);
    }

}
