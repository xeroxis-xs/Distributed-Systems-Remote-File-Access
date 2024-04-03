package server;

public class Main {

    public static void main(String[] args) {

        int serverPort = 12345;
        int BUFFER_SIZE = 1024;
        int HISTORY_SIZE = 100;
        int MONITOR_SIZE = 100;
        boolean AT_MOST_ONCE = true;

		if (args.length > 0) {
			try {
				if (args.length == 1) {
					serverPort = Integer.parseInt(args[0]);
				}
                else {
                    System.err.println("Invalid arguments. Please try again.");
				    System.exit(1);
                }
			} catch (Exception e) {
				System.err.println("Invalid arguments. Please try again.");
				System.exit(1);
			}
		}
        // Server with at-most-once semantics
        if (AT_MOST_ONCE) {
            System.out.println("\nServer: Invocation Semantics: At Most Once");
            System.out.println("Server: History initialised with " + HISTORY_SIZE + " records capacity");
        }
        // Server with at-least-once semantics
        else {
            System.out.println("\nServer: Invocation Semantics: At Least Once");
            System.out.println("Server: No history is maintained");
        }

        // Start the server and listen to port
        Server server = new Server(BUFFER_SIZE, HISTORY_SIZE, MONITOR_SIZE, AT_MOST_ONCE);
        server.listen(serverPort);

    }

}
