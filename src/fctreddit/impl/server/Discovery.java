package fctreddit.impl.server;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * <p>
 * A class to perform service discovery, based on periodic service contact
 * endpoint announcements over multicast communication.
 * </p>
 * 
 * <p>
 * Servers announce their *name* and contact *uri* at regular intervals. The
 * server actively collects received announcements.
 * </p>
 * 
 * <p>
 * Service announcements have the following format:
 * </p>
 * 
 * <p>
 * &lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;
 * </p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());



	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	// The pre-aggreed multicast endpoint assigned to perform discovery.
	// Allowed IP Multicast range: 224.0.0.1 - 239.255.255.255
	static final public InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
	static final int DISCOVERY_RETRY_TIMEOUT = 3000;
	static final int MAX_DATAGRAM_SIZE = 65536;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private static Discovery instance;  // Static field for the single instance

	private final InetSocketAddress addr;
	private final String serviceName;
	private final String serviceURI;
	private final MulticastSocket ms;

	private ConcurrentHashMap<String, ConcurrentHashMap<URI, Instant>> receivedAnnouncements = new ConcurrentHashMap<>();
	/**
	 * @param serviceName the name of the service to announce
	 * @param serviceURI  an uri string - representing the contact endpoint of the
	 *                    service being announced
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 */
	public Discovery(InetSocketAddress addr, String serviceName, String serviceURI) throws SocketException, UnknownHostException, IOException {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI = serviceURI;

		if (this.addr == null) {
			throw new RuntimeException("A multinet address has to be provided.");
		}
		
		this.ms = new MulticastSocket(addr.getPort());
		this.ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
	}

	public Discovery(InetSocketAddress addr) throws SocketException, UnknownHostException, IOException {
		this(addr, null, null);
	}



	/**
	 * Starts sending service announcements at regular intervals...
	 * @throws IOException 
	 */
	public void start() {


		//If this discovery instance was initialized with information about a service, start the thread that makes the
		//periodic announcement to the multicast address.

		if (this.serviceName != null && this.serviceURI != null) {

			Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", addr, serviceName,
					serviceURI));

			byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
			DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

			try {
				// start thread to send periodic announcements
				new Thread(() -> {
					for (;;) {
						try {
							ms.send(announcePkt);
							Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
						} catch (Exception e) {
							e.printStackTrace();
							// do nothing
						}
					}
				}).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// start thread to collect announcements received from the network.
		new Thread(() -> {
			DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
			for (;;) {
				try {
					pkt.setLength(MAX_DATAGRAM_SIZE);
					ms.receive(pkt);
					String msg = new String(pkt.getData(), 0, pkt.getLength());
					String[] msgElems = msg.split(DELIMITER);
					if (msgElems.length == 2) { // periodic announcement
						/*System.out.printf("FROM %s (%s) : %s\n", pkt.getAddress().getHostName(),
								pkt.getAddress().getHostAddress(), msg);*/

						Instant timestamp = Instant.now();
						try {
							String serviceName = msgElems[0];
						URI uri = URI.create(msgElems[1]);
							if (receivedAnnouncements.containsKey(serviceName)) {
								receivedAnnouncements.get(serviceName).put(uri, timestamp);
							} else {
								ConcurrentHashMap<URI, Instant> serviceMap = new ConcurrentHashMap<>();
								serviceMap.put(uri, timestamp);
								receivedAnnouncements.put(serviceName, serviceMap);
							}
						} catch (Exception e) {
							Log.info("Invalid URI: " + msgElems[1]);
						}

						// TODO: to complete by recording the received information

					}
				} catch (IOException e) {
					// do nothing
				}
			}
		}).start();
	}

	/**
	 * Returns the known services.
	 * 
	 * @param serviceName the name of the service being discovered
	 * @param minReplies  - minimum number of requested URIs. Blocks until the
	 *                    number is satisfied.
	 * @return an array of URI with the service instances discovered.
	 * 
	 */
	public URI[] knownUrisOf(String serviceName, int minReplies) throws InterruptedException {
		if (minReplies < 0) {
			throw new IllegalArgumentException("minReplies must be a positive number");
		}
			while (true) {
				ConcurrentHashMap<URI, Instant> urlsTuples = receivedAnnouncements.get(serviceName);

				if (urlsTuples != null && urlsTuples.size() >= minReplies) {
					return urlsTuples.keySet().toArray(new URI[0]);
				}

				Thread.sleep(DISCOVERY_RETRY_TIMEOUT);
			}
	}

		// Main just for testing purposes
		public static void main (String[]args) throws Exception {
			Discovery discovery = new Discovery(DISCOVERY_ADDR, "test",
					"http://" + InetAddress.getLocalHost().getHostAddress());
			discovery.start();
		}
	}
