package client;
import java.io.*;
import java.net.*;

public class FileClient {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileClient <server_ip> <filename>");
            return;
        }

        String serverIP = args[0];
        String filename = args[1];

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverIP);

            byte[] sendData = filename.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            socket.send(sendPacket);

            byte[] receiveData = new byte[BUFFER_SIZE];
            String filePath = "client/downloads/" + filename;
            FileOutputStream fos = new FileOutputStream(filePath);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (response.equals("Server: File not found!")) {
                    System.out.println("Client: File not found on server.");
                    break;
                }

                fos.write(receivePacket.getData(), 0, receivePacket.getLength());
                if (receivePacket.getLength() < BUFFER_SIZE) {
                    break;
                }
            }
            fos.close();
            System.out.println("Client: File downloaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
