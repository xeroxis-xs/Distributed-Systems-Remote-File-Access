package server;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            System.out.println("Server started...");

            while (true) {
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                byte[] requestData = receivePacket.getData();
                String request = new String(requestData, 0, receivePacket.getLength());
                String[] requestParts = request.split(":");

                int requestType = requestParts[0];
                String filePath = requestParts[1];
                long offset = Long.parseLong(requestParts[2]);
                int bytesToRead = Integer.parseInt(requestParts[3]);

                System.out.println("Received request for file: " + filePath + ", offset: " + offset + ", bytesToRead: " + bytesToRead);

                File file = new File(filePath);
                byte[] replyData;
                if (file.exists()) {
                    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                        raf.seek(offset);
                        byte[] buffer = new byte[bytesToRead];
                        int bytesRead = raf.read(buffer);
                        replyData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, replyData, 0, bytesRead);
                    }
                } else {
                    replyData = "File not found".getBytes();
                }

                DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
