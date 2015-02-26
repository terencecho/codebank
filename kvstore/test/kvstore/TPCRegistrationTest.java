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
@PrepareForTest({InetSocketAddress.class, Socket.class, TPCMaster.class, TPCRegistrationHandler.class, ThreadPool.class, KVMessage.class})
public class TPCRegistrationTest {
	
	Socket mockSocket;
	InetSocketAddress mockAddress;
	ThreadPool mockThreadPool;
	TPCMaster mockMaster;
	KVMessage mockMessage;
	
	@Test
	public void KVMessageErrorTest() throws Exception {
		try {
			mockSocket = mock(Socket.class);
			mockThreadPool = mock(ThreadPool.class);
			mockMaster = mock(TPCMaster.class);
			whenNew(ThreadPool.class).withArguments(anyInt()).thenReturn(mockThreadPool);
			whenNew(KVMessage.class).withArguments(anyString()).thenThrow(new KVException("fail"));
			TPCRegistrationHandler testHandler = new TPCRegistrationHandler(mockMaster);
			testHandler.handle(mockSocket);			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exceptions should be thrown");
		}
	}	
	
	@Test
	public void SlaveInfoCreationTest() {
		try {
			mockSocket = mock(Socket.class);
			mockThreadPool = mock(ThreadPool.class);
			mockMaster = mock(TPCMaster.class);
			mockMessage = mock(KVMessage.class);
			whenNew(ThreadPool.class).withArguments(anyInt()).thenReturn(mockThreadPool);
			whenNew(TPCSlaveInfo.class).withArguments(anyString()).thenThrow(new KVException("fail"));
			TPCRegistrationHandler testHandler = new TPCRegistrationHandler(mockMaster);
			testHandler.handle(mockSocket);			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exceptions should be thrown");
		}
	}
	
	@Test
	public void SendMessageTest() {
		try {
			mockSocket = mock(Socket.class);
			mockThreadPool = mock(ThreadPool.class);
			mockMaster = mock(TPCMaster.class);
			mockMessage = mock(KVMessage.class);
			whenNew(ThreadPool.class).withArguments(anyInt()).thenReturn(mockThreadPool);
			doThrow(new KVException("fail")).when(mockMessage).sendMessage(mockSocket);
			TPCRegistrationHandler testHandler = new TPCRegistrationHandler(mockMaster);
			testHandler.handle(mockSocket);
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exceptions should be thrown");
		}
	}
	
}
