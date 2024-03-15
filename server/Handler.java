package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;

import utils.ConsoleUI;

public class Handler {
    private static final int BUFFER_SIZE = 1024;
    private int serverPort;
    private DatagramSocket socket;

    public Handler() {

    }

    public void openPort(int serverPort) {
        try {
            // Open UDP Socket
            this.serverPort = serverPort;
            this.socket = new DatagramSocket(this.serverPort);
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("\nServer: Server started at " + (localhost.getHostAddress()).trim());
            System.out.println("Server: Port listening at " + serverPort);

            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
            this.received = false;

            // Prepare a byte buffer to store received data
            byte[] buffer = new byte[BUFFER_SIZE];

            // Create a DatagramPacket for receiving data
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // Set timeout for 5 seconds
            this.socket.setSoTimeout(5000); 

            while(!received) {
                // Receive datagram packet over UDP
                unmarshalledData = this.receiveOverUDP(receivePacket, requestPacket);
            }
        }
        catch (IOException e) {
            System.out.println("\nAn IO error occurred: " + e.getMessage());
        }
        return unmarshalledData;
    }

    

    public String receiveOverUDP(DatagramPacket receivePacket) {
        String unmarshalledData = null;
        try {
            // Receive data from server over UDP
            this.socket.receive(receivePacket);

            // Unmarshal the data into a String
            byte[] marshalledData = receivePacket.getData();
            unmarshalledData = utils.Marshaller.unmarshal(marshalledData);

            ConsoleUI.displaySeparator('=', 30);
            System.out.println("Raw Message from Client: " + unmarshalledData);
            ConsoleUI.displaySeparator('=', 30);
        }
        catch (SocketTimeoutException e) {
            System.out.println("\nTimeout occurred while waiting for response from client.");
            System.out.println("Retransmitting reply to client.");
            // try {
            //     // Resending
            //     this.socket.send(requestPacket);
            // }
            // catch (IOException ioe) {
            //     System.out.println("\nSecond retransmission failed. Exiting... ");
            //     System.out.println("An IO error occurred: " + ioe.getMessage());
            //     System.exit(1);
            // }
        }
        catch (IOException e) {
            System.out.println("\nAn IO error occurred: " + e.getMessage());
        }
        return unmarshalledData;
        
    }

}
