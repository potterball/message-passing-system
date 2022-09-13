/**
 * MessageReceiver Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageReceiver implements Runnable{

    /**
	 * The peer process that is receiving messages from other peers.
	 */
    private App source;

    /**
	 * Buffer that stores byte representation of message received.
	 */
    private byte[] buffer;

    /**
	 * Constructor that links the message receiver to the peer process.
     * 
	 * @param source peer process
	 */
    public MessageReceiver(App aSource) {
        source = aSource;
        buffer = new byte[1024];
    }

    private class PeerHandlingThread implements Runnable {

        /**
	     * Source address from whom the message was received.
	     */
        String src;

        /**
	     * Content of the message.
	     */
        String msg;

        /**
	     * Time at which the message was received.
	     */
        String timestamp;

        public PeerHandlingThread(String src, String message, String timestamp) {
            this.src = src.replaceAll("/", "");
            msg = message;
            this.timestamp = timestamp;
        }

        @Override
        /**
	     * Handles received peers.
	     */
        public void run() {

            String[] peer_info = msg.split(":");
            int port;

            try {
                port = Integer.parseInt(peer_info[1]);
            }
            catch(NumberFormatException ne) {
                System.out.println("Invalid peer received from " + src);
                return;
            }

            if(port < 1 || port > 65535) {
                System.out.println("Invalid peer received from " + src);
                return;
            }

            if(!peer_info[0].matches(source.getIPV4_Pattern())) {
                System.out.println("Invalid peer received from " + src);
                return;
            }

            Peer new_peer = new Peer(peer_info[0], port, src, timestamp);

            source.complete_peers_list.add(new_peer);

            if(!source.current_peers.add(new_peer))
            {
                source.current_peers.remove(new_peer);
                source.current_peers.add(new_peer);
            }

        }
    }

    private class SnipHandlingThread implements Runnable {

        /**
	     * Source address from whom the peer was received.
	     */
        String src;

        /**
	     * Content of the message.
	     */
        String msg;
        
        /**
	     * Time at which the message was received.
	     */
        String timestamp;

        public SnipHandlingThread(String src, String message, String timestamp) {
            this.src = src.replaceAll("/", "");
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

            if(Double.isNaN(Integer.parseInt(snip_info[0]))) {
                System.out.println("Invalid message received from " + src);
                return;
            }
            
            try {
                msg_lamport_timestamp = Integer.parseInt(snip_info[0]);
            }
            catch(NumberFormatException ne) {
                System.out.println("Invalid message received from " + src);
                return;
            }

            if(msg_lamport_timestamp < 0) {
                System.out.println("Invalid message received from " + src);
                return;
            }

            int current_timestamp = source.logical_clock.get();

            if(current_timestamp < msg_lamport_timestamp)
                source.logical_clock.compareAndSet(current_timestamp, msg_lamport_timestamp);
            
            Snip new_snip = new Snip(source.logical_clock.incrementAndGet(), snip_info[1], timestamp, src);

            source.message_list.add(new_snip);

            System.out.println("Message Received: " + msg + " from " + src);
        }
    }

    /**
     * Handles sending ack to first stop message received.
     * @param ip registry ip address
     * @param port registry udp port number 
     * @throws IOException
     */
    public void stopHandling(String ip, int port) throws IOException{

            String msg = "ack"+source.getTeamName();
            buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
            source.socket.send(packet);
            System.out.println("Sent ack for stop message.");
    }

    @Override
    /**
	 * Handles the accepting of messages received from other peers
	 */
    public void run() {

        while(!source.shutdown.get()) {

            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                source.socket.receive(packet);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String message = new String(buffer, 0, packet.getLength());

                switch(message.substring(0, 4)) {
                    case "peer":
                                source.pool.execute(new PeerHandlingThread(packet.getAddress() + ":" + packet.getPort(),
                                                                           message.substring(4),
                                                                           timestamp));
                                break;
                    case "snip":
                                source.pool.execute(new SnipHandlingThread(packet.getAddress() + ":" + packet.getPort(),
                                                                           message.substring(4),
                                                                           timestamp));
                                break;
                    case "stop":
                                stopHandling(packet.getAddress().toString().replaceAll("/", ""), packet.getPort());
                                source.shutdown.compareAndSet(false, true);
                                break;
                    default: System.out.println("Invalid message received from " + packet.getAddress() + ":" + packet.getPort());
                }
            }
            catch (Exception e) { 
                e.printStackTrace();
            }
        }

        System.out.println("Closing Message Receiver.");
    }
    
}
