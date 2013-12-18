/**
 * 
 */
package com.acertainbookstore.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * 
 * ReplicationAwareBookStoreHTTPProxy implements the client level synchronous
 * CertainBookStore API declared in the BookStore class. It keeps retrying the
 * API until a consistent reply is returned from the replicas
 * 
 */
public class ReplicationAwareBookStoreHTTPProxy implements BookStore {
	private HttpClient client;
	private Set<String> slaveAddresses;
	private String masterAddress;
	private String filePath = "src/proxy.properties";
	private volatile long snapshotId = 0;
	private boolean masterUp = true;
	
	private final static int SECOND=5000; 

	public long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(long snapShotId) {
		this.snapshotId = snapShotId;
	}

	/**
	 * Initialize the client object
	 */
	public ReplicationAwareBookStoreHTTPProxy() throws Exception {
		initializeReplicationAwareMappings();
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS); // max
																									// concurrent
																									// connections
																									// to
																									// every
																									// address
		client.setThreadPool(new QueuedThreadPool(
				BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS)); // max
																			// threads
		client.setTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS); // seconds
																					// timeout;
																					// if
																					// no
																					// server
																					// reply,
																					// the
																					// request
																					// expires
		client.start();
	}

	private void initializeReplicationAwareMappings() throws IOException {

		Properties props = new Properties();
		slaveAddresses = new HashSet<String>();

		props.load(new FileInputStream(filePath));
		this.masterAddress = props
				.getProperty(BookStoreConstants.KEY_MASTER);
		if (!this.masterAddress.toLowerCase().startsWith("http://")) {
			this.masterAddress = new String("http://" + this.masterAddress);
		}

		String slaveAddresses = props
				.getProperty(BookStoreConstants.KEY_SLAVE);
		for (String slave : slaveAddresses
				.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith("http://")) {
				slave = new String("http://" + slave);
			}
			this.slaveAddresses.add(slave);
		}
	}

	
	public String getReplicaAddress() {
		
		// We set the slave address has the double probability as master address, 
		// and divide the range [0, 1] into masterAddress part and slaveAddress part. 
		// Firstly, we randomly generate a number in [0, 1], if the number is in slaveAddress
		// probability part, we randomly return a slaveAddress; otherwise, we return the masterAddress. 
		
		int sizeOfSlave = slaveAddresses.size();
		Random random = new Random();
		String returnSlaveAddress = new String();
		float range = (float) 1/(1 + 2*sizeOfSlave);
		
		if(slaveAddresses.isEmpty()) {
			return masterAddress;
		}
		
		float probability = random.nextFloat();
		if (probability > range || masterUp == false){
			
			int randomIdx = new Random().nextInt(sizeOfSlave);
			int i = 0;
			for(String obj : slaveAddresses)
			{
			    if (i == randomIdx)
			        returnSlaveAddress =  obj;
			    i = i + 1;
			}
			
			return returnSlaveAddress;
		}
		else{
			return masterAddress;
		}
		
	}

	public String getMasterServerAddress() {
		return this.masterAddress;
	}

	public void buyBooks(Set<BookCopy> isbnSet) throws BookStoreException {
		if(masterUp) {
			String listISBNsxmlString = BookStoreUtility
					.serializeObjectToXMLString(isbnSet);
			Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);
	
			BookStoreResult result = null;
	
			ContentExchange exchange = new ContentExchange();
			String urlString = getMasterServerAddress() + "/"
					+ BookStoreMessageTag.BUYBOOKS;
			exchange.setMethod("POST");
			exchange.setURL(urlString);
			exchange.setRequestContent(requestContent);
			result = BookStoreUtility.SendAndRecv(this.client, exchange);
			this.setSnapshotId(result.getSnapshotId());
		}
	}

	@SuppressWarnings("unchecked")
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {

		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(isbnSet);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		
		BookStoreResult result = null;
		long getBooksStart = System.currentTimeMillis();
		
		do {
			ContentExchange exchange = new ContentExchange();
			String randomServer = getReplicaAddress();
			String urlString = randomServer + "/"
					+ BookStoreMessageTag.GETBOOKS;
			exchange.setMethod("POST");
			exchange.setURL(urlString);
			exchange.setRequestContent(requestContent);
			
			//Here we see if the caught exception is a timeout, which would mean the server 
			//belonging to the received address has failed. If it was a slave server, we remove it
			//from the Set of slaveServers and run the method again.
			//If it was a master server that failed, it is flagged as failed and function is
			//called again. 
			//This is done for all read methods in both proxies to achieve the fail-stop model
			//between proxies and servers
			try {
				result = BookStoreUtility.SendAndRecv(this.client, exchange);
			} catch (Exception ex) {
				if (ex instanceof BookStoreException)
				{
					BookStoreException bsEx = (BookStoreException) ex;
					if (bsEx.getMessage() != BookStoreClientConstants.strERR_CLIENT_REQUEST_TIMEOUT &&
							  bsEx.getMessage() != BookStoreClientConstants.strERR_CLIENT_REQUEST_EXCEPTION)
						throw bsEx;
				}
				
				if(!randomServer.equals(masterAddress)) {
					if(!slaveAddresses.isEmpty()) {
						slaveAddresses.remove(randomServer);
						return this.getBooks(isbnSet);
					}
				} else {
					masterUp = false;
					if(!slaveAddresses.isEmpty()) {
						return this.getBooks(isbnSet);
					}
				}	
			}
			
			long getBooksEnd = System.currentTimeMillis();
			if (getBooksEnd - getBooksStart > SECOND)
				throw new BookStoreException();
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());
		return (List<Book>) result.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		ContentExchange exchange = new ContentExchange();
		String urlEncodedNumBooks = null;

		try {
			urlEncodedNumBooks = URLEncoder.encode(Integer.toString(numBooks),
					"UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new BookStoreException("unsupported encoding of numbooks", ex);
		}

		BookStoreResult result = null;
		long getEditorPicksStart = System.currentTimeMillis();
		do {
			String randomServer = getReplicaAddress();
			String urlString = randomServer + "/"
					+ BookStoreMessageTag.EDITORPICKS + "?"
					+ BookStoreConstants.BOOK_NUM_PARAM + "="
					+ urlEncodedNumBooks;
			exchange.setURL(urlString);
			try {
				result = BookStoreUtility.SendAndRecv(this.client, exchange);
			} catch (Exception ex) {
				if (ex instanceof BookStoreException)
				{
					BookStoreException bsEx = (BookStoreException) ex;
					if (bsEx.getMessage() != BookStoreClientConstants.strERR_CLIENT_REQUEST_TIMEOUT &&
							  bsEx.getMessage() != BookStoreClientConstants.strERR_CLIENT_REQUEST_EXCEPTION)
						throw bsEx;
				}
				
				if(!randomServer.equals(masterAddress)) {
					if(!slaveAddresses.isEmpty()) {
						slaveAddresses.remove(randomServer);
						return this.getEditorPicks(numBooks);
					}
				} else {
					masterUp = false;
					if(!slaveAddresses.isEmpty()) {
						return this.getEditorPicks(numBooks);
					}
				}	
			}

			
			long getEditorPicksEnd = System.currentTimeMillis();
			if (getEditorPicksEnd - getEditorPicksStart > SECOND)
				throw new BookStoreException();
		} while (result.getSnapshotId() < this.getSnapshotId());
		this.setSnapshotId(result.getSnapshotId());

		return (List<Book>) result.getResultList();
	}

	public void stop() {
		try {
			client.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();

	}

	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		// TODO Auto-generated method stub
		throw new BookStoreException();
	}

}
