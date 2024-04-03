package server;

import utils.ConsoleUI;

/**
 * History Class is used to keep track of all the non-idempotent requests
 * that have been processed by the server
 */
public class History {
    private Record[] records;
    private int size;

    // Constructor
    public History(int capacity) {
        this.size = 0;
        records = new Record[capacity];
    }

    /**
     * Add a new record to the History
     *
     * @param requestCounter request counter
     * @param clientAddress address of client
     * @param clientPort port of client
     * @param replyContent reply content sent to client
     */
    public void addRecord(String requestCounter, String clientAddress, String clientPort, String replyContent) {
        Record newRecord = new Record(requestCounter, clientAddress, clientPort, replyContent);
        records[size] = newRecord;
        size++;
        System.out.println("Server: New record added in history");
    }

    /**
     * Message Filtering, Duplicate checking
     * Check if the request exist in the History
     * @param requestCounter request counter
     * @param clientAddress address of client
     * @param clientPort port of client
     * @return true = duplicate found, false = no duplicate found
     */
    public boolean isDuplicate(String requestCounter, String clientAddress, String clientPort) {
        for (int i = 0; i < size; i++) {
            if (records[i].getRequestCounter().equals(requestCounter) &&
            records[i].getClientAddress().equals(clientAddress) &&
            records[i].getClientPort().equals(clientPort)) {
                System.out.println("Server: Duplicated request from same client found in history");
                return true; // Found a duplicate
            }
        }
        System.out.println("Server: No duplicated request from same client found in history");
        return false; // No duplicate found
    }

    /**
     * Get the reply content from the History
     *
     * @param requestCounter request counter
     * @param clientAddress address of client
     * @param clientPort port of client
     * @return reply content
     */
    public String getReplyContent(String requestCounter, String clientAddress, String clientPort) {
        String content = "";
        for (int i = 0; i < size; i++) {
            if (records[i].getRequestCounter().equals(requestCounter) &&
            records[i].getClientAddress().equals(clientAddress) &&
            records[i].getClientPort().equals(clientPort)) {

                content = records[i].getReplyContent();
                System.out.println("Server: Content: " + content);
            }
        }
        return content;
    }

    /**
     * Display all records in the History
     */
    public void printAllRecords() {
        System.out.println("\n");
        System.out.println("+---------------------------------------+");
        System.out.println("|             History Records           |");
        System.out.println("+---------------------------------------+");
        System.out.println("Server: Total number of records in history: " + size);
        for (int i = 0; i < size; i++) {
            ConsoleUI.displaySeparator('-', 41);
            System.out.println("Server: Record #" + (i+1));
            System.out.println("Server: Request Counter: " + records[i].getRequestCounter());
            System.out.println("Server: Client Address: " + records[i].getClientAddress());
            System.out.println("Server: Client Port: " + records[i].getClientPort());
            System.out.println("Server: Reply Content: " + records[i].getReplyContent());
        }
        ConsoleUI.displaySeparator('-', 41);
    }
}

/**
 * Record Class is used to store the information of the request
 */
class Record {
    private String requestCounter;
    private String clientAddress;
    private String clientPort;
    private String replyContent;

    public Record(String requestCounter, String clientAddress, String clientPort, String replyContent) {
        this.requestCounter = requestCounter;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.replyContent = replyContent;
    }

    /**
     * Get the request counter
     * @return reqest counter
     */
    public String getRequestCounter() {
        return requestCounter;
    }

    /**
     * Get the client address
     * @return client address
     */
    public String getClientAddress() {
        return clientAddress;
    }

    /**
     * Get the client port
     * @return client port
     */
    public String getClientPort() {
        return clientPort;
    }

    /**
     * Get the reply content
     * @return reply content
     */
    public String getReplyContent() {
        return replyContent;
    }

}