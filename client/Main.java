package client;


public class Main {

    public static void main(String[] args) {

        int clientPort = 65535;
        String serverAddress = "127.0.0.1";
        int serverPort = 12345;

		if (args.length > 0) {
			try {
				if (args.length == 1) {
					clientPort = Integer.parseInt(args[0]);
				}
				if (args.length == 2) {
					clientPort = Integer.parseInt(args[0]);
					serverAddress = args[1];
				}
				if (args.length == 3) {
					clientPort = Integer.parseInt(args[0]);
					serverAddress = args[1];
					serverPort = Integer.parseInt(args[2]);

				}
			} catch (Exception e) {
				System.err.println("Invalid arguments. Please try again.");
				System.exit(1);
			}
		}

        System.out.println("\nInitialising: Server address set to " + serverAddress);
        System.out.println("Initialising: Server port set to " + serverPort);


        Client client = new Client(clientPort, serverAddress, serverPort);
        client.startConnection();

    }

}
