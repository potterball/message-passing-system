/**
 * PeerSender Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class PeerSender implements Runnable{
    
    /**
	 * The peer process that is sending peers.
	 */
    private App source;

    /**
	 * Buffer that stores byte representation of peer string.
	 */
    private byte[] buffer;

    /**
	 * Constructor that links the peer sender to the peer process.
     * 
	 * @param source peer process
	 */
    public PeerSender(App aSource) {
        source = aSource;
    }

    @Override
    /**
	 * Handles the sending of peers to the current active peers that
     * this peer is aware of.
	 */
    public void run() {

        int index;
        boolean flag;

        while(!source.shutdown.get()) {
            try {

                Thread.sleep(5000);

                if (source.current_peers.isEmpty())
                    continue;

                index = new Random().nextInt(source.current_peers.size());

                for(Peer p : source.current_peers) {

                    String receiving_ip = p.getIP();
                    int receiving_port = p.getPort();

                    flag = true;

                    if((receiving_ip.equals(LocalHost.getLocalHostExternalAddress()) || p.getIP().equals(InetAddress.getLocalHost().getHostAddress())) && receiving_port == source.socket.getLocalPort())
                        continue;


                    for(int i = source.complete_peers_list.size() - 1; i >= source.getNoOfRegistryPeers() ; i--) {
                        String[] peer_info = source.complete_peers_list.get(i).getSourceAddress().split(":");
                        if(receiving_ip.equals(peer_info[0]) && receiving_port == Integer.parseInt(peer_info[1])) {
                            Date current_time = new Date();
                            Date last = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source.complete_peers_list.get(i).getTimestamp());
                            long seconds = (current_time.getTime() - last.getTime())/1000;
                            
                            if(seconds > 10) {
                                flag = false;
                            }

                            break;
                        }
                    }

                    if(flag) {

                        Iterator<Peer> iterator = source.current_peers.iterator();

                        int j = 0;
                        Peer pe = null;

                        while(iterator.hasNext()) {

                            pe = iterator.next();

                            if(j == index)
                                break;
                            
                            j++;
                        }

                        String ip_being_sent = pe.getIP();
                        int port_being_sent = pe.getPort();
                        String peer_msg = "peer" + ip_being_sent + ":" + port_being_sent;

                        buffer = peer_msg.getBytes();

                        DatagramPacket packet = new DatagramPacket(buffer,
                                                                    buffer.length, 
                                                                    InetAddress.getByName(receiving_ip), 
                                                                    receiving_port);

                        source.socket.send(packet);

                        source.sent_peers.add(new SentPeer(ip_being_sent,
                                                            port_being_sent, 
                                                            receiving_ip + ":" + receiving_port,
                                                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    }
                }
            }
            catch(UnknownHostException ue) { 
                System.out.println("Can't convert string IP to InetAddress.");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Closing Peer Receiver.");
    }


}
