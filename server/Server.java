package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Server {
    private int BUFFER_SIZE;
    private int HISTORY_SIZE;
    private int MONITOR_SIZE;
    private Map<String, Long> fileTimestamps = new HashMap<>();
    private boolean AT_MOST_ONCE;
    private Handler handler = new Handler();
    private History history;
    private Monitor monitor;

    // Constructor
    public Server(int BUFFER_SIZE, int HISTORY_SIZE, int MONITOR_SIZE, boolean AT_MOST_ONCE) {
        if (AT_MOST_ONCE) {
            this.BUFFER_SIZE = BUFFER_SIZE;
            this.HISTORY_SIZE = HISTORY_SIZE;
            this.MONITOR_SIZE = MONITOR_SIZE;
            this.AT_MOST_ONCE = AT_MOST_ONCE;
            this.history = new History(this.HISTORY_SIZE);
            this.monitor = new Monitor(this.MONITOR_SIZE);
        } else {
            // At least once, no history
            this.BUFFER_SIZE = BUFFER_SIZE;
            this.MONITOR_SIZE = MONITOR_SIZE;
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

            // Process any incoming message
            processRequest(clientAddress, clientPort, unmarshalledData);

            // Constantly remove and inform expired subscribers
            informExpiredSubscribers();

        }
    }

    private void processRequest(InetAddress clientAddress, int clientPort, String unmarshalledData) {
        if (unmarshalledData != null) {
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
            if (AT_MOST_ONCE && isNonIdempotent(requestType)
                    && history.isDuplicate(requestCounter, clientAddressString, clientPortInt)) {

                String content = history.getReplyContent(requestCounter, clientAddressString, clientPortInt);
                System.out.println("Server: Replying the stored reply content found in history");
                handler.sendOverUDP(clientAddress, clientPort, content);
            } else {
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
                            // history.printAllRecords();
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
                    case "6":
                        System.out.println("Server: Client request Tmserver(timestamp) of the file");
                        getFileTimeStamp(clientAddress, clientPort, requestContents);
                        break;
                    default:
                        System.out.println("Server: Invalid request type.");
                }
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
                    content = "1:" + filePath + ":" + content;
                }
            } catch (IOException e) {
                System.out.println("Server: Error: Error reading file!");
                content = "1e4:Error reading file. Please try again.";
            }
        } else {
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
        System.out.println("Server: Content: " + stringToInsert);

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

                    System.out.println("Server: File content inserted successfully.");
                    content = "2:File content has been inserted successfully.";

                    // New changes to the file, update subscribers
                    informSubscribers(filePath);

                    // Close files
                    randomAccessFile.close();
                    tempRandomAccessFile.close();
                    tempFile.delete(); // Delete temporary file

                    // Add file and current timestamp to fileTimeStamps
                    fileTimestamps.put(filePath, System.currentTimeMillis());
                }
            } catch (IOException e) {
                System.out.println("Server: Error: Error inserting into file!");
                content = "2e4:Error inserting into file. Please try again.";
                e.printStackTrace();
            }
        } else {
            System.out.println("Server: File not found!");
            content = "2e1:File not found. Please try again.";
        }
        // Send the content to client
        handler.sendOverUDP(clientAddress, clientPort, content);
        return content;
    }

    private void startMonitor(InetAddress clientAddress, int clientPort, String requestContents) {
        String[] requestContentsParts = requestContents.split(":");
        String filePath = requestContentsParts[0];
        long monitorMinutes = Long.parseLong(requestContentsParts[1]);

        System.out.println("Server: Filepath: " + filePath);
        System.out.println("Server: Duration: " + monitorMinutes + " minute(s)");

        File file = new File(filePath);
        String content = "";
        if (file.exists()) {
            System.out.println("Server: File for monitoring found!");
            // Add to monitor list
            monitor.addSubscriber(clientAddress, clientPort, filePath, monitorMinutes);
            // monitor.printAllSubscribers();
            content = "3:File for monitoring found. Successfully registered for monitoring callbacks.";
        } else {
            System.out.println("Server: File for monitoring not found!");
            content = "3e3:File for monitoring not found. Failed to register for monitoring callbacks.";
        }

        handler.sendOverUDP(clientAddress, clientPort, content);
    }

    private void informSubscribers(String filePath) {

        try {
            // Open the file in read-only mode
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");

            // Get the length of the file
            long length = randomAccessFile.length();

            // Create a byte array to hold the file content
            byte[] fileContent = new byte[(int) length];

            // Read the entire file into the byte array
            randomAccessFile.readFully(fileContent);

            // Convert the byte array to a string (assuming text file)
            String fileContentString = new String(fileContent);

            System.out.println("Server: File content for subscribers: " + fileContentString);
            String content = "3e1:" + fileContentString;

            List<Subscriber> subscriberList = monitor.getAllSubscribersAsList();

            for (Subscriber subscriber : subscriberList) {
                if (subscriber.getFilePath().equals(filePath)) {
                    // Send the file updated content to each subscriber subscribed to the file
                    handler.sendOverUDP(subscriber.getClientAddress(), subscriber.getClientPort(), content);
                }
            }

            // Close the file
            randomAccessFile.close();
        } catch (IOException e) {
            System.out.println("Server: Error reading updated file");
            e.printStackTrace();
        }

    }

    private void informExpiredSubscribers() {

        // Get expired subscribers
        Map<InetAddress, Integer> removedSubscribers = monitor.removeExpiredSubscriber();

        // Inform expired subscribers
        for (Map.Entry<InetAddress, Integer> entry : removedSubscribers.entrySet()) {
            InetAddress removedClientAddress = entry.getKey();
            Integer removedClientPort = entry.getValue();
            String content = "3e2:Monitor interval expired. You will no longer receive monitoring update.";

            handler.sendOverUDP(removedClientAddress, removedClientPort, content);
        }
    }

    private void startIdempotent(InetAddress clientAddress, int clientPort, String requestContents) {

    }

    private void startNonIdempotent(InetAddress clientAddress, int clientPort, String requestContents) {

    }

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

    private void getFileTimeStamp(InetAddress clientAddress, int clientPort, String requestContents) {

        String[] requestContentsParts = requestContents.split(":");
        String filePath = requestContentsParts[0];

        System.out.println("Server: Filepath: " + filePath);

        File file = new File(filePath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: File to get timestamp found!");

            if (fileTimestamps.containsKey(filePath)) {
                long cachedTimestamp = fileTimestamps.get(filePath);

                content = "6:File to get timestamp found. Found modified action. Retrieved Tmserver t:"
                        + Long.toString(cachedTimestamp);

            } else {
                content = "6:File to get timestamp found. No modified action "
                        + Long.toString(System.currentTimeMillis());
            }
        } else {
            System.out.println("Server: File to get timestamp not found!");
            content = "6e1:File to get timestamp not found. Failed to get Tmserver for file.";
        }

        handler.sendOverUDP(clientAddress, clientPort, content);
    }

}
