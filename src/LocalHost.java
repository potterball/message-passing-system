/**
 * LocalHost Class
 *
 * CPSC 559
 * Project Iteration 2
 * Reference: https://stackoverflow.com/a/14541376
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class LocalHost {

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
                    e.printStackTrace();
                }
            }
        }
    }
}
