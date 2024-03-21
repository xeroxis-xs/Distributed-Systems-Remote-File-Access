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

using namespace std;

class Client
{
private:
    int clientPort;
    string serverAddress;
    int serverPort;

    Handler *handler;
    UserInputReader *inputReader;
    bool isMonitoring;

    struct CacheEntry
    {
        string content;
        chrono::time_point<chrono::system_clock> lastModified;
    };

    chrono::seconds freshnessInterval;
    void startServices();
    void startRead(string requestType);
    void startInsert(string requestType);
    void startMonitor(string requestType);
    void startIdempotent(string requestType);
    void startNonIdempotent(string requestType);
    void processReplyFromServer(string message);
    string concatenateFromIndex(vector<string> &elements, int startIndex, string delimiter);
    std::unordered_map<std::string, CacheEntry> cache;

public:
    Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES);

    void startConnection();
};

#endif
