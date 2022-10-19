package ahmedalijamaal.peertopeermessagingsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {

    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private ProcessAddress registry;

    /**
     * Team name to be registered at the registry process.
     */
    private String team_name;

    /**
     * Executor service that manages the creation and termination of the threads.
     */
    private ExecutorService pool;

    /**
     * Socket on which communication with peers takes place.
     */
    private DatagramSocket socket;

    /**
     * Maximum number of threads that can be executed at once.
     */
    private final int max = 20;

    private byte[] buffer;

    /**
     * Atomic flag that signals the arrival of a stop message.
     */
    private AtomicBoolean shutdown;

    /**
     * Set of all peers received.
     */
    protected Set<Peer> current_peers;

    /**
     * List of all peers received via UDP.
     */
    protected CopyOnWriteArrayList<Peer> complete_peers_list;

    /**
     * List of all peers sent to other peers.
     */
    protected CopyOnWriteArrayList<SentPeer> sent_peers;

    /**
     * Logical counter of the peer process.
     */
    protected AtomicInteger logical_clock;

    /**
     * List of all received messages.
     */
    protected CopyOnWriteArrayList<Snip> message_list;

    private int num_of_peers_from_registry = 0;

    /**
     * Constructor that sets up the attributes of the class
     */
    protected App() throws SocketException {
        shutdown = new AtomicBoolean(false);
        current_peers = ConcurrentHashMap.newKeySet();
        complete_peers_list = new CopyOnWriteArrayList<Peer>();
        sent_peers = new CopyOnWriteArrayList<SentPeer>();
        logical_clock = new AtomicInteger();
        message_list = new CopyOnWriteArrayList<Snip>();
        buffer = new byte[1024];

        pool = Executors.newFixedThreadPool(max);
        createSocket();
    }

    private static void setupLogger() {
        LogManager.getLogManager().reset();
        logr.setLevel(Level.ALL);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.SEVERE);
        logr.addHandler(ch);

        try {
            FileHandler fh = new FileHandler("logs.log", true);
            fh.setLevel(Level.INFO);
            logr.addHandler(fh);
        } catch (IOException ioe) {
            logr.log(Level.SEVERE, "File logger not working", ioe);
        }
    }

    /**
     * Accepts server name and port number as well as team name
     * to be registered under to connect to the registry server.
     * Initiates the peer process.
     * If we can't start the peer process or connect to the
     * registry server, the stack trace for the exception will
     * be printed and the program ended.
     * 
     * @param args registry process information and team name
     */
    public static void main(String[] args) {
        try {
            App app = new App();
            app.parseArguments(args);
            App.setupLogger();
            app.start();
        } catch (SocketException se) {
            logr.log(Level.SEVERE, se.getMessage(), se);
        } catch (Exception e) {
            logr.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Starts the peer process. Connects to the
     * registry server. Creates threads to deal with peer messages,
     * user input to be sent and sending of active peers.
     * If we can't start the peer process or connect to the
     */
    public void start() throws Exception {

        System.out.println(team_name + " - started UDP server at " + Util.getLocalHostExternalAddress() + ":"
                + socket.getLocalPort());

        RegistryCommunicator rc = new RegistryCommunicator(this);
        rc.manageRequests();

        pool.execute(new PeerSender(this));
        pool.execute(new MessageReceiver(this));
        pool.execute(new SnippetManager(this));

        while (!shutdown.get()) {
        }

        Thread.sleep(3000);

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        socket.close();

        rc.manageRequests();
    }

    protected void createSocket() throws SocketException {
        socket = new DatagramSocket();
    }

    /**
     * Parses command line arguments into specific fields using a hashmap
     * 
     * @param args arguments enterd by the user
     * @return hasmap containing field shorthand and the values entered by the user
     */
    private void parseArguments(String[] args) {

        // checks if the correct number of command line arguments are entered.
        if (args.length != 6) {
            System.out.println("incorrect usage, Usage: java App -s <server_name> -p <port_number> -t <team_name>");
            System.out.println("try again");
            System.exit(0);
        }

        HashMap<String, String> params = new HashMap<String, String>();

        int i = 0;
        while ((i + 1) < args.length) {
            params.put(args[i], args[i + 1]);
            i += 2;
        }

        if (params.get("-s") == null || params.get("-p") == null || params.get("-t") == null) {
            System.out.println("incorrect usage, Usage: java App -s <server_name> -p <port_number> -t <team_name>");
            System.out.println("try again");
            System.exit(0);
        }

        if (!ProcessAddress.validatePort(params.get("-p"))) {
            System.out.println("incorrect usage. Enter valid port number");
            System.exit(0);
        }

        registry = new ProcessAddress(params.get("-s"), Integer.parseInt(params.get("-p")));
        team_name = params.get("-t");
    }

    /**
     * Gets the IP of the registry.
     * 
     * @return The registry IP.
     */
    public String getRegistryIP() {
        return registry.getIP();
    }

    /**
     * Gets the port number of the registry.
     * 
     * @return The registry port number.
     */
    public int getRegistryPort() {
        return registry.getPort();
    }

    /**
     * Gets the team name the peer is registered under.
     * 
     * @return The peer team name.
     */
    public String getTeamName() {
        return team_name;
    }

    /**
     * Gets the number of peers received from the registry.
     * 
     * @return number of peers.
     */
    public int getNoOfRegistryPeers() {
        return num_of_peers_from_registry;
    }

    /**
     * Increments the number of peers received from the registry.
     */
    public void incrementNoOfRegistryPeers() {
        num_of_peers_from_registry++;
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public void socketSend(DatagramPacket packet) throws IOException {
        socket.send(packet);
    }

    public DatagramPacket socketReceive() throws IOException {
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivedPacket);
        return receivedPacket;
    }

    public void createNewThread(Runnable thread) {
        pool.execute(thread);
    }

    public void closeSocket() {
        socket.close();
    }

    public boolean getShutdown() {
        return shutdown.get();
    }

    public boolean compareAndSetShutdown(boolean expect, boolean update) {
        return shutdown.compareAndSet(expect, update);
    }

    public boolean isThePeerMe(Peer check) throws Exception {
        if ((check.getIP().equals(Util.getLocalHostExternalAddress())
                || check.getIP().equals(InetAddress.getLocalHost().getHostAddress()))
                && check.getPort() == this.getLocalPort()) {
            return true;
        }

        return false;
    }
}
