package com.acertainbookstore.testing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class TestBuyBooks {

	public static void main(String[] args) {

		StockManager storeManager = null;
		BookStore client = null;

		try {
			storeManager = new ReplicationAwareStockManagerHTTPProxy();
			client = new ReplicationAwareBookStoreHTTPProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int testISBN = 400;
		int buyCopies = 2;

		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(testISBN, buyCopies));
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
	}
}

