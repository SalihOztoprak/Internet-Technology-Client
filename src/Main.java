public class Main { public Main() {}
  public static void main(String[] args) { System.out.println("Usage:");
    System.out.println("\t--no-logs: don't show the messages from and to the server in the console.");
    System.out.println("\t--no-colors: log debug messages without colors in the console.");
    System.out.println("");

    if (args.length == 0) {
      System.out.println("Starting the chatServer.client with the default configuration.");
    } else {
      System.out.println("Starting the chatServer.client with:");
    }
    
    ClientConfiguration config = new ClientConfiguration();
    config.setShowLogging(false);
    config.setShowColors(true);
    for (String arg : args) {
      if (arg.equals("--no-colors")) {
        config.setShowColors(false);
        System.out.println(" * Colors in debug message disabled");
      } else if (arg.equals("--no-logs")) {
        config.setShowLogging(false);
        System.out.println(" * Logging of debug messages disabled");
      } else if (arg.equals("--host")) {
        config.setServerIp(arg);
        System.out.println(" * Drop message simulation enabled");
      } else if (arg.equals("--port")) {
        if (tryParseInt(arg)) {
          int port = Integer.parseInt(arg);
          if ((port > 1024) && (port < 65535)) {
            config.setServerPort(port);
            System.out.println(" * Port has been configured");
          } else {
            System.out.println(" ERROR: Invalid port number (should be between 1024 and 65535)");
          }
        } else {
          System.out.println(" ERROR: Port is not a valid number.");
        }
      }
    }
    System.out.println("-------------------------------");
    config.getClass();System.out.println("\tversion:\t" + "1.0");
    System.out.println("\thost:\t\t" + config.getServerIp());
    System.out.println("\tport:\t\t" + config.getServerPort());
    System.out.println("-------------------------------");
    new Client(config).start();
  }
  
  static boolean tryParseInt(String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {}
    return false;
  }
}
