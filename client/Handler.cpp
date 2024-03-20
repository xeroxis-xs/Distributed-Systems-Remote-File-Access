#include "Handler.hpp"
#include "../utils/Marshaller.hpp"
#include <iostream>
#include <cstring>
#include <vector>

Handler::Handler(int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES)
    : BUFFER_SIZE(BUFFER_SIZE), PACKET_SEND_LOSS_PROB(PACKET_SEND_LOSS_PROB),
      PACKET_RECV_LOSS_PROB(PACKET_RECV_LOSS_PROB), MAX_RETRIES(MAX_RETRIES), requestIdCounter(0) {
        clientAddress = getClientAddress();
      }


string Handler::getClientAddress() {
    char buffer[256];
    if (gethostname(buffer, sizeof(buffer)) == SOCKET_ERROR) {
        std::cerr << "Error getting hostname: " << WSAGetLastError() << std::endl;
        exit(EXIT_FAILURE);
    }

    struct hostent* h = gethostbyname(buffer);
    if (h == NULL) {
        std::cerr << "Error getting host information: " << WSAGetLastError() << std::endl;
        exit(EXIT_FAILURE);
    }

    struct in_addr addr;
    memcpy(&addr, h->h_addr_list[0], h->h_length);
    return inet_ntoa(addr);
}



void Handler::connectToServer(std::string serverAddress, int serverPort) {
    this->serverAddress.sin_family = AF_INET;
    this->serverAddress.sin_addr.s_addr = inet_addr(serverAddress.c_str());
    this->serverAddress.sin_port = htons(serverPort);
    std::cout << "\nSuccessfully connected to " << serverAddress << ":" << serverPort << std::endl;
}

void Handler::openPort(int clientPort) {
    this->clientPort = clientPort;
    this->socketDescriptor = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (this->socketDescriptor == INVALID_SOCKET) {
        std::cerr << "Error creating socket: " << WSAGetLastError() << std::endl;
        exit(EXIT_FAILURE);
    }

    SOCKADDR_IN clientAddress;
    clientAddress.sin_family = AF_INET;
    clientAddress.sin_addr.s_addr = htonl(INADDR_ANY);
    clientAddress.sin_port = htons(clientPort);

    if (bind(this->socketDescriptor, (SOCKADDR*)&clientAddress, sizeof(clientAddress)) == SOCKET_ERROR) {
        std::cerr << "Bind failed with error: " << WSAGetLastError() << std::endl;
        closesocket(this->socketDescriptor);
        WSACleanup();
        exit(EXIT_FAILURE);
    }

    std::cout << "Client port listening at " << clientPort << std::endl;
}

void Handler::disconnect() {
    closesocket(this->socketDescriptor);
    WSACleanup();
}

std::string Handler::sendOverUDP(std::string requestContent) {
    std::string unmarshalledData;
    try {
        // Create the message payload structure
        std::string messageType = "0";
        std::string requestId = this->generateRequestId(this->clientAddress, this->clientPort);
        std::string message = messageType + ":" + requestId + ":" + requestContent;

        // Marshal the data into a byte array
        std::vector<char> marshalledData = Marshaller::marshal(message);

        // Send data over UDP
        int bytesSent = sendto(this->socketDescriptor, marshalledData.data(), marshalledData.size(), 0,
                               (SOCKADDR*)&this->serverAddress, sizeof(this->serverAddress));
        if (bytesSent == SOCKET_ERROR) {
            std::cerr << "Send failed with error: " << WSAGetLastError() << std::endl;
            return "";
        }

        // Receive over UDP
        unmarshalledData = this->receiveOverUDP(this->socketDescriptor);

    } catch (std::exception& e) {
        std::cerr << "\nAn error occurred: " << e.what() << std::endl;
    }
    return unmarshalledData;
}


string Handler::receiveOverUDP(SOCKET socket) {
    std::string unmarshalledData;
    int timeout = 5000;

    // Prepare a byte buffer to store received data
    std::vector<char> buffer(BUFFER_SIZE);

    // Create a sockaddr structure for the server address
    SOCKADDR_IN serverAddr;
    int serverAddrLen = sizeof(serverAddr);

    int retries = 0;
    while (retries < MAX_RETRIES) {
        try {
            // Set timeout for 5 seconds
            setsockopt(socket, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));

            // Receive data from server over UDP
            int bytesReceived = recvfrom(socket, buffer.data(), buffer.size(), 0, (SOCKADDR*)&serverAddr, &serverAddrLen);
            if (bytesReceived == SOCKET_ERROR) {
                std::cerr << "Receive failed with error: " << WSAGetLastError() << std::endl;
                return "";
            }

            // Unmarshal the data into a String
            unmarshalledData = Marshaller::unmarshal(buffer);

            std::cout << "Raw Message from Server: " << unmarshalledData << std::endl;

            break;
        } catch (std::exception& e) {
            std::cerr << "\nAn error occurred: " << e.what() << std::endl;
        }
    }

    return unmarshalledData;
}

string Handler::monitorOverUDP() {
    std::string unmarshalledData;
    int timeout = 0;

    // Prepare a byte buffer to store received data
    std::vector<char> buffer(BUFFER_SIZE);

    // Create a sockaddr structure for the server address
    SOCKADDR_IN serverAddr;
    int serverAddrLen = sizeof(serverAddr);

    try {
        // Remove any timeout since it is a blocking operation
        setsockopt(this->socketDescriptor, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));

        // Receive data from server over UDP
        int bytesReceived = recvfrom(this->socketDescriptor, buffer.data(), buffer.size(), 0, (SOCKADDR*)&serverAddr, &serverAddrLen);
        if (bytesReceived == SOCKET_ERROR) {
            std::cerr << "Receive failed with error: " << WSAGetLastError() << std::endl;
            return "";
        }

        // Unmarshal the data into a String
        unmarshalledData = Marshaller::unmarshal(buffer);

        std::cout << "Raw Message from Server: " << unmarshalledData << std::endl;
    } catch (std::exception& e) {
        std::cerr << "\nAn error occurred: " << e.what() << std::endl;
    }

    return unmarshalledData;
}