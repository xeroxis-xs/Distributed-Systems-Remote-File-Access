#include <iostream>
#include <fstream>
#include <string>
#include <WinSock2.h>


#define SERVER_PORT 8080
#define BUFFER_SIZE 1024

void writeFileFromBytes(const char *filename, char *data, int size) {
    std::ofstream outFile(filename, std::ios::binary);
    outFile.write(data, size);
    outFile.close();
}

int main() { 


    
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup failed" << std::endl;
        return 1;
    }
    SOCKET sockfd;
    char buffer[BUFFER_SIZE];
    sockaddr_in serverAddr, clientAddr;
    int addrLen = sizeof(clientAddr);

    // Create UDP socket
    sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (sockfd == INVALID_SOCKET) {
        std::cerr << "Error creating socket: " << WSAGetLastError() << std::endl;
        WSACleanup();
        return 1;
    }

    // Initialize server address struct
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    serverAddr.sin_port = htons(SERVER_PORT);

    // Bind to the specified address
    if (bind(sockfd, (sockaddr *) &serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "Error binding socket: " << WSAGetLastError() << std::endl;
        closesocket(sockfd);
        WSACleanup();
        return 1;
    }

    std::cout << "Server started. Listening for incoming files..." << std::endl;

    while (true) {
        int numBytesReceived = recvfrom(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *) &clientAddr, &addrLen);
        if (numBytesReceived < 0) {
            std::cerr << "Error receiving data" << std::endl;
            return 1;
        }

        // Extract filename from client's message
        std::string filename(buffer, numBytesReceived);

        // Remove trailing newline character
        filename.pop_back();

        std::cout << "Received file: " << filename << std::endl;

        // Write received data to a file
        writeFileFromBytes(filename.c_str(), buffer, numBytesReceived);
    }

    
    closesocket(sockfd);
    WSACleanup();
    return 0;
}