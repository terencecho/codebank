package kvstore;

import static org.junit.Assert.*;

import java.net.Socket;

import org.junit.*;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Socket.class, KVMessage.class, TPCMasterHandler.class, TPCSlaveInfo.class, TPCMaster.class})

public class SlaveTest {
	
    TPCMaster master;
    KVCache masterCache;
    
	@Test
	public void multipleSameIDTest() throws KVException {
        masterCache = new KVCache(5,5);
        master = new TPCMaster(10, masterCache);
        
        TPCSlaveInfo testSlave1 = new TPCSlaveInfo("123@hostname:456");
        TPCSlaveInfo testSlave2 = new TPCSlaveInfo("123@hostname:457");
        TPCSlaveInfo testSlave3 = new TPCSlaveInfo("123@ghost:456");
        TPCSlaveInfo testSlave4 = new TPCSlaveInfo("123@ghost:457");
        master.registerSlave(testSlave1);
        master.registerSlave(testSlave1);
        master.registerSlave(testSlave1);
        master.registerSlave(testSlave2);
        master.registerSlave(testSlave2);
        master.registerSlave(testSlave2);
        master.registerSlave(testSlave3);
        master.registerSlave(testSlave3);
        master.registerSlave(testSlave3);
        master.registerSlave(testSlave4);
        master.registerSlave(testSlave4);
        master.registerSlave(testSlave4);
        
        assertEquals(1, master.getNumRegisteredSlaves());     
	}
	
	@Test
	public void multipleIDTest() throws KVException {
		masterCache = new KVCache(5,5);
		master = new TPCMaster(3, masterCache);
		
        TPCSlaveInfo testSlave1 = new TPCSlaveInfo("111@hostname:456");
        TPCSlaveInfo testSlave2 = new TPCSlaveInfo("112@hostname:456");
        TPCSlaveInfo testSlave3 = new TPCSlaveInfo("113@hostname:456");
        TPCSlaveInfo testSlave4 = new TPCSlaveInfo("114@hostname:456");
        master.registerSlave(testSlave1);
        master.registerSlave(testSlave2);
        master.registerSlave(testSlave3);
        
        assertEquals(3, master.getNumRegisteredSlaves());
        assertFalse(master.getSlave(111).equals(null));
        assertFalse(master.getSlave(112).equals(null));
        assertFalse(master.getSlave(113).equals(null));
        assertEquals(null, master.getSlave(114));
	}
	
	@Test
	public void sizeTest() throws KVException {
		masterCache = new KVCache(5,5);
		master = new TPCMaster(100, masterCache);
		TPCSlaveInfo testSlave;
		for (int i = 0; i < 100; i++) {
			testSlave = new TPCSlaveInfo(i + "@hostname:4444");
			master.registerSlave(testSlave);
	        assertEquals(i + 1, master.getNumRegisteredSlaves());
		}
		
		for (int j = 0; j < 100; j++) {
			assertFalse(master.getSlave(j).equals(null));
		}
		
		for (int k = 200; k < 300; k++) {
			assertEquals(null, master.getSlave(k));
		}
	}
	
	@Test
	public void nullTest() throws KVException {
		masterCache = new KVCache(5,5);
		master = new TPCMaster(100, masterCache);
		TPCSlaveInfo testSlave;
		
		for (int k = 200; k < 600; k++) {
			assertEquals(null, master.getSlave(k));
		}
	}

}
