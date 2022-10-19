package ahmedalijamaal.peertopeermessagingsystem;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.*;

public class MessageReceiverTest {

  private MessageReceiver messageReceiver;

  private final static ByteArrayOutputStream out = new ByteArrayOutputStream();

  private final static PrintStream sysOutBackup = System.out;

  @BeforeEach
  public void setupBeforeEachTest() throws IOException {
    out.reset();
    System.setOut(new PrintStream(out));
  }

  @Test
  @DisplayName("Should give an invalid message output to the command line")
  public void invalidMessageReceived() throws Exception {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = "bullsh#t".getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid message received from /" + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should give an invalid peer output to the command line")
  public void invalidPort0PeerMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {

        byte[] testMessage = ("peer100.100.100.100:0").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals("Invalid peer received from " +
        InetAddress.getLocalHost().getHostAddress() + ":50122\n",
        out.toString());
  }

  @Test
  @DisplayName("Should give an invalid peer output to the command line")
  public void invalidIPMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("peer50122.50122.50122.50122:50122").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid peer received from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should give an invalid peer output to the command line")
  public void invalidPortNumberFormatExceptionPeerMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("peer100.100.100.100:" + null).getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid peer received from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should add peer to lists")
  public void validPeerMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("peer100.100.100.100:50122").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(1, sourceMock.complete_peers_list.size());
    assertEquals(1, sourceMock.current_peers.size());
  }

  @Test
  @DisplayName("Should add peer to complete peer list but not to current peers")
  public void validPeerMessagethatAlreadyExistsReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("peer100.100.100.100:50122").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    Date mock_time = new Date();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mock_time);

    Peer new_peer = new Peer(new ProcessAddress("100.100.100.100", 50122),
        new ProcessAddress(InetAddress.getLocalHost().getHostAddress(), 50123), timestamp);
    sourceMock.complete_peers_list.add(new_peer);
    sourceMock.current_peers.add(new_peer);

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(2, sourceMock.complete_peers_list.size());
    assertEquals(1, sourceMock.current_peers.size());
  }

  @Test
  @DisplayName("Should give an invalid message output to the command line")
  public void invalidNegativeTimestampSnipMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("snip-1 hello").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid message received from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should give an invalid message output to the command line")
  public void invalidNaNTimestampSnipMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("snip" + (0. / 0) + " hello").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid message received from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should give an invalid message output to the command line")
  public void invalidNumberFormatExceptionSnipMessageReceived() throws IOException {

    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("snipA1 hello").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Invalid message received from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should print message received to the command line")
  public void validSnipMessageReceived() throws IOException {
    class AppStub extends App {
      protected AppStub() throws SocketException {
        super();
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("snip1 hello").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    App sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals(
        "Message Received: 1 hello from " + InetAddress.getLocalHost().getHostAddress() + ":50122\n", out.toString());
  }

  @Test
  @DisplayName("Should print closing message")
  public void shutdownTest() throws IOException {
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

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.run();

    assertEquals(
        "Closing Message Receiver.\n", out.toString());
  }

  @Test
  @DisplayName("Should print ack message")
  public void stopHandlerTest() throws IOException {
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
            () -> assertEquals("acktest", msg),
            () -> assertEquals(packet.getPort(), 50122),
            () -> assertEquals(packet.getAddress().toString().replaceFirst("/", ""),
                InetAddress.getLocalHost().getHostAddress()));
        yikes = true;
      }

      @Override
      public DatagramPacket socketReceive() throws IOException {
        byte[] testMessage = ("stop").getBytes();
        return new DatagramPacket(testMessage, testMessage.length,
            InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), 50122);
      }

      @Override
      public String getTeamName() {
        return "test";
      }

      @Override
      public void createNewThread(Runnable thread) {
        thread.run();
      }

    }

    AppStub sourceMock = new AppStub();
    sourceMock.closeSocket();

    messageReceiver = new MessageReceiver(sourceMock);
    messageReceiver.readPacket();

    assertEquals("Sent ack for stop message.\n", out.toString());
    assertEquals(true, sourceMock.yikes);
  }

  @AfterEach
  public void cleanupAfterEachTest() {
    System.setOut(new PrintStream(sysOutBackup));
  }

}
