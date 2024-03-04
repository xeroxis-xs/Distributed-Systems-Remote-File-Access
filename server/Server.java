package server;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;


    public static void main(String[] args) {
        ServerHandler serverHandler = new ServerHandler(PORT, BUFFER_SIZE);

        try {
            // // Open UDP Socket
            // DatagramSocket socket = new DatagramSocket(PORT);
            // InetAddress localhost = InetAddress.getLocalHost();
            // System.out.println("Server: Server started at " + (localhost.getHostAddress()).trim());

            while (true) {

                serverHandler.receiveOverUDP();

                // byte[] receiveData = new byte[BUFFER_SIZE];
                // DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                // socket.receive(receivePacket);

                // InetAddress clientAddress = receivePacket.getAddress();
                // int clientPort = receivePacket.getPort();
                // System.out.println("Server: Connected Client: " + clientAddress.toString().substring(1) + ":" + clientPort);

                // byte[] marshalledData = receivePacket.getData();
                // String unmarshalledData = util.Marshaller.unmarshal(marshalledData);

                // String[] messageParts = unmarshalledData.split(":");

                // int messageHeader = Integer.parseInt(messageParts[0]);
                // String[] messageBody = Arrays.copyOfRange(messageParts, 1, messageParts.length);

                // System.out.println("Server: Received new message!");
                // byte[] replyData = serverHandler.processRequest(messageHeader, messageBody);


                // byte[] requestData = receivePacket.getData();
                // String request = new String(requestData, 0, receivePacket.getLength());
                // String[] requestParts = request.split(":");

                // int requestType = Integer.parseInt(requestParts[0]);
                // String filePath = requestParts[1];
                // long offset = Long.parseLong(requestParts[2]);
                // int bytesToRead = Integer.parseInt(requestParts[3]);


                // File file = new File(filePath);
                // byte[] replyData;
                // if (file.exists()) {
                //     try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                //         raf.seek(offset);
                //         byte[] buffer = new byte[bytesToRead];
                //         int bytesRead = raf.read(buffer);
                //         replyData = new byte[bytesRead];
                //         System.arraycopy(buffer, 0, replyData, 0, bytesRead);
                //     }
                // } else {
                //     replyData = "File not found".getBytes();
                // }

                // DatagramPacket sendPacket = new DatagramPacket(replyData, replyData.length, clientAddress, clientPort);
                // socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
