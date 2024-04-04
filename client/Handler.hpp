/**
 * @file Handler.hpp
 * @brief Implementation of the Handler class for communication with a server.
 *
 * The Handler class provides functionality for managing communication parameters,
 * generating request IDs, and connecting to a server.
 */
#ifndef HANDLER_HPP
#define HANDLER_HPP

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <vector>
#include <atomic>
#include <Windows.h>
#include <WS2tcpip.h>
#include <random>
#include "../utils/Marshaller.hpp"

#pragma comment(lib, "Ws2_32.lib")

using namespace std;

/**
 * @brief Represents a network handler for communication over UDP.
 *
 * The `Handler` class provides methods for connecting to a server, sending and receiving data over UDP,
 * and monitoring network activity. It encapsulates functionality related to network communication.
 */
class Handler
{
private:
    int BUFFER_SIZE;
    double PACKET_SEND_LOSS_PROB;
    double PACKET_RECV_LOSS_PROB;
    double MONITORING_PACKET_RECV_LOSS_PROB;
    int MAX_RETRIES;
    string clientAddress;
    int clientPort;
    atomic<int> requestIdCounter;
    SOCKADDR_IN serverAddress;
    SOCKET socketDescriptor;

public:
    /**
     * @brief Constructs a new `Handler` object with specified parameters.
     *
     * @param BUFFER_SIZE The buffer size for data transmission.
     * @param PACKET_SEND_LOSS_PROB The probability of packet loss during sending.
     * @param PACKET_RECV_LOSS_PROB The probability of packet loss during reception.
     * @param MONITORING_PACKET_RECV_LOSS_PROB The probability of packet loss during monitoring.
     * @param MAX_RETRIES The maximum number of retries for communication.
     */
    Handler(int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB,double MONITORING_PACKET_RECV_LOSS_PROB, int MAX_RETRIES);

    /**
     * @brief Gets the client's IP address.
     *
     * @return The client's IP address as a string.
     */
    string getClientAddress();

    /**
     * @brief Generates a unique request ID based on client address and port.
     *
     * @param clientAddress The client's IP address.
     * @param clientPort The client's port number.
     * @return A unique request ID.
     */
    string generateRequestId(string clientAddress, int clientPort);

    /**
     * @brief Receives data over UDP from the specified socket.
     *
     * @param socket The socket descriptor.
     * @param requestContent The content to be sent.
     * @return The received data as a string.
     */
    string receiveOverUDP(SOCKET socket,  vector<char> requestContent);

    /**
     * @brief Sends data over UDP to the server.
     *
     * @param requestContent The content to be sent.
     * @return The response received from the server.
     */

    string sendOverUDP(string requestContent);

     /**
     * @brief Monitors network activity over UDP.
     *
     * @return Information about network activity.
     */
    string monitorOverUDP();

    /**
     * @brief Retrieves the error message associated with a Windows Sockets API error code.
     *
     * @param errorCode The error code.
     * @return The error message.
     */
    string GetWSAErrorMessage(int errorCode);
    double getRandomDouble();

    /**
     * @brief Connects to the server at the specified address and port.
     *
     * @param serverAddress The server's IP address.
     * @param serverPort The server's port number.
     */
    void connectToServer(string serverAddress, int serverPort);

    /**
     * @brief Opens a port for communication.
     *
     * @param clientPort The client's port number.
     */
    void openPort(int clientPort);

    /**
     * @brief Disconnects from the server.
     */
    void disconnect();
};

#endif