package ahmedalijamaal.peertopeermessagingsystem;

import java.net.DatagramPacket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver implements Runnable {

    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * The peer process that is receiving messages from other peers.
     */
    private App source;

    private static final String PEER = "peer";
    private static final String SNIP = "snip";
    private static final String STOP = "stop";

    /**
     * Constructor that links the message receiver to the peer process.
     * 
     * @param source peer process
     */
    public MessageReceiver(App aSource) {
        source = aSource;
    }

    @Override
    /**
     * Handles the accepting of messages received from other peers
     */
    public void run() {

        while (!source.getShutdown()) {

            readPacket();
        }

        System.out.println("Closing Message Receiver.");
    }

    protected void readPacket() {
        try {
            DatagramPacket packet = source.socketReceive();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
            try {
                source.createNewThread(
                        delegateToThread(source,
                                new ProcessAddress(packet.getAddress().toString().replaceAll("/", ""),
                                        packet.getPort()),
                                message, timestamp));
            } catch (IllegalArgumentException ioe) {
                System.out.println("Invalid message received from " + packet.getAddress() + ":" + packet.getPort());
            }

        } catch (Exception e) {
            logr.log(Level.WARNING, e.getMessage(), e);
        }
    }

    Runnable delegateToThread(App aSource, ProcessAddress sourceOfMessage, String message, String timestamp)
            throws IllegalArgumentException {
        if (message.equals(STOP)) {
            source.compareAndSetShutdown(false, true);
            return new StopHandler(aSource, sourceOfMessage);
        }
        switch (message.substring(0, 4)) {
            case PEER:
                return new PeerHandler(aSource, sourceOfMessage, message.substring(4), timestamp);
            case SNIP:
                return new SnipHandler(aSource, sourceOfMessage, message.substring(4), timestamp);
            default:
                throw new IllegalArgumentException("Incorrect Type Code value.");
        }
    }

}