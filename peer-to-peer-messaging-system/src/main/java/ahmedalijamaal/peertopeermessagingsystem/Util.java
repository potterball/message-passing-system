package ahmedalijamaal.peertopeermessagingsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    private final static Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Gets the public IP address of the local machine
     * 
     * @throws Exception throws error if there is trouble connecting to website
     */
    public static String getLocalHostExternalAddress() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logr.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }
}
