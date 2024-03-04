package client;

import java.io.*;
import java.net.*;

public class ClientHandler {

    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private InetAddress serverAddress;

    // public Handler(String serverIP) {
    //     try {
    //         this.serverAddress = InetAddress.getByName(serverIP);
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //     }
        
    // }

    public void connectToServer(String serverIP) throws Exception {
        try {
            this.serverAddress = InetAddress.getByName(serverIP);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void sendOverUDP(int number, String message) {
        try {
            
            // Open UDP Socket
            DatagramSocket socket = new DatagramSocket();

            // Marshal the data into a byte array
            byte[] marshalledData = util.Marshaller.marshal(number, message);

            // Convert into data packet
            DatagramPacket packet = new DatagramPacket(marshalledData, marshalledData.length, serverAddress, SERVER_PORT);

            // Send over UDP
            socket.send(packet);

            // Close UDP Socket
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveOverUDP(int number, String message) {
        try {
            
            // Open UDP Socket
            DatagramSocket socket = new DatagramSocket();

            // Marshal the data into a byte array
            byte[] marshalledData = util.Marshaller.marshal(number, message);

            // Convert into data packet
            DatagramPacket packet = new DatagramPacket(marshalledData, marshalledData.length, serverAddress, SERVER_PORT);

            // Send over UDP
            socket.send(packet);

            // Close UDP Socket
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
