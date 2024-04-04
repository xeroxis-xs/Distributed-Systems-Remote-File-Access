/**
 * @file Handler.cpp
 * @brief Implementation of the Handler class for communication with a server.
 *
 * The Handler class provides functionality for managing communication parameters,
 * generating request IDs, and connecting to a server.
 */
#include "Handler.hpp"

Handler::Handler(int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, double MONITORING_PACKET_RECV_LOSS_PROB, int MAX_RETRIES)
    : BUFFER_SIZE(BUFFER_SIZE), PACKET_SEND_LOSS_PROB(PACKET_SEND_LOSS_PROB),
      PACKET_RECV_LOSS_PROB(PACKET_RECV_LOSS_PROB), MONITORING_PACKET_RECV_LOSS_PROB(MONITORING_PACKET_RECV_LOSS_PROB) ,MAX_RETRIES(MAX_RETRIES), requestIdCounter(0)
{
    clientAddress = getClientAddress();
}

/**
 * @brief Retrieves the client's IP address.
 * @return The client's IP address.
 *
 * This initializes the Windows Sockets API, retrieves the computer's hostname,
 * and resolves it to an IP address. It then returns the IP address as a string.
 * If any errors occur during this process, appropriate error messages are displayed.
 */
string Handler::getClientAddress()
{
    WSADATA wsaData;
    int result = WSAStartup(MAKEWORD(2, 2), &wsaData);

    if (result != 0)
    {
        cerr << "WSAStartup failed: " << result << endl;
        return "1";
    }
    char buffer[256];
    char computerName[MAX_COMPUTERNAME_LENGTH + 1];
    DWORD size = sizeof(computerName);

    if (GetComputerNameA(computerName, &size))
    {
        cout << "Hostname: " << computerName << endl;
    }
    else
    {
        cerr << "Failed to get hostname." << endl;
        exit(EXIT_FAILURE);
    }
    struct addrinfo *resultAddr = nullptr;
    struct addrinfo hints;

    ZeroMemory(&hints, sizeof(hints));
    hints.ai_family = AF_INET; // IPv4
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;

    result = getaddrinfo(computerName, nullptr, &hints, &resultAddr);
    if (result != 0)
    {
        cerr << "getaddrinfo failed: " << result << endl;
        WSACleanup();
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in *addr = reinterpret_cast<struct sockaddr_in *>(resultAddr->ai_addr);
    char ipStr[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &(addr->sin_addr), ipStr, INET_ADDRSTRLEN);

    cout << "Host IP Address: " << ipStr << endl;

    freeaddrinfo(resultAddr);
    WSACleanup();
    return ipStr;
}

/**
 * @brief Generates a unique request ID.
 * @param clientAddress The client's IP address.
 * @param clientPort The client's port number.
 * @return The generated request ID.
 *
 * This constructs a request ID by incrementing a counter and combining it with the client's
 * IP address and port number. The resulting request ID is unique for each request.
 */
string Handler::generateRequestId(string clientAddress, int clientPort)
{
    return to_string(this->requestIdCounter++) + ":" + clientAddress + ":" + to_string(clientPort);
}

/**
 * @brief Connects to the server.
 * @param serverAddress The server's IP address.
 * @param serverPort The server's port number.
 *
 * This method initializes the server address structure with the specified IP address and port number.
 * It indicates a successful connection by printing a message to the console.
 */
void Handler::connectToServer(string serverAddress, int serverPort)
{
    this->serverAddress.sin_family = AF_INET;
    this->serverAddress.sin_addr.s_addr = inet_addr(serverAddress.c_str());
    this->serverAddress.sin_port = htons(serverPort);
    cout << "\nSuccessfully connected to " << serverAddress << ":" << serverPort << endl;
}

/**
 * @brief Opens a port for communication.
 * @param clientPort The client's port number.
 *
 * This method initializes the Windows Sockets API, creates a socket, binds it to the specified
 * client port, and listens for incoming connections. If any errors occur during this process,
 * appropriate error messages are displayed, and the program exits.
 */
void Handler::openPort(int clientPort)
{
    try
    {
        this->clientPort = clientPort;

        WSADATA wsaData;
        if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
        {
            throw runtime_error("WSAStartup failed");
        }

        this->socketDescriptor = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
        if (this->socketDescriptor == INVALID_SOCKET)
        {
            throw runtime_error("Error creating socket");
        }

        SOCKADDR_IN clientAddress;
        clientAddress.sin_family = AF_INET;
        clientAddress.sin_addr.s_addr = htonl(INADDR_ANY);
        clientAddress.sin_port = htons(clientPort);

        if (bind(this->socketDescriptor, reinterpret_cast<SOCKADDR *>(&clientAddress), sizeof(clientAddress)) == SOCKET_ERROR)
        {
            throw runtime_error("Bind failed");
        }

        cout << "Client port listening at " << clientPort << endl;
    }
    catch (const exception &e)
    {
        cerr << e.what() << endl;
        closesocket(this->socketDescriptor);
        WSACleanup();
        exit(EXIT_FAILURE);
    }
}

/**
 * @brief Disconnects from the server.
 *
 * This method closes the socket descriptor and cleans up the Windows Sockets API.
 */
void Handler::disconnect()
{
    closesocket(this->socketDescriptor);
    WSACleanup();
}

/**
 * @brief Sends data over UDP to the server.
 * @param requestContent The content to be sent.
 * @return The unmarshalled data received from the server.
 *
 * This method constructs a message payload by combining the message type, request ID,
 * and request content. It then marshals the data into a byte array and sends it over UDP.
 * If a simulated message loss occurs (based on the PACKET_SEND_LOSS_PROB), an empty string is returned.
 * Otherwise, the method receives data over UDP and returns the unmarshalled data.
 */
string Handler::sendOverUDP(string requestContent)
{
    string unmarshalledData = "";
    try
    {
        // Create the message payload structure
        string messageType = "0";
        string requestId = this->generateRequestId(this->clientAddress, this->clientPort);
        string message = messageType + ":" + requestId + ":" + requestContent;

        // Marshal the data into a byte array
        vector<char> marshalledData = Marshaller::marshal(message);


        double random = getRandomDouble();
        cout << "Random: "<< random << endl;
        if (random < PACKET_SEND_LOSS_PROB){
            cout << "***** Simulating sending message loss from client *****" << endl;
        }
        else {
             // Send data over UDP
            int bytesSent = sendto(this->socketDescriptor, marshalledData.data(), marshalledData.size(), 0,
                                (SOCKADDR *)&this->serverAddress, sizeof(this->serverAddress));
            if (bytesSent == SOCKET_ERROR)
            {
                cerr << "Send failed with error: " << WSAGetLastError() << endl;
                return "";
            }
        }

        // Receive over UDP
        unmarshalledData = this->receiveOverUDP(this->socketDescriptor, marshalledData);
    }
    catch (exception &e)
    {
        cerr << "\nAn error occurred: " << e.what() << endl;
    }
    return unmarshalledData;
}


/**
 * @brief Receives data over UDP from the server.
 * @param socket The socket descriptor.
 * @param marshalledData The marshalledData sent previously.
 * @return The unmarshalled data received from the server.
 *
 * This method sets a timeout for 5 seconds, receives data from the server over UDP,
 * and unmarshals the received data into a string. If a simulated message loss occurs
 * (based on the PACKET_RECV_LOSS_PROB), an empty string is returned.
 */
string Handler::receiveOverUDP(SOCKET socket,  vector<char> marshalledData)

{
    string unmarshalledData;
    string unmarshalledDataAssign;
    int timeout = 5000;

    // Prepare a byte buffer to store received data
    vector<char> buffer(BUFFER_SIZE);

    // Create a sockaddr structure for the server address
    SOCKADDR_IN serverAddr;
    int serverAddrLen = sizeof(serverAddr);

    int retries = 0;
    while (retries < MAX_RETRIES)
    {
        try
        {
            // Set timeout for 5 seconds
            setsockopt(socket, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout));

            srand(time(NULL));
            double randNum = static_cast<double>(rand()) / RAND_MAX;



            // Receive data from server over UDP
            int bytesReceived = recvfrom(socket, buffer.data(), buffer.size(), 0, (SOCKADDR *)&serverAddr, &serverAddrLen);
            if (bytesReceived == SOCKET_ERROR)
            {
                throw runtime_error("Receive failed with error: " + GetWSAErrorMessage(WSAGetLastError()));
            }
            else {
                if (randNum < PACKET_RECV_LOSS_PROB)
                {
                    cout << "\n***** Simulating receiving message loss from server *****" << endl;
                    break; // Simulate message loss by not receiving the packet
                }
                // Unmarshal the data into a String
                unmarshalledData = Marshaller::unmarshal(buffer);

                cout << "\nRaw Message from Server: " << unmarshalledData << endl;
            }



            break;
        }
        catch (exception &e)
        {
            cerr << "\nAn error occurred: " << e.what() << endl;
            retries++;
            // Reend data over UDP
            int bytesSent = sendto(this->socketDescriptor, marshalledData.data(), marshalledData.size(), 0,
                                (SOCKADDR *)&this->serverAddress, sizeof(this->serverAddress));
            if (bytesSent == SOCKET_ERROR)
            {
                cerr << "Send failed with error: " << WSAGetLastError() << endl;
                return "";
            }
            cerr << "Retransmitting ("<< retries<<")..." << endl;
        }
    }

    return unmarshalledData;
}

/**
 * @brief Monitors data over UDP from the server.
 * @return The unmarshalled data received from the server during monitoring.
 *
 * This method removes any timeout since it is a blocking operation, receives data from the server over UDP,
 * and unmarshals the received data into a string. If a simulated message loss occurs (based on the MONITORING_PACKET_RECV_LOSS_PROB),
 * an empty string is returned. Otherwise, the method returns the unmarshalled data.
 */
string Handler::monitorOverUDP()
{
    string unmarshalledData;
    int timeout = 1000;

    // Prepare a byte buffer to store received data
    vector<char> buffer(BUFFER_SIZE);

    // Create a sockaddr structure for the server address
    SOCKADDR_IN serverAddr;
    int serverAddrLen = sizeof(serverAddr);

    try
    {

        // Remove any timeout since it is a blocking operation
        setsockopt(this->socketDescriptor, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout));

        // srand(time(NULL));
        // double randNum = static_cast<double>(rand()) / RAND_MAX;


        // Receive data from server over UDP
        int bytesReceived = recvfrom(this->socketDescriptor, buffer.data(), buffer.size(), 0, (SOCKADDR *)&serverAddr, &serverAddrLen);
        if (bytesReceived == SOCKET_ERROR)
        {
            return "Monitoring: No update for the past 1 sec...";
        }
        // else
        // {
        //     if (randNum < MONITORING_PACKET_RECV_LOSS_PROB)
        //     {
        //         cout << "\n***** Simulating receiving message loss from server while monitoring *****" << endl;
        //         return ""; // Simulate message loss by not receiving the packet
        //     }
        //     // Unmarshal the data into a String
        //     unmarshalledData = Marshaller::unmarshal(buffer);

        //     cout << "Raw Message from Server: " << unmarshalledData << endl;
        // }

    }
    catch (exception &e)
    {
        cerr << "\nAn error occurred: " << e.what() << endl;
    }

    return unmarshalledData;
}

/**
 * @brief Retrieves the error message associated with a Windows Sockets API error code.
 * @param errorCode The error code to look up.
 * @return The error message corresponding to the error code.
 *
 * This method uses the Windows Sockets API function FormatMessageA to retrieve the error message
 * associated with the specified error code. If the message cannot be retrieved, it returns "Unknown error".
 */
string Handler::GetWSAErrorMessage(int errorCode)
{
    std::string errorMessage;
    LPSTR messageBuffer = nullptr;

    FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                   nullptr,
                   errorCode,
                   MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                   reinterpret_cast<LPSTR>(&messageBuffer),
                   0,
                   nullptr);

    if (messageBuffer)
    {
        errorMessage = messageBuffer;
        LocalFree(messageBuffer);
    }
    else
    {
        errorMessage = "Unknown error";
    }

    return errorMessage;
}

double Handler::getRandomDouble() {
    // Create a random device
    std::random_device rd;

    // Create a random number generator
    std::mt19937 gen(rd());

    // Create a uniform distribution between 0 and 1
    std::uniform_real_distribution<double> dis(0.0, 1.0);

    // Generate and return a random double
    return dis(gen);
}