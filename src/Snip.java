/**
 * Snip Class
 *
 * CPSC 559
 * Project Iteration 2
 * @author Ahmed Ali Jamaal Najjar
 *
 */

 public class Snip implements Comparable<Snip>{

    /**
	 * Logical timestamp of the received message.
	 */
    private int lamport_timestamp;

    /**
	 * Content of the message.
	 */
    private String content;

    /**
	 * Time at which the message was received.
	 */
    private String timestamp;

    /**
	 * Source address from whom the message was received.
	 */
    private String source;

    /**
	 * Constructor that sets up information of the received snip.
     * 
	 * @param lamport_timestamp the received snip's logical timestamp.
	 * @param content the received snip's content.
     * @param timestamp the time at which peer was received.
     * @param source the received peer's source address.
	 */
    public Snip(int lamport_timestamp, String content, String timestamp, String source) {
        this.lamport_timestamp = lamport_timestamp;
        this.content = content;
        this.timestamp = timestamp;
        this.source = source;
    }

    /**
	 * Gets the source address of the received message.
	 * @return The message source.
	 */
    public String getSource() {
        return source;
    }

    /**
	 * Gets the content of the received message.
	 * @return The content.
	 */
    public String getContent() {
        return content;
    }

    /**
	 * Gets the time message when the message was received.
	 * @return The time when received.
	 */
    public String getTimestamp() {
        return timestamp;
    }

    /**
	 * String representation of the received message's information.
     * @return Message information.
	 */
    public String toString() {
        return lamport_timestamp + " " + this.content + " " + this.source;
    }

    @Override
    /**
     * Compares two snips based on logical timestamp.
     * @param o the object that is being compared to the snip.
     * @return 0 if the logical timestamps are equal, 1 if snip being compared to has smaller timestamp else -1
     */
    public int compareTo(Snip o) {
        if(lamport_timestamp == o.lamport_timestamp)
            return content.compareTo(o.getContent());
        else if(lamport_timestamp > o.lamport_timestamp)
            return 1;
        else
            return -1;
    }

}
