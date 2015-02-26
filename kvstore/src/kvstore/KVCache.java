package kvstore;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import kvstore.xml.KVCacheEntry;
import kvstore.xml.KVCacheType;
import kvstore.xml.KVSetType;
import kvstore.xml.ObjectFactory;

import java.util.*;



/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on
 * the eviction policy.
 */
public class KVCache implements KeyValueInterface {

    /**
     * Constructs a second-chance-replacement cache.
     *
     * @param numSets the number of sets this cache will have
     * @param maxElemsPerSet the size of each set
     */
    private int numSets;
    private int maxElemsPerSet;
    private LinkedList<KVCacheEntry>[] cache;
    private ReentrantLock[] locks;
    
    @SuppressWarnings("unchecked")
    public KVCache(int numSets, int maxElemsPerSet) {
        /* begin */
        this.cache = new LinkedList[numSets];
        this.numSets = numSets;
        this.maxElemsPerSet = maxElemsPerSet;
        this.locks = new ReentrantLock[numSets];

        for (int i = 0; i < numSets; i ++) {
            cache[i] = new LinkedList<KVCacheEntry>();
        }
        
        for (int j = 0; j < numSets; j ++) {
            locks[j] =  new ReentrantLock();
        }
        /* end */
    }

    /**
     * Retrieves an entry from the cache.
     * Assumes access to the corresponding set has already been locked by the
     * caller of this method.
     *
     * @param  key the key whose associated value is to be returned.
     * @return the value associated to this key or null if no value is
     *         associated with this key in the cache
     */
    @Override
    public String get(String key) {
        /* begin */
        int keyhash = Math.abs(key.hashCode() % numSets);
        KVCacheEntry entry;
        String value = null;
        
        ListIterator<KVCacheEntry> iterator = cache[keyhash].listIterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getKey().equals(key)) {
                value = entry.getValue();
                entry.setIsReferenced("true");
                
                LinkedList<KVCacheEntry> ll = cache[keyhash];
                ll.remove(entry);
                ll.addLast(entry);
                break;
            }
        }
        
        return value;
        /* end */
    }

    /**
     * Adds an entry to this cache.
     * If an entry with the specified key already exists in the cache, it is
     * replaced by the new entry. When an entry is replaced, its reference bit
     * will be set to True. If the set is full, an entry is removed from
     * the cache based on the eviction policy. If the set is not full, the entry
     * will be inserted behind all existing entries. For this policy, we suggest
     * using a LinkedList over an array to keep track of entries in a set since
     * deleting an entry in an array will leave a gap in the array, likely not
     * at the end. More details and explanations in the spec. Assumes access to
     * the corresponding set has already been locked by the caller of this
     * method.
     *
     * @param key the key with which the specified value is to be associated
     * @param value a value to be associated with the specified key
     */
    @Override
    public void put(String key, String value) {
        /* begin */
        int keyhash = Math.abs(key.hashCode() % numSets);
        
        LinkedList<KVCacheEntry> ll = cache[keyhash];
        KVCacheEntry entry;
        KVCacheEntry new_entry;
        ListIterator<KVCacheEntry> iterator = ll.listIterator();
        
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getKey().equals(key)) {
                entry.setValue(value);
                entry.setIsReferenced("true");
                return;
            }
        }
        
        if (ll.size() < maxElemsPerSet) {
            entry = new KVCacheEntry();
            entry.setIsReferenced("true");
            entry.setKey(key);
            entry.setValue(value);
            ll.addLast(entry);
            return;
        } else {
            while (true) {
                entry = ll.poll();
                if (entry.getIsReferenced().equals("false")) {
                    new_entry = new KVCacheEntry();
                    new_entry.setIsReferenced("true");
                    new_entry.setKey(key);
                    new_entry.setValue(value);
                    ll.addLast(new_entry);
                    return;
                } else {
                    entry.setIsReferenced("false");
                    ll.addLast(entry);
                }
            }
        }
        /* end */
    }

    /**
     * Removes an entry from this cache.
     * Assumes access to the corresponding set has already been locked by the
     * caller of this method. Does nothing if called on a key not in the cache.
     *
     * @param key key with which the specified value is to be associated
     */
    @Override
    public void del(String key) {
        /* begin */
        int keyhash = Math.abs(key.hashCode() % numSets);
        KVCacheEntry entry;
        
        ListIterator<KVCacheEntry> iterator = cache[keyhash].listIterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getKey().equals(key)) {
                cache[keyhash].remove(entry);
                break;
            }
        }
        /* end */
    }

    /**
     * Get a lock for the set corresponding to a given key.
     * The lock should be used by the caller of the get/put/del methods
     * so that different sets can be #{modified|changed} in parallel.
     *
     * @param  key key to determine the lock to return
     * @return lock for the set that contains the key
     */

    public Lock getLock(String key) {
    	int keyhash = Math.abs(key.hashCode() % numSets);
    	return locks[keyhash];
    }
    
    /**
     * Get the size of a given set in the cache.
     * @param cacheSet Which set.
     * @return Size of the cache set.
     */
    int getCacheSetSize(int cacheSet) {
        /* begin */
        if(cacheSet >= 0 && cacheSet < numSets){
            return cache[cacheSet].size();
        }
        return -1;
        /* end */
    }

    private void marshalTo(OutputStream os) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(KVCacheType.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.marshal(getXMLRoot(), os);
    }

    private JAXBElement<KVCacheType> getXMLRoot() throws JAXBException {
        ObjectFactory factory = new ObjectFactory();
        KVCacheType xmlCache = factory.createKVCacheType();
        /* begin */
        KVSetType settype;
        KVCacheEntry entry;
        
        for (int i = 0; i < numSets; i ++) {
            settype = factory.createKVSetType();
            String id = Integer.toString(i);
            settype.setId(id);
            
            LinkedList<KVCacheEntry> ll = cache[i];
            ListIterator<KVCacheEntry> iterator = ll.listIterator();
            while (iterator.hasNext()) {
                entry = iterator.next();
                settype.getCacheEntry().add(entry);
            }
            xmlCache.getSet().add(settype);
        }
        return factory.createKVCache(xmlCache);
        /* end */
    }

    /**
     * Serialize this store to XML. See spec for details on output format.
     */
    public String toXML() {
        /* begin */
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            marshalTo(os);
        } catch (JAXBException e) {
            // BEST EFFORT: IGNORE
        }
        return os.toString();
        /* end */
    }
    @Override
    public String toString() {
        return this.toXML();
    }

}
