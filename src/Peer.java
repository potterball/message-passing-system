/**
 * Peer Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

public class Peer {
    
    /**
	 * Domain name/IP address of the received peer.
	 */
    private String ip;

    /**
	 * Port number address of the received peer.
	 */
    private int port;

    /**
	 * Source address from whom the peer was received.
	 */
    private String source_address;

    /**
	 * Time at which the peer was received.
	 */
    private String timestamp;

    /**
	 * Constructor that sets up information of the received peer.
     * 
	 * @param ip the received peer's IP.
	 * @param port the received peer's port number.
     * @param source the received peer's source address.
     * @param timestamp the time at which peer was received.
	 */
    public Peer(String ip, int port, String source, String timestamp) {
        this.ip = ip;
        this.port = port;
        this.source_address = source;
        this.timestamp = timestamp;
    }

    /**
	 * Gets the IP of a received peer.
     * 
	 * @return The peer IP.
	 */
    public String getIP() {
        return ip;
    }

    /**
	 * Gets the port number of a received peer.
	 * 
     * @return The peer port number.
	 */
    public int getPort() {
        return port;
    }

    /**
	 * Gets the source of a received peer.
	 * 
     * @return The peer's source.
	 */
    public String getSourceAddress() {
        return source_address;
    }

    /**
	 * Gets the time at which the peer was received.
	 * 
     * @return The time when received.
	 */
    public String getTimestamp() {
        return timestamp;
    }

    /**
	 * String representation of the received peer's information.
     * 
     * @return Peer information.
	 */
    public String toString() {
        return source_address + " " + ip + ":" + port + " " + timestamp;
    }

    @Override
    /**
     * Checks if two received peers are equal.
     * 
     * @param o the object that is being compared to the received peer.
     * @return boolean true if object is a peer and the fields are equal else false.
     */
    public boolean equals(Object o)
    {
        //Checks if o passed is an instance the Pair class
        if(o instanceof Peer)
        {
            Peer obj = (Peer) o;
            // return true if the first field of both pairs and the second field of both pairs are equal
            return ip.equals(obj.getIP()) &&  (port == obj.getPort());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return ip.hashCode() + Integer.hashCode(port);
    }
}
