package ahmedalijamaal.peertopeermessagingsystem;

public class SentPeer {

  /**
   * Domain name/IP address of the sent peer.
   */

  private ProcessAddress sentPeer;

  private ProcessAddress destination;

  /**
   * Time at which the peer was sent.
   */
  private String timestamp;

  /**
   * Constructor that sets up information of the sent peer.
   * 
   * @param sending   the sent peer's IP and port number.
   * @param dest      the sent peer's destination address.
   * @param timestamp time at which peer was sent.
   */
  public SentPeer(ProcessAddress sending, ProcessAddress dest, String timestamp) {
    this.sentPeer = sending;
    this.destination = dest;
    this.timestamp = timestamp;
  }

  /**
   * String representation of the sent peer's information.
   * 
   * @return Peer information.
   */
  public String toString() {
    return destination.toString() + " " + sentPeer.toString() + " " + timestamp;
  }
}
