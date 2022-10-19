package ahmedalijamaal.peertopeermessagingsystem;

public class PeerHandler implements Runnable {
  /**
   * The peer process that is receiving messages from other peers.
   */
  private App source;

  /**
   * Source address from whom the snip was received.
   */
  private ProcessAddress sourceOfMessage;

  /**
   * Content of the message.
   */
  private String msg;

  /**
   * Time at which the message was received.
   */
  private String timestamp;

  public PeerHandler(App aSource, ProcessAddress sourceOfMessage, String message, String timestamp) {
    this.source = aSource;
    this.sourceOfMessage = sourceOfMessage;
    this.msg = message;
    this.timestamp = timestamp;
  }

  @Override
  /**
   * Handles received peers.
   */
  public void run() {

    String[] peer_info = msg.split(":");
    int port;

    if (!ProcessAddress.validateIP(peer_info[0]) || !ProcessAddress.validatePort(peer_info[1])) {
      System.out.println("Invalid peer received from " + sourceOfMessage.toString());
      return;
    }

    port = Integer.parseInt(peer_info[1]);

    Peer new_peer = new Peer(new ProcessAddress(peer_info[0], port), sourceOfMessage, timestamp);

    source.complete_peers_list.add(new_peer);

    if (!source.current_peers.add(new_peer)) {
      source.current_peers.remove(new_peer);
      source.current_peers.add(new_peer);
    }

  }

}
