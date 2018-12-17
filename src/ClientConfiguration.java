public class ClientConfiguration
{
  public final String VERSION = "1.0";
  public final String DEFAULT_SERVER_IP = "127.0.0.1";
  public final int DEFAULT_SERVER_PORT = 1337;
  

  public final String RESET_CLI_COLORS = "\033[0m";
  public final String CLI_COLOR_INCOMING = "\033[31m";
  public final String CLI_COLOR_OUTGOING = "\033[32m";
  

  private String serverIp;
  
  private int serverPort;
  
  private boolean showColors = true;
  

  private boolean showLogging = true;
  
  public ClientConfiguration()
  {
    serverIp = "127.0.0.1";
    serverPort = 1337;
  }
  
  public boolean isShowColors() {
    return showColors;
  }
  
  public void setShowColors(boolean showColors) {
    this.showColors = showColors;
  }
  
  public boolean isShowLogging() {
    return showLogging;
  }
  
  public void setShowLogging(boolean showLogging) {
    this.showLogging = showLogging;
  }
  
  public String getServerIp()
  {
    return serverIp;
  }
  
  public void setServerIp(String serverIp) {
    this.serverIp = serverIp;
  }
  
  public int getServerPort() {
    return serverPort;
  }
  
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }
}
