package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.server.BookStoreHTTPServerUtility;
import com.acertainbookstore.server.MasterBookStoreHTTPMessageHandler;
import com.acertainbookstore.server.SlaveBookStoreHTTPMessageHandler;

public class ServersDownTest {

	private static StockManager storeManager;
	private static BookStore client;

	@BeforeClass
	public static void setUpBeforeClass() {
		/*
		BookStoreHTTPServerUtility.createServer(8081, masterHandler);
		SlaveBookStoreHTTPMessageHandler slaveHandler1 = new SlaveBookStoreHTTPMessageHandler();
		BookStoreHTTPServerUtility.createServer(8082, slaveHandler1);
		SlaveBookStoreHTTPMessageHandler slaveHandler2 = new SlaveBookStoreHTTPMessageHandler();
		BookStoreHTTPServerUtility.createServer(8083, slaveHandler2);
		 */
		
		try {
			storeManager = new ReplicationAwareStockManagerHTTPProxy();
			client = new ReplicationAwareBookStoreHTTPProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		((ReplicationAwareBookStoreHTTPProxy) client).stop();
		((ReplicationAwareStockManagerHTTPProxy) storeManager).stop();
	}

}
