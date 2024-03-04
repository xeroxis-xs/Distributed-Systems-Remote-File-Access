package client;

import java.util.Scanner;

public class ClientUI {

    private String serverIP;
    public ClientHandler clientHandler = new ClientHandler();
    public boolean isRunning = false;
    public boolean isConnected = false;


    public boolean startClient() {
        Scanner scanner = new Scanner(System.in);
        this.isRunning = true;

        while (isRunning) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Connect to a server");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    renderServerIP();
                    return true;
                case 2:
                    System.out.println("Exiting...");
                    this.isRunning = false;
                    System.exit(0);
                    return false;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
        return true;
    }

    public void startServices() {
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\nSelect a service:");
            System.out.println("1. Read a content from a file");
            System.out.println("2. Insert a content into a file");
            System.out.println("3. Monitor updates of a file");
            System.out.println("4. Idempotent service");
            System.out.println("5. Non-idempotent service");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    startRead(1);
                    break;
                case 2:
                    startInsert(2);
                    break;
                case 3:
                    startMonitor(3);
                    break;
                case 4:
                    startIdempotent(4);
                    break;
                case 5:
                    startNonIdempotent(5);
                    break;
                case 6:
                    System.out.println("Exiting...");
                    isRunning = false;
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            scanner.close();
        }
    }

    private void renderServerIP() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the IP address of the server: ");
        this.serverIP = scanner.nextLine();

        System.out.println("You have selected to connect to " + this.serverIP);
        this.startConnection();
        scanner.close();
    }

    public String getServerIP() {
        return this.serverIP;
    }


    public void startConnection() {
        try {
            this.isConnected = this.clientHandler.connectToServer(this.serverIP);
            System.out.println("\nYou have successfully connected to " + this.serverIP + " !");
            this.startServices();
        }
        catch (Exception e) {
            // Return to main page
            System.out.println("\nConnection to " + this.serverIP + " failed. Please try again.");
            this.startClient();
        }
    }

    private void startRead(int messageHeader) {
        Scanner scanner = new Scanner(System.in);
        String message;

        System.out.print("\nEnter the pathname of the file: ");
        String pathname = scanner.nextLine();

        System.out.print("Enter the offset of the file content (in bytes) to read from: ");
        long offset = scanner.nextLong();

        System.out.print("Enter the number of bytes to read: ");
        int bytesToRead = scanner.nextInt();

        System.out.println("You have selected to read " + bytesToRead + " bytes from " + pathname + " starting from byte " + offset + ".");
        message = messageHeader + ":" + pathname + ":" + offset + ":" + bytesToRead;

        this.clientHandler.sendOverUDP(message);
        scanner.close();

    }

    private void startInsert(int messageHeader) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter the pathname of the file: ");
        String pathname = scanner.nextLine();

        System.out.print("Enter the offset of the file content (in bytes) to insert into: ");
        int offset = scanner.nextInt();

        System.out.print("Enter the number of bytes to insert: ");
        int bytes = scanner.nextInt();

        System.out.println("You have selected to insert " + bytes + " bytes into " + pathname + " starting from byte " + offset + ".");
        scanner.close();
    }

    private void startMonitor(int messageHeader) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter first number: ");
        double num1 = scanner.nextDouble();

        System.out.print("Enter second number: ");
        double num2 = scanner.nextDouble();

        double result = num1 * num2;
        System.out.println("Result of multiplication: " + result);
        scanner.close();
    }

    private void startIdempotent(int messageHeader) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter numerator: ");
        double numerator = scanner.nextDouble();

        System.out.print("Enter denominator: ");
        double denominator = scanner.nextDouble();

        if (denominator != 0) {
            double result = numerator / denominator;
            System.out.println("Result of division: " + result);
        } else {
            System.out.println("Cannot divide by zero.");
        }
        scanner.close();
    }

    private void startNonIdempotent(int messageHeader) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter numerator: ");
        double numerator = scanner.nextDouble();

        System.out.print("Enter denominator: ");
        double denominator = scanner.nextDouble();

        if (denominator != 0) {
            double result = numerator / denominator;
            System.out.println("Result of division: " + result);
        } else {
            System.out.println("Cannot divide by zero.");
        }
        scanner.close();
    }
}

