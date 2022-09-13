/**
 * Registry Communicator Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

public class RegistryCommunicator {

    /**
	 * Socket on which communication with the registry takes place.
	 */
    private Socket socket;

    /**
	 * Buffered reader that reads requests from the socket connected to the registry.
	 */
    private BufferedReader reader;

    /**
	 * Buffered writer that writes to the socket connected to the registry.
	 */
    private BufferedWriter writer;

    /**
	 * The peer process that is connecting to the registry.
	 */
    private App source;

    /**
	 * The time at which peers were received from the registry.
	 */
    private String timestamp;

    /**
	 * List of all peers received from the registry.
	 */
    public ArrayList<Peer> registry_peers;

    /**
	 * Constructor that sets up information needed to send the connection request to the registry.
	 * @param source peer process who is connecting to the registry
	 */
    public RegistryCommunicator(App aSource) {
        source = aSource;
        registry_peers = new ArrayList<Peer>();
    }

    /**
	 * Manages requests and peers from the registry.
     * @throws Exception if there is any problems communicating with the registry.
	 */
    public void manageRequests() throws Exception {

        socket = new Socket(source.getRegistryIP(), source.getRegistryPort());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        System.out.println(source.getTeamName() + " - connected to Registry");

        String request;
        boolean loop = true;
        
        // loops until close request has been sent by the registry
        while(loop) {
            request = reader.readLine();
            // calls appropirate method depending on registry request
            switch(request) {
                case "get team name":
                                     System.out.println("Sending Team Name"); 
                                     sendTeam();
                                     System.out.println("Finished Request");
                                     break;
                case "get code":
                                System.out.println("Sending Source Code");
                                sendCode();
                                System.out.println("Finished Request");
                                break;
                case "receive peers":
                                     System.out.println("Receiving Peers");
                                     receivePeers();
                                     System.out.println("Finished Request");
                                     break;
                case "get report":
                                  System.out.println("Sending Report");
                                  sendReport();
                                  System.out.println("Finished Request");
                                  break;
                case "get location":
                                    System.out.println("Sending location");
                                    sendLocation();
                                    System.out.println("Finished Request");
                                    break;
                case "close":
                             loop = false;
                             break;
            }
        }

        System.out.println(source.getTeamName() + " - Finished with Registry.");

        reader.close();
        writer.close();
        socket.close();
    }

     /**
	 * Send the name of the team for this peer to the registry.
     * Communication protocol:
	 * recieve: 'get team name\n'
	 * send: <name of team><new line>
     * @throws Exception if there is any problems writing to the port.
	 */
    private void sendTeam() throws Exception {
        writer.write(source.getTeamName() + "\n");
        writer.flush();
    }



    /**
	 * Send the source code for this peer to the registry.
     * Communication protocol:
	 * recieve: 'get code\n'
	 * send: <programming language extension of source code><new line>
	 * send: <source code of Peer file><new line>
     * send: <source code of dependent files><new line>
	 * send: '...'<new line>
     * @throws Exception if there is any problems writing to the port
     *                   or opening source code files.
	 */
    private void sendCode() throws Exception {
        // sending programming language
        writer.write("java\n");

        //printFiles("/home/ugd/ahmedalijamaal.najja/cpsc559/Iteration3/src");
        printFiles("C:\\Users\\360me\\Documents\\w2022\\CPSC 559\\Iteration3LocalHost\\src");

        // sending end of code signifier to registry
        writer.write("...\n");
        writer.flush();
    }

    /**
     * Recursively opens files if they are directories and finds source
     * code and text files in the project src folder.
     * 
     * Reference: https://stackoverflow.com/a/24324367
     * 
     * @param path path of the file containing all source code
     * @throws Exception if there is any problems wrtiting to the port
     *                   or opening source code files.
     */
    private void printFiles(String path) throws Exception {

        File[] files = new File(path).listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                printFiles(path + file.getName() + "\\");
            } else {
                //reading source code and sending it to registry
                Scanner scanner = new Scanner(file);
                while(scanner.hasNextLine())
                    writer.write(scanner.nextLine() + "\n");
                scanner.close();
            }
        }
    }

    /**
	 * Accepts and reads a list of peers from the registry. 
	 * 
	 * Communication protocol:
     * receive: 'receive peers\n'
	 * receive: <number of peers><newline>
	 * for each peer: receive <ip address><colon><port><newline>
     * @throws Exception if there is any problems communicating with the registry.
     */
    private void receivePeers() throws Exception {
        String[] peer_info;
        int port;

        Peer new_peer = null;
        // parsing the number of peers from the message sent by the registry
        int numOfPeers = Integer.parseInt(reader.readLine());

        timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        for(int i = 0; i < numOfPeers; i++) {
            // received a peer and checks if it's information is redundant(already sent before and recorded)
            peer_info = reader.readLine().split(":");

            try {
                port = Integer.parseInt(peer_info[1]);
            }
            catch(NumberFormatException ne) {
                System.out.println("Invalid peer received from registry");
                continue;
            }

            if(port < 1 || port > 65535) {
                System.out.println("Invalid peer received from registry");
                continue;
            }

            if(!peer_info[0].matches(source.getIPV4_Pattern())) {
                System.out.println("Invalid peer received from registry");
                continue;
            }

            new_peer = new Peer(peer_info[0], port, 
                                ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().split("/")[1] + ":" + socket.getPort(),
                                timestamp);

            registry_peers.add(new_peer);
            source.incrementNoOfRegistryPeers();

            source.complete_peers_list.add(new_peer);

            // checks if received peer is already a member of the set of active peer, updates information
            // if it is already a member
            if(!source.current_peers.add(new_peer))
            {
                source.current_peers.remove(new_peer);
                source.current_peers.add(new_peer);
            }
        }
    }

    /**
	 * Sends a report of it's actions to the registry. This should include the peers stored in 
	 * the set of peers, peers received from other peers and the messages received.
	 * 
	 * @throws Exception if there is any problems communicating with the peer for this report request.
	 */
    private void sendReport() throws Exception
    {
        // sending the number of active peers
        writer.write(source.current_peers.size() + "\n");
        // sending the ip and port number of the active peers. Format: <ip><colon><port><newline>
        for(Peer p : source.current_peers)
        {
            writer.write(p.getIP() + ":" + p.getPort() + "\n");
        }

        // sending the number of sources that sent a list of peers
        writer.write("1\n");
        // sending the registry ip and port. Format: <ip><colon><port><newline>
        writer.write(source.getRegistryIP() + ":" + source.getRegistryPort() + "\n");
        // sending the time at which list of peers was received from the registry.
        writer.write(timestamp + "\n");
        // sending the number of peers received from the registry
        writer.write(registry_peers.size() + "\n");
        // sending the ip and port number of the peers received from the registry. Format: <ip><colon><port><newline>
        for(Peer p : registry_peers)
        {
            writer.write(p.getIP() + ":" + p.getPort() + "\n");
        }

        // sending the number of peers received via udp
        writer.write(source.complete_peers_list.size() - registry_peers.size() + "\n");
        // sending the ip and port number of peers received via udp. 
        // Format: <sourceIP><colon><sourcePort><space><receivedIP><colon><receivedPort><space><time received><newline>
        for(int i = registry_peers.size() ; i < source.complete_peers_list.size(); i++)
        {
            writer.write(source.complete_peers_list.get(i).toString() + "\n");
        }

        // sending the number of peers sent via udp
        writer.write(source.sent_peers.size() + "\n");
        // sending the ip and port number of peers sent via udp. 
        // Format: <destinationIP><colon><destinationPort><space><sentIP><colon><sentPort><space><time received><newline>
        for(SentPeer sp : source.sent_peers)
        {
            writer.write(sp.toString() + "\n");
        }

        // sort messages received based on lamport timestamp
        Collections.sort(source.message_list);
        // sending the number of messages received
        writer.write(source.message_list.size() + "\n");
        // sending message information
        // Format: <lamportTimestamp><space><content><space><sourceIP><colon><sourcePort><newline>
        for(Snip snip : source.message_list)
        {
            writer.write(snip.toString() + "\n");
        }

        writer.flush();
    }

     /**
	 * Send the name of the team for this peer to the registry.
     * Communication protocol:
	 * recieve: 'get team name\n'
	 * send: <name of team><new line>
     * @throws Exception if there is any problems communicating with the registry or retrieving local ip
	 */
    private void sendLocation() throws Exception {
        writer.write(LocalHost.getLocalHostExternalAddress() + ":" + source.socket.getLocalPort() + "\n");
        writer.flush();
    }
}
