package kvstore;

import static org.junit.Assert.*;
import static kvstore.KVConstants.*;

import java.net.Socket;

import org.junit.*;

public class TPCEndToEndTest extends TPCEndToEndTemplate {
	
	RandomString randomStringCreator = new RandomString(10);
	String randString;
	
	@Test
	public void justGet() throws KVException {
		try {
			client.get("Catherine");
            fail("Get request should fail with nonexistent key.");
		} catch (KVException e) {
			assertEquals(ERROR_NO_SUCH_KEY, e.getMessage());
		}
	}
	
	@Test
	public void BasicTest() throws KVException {
		client.put("Catherine", "basic");
		try {
			assertEquals("basic", client.get("Catherine"));
		} catch (KVException e) {
			fail("Get request should not fail with key in store.");
		}
	}
	
	@Test
	public void NullTest() throws KVException {
		client.put("Catherine", "basic");
		client.del("Catherine");
		try {
			client.get("Catherine");
            fail("Get request should fail with nonexistent key.");
		} catch (KVException e) {
			assertEquals(ERROR_NO_SUCH_KEY, e.getMessage());
		}
		for (int i = 0; i < 101; i++) {
			randString = randomStringCreator.nextString();
			try {
				client.get(randString);
	            fail("Get request should fail with nonexistent key.");
			} catch (KVException e) {
				assertEquals(ERROR_NO_SUCH_KEY, e.getMessage());				
			}
		}
	}
	
    @Test
    public void MultiTest() throws KVException {
    	for (int i = 0; i < 101; i++) {
    		client.put("Catherine" + Integer.toString(i), "basic" + Integer.toString(i));
    	}
        try {
        	for (int j = 0; j < 101; j++) {
        		assertEquals("basic" + Integer.toString(j), client.get("Catherine" + Integer.toString(j)));
        	}
        } catch (KVException e) {
            fail("Get request should not fail with key in store: " + e.getMessage());
        }
    }
    
    @Test
    public void stoppedSlave() throws KVException {
    	for (int i = 0; i < 11; i++) {
    		client.put("Catherine" + Integer.toString(i), "basic" + Integer.toString(i));
    	}
    	try {
        	for (int j = 0; j < 11; j++) {
        		assertEquals("basic" + Integer.toString(j), client.get("Catherine" + Integer.toString(j)));
        	}
    	} catch (KVException e) {
    		fail("Get request should not fail with key in store: " + e.getMessage());
    	}
    	
    	try {
    		stopSlave(Long.toString(SLAVE1));
    	} catch (Exception e) {
    		fail("Could not abort SLAVE1: " + e.getMessage());
    	}
    	
    	try {
        	for (int k = 0; k < 11; k++) {
        		assertEquals("basic" + Integer.toString(k), client.get("Catherine" + Integer.toString(k)));
        	}
    	} catch (KVException e) {
    		fail("Get request should not fail with key in store: " + e.getMessage());
    	}
    	
    	try {
    		stopSlave(Long.toString(SLAVE2));
    	} catch (Exception e) {
    		fail("Could not abort SLAVE2: " + e.getMessage());
    	}
    	
    	try {
        	for (int y = 0; y < 11; y++) {
        		assertEquals("basic" + Integer.toString(y), client.get("Catherine" + Integer.toString(y)));
        	}
    	} catch (KVException e) {
    		fail("Get request should not fail with key in store: " + e.getMessage());
    	}
    }
	
	@Test
	public void basicOverwriteTest() throws KVException {
		client.put("Catherine",  "yo!");
		client.put("Catherine",  "yo!!");
		client.put("Catherine",  "yo!!!");
		client.put("Catherine",  "yo!!!!");
		client.put("Catherine",  "yo!!!!!");
		try {
			assertEquals("yo!!!!!", client.get("Catherine"));
		} catch (KVException e) {
			fail("Overwrite didn't happen.");
		}
	}
	
	@Test
	public void threadOverwriteTest() throws KVException {
		client.put("Catherine", "yo");
		
		Thread thread = new Thread(
    			new Runnable() {
    				public void run() {
    					try {
							client.put("Catherine", "yo!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    					
    					try {
							client.put("Catherine", "yo!!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    					
    					try {
							client.put("Catherine", "yo!!!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    					
    					try {
							client.put("Catherine", "yo!!!!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    					
    					try {
							client.put("Catherine", "yo!!!!!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    				}
    			});
		
		thread.start();
		
		while (thread.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		
		assertEquals("yo!!!!!", client.get("Catherine"));
	}
	
	
	
	@Test
	public void concurrentGetTest() throws KVException {
		client.put("Catherine", "yo");
		
		Thread threadOne = new Thread(
    			new Runnable() {
    				public void run() {
    					try {
							client.put("Catherine", "dumb!");
						} catch (KVException e) {
							e.printStackTrace();
						}
    				}
    			});
		
		Thread threadTwo = new Thread(
    			new Runnable() {
    				public void run() {
    					try {
							Thread.sleep(3 * TPCMaster.TIMEOUT);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
						}
    					try {
							assertEquals("dumb!", client.get("Catherine"));
						} catch (KVException e) {
							e.printStackTrace();
						}
    					
    				}
    			});
		
		Thread threadThree = new Thread(
    			new Runnable() {
    				public void run() {
    					try {
							Thread.sleep(3 * TPCMaster.TIMEOUT);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
						}
    					try {
							assertEquals("dumb!", client.get("Catherine"));
						} catch (KVException e) {
							e.printStackTrace();
						}
    				}
    			});
		
		threadOne.start();
		threadTwo.start();
		threadThree.start();
		
		// SLEEP TIME REQUIRED FOR SOCKET CREATION
		while (threadOne.isAlive() || threadTwo.isAlive() || threadThree.isAlive()) {
			try {
				Thread.sleep(TPCMaster.TIMEOUT);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
    @Test
    public void MultipleOverwriteTest() throws KVException {
        for (int value = 0; value < 51; value++) {
            client.put("CatherineIsBasic", Integer.toString(value));
        }
        String val = client.get("CatherineIsBasic");
        assertEquals(val, "50");
    }
    
    @Test
    public void ManyMultipleOverwriteTest() throws KVException {
    	int count = 0;
    	while (count < 10) {
    		MultipleOverwriteTest();
    		count = count + 1;
    	}
    }
    
    @Test 
    public void DeleteNonExistent() throws KVException {
        try {
            client.del("Catherine");
            fail("Key Shouldnt Exist");
        } catch (KVException e){
            
        }
    }
    
    @Test
    public void OversizedKey() {
        String key = "i";
        for (int i = 0; i <= 256; i ++){
            key += "i"; 
        }
        
        try {
            client.put(key, "value");
            fail("Key should have been too long");
        } catch (KVException e) {
        	//PASS
        }
    }
    
    @Test
    public void NullKey() {
        try {
            client.put(null, "value");
            fail("Should have failed because of empty key");
        } catch (KVException e) {
            assertEquals(ERROR_INVALID_KEY, e.getMessage());
        }
    }
    
    @Test
    public void NullValue() {
        try {
            client.put("key", null);
            fail("Should have failed because of empty value");
        } catch (KVException e) {
            assertEquals(ERROR_INVALID_VALUE, e.getMessage());
        }
    }

}
