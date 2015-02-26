package kvstore;

import static org.junit.Assert.*;

import java.net.Socket;

import org.junit.*;

import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Socket.class, KVMessage.class, TPCMasterHandler.class, TPCSlaveInfo.class, TPCMaster.class})

public class TPCMasterTest2 {
    
    TPCMaster master;
    KVCache masterCache;
    
    @Ignore
    @Test
    public void BadMasterTest() throws KVException {
        long SLAVE1 = 0;
        long SLAVE2 = 9223372036854775807L;
        
        masterCache = new KVCache(5,5);
        master = new TPCMaster(0, masterCache);
        
        TPCSlaveInfo slave1 = new TPCSlaveInfo(SLAVE1 + "@111.111.111.111:1");
        TPCSlaveInfo slave2 = new TPCSlaveInfo(SLAVE2 + "@111.111.111.111:2");
        
        master.registerSlave(slave1);
        master.registerSlave(slave2);
        
        assertEquals(master.getNumRegisteredSlaves(), 0);
        
        master = new TPCMaster(-1, masterCache);
        
        master.registerSlave(slave1);
        master.registerSlave(slave2);
        
        assertEquals(master.getNumRegisteredSlaves(), 0);  
    }
    
    @Test 
    public void BadFindReplicaTest1() throws KVException {

        masterCache = new KVCache(5,5);
        master = new TPCMaster(0, masterCache);
        
        TPCSlaveInfo firstReplica = master.findFirstReplica("key");
        
        assertEquals(firstReplica, null);
    }
    
    @Test
    public void BadFindReplicaTest2() throws KVException {
        long SLAVE1 = 0;
        @SuppressWarnings("unused")
        TPCSlaveInfo firstReplica;
        masterCache = new KVCache(5,5);
        master = new TPCMaster(0, masterCache);
        
        TPCSlaveInfo slave1 = new TPCSlaveInfo(SLAVE1 + "@111.111.111.111:1");
        master.registerSlave(slave1);
        
        try {
            firstReplica = master.findFirstReplica(null);
            fail("Should not have accepted null key");
        } catch (NullPointerException e) {
            
        }
    }
    
    @Test 
    public void BadSuccessorTest() throws KVException {
        long SLAVE1 = 0;
        TPCSlaveInfo slave1 = new TPCSlaveInfo(SLAVE1 + "@111.111.111.111:1");
        
        masterCache = new KVCache(5,5);
        master = new TPCMaster(0, masterCache);
        
        TPCSlaveInfo successor = master.findSuccessor(slave1);
        
        assertEquals(successor, null);
    }
}
