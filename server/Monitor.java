package server;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import utils.ConsoleUI;

/**
 * Monitor Class is used to keep track of all the subscribers that are currently
 * monitoring the server.
 */
public class Monitor {
    private Subscriber[] subscribers;
    private int size;

    public Monitor(int capacity) {
        this.size = 0;
        subscribers = new Subscriber[capacity];
    }

    /**
     * Add a new subsriber client to the monitor list
     *
     * @param clientAddress  address of the client
     * @param clientPort     port of the client
     * @param filePath       path of the file to be monitored
     * @param monitorMinutes time in minutes the client will monitor the file
     */
    public void addSubscriber(InetAddress clientAddress, int clientPort, String filePath, long monitorMinutes) {
        // Check if array needs to be resized

        if (size == subscribers.length) {
            // Resize the array
            subscribers = Arrays.copyOf(subscribers, subscribers.length * 2);
        }

        Subscriber newSubscriber = new Subscriber(clientAddress, clientPort, filePath, monitorMinutes);

        // if (checkDuplicatedSubscribers(newSubscriber)) {
        // return;
        // }
        subscribers[size] = newSubscriber;
        size++;
        System.out.println("Server: New subscriber added to monitor list");
    }

    /**
     * Remove a subscriber from the monitor list
     *
     * @param clientAddress address of the client
     * @param clientPort    port of the client
     * @param filePath      path of the file that was monitored
     */
    public void removeSubscriber(InetAddress clientAddress, int clientPort, String filePath) {
        for (int i = 0; i < size; i++) {
            if (subscribers[i].getClientAddress().equals(clientAddress) && subscribers[i].getClientPort() == clientPort
                    && subscribers[i].getFilePath().equals(filePath)) {

                System.arraycopy(subscribers, i + 1, subscribers, i, size - i - 1);
                size--;
                System.out.println("Server: Subscriber " + (subscribers[i].getClientAddress()).getHostAddress() + ":"
                        + subscribers[i].getClientPort() + " with filepath " + subscribers[i].getFilePath()
                        + " was removed");
                return;
            }
            System.out.println("Server: Subscriber not found in monitor list");
        }
    }

    /**
     * Get all subscribers as a list
     * 
     * @return list of subscribers
     */
    public List<Subscriber> getAllSubscribersAsList() {
        return Arrays.asList(Arrays.copyOf(subscribers, size));
    }

    /**
     * Remove all expired subscribers from the monitor list
     * 
     * @return the address and port of the subscribers that were removed
     */
    public Map<InetAddress, Integer> removeExpiredSubscriber() {
        Map<InetAddress, Integer> removedSubscribers = new HashMap<>();

        for (int i = 0; i < size; i++) {
            if (subscribers[i].getEndTime().isBefore(LocalDateTime.now())) {
                // Store the list of removed subscribers
                removedSubscribers.put(subscribers[i].getClientAddress(), subscribers[i].getClientPort());
                System.out.println("Server: Subscriber " + (subscribers[i].getClientAddress()).getHostAddress() + ":"
                        + subscribers[i].getClientPort() + " with filepath " + subscribers[i].getFilePath()
                        + " was removed");

                // Remove and shift elements to fill the gap
                System.arraycopy(subscribers, i + 1, subscribers, i, size - i - 1);
                size--;

            }
        }
        return removedSubscribers;
    }

    /**
     * Check for duplicated subscribers.
     * 
     * @return true if there are duplicated subscribers, false otherwise.
     */
    public boolean checkDuplicatedSubscribers(Subscriber subscriber) {
        // Iterate through the subscribers array

        for (int i = 0; i < subscribers.length; i++) {
            Subscriber otherSubscriber = subscribers[i];
            // Check for duplicate
            if (isDuplicate(subscriber, otherSubscriber)) {
                return true; // Found duplicate
            }
        }
        return false; // No duplicates found
    }

    /**
     * Check if two subscribers are duplicates.
     * 
     * @param subscriber1 First subscriber
     * @param subscriber2 Second subscriber
     * @return true if the subscribers are duplicates, false otherwise.
     */
    private boolean isDuplicate(Subscriber subscriber1, Subscriber subscriber2) {
        // Check if client address, client port, and file path are the same
        return subscriber1.getClientAddress().equals(subscriber2.getClientAddress()) &&
                subscriber1.getClientPort() == subscriber2.getClientPort() &&
                subscriber1.getFilePath().equals(subscriber2.getFilePath());
    }

    /**
     * Print all subscribers in the monitor list
     */
    public void printAllSubscribers() {
        System.out.println("\n");
        System.out.println("+---------------------------------------+");
        System.out.println("|            Subscriber List            |");
        System.out.println("+---------------------------------------+");
        System.out.println("Server: Total number of subscribers in monitor: " + size);
        for (int i = 0; i < size; i++) {
            ConsoleUI.displaySeparator('-', 41);
            System.out.println("Server: Subscriber #" + (i + 1));
            System.out.println("Server: Client Address: " + (subscribers[i].getClientAddress()).getHostAddress());
            System.out.println("Server: Client Port: " + subscribers[i].getClientPort());
            System.out.println("Server: Filepath: " + subscribers[i].getFilePath());
            System.out.println("Server: Start Time: " + subscribers[i].getStartTime());
            System.out.println("Server: End Time: " + subscribers[i].getEndTime());
        }
        ConsoleUI.displaySeparator('-', 41);
    }
}

/**
 * Subscriber Class is used to store the information of the subscriber client
 */
class Subscriber {
    private InetAddress clientAddress;
    private int clientPort;
    private String filePath;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Subscriber(InetAddress clientAddress, int clientPort, String filePath, long monitorMinutes) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.filePath = filePath;
        this.startTime = LocalDateTime.now();
        this.endTime = startTime.plusMinutes(monitorMinutes);
    }

    /**
     * Get the client address
     * 
     * @return client address
     */
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    /**
     * Get the client port
     * 
     * @return client port
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * Get the file path that the client is monitoring
     * 
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get the start time of the monitoring
     * 
     * @return time of monitoring start
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Get the end time of the monitoring
     * 
     * @return time of monitoring end
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
}