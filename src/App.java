/**
 * App Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    /**
	 * Domain name/IP address of the machine on which the registry process is located.
	 */
    private String registry_ip;

    /**
	 * Port number of the registry process.
	 */
    private int registry_port;

    /**
	 * Team name to be registered at the registry process.
	 */
    private String team_name;

    /**
	 * Executor service that manages the creation and termination of the threads.
	 */
    public ExecutorService pool;

    /**
	 * Socket on which communication with peers takes place.
	 */
    public DatagramSocket socket;

    /**
	 * Maximum number of threads that can be executed at once.
	 */
    private final int max = 20;

    /**
	 * Atomic flag that signals the arrival of a stop message.
	 */
    public AtomicBoolean shutdown;
    
    /**
	 * Set of all peers received.
	 */
    public Set<Peer> current_peers;
    
    /**
	 * List of all peers received via UDP.
	 */
    public CopyOnWriteArrayList<Peer> complete_peers_list;

    /**
	 * List of all peers sent to other peers.
	 */
    public CopyOnWriteArrayList<SentPeer> sent_peers;

    /**
	 * Logical counter of the peer process.
	 */
    public AtomicInteger logical_clock;

    /**
	 * List of all received messages.
	 */
    public CopyOnWriteArrayList<Snip> message_list;

    private int num_of_peers_from_registry = 0;

    private final String IPV4_PATTERN =
           "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
           "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
           "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
           "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

    /**
	 * Constructor that sets up the attributes of the class
	 */
    private App() {
        shutdown = new AtomicBoolean(false);
        current_peers = ConcurrentHashMap.newKeySet();
        complete_peers_list = new CopyOnWriteArrayList<Peer>();
        sent_peers = new CopyOnWriteArrayList<SentPeer>();
        logical_clock = new AtomicInteger();
        message_list = new CopyOnWriteArrayList<Snip>();
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
    public static void main(String[] args) throws Exception {
        App app = new App();
        app.parseArguments(args);
        try {
            app.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Starts the peer process. Connects to the
     * registry server. Creates threads to deal with peer messages,
     * user input to be sent and sending of active peers.
	 * If we can't start the peer process or connect to the
	 */
    public void start() throws Exception{
        
        pool = Executors.newFixedThreadPool(max);
        socket = new DatagramSocket();
        System.out.println(team_name + " - started UDP server at " + LocalHost.getLocalHostExternalAddress() + ":" + socket.getLocalPort());

        RegistryCommunicator rc = new RegistryCommunicator(this);
        rc.manageRequests();

        pool.execute(new PeerSender(this));
        pool.execute(new MessageReceiver(this));
        pool.execute(new SnippetManager(this));

        while(!shutdown.get()) {
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
		pool.shutdownNow();

        socket.close();

        rc.manageRequests();
    }

    /**
	 * Parses command line arguments into specific fields using a hashmap
	 * 
	 * @param args arguments enterd by the user
     * @return hasmap containing field shorthand and the values entered by the user
	 */
	private void parseArguments(String[] args) {

        // checks if the correct number of command line arguments are entered.
        if(args.length != 6)
        {
            System.out.println("incorrect usage, Usage: java App -s <server_name> -p <port_number> -t <team_name>");
			System.out.println("try again");
			System.exit(0);
        }

		HashMap<String, String> params = new HashMap<String, String>();

		int i = 0;
		while ((i + 1) < args.length) {
			params.put(args[i], args[i+1]);
			i += 2;
		}
		
		if(params.get("-s") == null || params.get("-p") == null || params.get("-t") == null)
        {
            System.out.println("incorrect usage, Usage: java App -s <server_name> -p <port_number> -t <team_name>");
			System.out.println("try again");
			System.exit(0);
        }

        if(!params.get("-p").matches("[0-9]+"))
        {
            System.out.println("incorrect usage. Enter valid port number");
            System.exit(0);
        }

        registry_ip = params.get("-s");
        registry_port = Integer.parseInt(params.get("-p"));
        team_name = params.get("-t");
	}

    /**
	 * Gets the IP of the registry.
	 * @return The registry IP.
	 */
    public String getRegistryIP() {
        return registry_ip;
    }

    /**
	 * Gets the port number of the registry.
	 * @return The registry port number.
	 */
    public int getRegistryPort() {
        return registry_port;
    }

    /**
	 * Gets the team name the peer is registered under.
	 * @return The peer team name.
	 */
    public String getTeamName() {
        return team_name;
    }

    /**
	 * Gets the number of peers received from the registry.
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

    public String getIPV4_Pattern() {
        return IPV4_PATTERN;
    }
}
