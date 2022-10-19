package ahmedalijamaal.peertopeermessagingsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnippetManager implements Runnable {

    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
     * 
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

        System.out.println(
                source.getTeamName() + " - Instruction: Type messages on Command Line you want to send to peers.");

        while (!source.getShutdown()) {

            try {

                Thread.sleep(500);

                if (!scanner.ready())
                    continue;

                msg = scanner.readLine();

                sendSnip(msg);

            } catch (UnknownHostException ue) {
                System.out.println("Can't convert string IP to InetAddress.");
            } catch (Exception e) {
                logr.log(Level.WARNING, e.getMessage(), e);
            }
        }

        try {
            scanner.close();
        } catch (IOException e) {
            logr.log(Level.WARNING, e.getMessage(), e);
        }
        System.out.println("Closing Snippet Manager.");
    }

    protected void sendSnip(String msg) throws Exception {
        DatagramPacket packet;

        msg = "snip" + source.logical_clock.incrementAndGet() + " " + msg;
        System.out.println("Sending to peers: " + msg);
        buffer = msg.getBytes();

        for (Peer p : source.current_peers) {

            if (source.isThePeerMe(p))
                continue;

            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(p.getIP()), p.getPort());
            source.socketSend(packet);
        }
    }
}
