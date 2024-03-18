package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Server {
    private int BUFFER_SIZE;
    private int HISTORY_SIZE;
    private boolean AT_MOST_ONCE;
    private Handler handler = new Handler();
    private History history;

    // Constructor
    public Server(int BUFFER_SIZE, int HISTORY_SIZE, boolean AT_MOST_ONCE) {
        if (AT_MOST_ONCE) {
            this.BUFFER_SIZE = BUFFER_SIZE;
            this.HISTORY_SIZE = HISTORY_SIZE;
            this.AT_MOST_ONCE = AT_MOST_ONCE;
            this.history = new History(this.HISTORY_SIZE);
        }
        else {
            // At least once, no history
            this.BUFFER_SIZE = BUFFER_SIZE;
        }
        
    }

    public void listen(int serverPort) {
        // Open UDP Socket
        handler.openPort(serverPort);

        // Prepare a byte buffer to store received data
        byte[] buffer = new byte[BUFFER_SIZE];

        // Create a DatagramPacket for receiving data
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        while (true) {
            // Receive datagram packet over UDP
            Object[] result = handler.receiveOverUDP(receivePacket);

            InetAddress clientAddress = (InetAddress) result[0];
            int clientPort = (int) result[1];
            String unmarshalledData = (String) result[2];

            processRequest(clientAddress, clientPort, unmarshalledData);
        }
    }

    private void processRequest(InetAddress clientAddress, int clientPort, String unmarshalledData) {

        String[] messageParts = unmarshalledData.split(":");
        String messageType = messageParts[0]; // 0 is request; 1 is reply
        String requestCounter = messageParts[1];
        String clientAddressString = messageParts[2];
        String clientPortInt = messageParts[3];
        String requestType = messageParts[4];
        String requestContents = concatenateFromIndex(messageParts, 5, ":");

        // System.out.println("\nmessageType: " + messageType);
        // System.out.println("requestCounter: " + requestCounter);
        // System.out.println("clientAddress: " + clientAddress);
        // System.out.println("clientPort: " + clientPort);
        // System.out.println("requestType: " + requestType);
        // System.out.println("requestContents: " + requestContents);

        // If server is at most once, request is non-idempotent and performed before
        // Check duplicate in history
        if (AT_MOST_ONCE && isNonIdempotent(requestType) && history.isDuplicate(requestCounter, clientAddressString, clientPortInt)) {
            
            String content = history.getReplyContent(requestCounter, clientAddressString, clientPortInt);
            System.out.println("Server: Replying the stored reply content found in history");
            handler.sendOverUDP(clientAddress, clientPort, content);
        }
        else {
            String replyContent = "";
            // Proceed as per normal
            switch (requestType) {
                case "1":
                    System.out.println("Server: Client request to read a content from a file");
                    startRead(clientAddress, clientPort, requestContents);
                    break;
                case "2":
                    System.out.println("Server: Client request to insert a content into a file");
                    replyContent = startInsert(clientAddress, clientPort, requestContents);
                    if (AT_MOST_ONCE) {
                        // Add record
                        history.addRecord(requestCounter, clientAddressString, clientPortInt, replyContent);
                        history.printAllRecords();
                    }
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
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                boolean error = false;
                // Check if offset is valid
                if (offset < 0 || offset >= randomAccessFile.length()) {
                    System.out.println("Server: Error: Invalid offset");
                    content = "1e2:Invalid byte offset. Please try again.";
                    error = true;
                }
                // Check if number of bytes is valid
                if (bytesToRead <= 0 || offset + bytesToRead > randomAccessFile.length()) {
                    System.out.println("Server: Error: Invalid number of bytes");
                    content = "1e3:Invalid byte offset. Please try again.";
                    error = true;
                }
                // No error
                if (!error) {
                    // Set the file pointer to the specified offset
                    randomAccessFile.seek(offset);

                    // Read the specified number of bytes
                    byte[] buffer = new byte[bytesToRead];
                    int bytesRead = randomAccessFile.read(buffer);

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
        handler.sendOverUDP(clientAddress, clientPort, content);
    }

    private String startInsert(InetAddress clientAddress, int clientPort, String requestContents) {
        String[] requestContentsParts = requestContents.split(":");
        String filePath = requestContentsParts[0];
        long offset = Long.parseLong(requestContentsParts[1]);
        String stringToInsert = requestContentsParts[2];

        System.out.println("Server: Filepath: " + filePath);
        System.out.println("Server: Offset: " + offset);
        System.out.println("Server: Bytes: " + stringToInsert);

        File file = new File(filePath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: File found!");
            try {
                // Read file content
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                boolean error = false;
                // Check if offset is valid
                if (offset < 0 || offset >= randomAccessFile.length()) {
                    System.out.println("Server: Error: Invalid offset");
                    content = "2e2:Invalid byte offset. Please try again.";
                    error = true;
                }
                // Check if number of bytes is valid
                if (offset < 0 || offset > file.length()) {
                    System.out.println("Server: Error: Invalid number of bytes");
                    content = "2e3:Invalid byte offset. Please try again.";
                    error = true;
                }
                // No issue
                if (!error) {

                    // Create a temporary file to store the data after the insertion point
                    File tempFile = File.createTempFile("temp", null);
                    RandomAccessFile tempRandomAccessFile = new RandomAccessFile(tempFile, "rw");

                    // Set the file pointers
                    randomAccessFile.seek(offset);
                    tempRandomAccessFile.seek(0);

                    // Transfer data after insertion point to temporary file
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = randomAccessFile.read(buffer)) != -1) {
                        tempRandomAccessFile.write(buffer, 0, bytesRead);
                    }

                    // Insert string
                    randomAccessFile.seek(offset);
                    randomAccessFile.writeBytes(stringToInsert);

                    // Append the data from the temporary file back to the original file
                    tempRandomAccessFile.seek(0);
                    while ((bytesRead = tempRandomAccessFile.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, bytesRead);
                    }

                    // Close files
                    randomAccessFile.close();
                    tempRandomAccessFile.close();
                    tempFile.delete(); // Delete temporary file

                    System.out.println("Server: File content inserted successfully.");
                    content = "2:File content has been inserted successfully.";
                }
            }
            catch (IOException e) {
                System.out.println("Server: Error: Error inserting into file!");
                content = "2e4:Error inserting into file. Please try again.";
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Server: File not found!");
            content = "2e1:File not found. Please try again.";
        }
        // Send the file content to client
        handler.sendOverUDP(clientAddress, clientPort, content);
        return content;
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

    private boolean isNonIdempotent(String requestType) {
        if (requestType.equals("2") || requestType.equals("5")) {
            System.out.println("Server: Non-idempotent operation");
            return true;
        }
        System.out.println("Server: Idempotent operation");
        return false;
    }

    private String concatenateFromIndex(String[] elements, int startIndex, String delimiter) {
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
