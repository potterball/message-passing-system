package ahmedalijamaal.peertopeermessagingsystem;

public class SnipHandler implements Runnable {

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

  public SnipHandler(App aSource, ProcessAddress sourceOfMessage, String message, String timestamp) {
    this.source = aSource;
    this.sourceOfMessage = sourceOfMessage;
    this.msg = message;
    this.timestamp = timestamp;
  }

  @Override
  /**
   * Handles received messages/snips.
   */
  public void run() {

    String[] snip_info = msg.split(" ", 2);

    int msg_lamport_timestamp;

    if (snip_info[0].equals("NaN")) {
      System.out.println("Invalid message received from " + sourceOfMessage.toString());
      return;
    }

    try {
      msg_lamport_timestamp = Integer.parseInt(snip_info[0]);
    } catch (NumberFormatException ne) {
      System.out.println("Invalid message received from " + sourceOfMessage.toString());
      return;
    }

    if (msg_lamport_timestamp < 0) {
      System.out.println("Invalid message received from " + sourceOfMessage.toString());
      return;
    }

    int current_timestamp = source.logical_clock.get();

    if (current_timestamp < msg_lamport_timestamp)
      source.logical_clock.compareAndSet(current_timestamp, msg_lamport_timestamp);

    Snip new_snip = new Snip(source.logical_clock.incrementAndGet(), snip_info[1], timestamp, sourceOfMessage);

    source.message_list.add(new_snip);

    System.out.println("Message Received: " + msg + " from " + sourceOfMessage.toString());
  }

}
