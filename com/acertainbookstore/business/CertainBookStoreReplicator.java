package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.client.BookStoreClientConstants;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.Replicator;
import com.acertainbookstore.server.ReplicationHTTPProxy;

/**
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {

	private ExecutorService exec;
	private ReplicationHTTPProxy replicationProxy;
	
	public CertainBookStoreReplicator(int maxReplicatorThreads) {
		// We initialize an executor service which spawns threads
		// to process the replication requests.
		this.exec = Executors.newFixedThreadPool(maxReplicatorThreads);
		this.replicationProxy = new ReplicationHTTPProxy();
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request) {
		List<Future<ReplicationResult>> results = new ArrayList<Future<ReplicationResult>>();
		
		for (String server : slaveServers)
		{
			CertainBookStoreReplicationTask task = 
					new CertainBookStoreReplicationTask(server, request, replicationProxy);
			results.add(exec.submit(task));
		}
		
		return results;
	}
}
