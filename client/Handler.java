package client;

import java.io.*;
import java.net.*;
import java.util.UUID;

import utils.ConsoleUI;

public class Handler {

    private static final int BUFFER_SIZE = 1024;
    private InetSocketAddress serverAddress;
    private DatagramSocket socket;
    private String requestId;


    public void connectToServer(String serverAddress, int serverPort) throws Exception {
        try {
            // Connect to Server
            this.serverAddress = new InetSocketAddress(serverAddress, serverPort);
            System.out.println("\nSuccessfully connected to " + serverAddress + ":" + serverPort);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void openPort(int clientPort) throws Exception {
        try {
            // Open UDP Socket
            this.socket = new DatagramSocket();
            System.out.println("Client port listening at " + clientPort);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getClientAddress() {
        InetAddress clInetAddress = InetAddress.getLocalHost();
    }

    public void generateRequestId() {
        this.requestId = UUID.randomUUID().toString().substring(0,8);
    }

    public void disconnect() {
        // Close UDP Socket
        this.socket.close();
    }

    public void sendOverUDP(String message) {
        try {

            // Marshal the data into a byte array
            byte[] marshalledData = utils.Marshaller.marshal(message);

            // Convert into data packet
            DatagramPacket requestPacket = new DatagramPacket(marshalledData, marshalledData.length, serverAddress);

            // Send over UDP
            this.socket.send(requestPacket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean receiveOverUDP() {
        String unmarshalledData = "";
        try {

            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket);

            // Unmarshal the data into a String
            byte[] marshalledData = receivePacket.getData();
            unmarshalledData = utils.Marshaller.unmarshal(marshalledData);

            // ConsoleUI.displayBox("File content: " + unmarshalledData);

            ConsoleUI.displaySeparator('=', 30);
            System.out.println("File content: " + unmarshalledData);
            ConsoleUI.displaySeparator('=', 30);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
