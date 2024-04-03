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

    struct CacheEntry
    {
        string content;
        long long Tc;
        long long Tmclient;
    };

    long freshnessInterval;
    void startServices();
    void startRead(string requestType);
    void startInsert(string requestType);
    void startMonitor(string requestType);
    void startDelete(string requestType);
    void startAppend(string requestType);
    void processReplyFromServer(string message);
    void monitorTimer(long monitorMinutes);
    string concatenateFromIndex(vector<string> &elements, int startIndex, string delimiter);
    std::unordered_map<std::string, CacheEntry> cache;

public:
    Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, double MONITORING_PACKET_RECV_LOSS_PROB,int MAX_RETRIES, long freshnessInterval);

    void startConnection();
    void printCacheContent();
};

#endif
