package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Server class
 */
public class Server {
    private int BUFFER_SIZE;
    private int HISTORY_SIZE;
    private int MONITOR_SIZE;
    private Map<String, Long> fileTmservers = new HashMap<>();
    private boolean AT_MOST_ONCE;
    private History history;
    private Monitor monitor;
    private Handler handler;


    // Constructor
    public Server(int BUFFER_SIZE, int HISTORY_SIZE, int MONITOR_SIZE, boolean AT_MOST_ONCE, double PACKET_SEND_LOSS_PROB) {

        this.handler = new Handler(PACKET_SEND_LOSS_PROB);

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

    /**
     * Open UDP socket and run a whileloop to listen for messages
     *
     * @param serverPort port of the server for client to conenct to
     */
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

    /**
     * Process any messages received and perform task
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param unmarshalledData unmarshalled data from client
     */
    private void processRequest(InetAddress clientAddress, int clientPort, String unmarshalledData) {
        if (unmarshalledData != null) {
            String[] messageParts = unmarshalledData.split(":");
            // String messageType = messageParts[0]; // 0 is request; 1 is reply
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
                        startDelete(clientAddress, clientPort, requestContents);
                        break;
                    case "5":
                        System.out.println("Server: Client request for non-idempotent service");
                        replyContent = startAppend(clientAddress, clientPort, requestContents);
                        if (AT_MOST_ONCE) {
                            // Add record
                            history.addRecord(requestCounter, clientAddressString, clientPortInt, replyContent);
                        }
                        break;
                    case "6":
                        System.out.println("Server: Client request Tmserver(timestamp) of the file");
                        getFileTmserver(clientAddress, clientPort, requestContents);
                        break;
                    default:
                        System.out.println("Server: Invalid request type.");
                }
            }
        }

    }

    /**
     * Exectute Read File Operation
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     */
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
                    content = "1e3:Invalid number of bytes. Please try again.";
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
                    long Tmserver = 0;
                    if (fileTmservers.containsKey(filePath)) {
                        Tmserver = fileTmservers.get(filePath);
                    }
                    content = "1:" + filePath + ":" + offset + ":" + bytesToRead + ":" + Tmserver + ":"
                            + content;
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

    /**
     * Exectute Insert Content Into File Operation
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     * @return a reply content that will be stored in the History
     */
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
                if (offset < 0 || offset > randomAccessFile.length()) {
                    System.out.println("Server: Error: Invalid offset");
                    content = "2e2:Invalid byte offset. Please try again.";
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
                    // to indicate that the file has been modified
                    fileTmservers.put(filePath, System.currentTimeMillis());
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

    /**
     * Exectute Monitor File Operation
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     */
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
        // Inform client about the status
        handler.sendOverUDP(clientAddress, clientPort, content);
    }

    /**
     * Issue Callback
     * Inform subscribers that the subscribed file has an updated content
     * Updated content caused by Insert and Append operation
     *
     * @param filePath the file that has the updated content
     */
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

    /**
     * Inform the subscribers that their monitoring has expired
     */
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

    /**
     * Issue Callback
     * Inform subscribers that the subscribed file has been deleted
     *
     * @param filePath the file that has been deleted
     */
    private void informSubscribersAboutDeletion(String filePath){
        String content = "4e3:The file " + filePath + " has been deleted. Monitoring stopped.";

        List<Subscriber> subscribers = monitor.getAllSubscribersAsList();

        // Inform each subscriber about the deletion
        for (Subscriber subscriber : subscribers) {
            if(subscriber.getFilePath().equals(filePath)){
                handler.sendOverUDP(subscriber.getClientAddress(), subscriber.getClientPort(), content);
                // Remove subscriber since monitoring is stopped
                monitor.removeSubscriber(subscriber.getClientAddress(), subscriber.getClientPort(), filePath);
            }

        }
    }

    /**
     * Exectute File Delete Operation
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     */
    private void startDelete(InetAddress clientAddress, int clientPort, String requestContents) {
        String[] requestContentsParts = requestContents.split(":");
        String filePath = requestContentsParts[0];

        System.out.println("Server: Filepath: " + filePath);

        File file = new File(filePath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: File found!");
            File myObj = new File(filePath);
            if (myObj.delete()) {
                System.out.println("Server: Deleted the file: " + myObj.getName());
                content = "4:File has been deleted successfully.";

                // Inform subscribers about file deletion
                informSubscribersAboutDeletion(filePath);
            } else {
                System.out.println("Server: Failed to delete the file.");
                content = "4e2:Error deleting file. Please try again.";
            }
        } else {
            System.out.println("Server: File not found!");
            content = "4e1:File not found. Please try again.";
        }
        handler.sendOverUDP(clientAddress, clientPort, content);
    }

    /**
     * Exectute File Append Operation
     *
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     * @return a reply content that will be stored in the History
     */
    private String startAppend(InetAddress clientAddress, int clientPort, String requestContents) {
        String[] requestContentsParts = requestContents.split(":");
        // Source file to append from
        String srcPath = requestContentsParts[0];
        // Destination file to append to
        String targetPath = requestContentsParts[1];
        Boolean readFlag = false;

        File file = new File(srcPath);
        String content = "";

        if (file.exists()) {
            System.out.println("Server: Source file found!");
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {

                // Set the file pointer to the specified offset
                randomAccessFile.seek(0);

                // Read the specified number of bytes
                byte[] buffer = new byte[(int) file.length()];
                int bytesRead = randomAccessFile.read(buffer);

                // Convert the bytes to a String
                content = new String(buffer, 0, bytesRead);
                System.out.println("Server: Source file content: " + content);
                readFlag = true;
            } catch (IOException e) {
                System.out.println("Server: Error: Error reading source file!");
                content = "5e2:Error reading source file. Please try again.";
            }
        }
        else {
            System.out.println("Server: Source file not found!");
            content = "5e1:Source file not found. Please try again.";
        }

        file = new File(targetPath);

        if(readFlag) {
            if (file.exists()) {
                System.out.println("Server: Target file found!");
                try {
                    // Read file content
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                    // Create a temporary file to store the data after the insertion point
                    File tempFile = File.createTempFile("temp", null);
                    RandomAccessFile tempRandomAccessFile = new RandomAccessFile(tempFile, "rw");

                    // Set the file pointers
                    randomAccessFile.seek(file.length());
                    tempRandomAccessFile.seek(0);

                    // Transfer data after insertion point to temporary file
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = randomAccessFile.read(buffer)) != -1) {
                        tempRandomAccessFile.write(buffer, 0, bytesRead);
                    }

                    // Insert string
                    randomAccessFile.seek(file.length());
                    randomAccessFile.writeBytes(content);

                    // Append the data from the temporary file back to the original file
                    tempRandomAccessFile.seek(0);
                    while ((bytesRead = tempRandomAccessFile.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, bytesRead);
                    }

                    System.out.println("Server: File content appended successfully.");
                    content = "5:File content has been appended successfully.";

                    // New changes to the file, update subscribers
                    informSubscribers(targetPath);

                    // Close files
                    randomAccessFile.close();
                    tempRandomAccessFile.close();
                    tempFile.delete(); // Delete temporary file

                    // Add file and current timestamp to fileTimeStamps since target file is modified
                    fileTmservers.put(targetPath, System.currentTimeMillis());

                } catch (IOException e) {
                    System.out.println("Server: Error: Error appending into destination file!");
                    content = "5e4:Error appendeding into destination file. Please try again.";
                    e.printStackTrace();
                }
            } else {
                System.out.println("Server: Destination file not found!");
                content = "5e3:Destination file not found. Please try again.";
            }
        }
        // Send the content to client
        handler.sendOverUDP(clientAddress, clientPort, content);
        return content;
    }


    /**
     * Check if a request is non-idempotent
     * @param requestType
     * @return true = non-idempotent, false = idempotent
     */
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

    /**
     * Get Tmserver(timestamp) of the file and send to client
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param requestContents application specific content
     */
    private void getFileTmserver(InetAddress clientAddress, int clientPort, String requestContents) {

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
            System.out.println("Server: File to get Tmserver found!");

            if (fileTmservers.containsKey(filePath)) {
                long Tmserver = fileTmservers.get(filePath);

                content = "6:" + filePath + ":" + offset + ":" + bytesToRead + ":" + Long.toString(Tmserver)
                        + ":File to get Tmserver found. Found existing Tmserver";

            } else {
                content = "6:" + filePath + ":" + offset + ":" + bytesToRead + ":" + Long.toString(0)
                        + ":File to get Tmserver found. No modified action ";
            }
        } else {
            System.out.println("Server: File to get Tmserver not found!");
            content = "6e1:File to get Tmserver not found. Failed to get Tmserver for file.";
        }

        handler.sendOverUDP(clientAddress, clientPort, content);
    }
}