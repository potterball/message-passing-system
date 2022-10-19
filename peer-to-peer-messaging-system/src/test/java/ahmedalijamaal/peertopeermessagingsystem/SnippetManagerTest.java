package ahmedalijamaal.peertopeermessagingsystem;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SnippetManagerTest {

  private SnippetManager snippetManager;

  // Check if the message being sent is actually the right info when the mock
  // setssetsup
  // one peer in current_peer_list. will have to mock sendPacket
  @Test
  @DisplayName("Should construct the right message")
  public void oneActivePeerTest() throws Exception {
    class AppStub extends App {
      boolean yikes;

      protected AppStub() throws SocketException {
        super();
        yikes = false;
      }

      @Override
      public void socketSend(DatagramPacket packet) throws IOException {
        String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
        assertAll("Packet contents should be appropriate",
            () -> assertEquals("snip1 hello", msg),
            () -> assertEquals(packet.getPort(), 50122),
            () -> assertEquals(packet.getAddress().toString().replaceFirst("/", ""),
                InetAddress.getLocalHost().getHostAddress()));
        yikes = true;
      }

      @Override
      public int getLocalPort() {
        return 1;
      }
    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    // mocking the time to 10 seconds ago
    Date mock_time = new Date();
    mock_time.setTime(mock_time.getTime());
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    snippetManager = new SnippetManager(sourceMock);
    snippetManager.sendSnip("hello");

    assertTrue(sourceMock.yikes);
  }

  @Test
  @DisplayName("Should not send a message")
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

    // mocking the time to 10 seconds ago
    Date mock_time = new Date();
    mock_time.setTime(mock_time.getTime());
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    snippetManager = new SnippetManager(sourceMock);
    snippetManager.sendSnip("hello");

    assertTrue(sourceMock.yikes);
  }

  @Test
  @DisplayName("Should send snip twice")
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

    // mocking the time to 10 seconds ago
    Date mock_time = new Date();
    mock_time.setTime(mock_time.getTime());
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    snippetManager = new SnippetManager(sourceMock);
    snippetManager.sendSnip("hello");

    assertEquals(2, sourceMock.counter);
  }

  @Test
  @DisplayName("Should send snip only once")
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

    // mocking the time to 10 seconds ago
    Date mock_time = new Date();
    mock_time.setTime(mock_time.getTime());
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    // set up one active peer in sourceMock.current_peers and complete_peers_list
    Peer new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    new_peer = new Peer(new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50122), timestamp);
    sourceMock.current_peers.add(new_peer);

    snippetManager = new SnippetManager(sourceMock);
    snippetManager.sendSnip("hello");

    assertEquals(1, sourceMock.counter);
  }
}
