/**
 * SnippetManager Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SnippetManager implements Runnable{
    
    /**
	 * The peer process that is accepting messages from the user.
	 */
    private App source;

    /**
	 * Buffer that stores byte representation of message being sent.
	 */
    private byte[] buffer;

    /**
	 * Buffered reader that reads user input.
	 */
    private BufferedReader scanner;

    /**
	 * Constructor that links the snippet manager to the peer process.
	 * @param source peer process
	 */
    public SnippetManager(App aSource) {
        source = aSource;
        buffer = new byte[1024];
    }

    /**
	 * Handles the accepting of messages typed into the terminal and the
     * sending of messages to the current active peers that this peer is
     * aware of.
	 */
    public void run() {

        String msg;
        scanner = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(source.getTeamName() + " - Instruction: Type messages on Command Line you want to send to peers.");
        
        while(!source.shutdown.get()) {

            try {

                Thread.sleep(500);

                if(!scanner.ready())
                    continue;

                msg = scanner.readLine();

                DatagramPacket packet;

                msg = "snip" + source.logical_clock.incrementAndGet() + " " + msg;
                System.out.println("Sending to peers: " + msg);
                buffer = msg.getBytes();

                for(Peer p : source.current_peers) {

                    if((p.getIP().equals(LocalHost.getLocalHostExternalAddress()) || p.getIP().equals(InetAddress.getLocalHost().getHostAddress())) && p.getPort() == source.socket.getLocalPort())
                        continue;
                    
                    packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(p.getIP()), p.getPort());
                    source.socket.send(packet);
                }            
            }
            catch(UnknownHostException ue) { 
                System.out.println("Can't convert string IP to InetAddress.");
            }
            catch (Exception e){ 
                e.printStackTrace();
            }
        }

        try {
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Closing Snippet Manager.");
    }
}
