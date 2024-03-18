package client;


public class Main {

    public static void main(String[] args) {

        String serverAddress = "127.0.0.1";
        int serverPort = 12345;
        int clientPort = 65535;
		double PACKET_SEND_LOSS_PROB = 0.8;
		double PACKET_RECV_LOSS_PROB = 0.8;
		int BUFFER_SIZE = 1024;
		int MAX_RETRIES = 10;

		if (args.length > 0) {
			try {
				if (args.length == 1) {
					serverAddress = args[0];
				}
				else if (args.length == 2) {
					serverAddress = args[0];
					serverPort = Integer.parseInt(args[1]);
				}
				else if (args.length == 3) {
					serverAddress = args[0];
					serverPort = Integer.parseInt(args[1]);
                    clientPort = Integer.parseInt(args[2]);
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

        System.out.println("\nInitialising: Server address set to " + serverAddress);
        System.out.println("Initialising: Server port set to " + serverPort);


        Client client = new Client(clientPort, serverAddress, serverPort, BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB, MAX_RETRIES);
        client.startConnection();

    }

}
