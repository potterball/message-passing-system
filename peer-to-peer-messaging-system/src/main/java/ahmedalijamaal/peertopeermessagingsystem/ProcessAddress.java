package ahmedalijamaal.peertopeermessagingsystem;

public class ProcessAddress {

  private String ipAddress;

  private int port;

  private final static String IPV4_PATTERN = "^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
      "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
      "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
      "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$";

  public ProcessAddress(String ip, int portNum) {
    this.ipAddress = ip;
    this.port = portNum;
  }

  public String getIP() {
    return ipAddress;
  }

  public int getPort() {
    return port;
  }

  public String toString() {
    return ipAddress + ":" + port;
  }

  public static boolean validatePort(String portNum) {
    int port;
    try {
      port = Integer.parseInt(portNum);
    } catch (NumberFormatException ne) {
      return false;
    }

    if (port < 1 || port > 65535) {
      return false;
    }

    return true;
  }

  public static boolean validateIP(String ip) {
    if (!ip.matches(IPV4_PATTERN)) {
      return false;
    }

    return true;
  }

}
