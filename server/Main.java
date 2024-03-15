package server;

public class Main {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        // Server server = new Server(serverPort);

        // try {
        //     while (true) {
        //         serverHandler.receiveOverUDP();
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        boolean atLeastOnce = true;
        double packetLossProb = 0;
        int serverPort = 12345;

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

        Server server = new Server();
        server.listen(serverPort);

    }

}
