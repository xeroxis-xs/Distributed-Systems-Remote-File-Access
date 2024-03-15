package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Server {
    private int BUFFER_SIZE = 1024;
    public Handler handler = new Handler();

    public Server() {

    }

    public void listen(int serverPort) {
        // Open UDP Socket
        this.handler.openPort(serverPort);

        // Prepare a byte buffer to store received data
        byte[] buffer = new byte[BUFFER_SIZE];

        // Create a DatagramPacket for receiving data
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        while (true) {
            // Receive datagram packet over UDP
            Object[] result = this.handler.receiveOverUDP(receivePacket);

            InetAddress clientAddress = (InetAddress) result[0];
            int clientPort = (int) result[1];
            String unmarshalledData = (String) result[2];

            processRequest(clientAddress, clientPort, unmarshalledData);
        }
    }

    public void processRequest(InetAddress clientAddress, int clientPort, String unmarshalledData) {

        String[] messageParts = unmarshalledData.split(":");
        String messageType = messageParts[0]; // 0 is request; 1 is reply
        String requestCounter = messageParts[1];
        // String clientAddress = messageParts[2];
        // String clientPort = messageParts[3];
        String requestType = messageParts[4];
        String requestContents = concatenateFromIndex(messageParts, 5, ":");

        // System.out.println("\nmessageType: " + messageType);
        // System.out.println("requestCounter: " + requestCounter);
        // System.out.println("clientAddress: " + clientAddress);
        // System.out.println("clientPort: " + clientPort);
        // System.out.println("requestType: " + requestType);
        // System.out.println("requestContents: " + requestContents);

        switch (requestType) {
            case "1":
                System.out.println("Server: Client request to read a content from a file");
                startRead(clientAddress, clientPort, requestContents);
                break;
            case "2":
                System.out.println("Server: Client request to insert a content into a file");
                startInsert(clientAddress, clientPort, requestContents);
                break;
            case "3":
                System.out.println("Server: Client request to monitor updates of a file");
                startMonitor(clientAddress, clientPort, requestContents);
                break;
            case "4":
                System.out.println("Server: Client request for idempotent service");
                startIdempotent(clientAddress, clientPort, requestContents);
                break;
            case "5":
                System.out.println("Server: Client request for non-idempotent service");
                startNonIdempotent(clientAddress, clientPort, requestContents);
                break;
            default:
                System.out.println("Server: Invalid request type.");
        }
    }

    private void startRead(InetAddress clientAddress, int clientPort, String requestContents) {
        String[] requestContentsParts = requestContents.split(":");
        String filePath = requestContentsParts[0];
        long offset = Long.parseLong(requestContentsParts[1]);
        int bytesToRead = Integer.parseInt(requestContentsParts[2].trim());

        System.out.println("Server: Filepath: " + filePath);
        System.out.println("Server: Offset: " + offset);
        System.out.println("Server: Bytes: " + bytesToRead);

        File file = new File(filePath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: File found!");
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

                // Check if offset is valid
                if (offset < 0 || offset >= raf.length()) {
                    System.err.println("Server: Error: Invalid offset");
                    content = "1e2:Invalid byte offset. Please try again.";
                }
                // Check if number of bytes is valid
                else if (bytesToRead <= 0 || offset + bytesToRead > raf.length()) {
                    System.err.println("Server: Error: Invalid number of bytes");
                    content = "1e3:Invalid byte offset. Please try again.";
                }
                // No issue
                else {
                    // Set the file pointer to the specified offset
                    raf.seek(offset);

                    // Read the specified number of bytes
                    byte[] buffer = new byte[bytesToRead];
                    int bytesRead = raf.read(buffer);

                    // Convert the bytes to a String
                    content = new String(buffer, 0, bytesRead);
                    System.out.println("Server: File content: " + content);
                    content = "1:" + content;
                }
            }
            catch (IOException e) {
                System.out.println("Server: Error: Error reading file!");
                content = "1e4:Error reading file. Please try again.";
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Server: File not found!");
            content = "1e1:File not found. Please try again.";
        }
        // Send the file content to client
        this.handler.sendOverUDP(clientAddress, clientPort, content);
    }

    private byte[]  startInsert(InetAddress clientAddress, int clientPort, String requestContents) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startMonitor(InetAddress clientAddress, int clientPort, String requestContents) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startIdempotent(InetAddress clientAddress, int clientPort, String requestContents) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startNonIdempotent(InetAddress clientAddress, int clientPort, String requestContents) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    // public void sendOverUDP(String message) {
    //     try {

    //         // Marshal the data into a byte array
    //         byte[] marshalledData = utils.Marshaller.marshal(message);

    //         // Convert into data packet
    //         DatagramPacket packet = new DatagramPacket(marshalledData, marshalledData.length, this.clientAddress, this.clientPort);

    //         // Send over UDP
    //         this.socket.send(packet);
    //     }
    //     catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void receiveOverUDP() {
    //     try {
    //         byte[] receiveData = new byte[BUFFER_SIZE];
    //         DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
    //         this.socket.receive(packet);

    //         this.clientAddress = packet.getAddress();
    //         this.clientPort = packet.getPort();
    //         System.out.println("Server: Connected Client: " + clientAddress.toString().substring(1) + ":" + clientPort);

    //         byte[] marshalledData = packet.getData();
    //         String unmarshalledData = utils.Marshaller.unmarshal(marshalledData);

    //         String[] messageParts = unmarshalledData.split(":");

    //         int messageHeader = Integer.parseInt(messageParts[0]);
    //         String[] messageBody = Arrays.copyOfRange(messageParts, 1, messageParts.length);

    //         System.out.println("Server: Received new message!");
    //         this.processRequest(messageHeader, messageBody);
    //     }
    //     catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    public String concatenateFromIndex(String[] elements, int startIndex, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();

        // Iterate through the elements starting from the startIndex
        for (int i = startIndex; i < elements.length; i++) {
            // Append the current element
            stringBuilder.append(elements[i]);

            // Append delimiter if not the last element
            if (i < elements.length - 1) {
                stringBuilder.append(delimiter);
            }
        }

        // Convert StringBuilder to String and return
        return stringBuilder.toString();
    }


}
