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

public class TestAddBooks {

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
		int amountOfBooks = 100;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN,
				"Egils saga Skalla-Gr’mssonar",
				"Viking Vikingsson", (float) 100, amountOfBooks, 0, 0, 0,
				false));
		int testISBN2 = 100;
		int amountOfBooks2 = 1;
		Set<StockBook> booksToAdd2 = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN2,
				"Principles of Computer System and Design",
				"Vivek", (float) 100, amountOfBooks2, 0, 0, 0,
				false));
		
		try {
			storeManager.addBooks(booksToAdd);
			System.out.println("Adding books sucessfully.");
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
	}
}

