package com.acertainbookstore.testing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class MasterFailTestGetbooks {

	public static void main(String[] args) {

		StockManager storeManager = null;
		BookStore client = null;

		try {
			storeManager = new ReplicationAwareStockManagerHTTPProxy();
			client = new ReplicationAwareBookStoreHTTPProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	

		List<StockBook> listBooks = null;
		try {
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
		for (Book book : listBooks) {
			System.out.println(book);
		}
		
	}
}

