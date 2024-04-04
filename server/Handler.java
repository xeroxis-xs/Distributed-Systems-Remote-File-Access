package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import utils.ConsoleUI;

public class Handler {
    private int serverPort;
    private DatagramSocket socket;
    private AtomicInteger requestIdCounter = new AtomicInteger(0);
    private double PACKET_SEND_LOSS_PROB;

    public Handler(double PACKET_SEND_LOSS_PROB) {
        this.PACKET_SEND_LOSS_PROB = PACKET_SEND_LOSS_PROB;
    }

    public void openPort(int serverPort) {
        try {
            // Open UDP Socket
            this.serverPort = serverPort;
            this.socket = new DatagramSocket(this.serverPort);
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Server: Server started at " + (localhost.getHostAddress()).trim());
            System.out.println("Server: Port listening at " + serverPort);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateRequestId(String clientAddress, int clientPort) {
        // Generate a unique request Id based on client address and port
        return this.requestIdCounter.incrementAndGet() + ":" + clientAddress + ":" + clientPort;
    }

    public String sendOverUDP(InetAddress clientAddress, int clientPort, String requestContent) {
        String unmarshalledData = null;
        try {
            // Create the message payload structure
            String messageType = "1";
            String requestId = this.generateRequestId(clientAddress.getHostAddress(), clientPort);
            String message = messageType + ":" + requestId + ":" + requestContent;

            // Marshal the data into a byte array
            byte[] marshalledData = utils.Marshaller.marshal(message);

            // Create a DatagramPacket for sending data
            DatagramPacket sendPacket = new DatagramPacket(marshalledData, marshalledData.length, clientAddress,
                    clientPort);

            if (Math.random() < PACKET_SEND_LOSS_PROB) {
                System.out.println("***** Simulating sending message loss from server *****");
            } else {
                // Send over UDP
                this.socket.send(sendPacket);
            }

        } catch (IOException e) {
            System.out.println("\nAn IO error occurred: " + e.getMessage());
        }
        return unmarshalledData;
    }

    public Object[] receiveOverUDP(DatagramPacket receivePacket) {
        Object[] result = new Object[3];
        InetAddress clientAddress = null;
        int clientPort = 0;
        String unmarshalledData = null;

        try {
            // Set a timeout for the socket (in milliseconds) so it does not block the
            // execution
            this.socket.setSoTimeout(1000); // Timeout set to 1 seconds
            // Receive data from server over UDP
            this.socket.receive(receivePacket);

            // Get the client address and port
            clientAddress = receivePacket.getAddress();
            clientPort = receivePacket.getPort();

            // Unmarshal the data from byte array into a String
            byte[] marshalledData = receivePacket.getData();
            unmarshalledData = utils.Marshaller.unmarshal(marshalledData);

            ConsoleUI.displaySeparator('=', 41);
            System.out.println("Raw Message from Client: " + unmarshalledData);
            ConsoleUI.displaySeparator('=', 41);
        } catch (SocketTimeoutException ste) {

        } catch (IOException e) {
            System.out.println("\nAn IO error occurred: " + e.getMessage());
        }
        result[0] = clientAddress;
        result[1] = clientPort;
        result[2] = unmarshalledData;

        return result;

    }

}
