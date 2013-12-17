package com.acertainbookstore.business;

import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.acertainbookstore.server.ReplicationHTTPProxy;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {
	
	private String slaveServer;
	private ReplicationRequest request;
	private ReplicationHTTPProxy replicationProxy;

	public CertainBookStoreReplicationTask(String slaveServer, ReplicationRequest request, ReplicationHTTPProxy replicationProxy) {
		this.slaveServer = slaveServer;
		this.request = request;
	}

	@Override
	public ReplicationResult call() throws Exception {
		BookStoreMessageTag messageTag = request.getMessageType();
		ReplicationResult replicationResult = new ReplicationResult(slaveServer, false);
		
		switch (messageTag)
		{
			case ADDBOOKS:
				Set<StockBook> bookSet = (Set<StockBook>) request.getDataSet();
				replicationResult = replicationProxy.addBooks(bookSet, slaveServer);
			case ADDCOPIES:
				Set<BookCopy> bookCopySet = (Set<BookCopy>) request.getDataSet();
				replicationResult = replicationProxy.addCopies(bookCopySet, slaveServer);
			default:
				break;
		}
		
		return replicationResult;
		
	}

}
