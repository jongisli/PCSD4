package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import com.acertainbookstore.utils.BookStoreMessageTag;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {
	
	private String slaveServer;
	private ReplicationRequest request;

	public CertainBookStoreReplicationTask(String slaveServer, ReplicationRequest request) {
		this.slaveServer = slaveServer;
		this.request = request;
	}

	@Override
	public ReplicationResult call() throws Exception {
		BookStoreMessageTag messageTag = request.getMessageType();
		return null;
		
	}

}
