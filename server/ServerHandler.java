package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class ServerHandler {
    private int BUFFER_SIZE;
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;

    public ServerHandler(int PORT, int BUFFER_SIZE) {
        this.BUFFER_SIZE = BUFFER_SIZE;
        try {
            // Open UDP Socket
            this.socket = new DatagramSocket(PORT);
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Server: Server started at " + (localhost.getHostAddress()).trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRequest(int messageHeader, String[] messageBody) {
        switch (messageHeader) {
            case 1:
                System.out.println("Server: Request Type: " + messageHeader + ". Read a content from a file");
                startRead(messageBody);
                break;
            case 2:
                System.out.println("Server: Request Type: " + messageHeader + ". Insert a content into a file");
                startInsert(messageBody);
                break;
            case 3:
                System.out.println("Server: Request Type: " + messageHeader + ". Monitor updates of a file");
                startMonitor(messageBody);
                break;
            case 4:
                System.out.println("Server: Request Type: " + messageHeader + ". Idempotent service");
                startIdempotent(messageBody);
                break;
            case 5:
                System.out.println("Server: Request Type: " + messageHeader + ". Non-idempotent service");
                startNonIdempotent(messageBody);
                break;
            default:
                System.out.println("Server: Invalid request type.");
        }
    }

    private void startRead(String[] messageBody) {
        String filePath = messageBody[0];
        long offset = Long.parseLong(messageBody[1]);
        int bytesToRead = Integer.parseInt(messageBody[2].trim());
        System.out.println("Server: Filepath: " + filePath);
        System.out.println("Server: Offset: " + offset);
        System.out.println("Server: Bytes: " + bytesToRead);

        File file = new File(filePath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: File found!");
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                // Set the file pointer to the specified offset
                raf.seek(offset);

                // Read the specified number of bytes
                byte[] buffer = new byte[bytesToRead];
                int bytesRead = raf.read(buffer);

                // Convert the bytes to a String
                content = new String(buffer, 0, bytesRead);
                System.out.println("Server: File content: " + content);
            }
            catch (IOException e) {
                System.out.println("Server: Error reading file!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Server: File not found!");
            content = "File not found";
        }
        this.sendOverUDP(content);
    }

    private byte[]  startInsert(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startMonitor(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startIdempotent(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startNonIdempotent(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    public void sendOverUDP(String message) {
        try {

            // Marshal the data into a byte array
            byte[] marshalledData = util.Marshaller.marshal(message);

            // Convert into data packet
            DatagramPacket packet = new DatagramPacket(marshalledData, marshalledData.length, this.clientAddress, this.clientPort);

            // Send over UDP
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveOverUDP() {
        try {
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(packet);

            this.clientAddress = packet.getAddress();
            this.clientPort = packet.getPort();
            System.out.println("Server: Connected Client: " + clientAddress.toString().substring(1) + ":" + clientPort);

            byte[] marshalledData = packet.getData();
            String unmarshalledData = util.Marshaller.unmarshal(marshalledData);

            String[] messageParts = unmarshalledData.split(":");

            int messageHeader = Integer.parseInt(messageParts[0]);
            String[] messageBody = Arrays.copyOfRange(messageParts, 1, messageParts.length);

            System.out.println("Server: Received new message!");
            this.processRequest(messageHeader, messageBody);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
