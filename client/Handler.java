package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import utils.ConsoleUI;

public class Handler {

    private int BUFFER_SIZE;
    private double PACKET_LOSS_PROB;
    private int MAX_RETRIES;
    private InetSocketAddress serverAddress;
    private String clientAddress;
    private int clientPort;
    private DatagramSocket socket;
    private AtomicInteger requestIdCounter = new AtomicInteger(0);

    public Handler(int BUFFER_SIZE, double PACKET_LOSS_PROB, int MAX_RETRIES) {
        this.clientAddress = this.getClientAddress();
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.PACKET_LOSS_PROB = PACKET_LOSS_PROB;
        this.MAX_RETRIES = MAX_RETRIES;
    }

    public void connectToServer(String serverAddress, int serverPort) throws Exception {
        try {
            // Connect to Server
            this.serverAddress = new InetSocketAddress(serverAddress, serverPort);
            System.out.println("\nSuccessfully connected to " + serverAddress + ":" + serverPort);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openPort(int clientPort) throws Exception {
        try {
            // Open UDP Socket
            this.clientPort = clientPort;
            this.socket = new DatagramSocket(this.clientPort);
            System.out.println("Client port listening at " + clientPort);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientAddress() {
        String clientAddress = null;
        try {
            // Get the address of client
            InetAddress clientHost = InetAddress.getLocalHost();
            clientAddress = (clientHost.getHostAddress()).trim();
        }
        catch (UnknownHostException e) {
            System.out.println("\nFailed to get localhost address: " + e.getMessage());
        }
        return clientAddress;
    }

    public String generateRequestId(String clientAddress, int clientPort) {
        // Generate a unique request Id based on client address and port
        return this.requestIdCounter.incrementAndGet() + ":" + clientAddress + ":" + clientPort;
    }

    public void disconnect() {
        // Close UDP Socket
        this.socket.close();
    }

    public String sendOverUDP(String requestContent) {
        String unmarshalledData = null;
        try {
            // Create the message payload structure
            String messageType = "0";
            String requestId = this.generateRequestId(this.clientAddress, this.clientPort);
            String message = messageType + ":" + requestId + ":" + requestContent;

            // Marshal the data into a byte array
            byte[] marshalledData = utils.Marshaller.marshal(message);

            // Create a DatagramPacket for sending data
            DatagramPacket requestPacket = new DatagramPacket(marshalledData, marshalledData.length, serverAddress);

            // Send over UDP
            this.socket.send(requestPacket);

            // receive iover UDP
            unmarshalledData = this.receiveOverUDP(requestPacket);

        }
        catch (IOException e) {
            System.out.println("\nAn IO error occurred: " + e.getMessage());
        }
        return unmarshalledData;
    }


    public String receiveOverUDP(DatagramPacket requestPacket) {

        String unmarshalledData = null;
        int timeout = 5000;

        // Prepare a byte buffer to store received data
        byte[] buffer = new byte[BUFFER_SIZE];

        // Create a DatagramPacket for receiving data
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                // Set timeout for 5 seconds
                this.socket.setSoTimeout(timeout);

                // Receive data from server over UDP
                this.socket.receive(receivePacket);

                // Unmarshal the data into a String
                byte[] marshalledData = receivePacket.getData();
                unmarshalledData = utils.Marshaller.unmarshal(marshalledData);

                ConsoleUI.displaySeparator('=', 30);
                System.out.println("Raw Message from Server: " + unmarshalledData);
                ConsoleUI.displaySeparator('=', 30);

                if (Math.random() < PACKET_LOSS_PROB){
                    System.out.println("\n*** Simulating receiving message loss from server ***");
                    continue;
                }

                break;
            }
            catch (SocketTimeoutException e) {
                retries++;
                System.out.println("\nTimeout occurred while waiting for response from server.");
                System.out.println("Retransmitting request to server. Retry (" + retries + ")\n");

                try {
                    // Resending
                    this.socket.send(requestPacket);
                }
                catch (IOException ioe) {
                    System.out.println("\nError retransmitting. Exiting... ");
                    System.out.println("An IO error occurred: " + ioe.getMessage());
                    System.exit(1);
                }
            }
            catch (IOException e) {
                System.out.println("\nAn IO error occurred: " + e.getMessage());
            }
        }

        return unmarshalledData;

    }

}
