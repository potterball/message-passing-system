package ahmedalijamaal.peertopeermessagingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PeerSenderTest {

  private PeerSender peerSender;

  private final static ByteArrayOutputStream out = new ByteArrayOutputStream();

  private final static PrintStream sysOutBackup = System.out;

  // have one peer with old time stamp (seconds > 10) and check if message is sent
  // i.e. socketSend() is not invoked
  @Test
  @DisplayName("Should not send a peer")
  public void oneInactivePeerTest() throws Exception {

    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        fail();
      }

      @Override
      public int getLocalPort() {
        return 1;
      }
    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time to 10 seconds ago
    Date mock_time = new Date();
    mock_time.setTime(mock_time.getTime() - 20000);
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertTrue(true);

  }

  // have one peer with timestamp (seconds > 10) and check if message is sent i.e.
  // socketSend() is not invoked
  @Test
  @DisplayName("Should send a peer message out")
  public void oneActivePeerTest() throws Exception {
    class AppStub extends App {
      boolean yikes;

      protected AppStub() throws SocketException {
        super();
        yikes = false;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        yikes = true;
      }

      @Override
      public int getLocalPort() {
        return 1;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time
    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertTrue(sourceMock.yikes);
  }

  @Test
  @DisplayName("Should not send any peer message out")
  public void checkisPeerMeTest() throws Exception {
    class AppStub extends App {
      boolean yikes;

      protected AppStub() throws SocketException {
        super();
        yikes = true;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        yikes = false;
      }

      @Override
      public int getLocalPort() {
        return 50122;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time
    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertTrue(sourceMock.yikes);
  }

  @Test
  @DisplayName("Should send peer message out twice")
  public void moreThanOneActivePeerTest() throws Exception {
    class AppStub extends App {
      int counter;

      protected AppStub() throws SocketException {
        super();
        counter = 0;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        counter++;
      }

      @Override
      public int getLocalPort() {
        return 1;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time
    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertEquals(2, sourceMock.counter);
  }

  @Test
  @DisplayName("Should send a peer message out once")
  public void moreThanOneActivePeerButOneOfThemIsMeTest() throws Exception {
    class AppStub extends App {
      int counter;

      protected AppStub() throws SocketException {
        super();
        counter = 0;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        counter++;
      }

      @Override
      public int getLocalPort() {
        return 50122;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time
    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertEquals(1, sourceMock.counter);
  }

  @Test
  @DisplayName("Should send a peer message out once")
  public void oneActivePeerAndOneInactivePeerTest() throws Exception {
    class AppStub extends App {
      int counter;

      protected AppStub() throws SocketException {
        super();
        counter = 0;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        counter++;
      }

      @Override
      public int getLocalPort() {
        return 1;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time
    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    mock_time.setTime(mock_time.getTime() - 20000);
    timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    peerSender = new PeerSender(sourceMock);
    peerSender.sendPeer();

    assertEquals(1, sourceMock.counter);
  }

  @Test
  @DisplayName("Should print closing message")
  public void shutdownTest() throws IOException {
    out.reset();
    System.setOut(new PrintStream(out));

    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public boolean getShutdown() {
        return true;
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    peerSender = new PeerSender(sourceMock);
    peerSender.run();

    assertEquals(
        "Closing Peer Sender.\n", out.toString());

    System.setOut(new PrintStream(sysOutBackup));
  }

}
