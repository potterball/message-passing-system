package ahmedalijamaal.peertopeermessagingsystem;

public class Peer {

  /**
   * Domain name/IP address and Port number of the received peer.
   */
  private ProcessAddress peer;

  /**
   * Source address from whom the peer was received.
   */
  private ProcessAddress source;

  /**
   * Time at which the peer was received.
   */
  private String timestamp;

  public Peer(ProcessAddress received, ProcessAddress source, String timestamp) {
    this.peer = received;
    this.source = source;
    this.timestamp = timestamp;
  }

  /**
   * Gets the IP of a received peer.
   * 
   * @return The peer IP.
   */
  public String getIP() {
    return peer.getIP();
  }

  /**
   * Gets the port number of a received peer.
   * 
   * @return The peer port number.
   */
  public int getPort() {
    return peer.getPort();
  }

  /**
   * Gets the source of a received peer.
   * 
   * @return The peer's source.
   */
  public String getSourceAddress() {
    return source.toString();
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
    return source.toString() + " " + peer.toString() + " " + timestamp;
  }

  @Override
  /**
   * Checks if two received peers are equal.
   * 
   * @param o the object that is being compared to the received peer.
   * @return boolean true if object is a peer and the fields are equal else false.
   */
  public boolean equals(Object o) {
    // Checks if o passed is an instance the Pair class
    if (o instanceof Peer) {
      Peer obj = (Peer) o;
      // return true if the first field of both pairs and the second field of both
      // pairs are equal
      return peer.getIP().equals(obj.getIP()) && (peer.getPort() == obj.getPort());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return peer.getIP().hashCode() + Integer.hashCode(peer.getPort());
  }
}