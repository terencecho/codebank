package kvstore;

import static autograder.TestUtils.*;
import static kvstore.KVConstants.*;
import static kvstore.Utils.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Random;

import kvstore.Utils.ErrorLogger;
import kvstore.Utils.RandomString;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ3_CODE;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InetSocketAddress.class, Socket.class, TPCSlaveInfo.class})
public class TPCSlaveInfoTest {
	
	@Test
	public void basicTest() throws KVException {
		TPCSlaveInfo testSlave;
		String testString;
		
		testString = "54321@456:1234";
		testSlave = new TPCSlaveInfo(testString);	
		assertNotNull(testSlave);
		assertEquals(54321, testSlave.getSlaveID());
		assertEquals("456", testSlave.getHostname());
		assertEquals(1234, testSlave.getPort());
		
		testString = "54321@hostname:1234";
		testSlave = new TPCSlaveInfo(testString);	
		assertNotNull(testSlave);
		assertEquals(54321, testSlave.getSlaveID());
		assertEquals("hostname", testSlave.getHostname());
		assertEquals(1234, testSlave.getPort());
	}
	
	@Test
	public void nullTest() throws KVException {
		TPCSlaveInfo testSlave;
		String testString = null;
		
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("null test failed");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}		
	}
	
	@Test
	public void constructionTest() {
		TPCSlaveInfo testSlave;
		String testString;
		
		testString = "12@34@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra @");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "12:34@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra :");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}	
		
		testString = "1234@host:12:34";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra :");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "12@34@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra @");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}	
		
		testString = "12@34@host@name:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra @");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "12@34@hostname:12@34";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra @");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "12.34@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra .");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1234@hostname:12.34";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("extra .");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
	}
	
	@Test
	public void idTest() throws KVException {
		TPCSlaveInfo testSlave;
		String testString;
		
		testString = "-1111@hostname:1234";
		testSlave = new TPCSlaveInfo(testString);	
		assertNotNull(testSlave);
		assertEquals(-1111, testSlave.getSlaveID());
		assertEquals("hostname", testSlave.getHostname());
		assertEquals(1234, testSlave.getPort());
		
		testString = "111badport@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: letters and numbers");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "badport@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: letters");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "bad%port@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: special characters");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "%@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: special character");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "12%34@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: special characters");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1%@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: special character");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "%0@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad id: special character");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "not_even_:a:_number@&*#$f00:-:::_magic_";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad overall");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
        String testID = Long.toString(Long.MAX_VALUE);
		testString = testID + "@poops:4141";
		testSlave = new TPCSlaveInfo(testString);
		assertNotNull(testSlave);
		assertEquals(Long.MAX_VALUE, testSlave.getSlaveID());
		assertEquals("poops", testSlave.getHostname());
		assertEquals(4141, testSlave.getPort());	
	}
	
	@Test
	public void hostTest() {
		TPCSlaveInfo testSlave;
		String testString;
		
		testString = "54321@hostname:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
		} catch (KVException e) {
			fail("should not error on proper host passing");
		}
		
		testString = "54321@host123name:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
		} catch (KVException e) {
			fail("should not error on proper host passing");
		}
		
		testString = "54321@host-name:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
		} catch (KVException e) {
			fail("should not error on proper host passing");
		}	
		
		testString = "54321@host^name:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("alphanumeric only");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}	
		
		testString = "54321@host name:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("alphanumeric only");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}		
		
		testString = "54321@ho st^nam e:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("alphanumeric only");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}		
		
		testString = "54321@%%%%%:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("alphanumeric and . - only");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "54321@!!!!!:1234";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("alphanumeric and . - only");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
			
	}
	
	@Test
	public void portTest() throws KVException {
		TPCSlaveInfo testSlave;
		String testString;
		
		testString = "1111@hostname:12el";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: letters and numbers");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:elel";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: letters");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:-1111";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: negative");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:11%11";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: special character %");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:11-11";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: special character -");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:11.11";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: special character .");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		testString = "1111@hostname:%%%%";
		try {
			testSlave = new TPCSlaveInfo(testString);
			fail("bad port: special character .");
		} catch (KVException e) {
			assertEquals(ERROR_INVALID_FORMAT, e.getMessage());
		}
		
		String testPort = Integer.toString(Integer.MAX_VALUE);
		testString = "1111@po-ops:" + testPort;
		testSlave = new TPCSlaveInfo(testString);
		assertNotNull(testSlave);
		assertEquals(1111, testSlave.getSlaveID());
		assertEquals("po-ops", testSlave.getHostname());
		assertEquals(Integer.MAX_VALUE, testSlave.getPort());	
		
	}

	Socket mockSocket;
	InetSocketAddress mockAddress;
	
	@Test
	public void closeSocketTest() throws KVException {
		try {
			mockSocket = mock(Socket.class);
			TPCSlaveInfo testSlave = new TPCSlaveInfo("54321@hostname:1234");
			testSlave.closeHost(mockSocket);
		} catch (Exception e) {
			fail("Socket should have closed successfully");
		}
	}
	
	@Test
	public void socketExceptionTest() throws KVException {
		try {
			mockSocket = mock(Socket.class);
			TPCSlaveInfo testSlave = new TPCSlaveInfo("54321@hostname:1234");
			doThrow(new IOException()).when(mockSocket).close();
			testSlave.closeHost(mockSocket);
		} catch (Exception e) {
			fail("Socket should have closed successfully despite exception");
		}
	}
	
//	@Test
//	public void socketErrorTest() throws KVException {
//		try {
//			mockSocket = mock(Socket.class);
//			whenNew(Socket.class).withArguments(anyString(), anyInt()).thenThrow(new IOException());
//			TPCSlaveInfo testSlave = new TPCSlaveInfo("54321@ghostname:1234");
//			testSlave.connectHost(9999);
//			fail("Did not throw error, should have thrown \"ERROR_COULD_NOT_CREATE_SOCKET\"");
//		} catch (KVException e) {
//            assertEquals(ERROR_COULD_NOT_CREATE_SOCKET, e.getMessage());
//        } catch (Exception e) {
//        	System.out.println(e.getMessage());
//            fail("Threw other error, should have thrown \"ERROR_COULD_NOT_CREATE_SOCKET\"");
//        }
//	}
		
    @Test
    public void couldNotConnectTest() throws KVException {
        try {
        	setupSocket();
        	setupInet();
            doThrow(new IOException()).when(mockSocket).connect(refEq(mockAddress), anyInt());
            TPCSlaveInfo slaveInfo = new TPCSlaveInfo("54321@ghostname:1234");
            slaveInfo.connectHost(9999);
            fail("Did not throw error, should have thrown \"ERROR_COULD_NOT_CONNECT\"");
        } catch (KVException e) {
            assertEquals(ERROR_COULD_NOT_CONNECT, e.getMessage());
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            fail("Threw other error, should have thrown \"ERROR_COULD_NOT_CONNECT\"");
        }
    }
    
    @Test
    public void socketTimeoutTest() throws KVException {
        try {
        	setupSocket();
        	setupInet();
            doThrow(new SocketTimeoutException()).when(mockSocket).connect(refEq(mockAddress), anyInt());
            TPCSlaveInfo slaveInfo = new TPCSlaveInfo("54321@ghostname:1234");
            slaveInfo.connectHost(9999);
            fail("Did not throw error, should have thrown \"ERROR_SOCKET_TIMEOUT\"");
        } catch (KVException e) {
            assertEquals(ERROR_SOCKET_TIMEOUT, e.getMessage());
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            fail("Threw other error, should have thrown \"ERROR_SOCKET_TIMEOUT\"");
        }
    }
    
    
    /* ----------------------- BEGIN HELPER METHODS ------------------------ */    
    
    private void setupSocket() throws Exception {
        mockSocket = mock(Socket.class);
        whenNew(Socket.class).withNoArguments().thenReturn(mockSocket);
    }
    
    private void setupInet() throws Exception {
        mockAddress = mock(InetSocketAddress.class);
        whenNew(InetSocketAddress.class).withArguments(anyString(), anyInt()).thenReturn(mockAddress);
    }
}
