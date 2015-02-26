package kvstore;

import static autograder.TestUtils.kTimeoutQuick;
import static kvstore.KVConstants.ERROR_NO_SUCH_KEY;
import static kvstore.KVConstants.RESP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ3_CODE;

public class KVStoreTest {

    KVStore store;

    @Before
    public void setupStore() {
        store = new KVStore();
    }

    @Test(timeout = kTimeoutQuick)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1,
        desc = "Verify get returns value just put into store")
    public void putAndGetOneKey() throws KVException {
        String key = "this is the key.";
        String val = "this is the value.";
        store.put(key, val);
        assertEquals(val, store.get(key));
    }
    
    @Test
    public void dumpTest() {
    	String xmlDump = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    						+ "<KVStore>"
    						+ "<KVPair>"
    						+ "<Key>Catherine</Key>"
    						+ "<Value>basic</Value>"
    						+ "</KVPair>"
    						+ "<KVPair>"
    						+ "<Key>Dickson</Key>"
    						+ "<Value>notBasic</Value>"
    						+ "</KVPair>"
    						+ "</KVStore>";
    	
    	store.put("Catherine", "basic");
    	store.put("Dickson", "notBasic");
    	assertEquals(xmlDump, store.toXML());
    }
    
    @Test
    public void fileTest() {
    	store.put("Catherine", "basic");
    	store.put("Dickson", "notBasic");
    	store.dumpToFile("outerTest.xml");
    	File file = new File("outerTest.xml");
    	try {
			String content = new Scanner(file).useDelimiter("\\Z").next();
	    	String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVStore><KVPair><Key>Catherine</Key><Value>basic</Value></KVPair><KVPair><Key>Dickson</Key><Value>notBasic</Value></KVPair></KVStore>";
	    	assertEquals(test, content);
		} catch (FileNotFoundException e) {
			fail("Shouldn't ever get here.");
		}
    }
    
    @Test
    public void restoreTest() {
    	store.put("Catherine", "basic");
    	store.put("Dickson", "notBasic");
    	
    	store.dumpToFile("restoreTest.xml");
    	File file = new File("restoreTest.xml");
    	try {
			String content = new Scanner(file).useDelimiter("\\Z").next();
	    	String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVStore><KVPair><Key>Catherine</Key><Value>basic</Value></KVPair><KVPair><Key>Dickson</Key><Value>notBasic</Value></KVPair></KVStore>";
	    	assertEquals(test, content);
		} catch (FileNotFoundException e) {
			fail("Shouldn't ever get here.");
		}
    	
    	store.restoreFromFile("restoreTest.xml");
    	try {
    		assertEquals("basic", store.get("Catherine"));
    		assertEquals("notBasic", store.get("Dickson"));
    	} catch (KVException e) {
    		
    	}
    }
    
    @Test
    public void bigTest() {
    	for (int i = 1; i <= 100; i++) {
    		store.put(Integer.toString(i), "Pullinghard" + Integer.toString(i));
    	}
    	
    	store.dumpToFile("bigTest.xml");
    	File file = new File("bigTest.xml");
    	try {
			String content = new Scanner(file).useDelimiter("\\Z").next();
	    	String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVStore><KVPair><Key>98</Key><Value>Pullinghard98</Value></KVPair><KVPair><Key>66</Key><Value>Pullinghard66</Value></KVPair><KVPair><Key>76</Key><Value>Pullinghard76</Value></KVPair><KVPair><Key>52</Key><Value>Pullinghard52</Value></KVPair><KVPair><Key>27</Key><Value>Pullinghard27</Value></KVPair><KVPair><Key>1</Key><Value>Pullinghard1</Value></KVPair><KVPair><Key>88</Key><Value>Pullinghard88</Value></KVPair><KVPair><Key>87</Key><Value>Pullinghard87</Value></KVPair><KVPair><Key>80</Key><Value>Pullinghard80</Value></KVPair><KVPair><Key>36</Key><Value>Pullinghard36</Value></KVPair><KVPair><Key>7</Key><Value>Pullinghard7</Value></KVPair><KVPair><Key>26</Key><Value>Pullinghard26</Value></KVPair><KVPair><Key>43</Key><Value>Pullinghard43</Value></KVPair><KVPair><Key>14</Key><Value>Pullinghard14</Value></KVPair><KVPair><Key>64</Key><Value>Pullinghard64</Value></KVPair><KVPair><Key>95</Key><Value>Pullinghard95</Value></KVPair><KVPair><Key>100</Key><Value>Pullinghard100</Value></KVPair><KVPair><Key>19</Key><Value>Pullinghard19</Value></KVPair><KVPair><Key>91</Key><Value>Pullinghard91</Value></KVPair><KVPair><Key>42</Key><Value>Pullinghard42</Value></KVPair><KVPair><Key>74</Key><Value>Pullinghard74</Value></KVPair><KVPair><Key>21</Key><Value>Pullinghard21</Value></KVPair><KVPair><Key>86</Key><Value>Pullinghard86</Value></KVPair><KVPair><Key>57</Key><Value>Pullinghard57</Value></KVPair><KVPair><Key>29</Key><Value>Pullinghard29</Value></KVPair><KVPair><Key>65</Key><Value>Pullinghard65</Value></KVPair><KVPair><Key>11</Key><Value>Pullinghard11</Value></KVPair><KVPair><Key>8</Key><Value>Pullinghard8</Value></KVPair><KVPair><Key>78</Key><Value>Pullinghard78</Value></KVPair><KVPair><Key>58</Key><Value>Pullinghard58</Value></KVPair><KVPair><Key>97</Key><Value>Pullinghard97</Value></KVPair><KVPair><Key>71</Key><Value>Pullinghard71</Value></KVPair><KVPair><Key>94</Key><Value>Pullinghard94</Value></KVPair><KVPair><Key>39</Key><Value>Pullinghard39</Value></KVPair><KVPair><Key>85</Key><Value>Pullinghard85</Value></KVPair><KVPair><Key>48</Key><Value>Pullinghard48</Value></KVPair><KVPair><Key>79</Key><Value>Pullinghard79</Value></KVPair><KVPair><Key>30</Key><Value>Pullinghard30</Value></KVPair><KVPair><Key>55</Key><Value>Pullinghard55</Value></KVPair><KVPair><Key>5</Key><Value>Pullinghard5</Value></KVPair><KVPair><Key>62</Key><Value>Pullinghard62</Value></KVPair><KVPair><Key>13</Key><Value>Pullinghard13</Value></KVPair><KVPair><Key>72</Key><Value>Pullinghard72</Value></KVPair><KVPair><Key>84</Key><Value>Pullinghard84</Value></KVPair><KVPair><Key>49</Key><Value>Pullinghard49</Value></KVPair><KVPair><Key>37</Key><Value>Pullinghard37</Value></KVPair><KVPair><Key>77</Key><Value>Pullinghard77</Value></KVPair><KVPair><Key>6</Key><Value>Pullinghard6</Value></KVPair><KVPair><Key>63</Key><Value>Pullinghard63</Value></KVPair><KVPair><Key>56</Key><Value>Pullinghard56</Value></KVPair><KVPair><Key>38</Key><Value>Pullinghard38</Value></KVPair><KVPair><Key>16</Key><Value>Pullinghard16</Value></KVPair><KVPair><Key>92</Key><Value>Pullinghard92</Value></KVPair><KVPair><Key>3</Key><Value>Pullinghard3</Value></KVPair><KVPair><Key>23</Key><Value>Pullinghard23</Value></KVPair><KVPair><Key>20</Key><Value>Pullinghard20</Value></KVPair><KVPair><Key>83</Key><Value>Pullinghard83</Value></KVPair><KVPair><Key>60</Key><Value>Pullinghard60</Value></KVPair><KVPair><Key>75</Key><Value>Pullinghard75</Value></KVPair><KVPair><Key>4</Key><Value>Pullinghard4</Value></KVPair><KVPair><Key>99</Key><Value>Pullinghard99</Value></KVPair><KVPair><Key>46</Key><Value>Pullinghard46</Value></KVPair><KVPair><Key>70</Key><Value>Pullinghard70</Value></KVPair><KVPair><Key>69</Key><Value>Pullinghard69</Value></KVPair><KVPair><Key>10</Key><Value>Pullinghard10</Value></KVPair><KVPair><Key>53</Key><Value>Pullinghard53</Value></KVPair><KVPair><Key>22</Key><Value>Pullinghard22</Value></KVPair><KVPair><Key>32</Key><Value>Pullinghard32</Value></KVPair><KVPair><Key>9</Key><Value>Pullinghard9</Value></KVPair><KVPair><Key>28</Key><Value>Pullinghard28</Value></KVPair><KVPair><Key>47</Key><Value>Pullinghard47</Value></KVPair><KVPair><Key>15</Key><Value>Pullinghard15</Value></KVPair><KVPair><Key>82</Key><Value>Pullinghard82</Value></KVPair><KVPair><Key>35</Key><Value>Pullinghard35</Value></KVPair><KVPair><Key>96</Key><Value>Pullinghard96</Value></KVPair><KVPair><Key>54</Key><Value>Pullinghard54</Value></KVPair><KVPair><Key>68</Key><Value>Pullinghard68</Value></KVPair><KVPair><Key>34</Key><Value>Pullinghard34</Value></KVPair><KVPair><Key>61</Key><Value>Pullinghard61</Value></KVPair><KVPair><Key>89</Key><Value>Pullinghard89</Value></KVPair><KVPair><Key>73</Key><Value>Pullinghard73</Value></KVPair><KVPair><Key>44</Key><Value>Pullinghard44</Value></KVPair><KVPair><Key>31</Key><Value>Pullinghard31</Value></KVPair><KVPair><Key>50</Key><Value>Pullinghard50</Value></KVPair><KVPair><Key>18</Key><Value>Pullinghard18</Value></KVPair><KVPair><Key>25</Key><Value>Pullinghard25</Value></KVPair><KVPair><Key>24</Key><Value>Pullinghard24</Value></KVPair><KVPair><Key>51</Key><Value>Pullinghard51</Value></KVPair><KVPair><Key>17</Key><Value>Pullinghard17</Value></KVPair><KVPair><Key>90</Key><Value>Pullinghard90</Value></KVPair><KVPair><Key>33</Key><Value>Pullinghard33</Value></KVPair><KVPair><Key>93</Key><Value>Pullinghard93</Value></KVPair><KVPair><Key>12</Key><Value>Pullinghard12</Value></KVPair><KVPair><Key>41</Key><Value>Pullinghard41</Value></KVPair><KVPair><Key>67</Key><Value>Pullinghard67</Value></KVPair><KVPair><Key>2</Key><Value>Pullinghard2</Value></KVPair><KVPair><Key>81</Key><Value>Pullinghard81</Value></KVPair><KVPair><Key>59</Key><Value>Pullinghard59</Value></KVPair><KVPair><Key>40</Key><Value>Pullinghard40</Value></KVPair><KVPair><Key>45</Key><Value>Pullinghard45</Value></KVPair></KVStore>";
	    	assertEquals(test, content);
		} catch (FileNotFoundException e) {
			fail("Shouldn't ever get here.");
		}
    	
    	store.restoreFromFile("bigTest.xml");
    	try {
        	for (int j = 1; j <= 100; j++) {
        		assertEquals("Pullinghard" + Integer.toString(j), store.get(Integer.toString(j)));
        	}
    	} catch (KVException e) {
    		
    	}
    }
    

}