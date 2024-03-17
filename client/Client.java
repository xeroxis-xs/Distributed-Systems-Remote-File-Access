package client;

import utils.InputGetter;
import utils.ConsoleUI;

public class Client {

    private int clientPort;
    private String serverAddress;
    private int serverPort;

    public Handler handler;
    public InputGetter ig = new InputGetter();
    public boolean isConnected = false;


    public Client(int clientPort, String serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_LOSS_PROB, int MAX_RETRIES) {
        this.clientPort = clientPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        handler = new Handler(BUFFER_SIZE, PACKET_LOSS_PROB, MAX_RETRIES);
    }

    // public void startClient() {
    //     int choice;

    //     do {
    //         System.out.println("+---------------------------------------+");
    //         System.out.println("|               Welcome!                |");
    //         System.out.println("+---------------------------------------+");
    //         System.out.println("| [1] Connect to a server               |");
    //         System.out.println("| [2] Exit                              |");
    //         System.out.println("+---------------------------------------+");
    //         System.out.print("\nEnter your choice: ");

    //         choice = ig.getInt();

    //         switch (choice) {
    //             case 1:
    //                 renderServerIP();
    //                 break;
    //             case 2:
    //                 System.out.println("Exiting...");
    //                 System.exit(0);
    //                 break;
    //             default:
    //                 System.out.println("Invalid choice. Please try again.");
    //                 break;
    //         }
    //     } while (choice != 2);
    // }

    public void startServices() {
        int choice;
        do {
            System.out.println("\n");
            System.out.println("+---------------------------------------+");
            System.out.println("|    Welcome to Remote File Service!    |");
            System.out.println("+---------------------------------------+");
            System.out.println("|                                       |");
            System.out.println("| [1] Read a content from a file        |");
            System.out.println("| [2] Insert a content into a file      |");
            System.out.println("| [3] Monitor updates of a file         |");
            System.out.println("| [4] Idempotent service                |");
            System.out.println("| [5] Non-idempotent service            |");
            System.out.println("| [6] Exit                              |");
            System.out.println("|                                       |");
            System.out.println("+---------------------------------------+");
            System.out.print("\nEnter your choice: ");
            choice = ig.getInt();

            switch (choice) {
                case 1:
                    startRead("1");
                    break;
                case 2:
                    startInsert("2");
                    break;
                case 3:
                    startMonitor("3");
                    break;
                case 4:
                    startIdempotent("4");
                    break;
                case 5:
                    startNonIdempotent("5");
                    break;
                case 6:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (choice != 6);
    }

    // private void renderServerIP() {

    //     System.out.print("\nEnter the IP address of the server: ");
    //     this.serverIP = ig.getString();

    //     System.out.println("You have selected to connect to " + this.serverIP);
    //     this.startConnection();

    // }

    // public String getServerIP() {
    //     return this.serverIP;
    // }


    public void startConnection() {
        // Open UDP Port
        // Connect to Server
        try {

            handler.connectToServer(serverAddress, serverPort);
            handler.openPort(clientPort);

            startServices();
        }
        catch (Exception e) {
            // Exit the program
            System.out.println("\nConnection to " + serverAddress + " failed. Please try again.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void startRead(String requestType) {

        System.out.println("\nEnter the pathname of the file: ");
        System.out.println("E.g. server/storage/hello.txt");
        String pathname = ig.getString();

        System.out.println("\nEnter the offset of the file content (in bytes) to read from: ");
        System.out.println("E.g. 0");
        long offset = ig.getLong();

        System.out.println("\nEnter the number of bytes to read: ");
        System.out.println("E.g. 2");
        int bytesToRead = ig.getInt();

        System.out.println("You have selected to read " + bytesToRead + " bytes from " + pathname + " starting from byte " + offset + ".");
        String requestContent = requestType + ":" + pathname + ":" + offset + ":" + bytesToRead;

        // Send request and receive reply from server
        String replyFromServer = handler.sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }

    private void startInsert(String requestType) {

        System.out.println("\nEnter the pathname of the file: ");
        System.out.println("E.g. server/storage/hello.txt");
        String pathname = ig.getString();

        System.out.println("\nEnter the offset of the file content (in bytes) to insert from: ");
        System.out.println("E.g. 0");
        long offset = ig.getLong();

        System.out.println("\nEnter the content to be inserted into the file: ");
        System.out.println("E.g. abc");
        String stringToInsert = ig.getString();

        System.out.println("You have selected to insert '" + stringToInsert + "' into " + pathname + " starting from byte " + offset + ".");
        String requestContent = requestType + ":" + pathname + ":" + offset + ":" + stringToInsert;

        // Send request and receive reply from server
        String replyFromServer = handler.sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }

    private void startMonitor(String requestType) {

    }

    private void startIdempotent(String requestType) {

    }

    private void startNonIdempotent(String requestType) {

    }

    private void processReplyFromServer(String message) {
        String[] messageParts = message.split(":");
        String messageType = messageParts[0]; // 0 is request; 1 is reply
        String replyCounter = messageParts[1];
        String serverAddress = messageParts[2];
        String serverPort = messageParts[3];
        String replyType = messageParts[4];
        String replyContents = concatenateFromIndex(messageParts, 5, ":");

        // System.out.print("\nmessageType: " + messageType);
        // System.out.print("\nreplyCounter: " + replyCounter);
        // System.out.print("\nserverAddress: " + serverAddress);
        // System.out.print("\nserverPort: " + serverPort);
        // System.out.print("\nreplyType: " + replyType);
        // System.out.print("\nreplyContents: " + replyContents);

        ConsoleUI.displaySeparator('=', 40);
        switch (replyType) {
            case "1":
                System.out.println("Request successful: " + replyContents);
                break;
            case "1e1":
                System.out.println("Request failed: " + replyContents);
                break;
            case "1e2":
                System.out.println("Request failed: " + replyContents);
                break;
            case "1e3":
                System.out.println("Request failed: " + replyContents);
                break;
            case "1e4":
                System.out.println("Request failed: " + replyContents);
                break;
            case "2":
                System.out.println("Request successful: " + replyContents);
                break;
            case "2e1":
                System.out.println("Request failed: " + replyContents);
                break;
            case "2e2":
                System.out.println("Request failed: " + replyContents);
                break;
            case "2e3":
                System.out.println("Request failed: " + replyContents);
                break;
            case "2e4":
                System.out.println("Request failed: " + replyContents);
                break;
            
            default:
                System.out.println("Request failed: " + replyContents);
                break;
        }
        ConsoleUI.displaySeparator('=', 40);
    }

    public String concatenateFromIndex(String[] elements, int startIndex, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through the elements starting from the startIndex
        for (int i = startIndex; i < elements.length; i++) {
            // Append the current element
            stringBuilder.append(elements[i]);

            // Append delimiter if not the last element
            if (i < elements.length - 1) {
                stringBuilder.append(delimiter);
            }
        }

        // Convert StringBuilder to String and return
        return stringBuilder.toString();
    }

}

