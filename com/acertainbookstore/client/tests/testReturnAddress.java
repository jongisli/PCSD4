package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.ReplicatedBookStore;
import com.acertainbookstore.interfaces.ReplicatedStockManager;
import com.acertainbookstore.interfaces.StockManager;

public class testReturnAddress {

	private static ReplicationAwareStockManagerHTTPProxy storeManager;
	private static ReplicationAwareBookStoreHTTPProxy client;

	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			storeManager = new ReplicationAwareStockManagerHTTPProxy();
			client = new ReplicationAwareBookStoreHTTPProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddress() {
		
		String returnAddressStock = storeManager.getReplicaAddress();
		String returnAddressbook  = client.getReplicaAddress();
		System.out.println("I am the replicated address: "+returnAddressStock);
		System.out.println("I am the replicated address: "+returnAddressbook);
		
	}

}
