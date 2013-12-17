package com.acertainbookstore.server;

import java.util.Set;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;

public class ReplicationHTTPProxy {
	
	private HttpClient replicatorClient;
	
	public ReplicationHTTPProxy()
	{
		replicatorClient = new HttpClient();
		replicatorClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		replicatorClient.setMaxConnectionsPerAddress(10);
		replicatorClient.setThreadPool(new QueuedThreadPool(10));
		replicatorClient.setTimeout(3000); 
		
		try {
			replicatorClient.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public ReplicationResult addBooks(Set<StockBook> bookSet, String slaveServer) {
		String listBooksxmlString = BookStoreUtility.serializeObjectToXMLString(bookSet);
		Buffer requestContent = new ByteArrayBuffer(listBooksxmlString);
		ContentExchange exchange = new ContentExchange();
		String urlString = slaveServer + "/" + BookStoreMessageTag.ADDBOOKS;
		
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		
		try {
			BookStoreUtility.SendAndRecv(replicatorClient, exchange);
		} catch (BookStoreException e) {
			return new ReplicationResult(slaveServer, false);
		}
		// TODO: Do I need to worry about snapshotIDs here?
		return new ReplicationResult(slaveServer, true);
	}

	public ReplicationResult addCopies(Set<BookCopy> bookCopySet, String slaveServer) {
		String listBookCopiesxmlString = BookStoreUtility.serializeObjectToXMLString(bookCopySet);
		Buffer requestContent = new ByteArrayBuffer(listBookCopiesxmlString);
		ContentExchange exchange = new ContentExchange();
		String urlString = slaveServer + "/" + BookStoreMessageTag.ADDCOPIES;
		
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		
		try {
			BookStoreUtility.SendAndRecv(replicatorClient, exchange);
		} catch (BookStoreException e) {
			return new ReplicationResult(slaveServer, false);
		}
		// TODO: Do I need to worry about snapshotIDs here?
		return new ReplicationResult(slaveServer, true);
	}


}
