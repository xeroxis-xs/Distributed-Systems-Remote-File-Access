package client;

import utils.InputGetter;
import utils.ConsoleUI;

public class Client {

    private int clientPort;
    private String serverAddress;
    private int serverPort;

    private Handler handler;
    private InputGetter ig = new InputGetter();
    private boolean isMonitoring = false;

    public Client(int clientPort, String serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES) {
        this.clientPort = clientPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        handler = new Handler(BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB, MAX_RETRIES);
    }

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

    private void startServices() {
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


    

    private void startRead(String requestType) {

        System.out.println("\nEnter the pathname of the target file to be read: ");
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

        System.out.println("\nEnter the pathname of the target file to insert into: ");
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
        System.out.println("\nEnter the pathname of the target file to be monitored: ");
        System.out.println("E.g. server/storage/hello.txt");
        String pathname = ig.getString();

        System.out.println("\nEnter the duration (in minutes) you would like to receive updates for: ");
        System.out.println("E.g. 1");
        long monitorMinutes = ig.getLong();

        System.out.println("You have selected to monitor " + pathname + " for " + monitorMinutes + " minute(s)");
        String requestContent = requestType + ":" + pathname + ":" + monitorMinutes;

        // Send request and receive reply from server
        String replyFromServer = handler.sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);

        while(isMonitoring) {
            // Recevives monitoring updates from server
            String monitorFromServer = handler.monitorOverUDP();
            // Process monitoring reply from server
            processReplyFromServer(monitorFromServer);
        }
        
    }

    //Deleting Files
    private void startIdempotent(String requestType) {
        System.out.println("\nEnter the pathname of the target file to delete: ");
        System.out.println("E.g. server/storage/hello.txt");
        String pathname = ig.getString();

        System.out.println("You have selected to delete '"+ pathname + ".");
        String requestContent = requestType + ":" + pathname;

        // Send request and receive reply from server
        String replyFromServer = handler.sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }

    private void startNonIdempotent(String requestType) {
        System.out.println("\nEnter the pathname of the target file to be read: ");
        System.out.println("E.g. server/storage/hello.txt");
        String srcPath = ig.getString();

        System.out.println("\nEnter the offset of the file content (in bytes) to read from: ");
        System.out.println("E.g. 0");
        long offset = ig.getLong();

        System.out.println("\nEnter the number of bytes to read: ");
        System.out.println("E.g. 2");
        int bytesToRead = ig.getInt();

        System.out.println("\nEnter the pathname of the target file to be appended to: ");
        System.out.println("E.g. server/storage/hello.txt");
        String targetPath = ig.getString();

        System.out.println("You have selected to read " + bytesToRead + " bytes from " + srcPath + " starting from byte " + offset + ", and appending it to the back of " + targetPath + ".");
        String requestContent = requestType + ":" + srcPath + ":" + offset + ":" + bytesToRead + ":" + targetPath;
        
        // Send request and receive reply from server
        String replyFromServer = handler.sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }

    private void processReplyFromServer(String message) {
        // if (message != null) {
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

        ConsoleUI.displaySeparator('=', 41);
        switch (replyType) {
            case "1":
                System.out.println("Read request successful: " + replyContents);
                break;
            case "1e1":
                System.out.println("Read request failed: " + replyContents);
                break;
            case "1e2":
                System.out.println("Read request failed: " + replyContents);
                break;
            case "1e3":
                System.out.println("Read request failed: " + replyContents);
                break;
            case "1e4":
                System.out.println("Read request failed: " + replyContents);
                break;
            case "2":
                System.out.println("Insert request successful: " + replyContents);
                break;
            case "2e1":
                System.out.println("Insert request failed: " + replyContents);
                break;
            case "2e2":
                System.out.println("Insert request failed: " + replyContents);
                break;
            case "2e3":
                System.out.println("Insert request failed: " + replyContents);
                break;
            case "2e4":
                System.out.println("Insert request failed: " + replyContents);
                break;
            case "3":
                System.out.println("Monitor request successful: " + replyContents);
                isMonitoring = true;
                break;
            case "3e1":
                System.out.println("Monitor update: " + replyContents);
                break;
            case "3e2":
                System.out.println("Monitor request ended: " + replyContents);
                isMonitoring = false;
                break;
            case "3e3":
                System.out.println("Monitor request failed: " + replyContents);
                break;
            case "4":
                System.out.println("Delete request successful: " + replyContents);
                break;
            case "4e1":
                System.out.println("Delete request failed: " + replyContents);
                break;
            case "4e2":
                System.out.println("Delete request failed: " + replyContents);
                break;
            case "5":
                System.out.println("File appended successful: " + replyContents);
                break;
            case "5e1":
                System.out.println("Read source file request failed: " + replyContents);
                break;
            case "5e2":
                System.out.println("Read source file request failed: " + replyContents);    
                break;
            case "5e3":
                System.out.println("Read source file request failed: " + replyContents);
                break;
            case "5e4":
                System.out.println("Read source file request failed: " + replyContents);
                break;
            case "5e5":
                System.out.println("Append to target file request failed: " + replyContents);
                break;
            case "5e6":
                System.out.println("Append to target file request failed: " + replyContents);
                break;
            default:
                System.out.println("Other server reply: " + replyContents);
                break;
        }
        ConsoleUI.displaySeparator('=', 41);
        // }
        
    }

    private String concatenateFromIndex(String[] elements, int startIndex, String delimiter) {
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

