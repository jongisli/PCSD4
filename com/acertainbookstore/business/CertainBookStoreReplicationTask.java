package com.acertainbookstore.business;

import java.util.Set;
import java.util.concurrent.Callable;


import com.acertainbookstore.server.ReplicationHTTPProxy;
import com.acertainbookstore.utils.BookStoreMessageTag;

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
		this.replicationProxy = replicationProxy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReplicationResult call() throws Exception {
		BookStoreMessageTag messageTag = request.getMessageType();
		ReplicationResult replicationResult = new ReplicationResult(slaveServer, false);
		
		switch (messageTag)
		{
			case ADDBOOKS: {
				Set<StockBook> bookSet = (Set<StockBook>) request.getDataSet();
				replicationResult = replicationProxy.addBooks(bookSet, slaveServer);
				break;
			}
			case ADDCOPIES: {
				Set<BookCopy> bookCopySet = (Set<BookCopy>) request.getDataSet();
				replicationResult = replicationProxy.addCopies(bookCopySet, slaveServer); 
				break;
			}
			case UPDATEEDITORPICKS: {
				Set<BookEditorPick> bookEditorPicksSet = (Set<BookEditorPick>) request.getDataSet();
				replicationResult = replicationProxy.updateEditorPicks(bookEditorPicksSet, slaveServer);
				break;
			}
			case BUYBOOKS: {
				Set<BookCopy> bookCopySet = (Set<BookCopy>) request.getDataSet();
				replicationResult = replicationProxy.buyBooks(bookCopySet, slaveServer); 
				break;
			}
			default:
				break;
		}
		
		return replicationResult;
		
	}

}
