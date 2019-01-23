/**
 * This class defines all the configuration of the client such as server_ip and server_port
 */
public class ClientConfiguration {
    public final String VERSION = "1.0";
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 1337;

    public final String RESET_CLI_COLORS = "\033[0m";
    public final String CLI_COLOR_INCOMING = "\033[31m";
    public final String CLI_COLOR_OUTGOING = "\033[32m";

    private String serverIp;
    private int serverPort;
    private boolean showColors = true;
    private boolean showLogging = true;

    /**
     * This is the default constructor for creating the configuration
     */
    public ClientConfiguration() {
        serverIp = DEFAULT_SERVER_IP;
        serverPort = DEFAULT_SERVER_PORT;
    }

    /**
     * This method returns the status of color showing
     *
     * @return true if you selected to show the colors
     */
    public boolean isShowColors() {
        return showColors;
    }

    /**
     * This method sets the status of color showing
     *
     * @param showColors true if you want to show the colors
     */
    public void setShowColors(boolean showColors) {
        this.showColors = showColors;
    }

    /**
     * This method returns the status of logging
     *
     * @return true if you selected to show logging
     */
    public boolean isShowLogging() {
        return showLogging;
    }

    /**
     * This method sets the status of showing logs
     *
     * @param showLogging true if you want to show the logs
     */
    public void setShowLogging(boolean showLogging) {
        this.showLogging = showLogging;
    }

    /**
     * This method returns the server ip
     *
     * @return The server ip
     */
    public String getServerIp() {
        return serverIp;
    }

    /**
     * This method sets the server ip
     *
     * @param serverIp The new server ip
     */
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    /**
     * This method returns the server port
     *
     * @return The server port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * This method sets the server port
     *
     * @param serverPort The new server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
