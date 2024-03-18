package server;

import utils.ConsoleUI;

public class History {
    private Record[] records;
    private int size;

    // Constructor
    public History(int capacity) {
        this.size = 0;
        records = new Record[capacity];
    }

    // Add a record to the history
    public void addRecord(String requestCounter, String clientAddress, String clientPort, String replyContent) {
        Record newRecord = new Record(requestCounter, clientAddress, clientPort, replyContent);
        records[size] = newRecord;
        size++;
        System.out.println("Server: New record added in history");
    }

    // Check for duplicate 
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

    // Get the reply content
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

    // Display all records
    public void printAllRecords() {
        ConsoleUI.displaySeparator('*', 40);
        System.out.println("Server: Total number of records in history: " + size);
        for (int i = 0; i < size; i++) {
            ConsoleUI.displaySeparator('-', 40);
            System.out.println("Server: Record " + (i+1));
            System.out.println("Server: Request Counter: " + records[i].getRequestCounter());
            System.out.println("Server: Client Address: " + records[i].getClientAddress());
            System.out.println("Server: Client Port: " + records[i].getClientPort());
            System.out.println("Server: Reply Content: " + records[i].getReplyContent());
        }
        ConsoleUI.displaySeparator('*', 40);
    }
}

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

    // Getters
    public String getRequestCounter() {
        return requestCounter;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getClientPort() {
        return clientPort;
    }

    public String getReplyContent() {
        return replyContent;
    }

}