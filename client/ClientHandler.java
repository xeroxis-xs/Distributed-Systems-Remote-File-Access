package client;

import java.io.*;
import java.net.*;

public class ClientHandler {

    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private InetAddress serverAddress;
    private DatagramSocket socket;


    public boolean connectToServer(String serverIP) throws Exception {
        try {
            this.serverAddress = InetAddress.getByName(serverIP);
            // Open UDP Socket
            this.socket = new DatagramSocket();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    public void disconnect() {
        // Close UDP Socket
        this.socket.close();
    }

    public void sendOverUDP(String message) {
        try {

            // Marshal the data into a byte array
            byte[] marshalledData = util.Marshaller.marshal(message);

            // Convert into data packet
            DatagramPacket packet = new DatagramPacket(marshalledData, marshalledData.length, serverAddress, SERVER_PORT);

            // Send over UDP
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveOverUDP() {
        String unmarshalledData = "";
        try {

            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(receivePacket);

            // Unmarshal the data into a String
            byte[] marshalledData = receivePacket.getData();
            unmarshalledData = util.Marshaller.unmarshal(marshalledData);
            System.out.println("data: " + unmarshalledData);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return unmarshalledData;
    }

}
