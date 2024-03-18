package server;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import utils.ConsoleUI;

public class Monitor {
    private Subscriber[] subscribers;
    private int size;
    

    public Monitor(int capacity) {
        this.size = 0;
        subscribers = new Subscriber[capacity];
    }

    public void addSubscriber(InetAddress clientAddress, int clientPort, String filePath, long monitorMinutes) {
        // Check if array needs to be resized
        if (size == subscribers.length) {
            // Resize the array
            subscribers = Arrays.copyOf(subscribers, subscribers.length * 2);
        }

        Subscriber newSubscriber = new Subscriber(clientAddress, clientPort, filePath, monitorMinutes);
        subscribers[size] = newSubscriber;
        size++;
        System.out.println("Server: New subscriber added to monitor list");
    }

    // Get all subscribers as a collection
    public List<Subscriber> getAllSubscribersAsList() {
        return Arrays.asList(Arrays.copyOf(subscribers, size));
    }

    // Remove expired subscriber
    public Map<InetAddress, Integer> removeExpiredSubscriber() {
        Map<InetAddress, Integer> removedSubscribers = new HashMap<>();
        
        for (int i = 0; i < size; i++) {
            if (subscribers[i].getEndTime().isBefore(LocalDateTime.now())) {
                // Store the list of removed subscribers
                removedSubscribers.put(subscribers[i].getClientAddress(), subscribers[i].getClientPort());
                System.out.println("Server: Subscriber " + (subscribers[i].getClientAddress()).getHostAddress() + ":" + subscribers[i].getClientPort() + " with filepath " + subscribers[i].getFilePath() + " was removed");
                
                // Remove and shift elements to fill the gap
                System.arraycopy(subscribers, i + 1, subscribers, i, size - i - 1);
                size--;
                
            }
        }
        return removedSubscribers;
    }

    // Display all subscribers
    public void printAllSubscribers() {
        System.out.println("\n");
        System.out.println("+---------------------------------------+");
        System.out.println("|            Subscriber List            |");
        System.out.println("+---------------------------------------+");
        System.out.println("Server: Total number of subscribers in monitor: " + size);
        for (int i = 0; i < size; i++) {
            ConsoleUI.displaySeparator('-', 41);
            System.out.println("Server: Subscriber #" + (i+1));
            System.out.println("Server: Client Address: " + (subscribers[i].getClientAddress()).getHostAddress());
            System.out.println("Server: Client Port: " + subscribers[i].getClientPort());
            System.out.println("Server: Filepath: " + subscribers[i].getFilePath());
            System.out.println("Server: Start Time: " + subscribers[i].getStartTime());
            System.out.println("Server: End Time: " + subscribers[i].getEndTime());
        }
        ConsoleUI.displaySeparator('-', 41);
    }
}

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

    // Getters
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}