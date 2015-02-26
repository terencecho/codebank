package kvstore;

import static autograder.TestUtils.*;
import static kvstore.KVConstants.*;
import static kvstore.Utils.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;

import kvstore.Utils.ErrorLogger;
import kvstore.Utils.RandomString;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ3_CODE;

import static org.mockito.Mockito.*;

public class EndToEndTest extends EndToEndTemplate {
	  
    @Test
    public void BasicTest() throws KVException {  
        client.put("Catherine", "basic");
        assertEquals("basic", client.get("Catherine"));
        client.del("Catherine");
        try {        
            assertEquals("basic", client.get("Catherine"));
            fail("Get request should fail with nonexistent key.");
        } catch (KVException e){
            assertEquals(ERROR_NO_SUCH_KEY, e.getMessage());
        }
    }
    
    @Test
    public void BasicTest2() throws KVException {
        client.put("Catherine", "basic");
        client.put("Catherine2", "basic");
        client.put("Catherine3", "basic");
        client.put("Catherine4", "basic");
        try {
            assertEquals("basic", client.get("Catherine"));
            assertEquals("basic", client.get("Catherine2"));
            assertEquals("basic", client.get("Catherine3"));
            assertEquals("basic", client.get("Catherine4"));
        } catch (KVException e) {
            fail("Should have gotten key");
        }
    }
    
    @Test
    public void BasicTest3() throws KVException {
        client.put("Catherine", "basic");
        client.put("Catherine2", "basic");
        client.put("Catherine3", "basic");
        client.put("Catherine4", "basic");
        try {
            assertEquals("basic", client.get("Catherine"));
            assertEquals("basic", client.get("Catherine2"));
            assertEquals("basic", client.get("Catherine3"));
            assertEquals("basic", client.get("Catherine4"));
            client.del("Catherine2");
            client.get("Catherine2");
            fail("Key should have been deleted");
        } catch (KVException e) {
            assertEquals(ERROR_NO_SUCH_KEY, e.getMessage());
        }
    }
    
    
    @Test
    public void MultiplePutGetTest() throws KVException {
        for (int key = 0; key < 1001; key++) {
            client.put(Integer.toString(key), Integer.toString(key));
        }
        String val;
        String no;
        for (int test = 0; test < 1001; test++) {
            no = Integer.toString(test);
            val = client.get(no);
            assertEquals(no, val);
        }
    }
    
    @Test
    public void RandStressTest() throws KVException {
    	
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
            assertEquals(ERROR_OVERSIZED_KEY, e.getMessage());
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
    
    @Test
    public void EmptyKey() {
        try {
            client.put("", "value");
            fail("Should have failed because of empty key");
        } catch (KVException e) {
            assertEquals(ERROR_INVALID_KEY, e.getMessage());
        }
    }
    
    @Test
    public void EmptyValue() {
        try {
            client.put("key", "");
            fail("Should have failed because of empty value");
        } catch (KVException e) {
            assertEquals(ERROR_INVALID_VALUE, e.getMessage());
        }
    }
    
}
