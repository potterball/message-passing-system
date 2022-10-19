package ahmedalijamaal.peertopeermessagingsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StopHandler implements Runnable {

  private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  /**
   * The peer process that is receiving messages from other peers.
   */
  private App source;

  /**
   * Source address from whom the snip was received.
   */
  private ProcessAddress sourceOfMessage;

  public StopHandler(App aSource, ProcessAddress sourceOfMessage) {
    this.source = aSource;
    this.sourceOfMessage = sourceOfMessage;
  }

  @Override
  public void run() {
    try {
      String msg = "ack" + source.getTeamName();
      byte[] buffer = msg.getBytes();
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(sourceOfMessage.getIP()),
          sourceOfMessage.getPort());
      source.socketSend(packet);
      System.out.println("Sent ack for stop message.");
    } catch (UnknownHostException uhe) {
      logr.log(Level.WARNING, "Could not find host of stop", uhe);
    } catch (IOException ioe) {
      logr.log(Level.WARNING, "Could not send ack", ioe);
    }
  }
}
