/**
 * Sent Peer Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

public class SentPeer {

    /**
	 * Domain name/IP address of the sent peer.
	 */
    private String ip;

    /**
	 * Port number address of the sent peer.
	 */
    private int port;

    /**
	 * Destination address to whom the peer was sent.
	 */
    private String destination_address;

    /**
	 * Time at which the peer was sent.
	 */
    private String timestamp;

    /**
	 * Constructor that sets up information of the sent peer.
     * 
	 * @param ip the sent peer's IP.
	 * @param port the sent peer's port number.
     * @param dest the sent peer's destination address.
     * @param timestamp time at which peer was sent.
	 */
    public SentPeer(String ip, int port, String dest, String timestamp) {
        this.ip = ip;
        this.port = port;
        this.destination_address = dest;
        this.timestamp = timestamp;
    }

    /**
	 * String representation of the sent peer's information.
     * 
     * @return Peer information.
	 */
    public String toString() {
        return destination_address + " " + ip + ":" + port + " " + timestamp;
    }
}
