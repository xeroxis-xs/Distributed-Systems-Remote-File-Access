/**
 * @file Client.hpp
 * @brief Declaration of the Client class for communication with a server.
 *
 * The Client class provides functionality for connecting to a server, handling requests,
 * managing cache entries, and monitoring server responses.
 */

#ifndef CLIENT_HPP
#define CLIENT_HPP

#include <string>
#include <vector>
#include "../utils/UserInputReader.hpp"
#include "../utils/ConsoleUI.hpp"
#include "Handler.hpp"
#include <unordered_map>
#include <iostream>
#include <chrono>
#include <thread>

using std::string, std::chrono::system_clock, std::chrono::milliseconds, std::chrono::duration_cast;

class Client
{
    
private:
    int clientPort;
    string serverAddress;
    int serverPort;
    bool timerFlag;
    Handler *handler;
    UserInputReader *inputReader;
    bool isMonitoring;

    /**
     * @struct CacheEntry
     * @brief Represents an entry in the cache.
     */
    struct CacheEntry
    {
        string content;
        long long Tc;
        long long Tmclient;
    };

    long freshnessInterval;
    /**
     * @brief Initiates various services based on user input.
     */
    void startServices();

    /**
     * @brief Initiates a read request.
     * @param requestType The type of request (e.g., "1").
     */
    void startRead(string requestType);

     /**
     * @brief Initiates an insert request.
     * @param requestType The type of request (e.g., "2").
     */
    void startInsert(string requestType);

    /**
     * @brief Initiates monitoring of a target file.
     * @param requestType The type of request (e.g., "3").
     */
    void startMonitor(string requestType);

    /**
     * @brief Initiates a delete request.
     * @param requestType The type of request (e.g., "4").
     */
    void startDelete(string requestType);

    /**
     * @brief Initiates an append request.
     * @param requestType The type of request (e.g., "5").
     */
    void startAppend(string requestType);

    /**
     * @brief Processes the reply received from the server.
     * @param message The reply message from the server.
     */
    void processReplyFromServer(string message);

     /**
     * @brief Monitors the specified duration and stops monitoring.
     * @param monitorMinutes The duration (in minutes) to monitor.
     */
    void monitorTimer(long monitorMinutes);

    /**
     * @brief Concatenates elements from a vector into a single string starting from a given index.
     * @param elements The vector of strings to concatenate.
     * @param startIndex The index from which to start concatenation.
     * @param delimiter The delimiter to use between concatenated elements.
     * @return The concatenated string.
     */
    string concatenateFromIndex(vector<string> &elements, int startIndex, string delimiter);
    std::unordered_map<std::string, CacheEntry> cache;

public:

    /**
     * @brief Constructor for the Client class.
     * @param clientPort The client's port number.
     * @param serverAddress The server's address.
     * @param serverPort The server's port number.
     * @param BUFFER_SIZE The buffer size for communication.
     * @param PACKET_SEND_LOSS_PROB The packet send loss probability.
     * @param PACKET_RECV_LOSS_PROB The packet receive loss probability.
     * @param MONITORING_PACKET_RECV_LOSS_PROB The monitoring packet receive loss probability.
     * @param MAX_RETRIES The maximum number of retries for communication.
     * @param freshnessInterval The freshness interval for cache entries.
     */
    Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, double MONITORING_PACKET_RECV_LOSS_PROB,int MAX_RETRIES, long freshnessInterval);

    /**
     * @brief Initiates the client-server connection.
     */
    void startConnection();

    /**
     * @brief Prints the content of the cache.
     */
    void printCacheContent();
};

#endif
