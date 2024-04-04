/**
 * @file Client.cpp
 * @brief Implementation of the Client class methods.
 *
 * The Client class provides functionality for connecting to a server, handling requests,
 * managing cache entries, and monitoring server responses.
 */
#include "Client.hpp"
#include "Handler.hpp"

#include <algorithm>
#include <iomanip>
#include <sstream>
using std::replace;

/**
 * @brief Constructor for the Client class.
 * @param clientPort The port number for the client.
 * @param serverAddress The address of the server.
 * @param serverPort The port number of the server.
 * @param BUFFER_SIZE The buffer size for communication.
 * @param PACKET_SEND_LOSS_PROB The packet send loss probability.
 * @param PACKET_RECV_LOSS_PROB The packet receive loss probability.
 * @param MONITORING_PACKET_RECV_LOSS_PROB The monitoring packet receive loss probability.
 * @param MAX_RETRIES The maximum number of retries for requests.
 * @param freshnessInterval The freshness interval for cache entries.
 */
Client::Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, double MONITORING_PACKET_RECV_LOSS_PROB, int MAX_RETRIES, long freshnessInterval)
{
    this->clientPort = clientPort;
    this->serverAddress = serverAddress;
    this->serverPort = serverPort;
    this->freshnessInterval = freshnessInterval;
    handler = new Handler(BUFFER_SIZE, PACKET_SEND_LOSS_PROB,PACKET_RECV_LOSS_PROB,MONITORING_PACKET_RECV_LOSS_PROB,  MAX_RETRIES);
    inputReader = new UserInputReader();
    isMonitoring = false;
    timerFlag = false;
}

/**
 * @brief Initiates the client connection.
 */
void Client::startConnection()
{
    try
    {
        handler->connectToServer(serverAddress, serverPort);
        handler->openPort(clientPort);
        startServices();
    }
    catch (exception &e)
    {
        cout << "\nConnection to " << serverAddress << " failed. Please try again." << endl;
        cout << e.what() << endl;
        exit(1);
    }
}

/**
 * @brief Starts various services for user to choose (read, insert, monitor, delete, append).
 */
void Client::startServices()
{
    int choice;
    do
    {
        cout << "\n";
        cout << "+---------------------------------------+" << endl;
        cout << "|    Welcome to Remote File Service!    |" << endl;
        cout << "+---------------------------------------+" << endl;
        cout << "|                                       |" << endl;
        cout << "| [1] Read a content from a file        |" << endl;
        cout << "| [2] Insert a content into a file      |" << endl;
        cout << "| [3] Monitor updates of a file         |" << endl;
        cout << "| [4] Delete a file                     |" << endl;
        cout << "| [5] Append a file to a file           |" << endl;
        cout << "| [6] Exit                              |" << endl;
        cout << "|                                       |" << endl;
        cout << "+---------------------------------------+" << endl;
        cout << "\nEnter your choice: ";
        choice = inputReader->getInt();

        switch (choice)
        {
        case 1:
            startRead("1");
            break;
        case 2:
            startInsert("2");
            break;
        case 3:
            startMonitor("3");
            break;
        case 4:
            startDelete("4");
            break;
        case 5:
            startAppend("5");
            break;
        case 6:
            cout << "Exiting..." << endl;
            exit(0);
            break;
        default:
            cout << "Invalid choice. Please try again." << endl;
            break;
        }
    } while (choice != 6);
    cout << "Program exiting ." << endl;
}

/**
 * @brief Initiates an read request.
 * @param requestType The type of request (e.g., "1").
 *
 * This method prompts the user to enter the pathname of the target file where content
 * should be read, the offset (in bytes) from which to read, and the bytes to be
 * read. It constructs a request to the server, sends it, and processes the reply
 * from the server.
 * */
void Client::startRead(string requestType)
{
    // Request the file path to be read from
    cout << "\nEnter the pathname of the target file to be read: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    // Request the offset bytes to be read from
    cout << "\nEnter the offset of the file content (in bytes) to read from: ";
    cout << "\nE.g. 0" << endl;
    long offset = inputReader->getLong();

    // Request the number of bytes to read
    cout << "\nEnter the number of bytes to read: ";
    cout << "\nE.g. 2" << endl;
    int bytesToRead = inputReader->getInt();

    cout << "You have selected to read " << bytesToRead << " bytes from " << pathname << " starting from byte " << offset << "." << endl;
    string pathnameOffsetBytesToReady = pathname + ":" + to_string(offset) + ":" + to_string(bytesToRead);
    string requestContent = requestType + ":" + pathnameOffsetBytesToReady;

    // Check if request exists in the cache
    if (cache.find(pathnameOffsetBytesToReady) != cache.end())
    {
        cout << "Found in cache!" << endl;
        auto &entry = cache[pathnameOffsetBytesToReady];
        auto currentTime = system_clock::now();
        system_clock::duration epochTime = currentTime.time_since_epoch();
        milliseconds milliseconds = duration_cast<chrono::milliseconds>(epochTime);

        long long millisecondsCount = milliseconds.count();

        auto timeSinceLastValidated = millisecondsCount - entry.Tc;

        if (timeSinceLastValidated < (freshnessInterval * 1000))
        {
            // Content is fresh, retrieve from cache
            std::cout << "Cache is still fresh" << std::endl;
            std::cout << "Reading from client cache..." << std::endl;
            std::cout << "Content : " << entry.content << std::endl;
        }
        else
        {
            // Issue getattr call to server to obtain Tmserver
            // Content is not fresh check from server
            std::cout << "Cache is not fresh, requesting Tmserver from server" << std::endl;
            string requestTmserverContent = "6:" + pathnameOffsetBytesToReady;
            string replyFromServer = handler->sendOverUDP(requestTmserverContent);
            processReplyFromServer(replyFromServer);
        }
    }
    else
    {
        cout << "Not found in cache, sending read request to server: " << requestContent << endl;

        // Send request and receive reply from server
        string replyFromServer = handler->sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }
}

/**
 * @brief Initiates an insert request.
 * @param requestType The type of request (e.g., "2").
 *
 * This method prompts the user to enter the pathname of the target file where content
 * should be inserted, the offset (in bytes) from which to insert, and the content to be
 * inserted. It constructs a request to the server, sends it, and processes the reply
 * from the server.
 */
void Client::startInsert(string requestType)
{
    // Request file path to insert content into
    cout << "\nEnter the pathname of the target file to insert into: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    // Request the offset bytes to insert from
    cout << "\nEnter the offset of the file content (in bytes) to insert from: ";
    cout << "\nE.g. 0" << endl;
    long offset = inputReader->getLong();

    // Request the content to be inserted
    cout << "\nEnter the content to be inserted into the file: ";
    cout << "\nE.g. abc" << endl;
    string stringToInsert = inputReader->getString();

    cout << "You have selected to insert '" << stringToInsert << "' into " << pathname << " starting from byte " << offset << "." << endl;
    string requestContent = requestType + ":" + pathname + ":" + to_string(offset) + ":" + stringToInsert;

    // Send request and receive reply from server
    string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);
}

/**
 * @brief Initiates monitoring of a target file.
 * @param requestType The type of request (e.g., "3").
 *
 * This method prompts the user to enter the pathname of the target file to be monitored
 * and the duration (in minutes) for which they want to receive updates. It constructs
 * a request to the server, starts a timer thread for monitoring, and continuously receives
 * monitoring updates from the server until the monitoring period ends.
 */
void Client::startMonitor(string requestType)
{   
    // Request the file path to be monitored
    cout << "\nEnter the pathname of the target file to be monitored: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    // Request the duration to be monitored for
    cout << "\nEnter the duration (in minutes) you would like to receive updates for: ";
    cout << "\nE.g. 1" << endl;
    long monitorMinutes = inputReader->getLong();

    cout << "You have selected to monitor " << pathname << " for " << monitorMinutes << " minute(s)" << endl;
    string requestContent = requestType + ":" + pathname + ":" + to_string(monitorMinutes);

    // Send request and receive reply from server
    string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);

    std::thread timerThread(&Client::monitorTimer, this, monitorMinutes);
    timerThread.detach(); // Detach the thread to run independently
    timerFlag = true;

    while (isMonitoring) {
        // Receives monitoring updates from server
        string monitorFromServer = handler->monitorOverUDP();
        // Process monitoring reply from server
        processReplyFromServer(monitorFromServer);

        if(!isMonitoring){
            break;
        }
    }
}

/**
 * @brief Initiates a delete request.
 * @param requestType The type of request (e.g., "4").
 *
 * This method prompts the user to enter the pathname of the target file to be deleted.
 * It constructs a request to the server, sends it, and processes the reply from the server.
 */
void Client::startDelete(string requestType)
{
    // Request the file path to be deleted
    cout << "\nEnter the pathname of the target file to be deleted: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    cout << "You have selected to detele " << pathname << "." << endl;
    string requestContent = requestType + ":" + pathname;

    // Send request and receive reply from server
    string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);
}

/**
 * @brief Initiates an append request.
 * @param requestType The type of request (e.g., "5").
 *
 * This method prompts the user to enter the pathname of the source file to be appended from
 * and the destination file to which the content will be appended. It constructs a request to
 * the server, sends it, and processes the reply from the server.
 */
void Client::startAppend(string requestType)
{
    // Request the source file path where content will be retrieved from
    cout << "\nEnter the pathname of the source file to be appended from: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string srcPath = inputReader->getString();

    // Request the destination file where the content will be appended to
    cout << "\nEnter the pathname of the destination file to be appended to: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string targetPath = inputReader->getString();

    cout << "You have selected to append a file from " << srcPath << " to a file at " << targetPath << "." << endl;
    string pathname = srcPath + ":" + targetPath;
    string requestContent = requestType + ":" + pathname;

    string replyFromServer = handler->sendOverUDP(requestContent);
    processReplyFromServer(replyFromServer);
}

/**
 * @brief Processes the reply received from the server.
 * @param message The reply message from the server.
 *
 * This method parses the reply message and handles different reply types based on the
 * specified format. It extracts relevant information such as message type, server address,
 * port, and reply contents. Depending on the reply type, it displays appropriate messages
 * or updates cache entries.
 */
void Client::processReplyFromServer(string message)
{
    // Splitting the message into parts
    if(message.empty()) {
        cout << "Simulated message loss.." << endl;
        return;
    }
    else if(message == "Monitoring") {
        return;
    }

    vector<string> messageParts;
    string delimiter = ":";
    size_t pos = 0;
    while ((pos = message.find(delimiter)) != string::npos)
    {
        messageParts.push_back(message.substr(0, pos));
        message.erase(0, pos + delimiter.length());
    }
    messageParts.push_back(message); // Push the last part

    // Checking if we have enough parts to proceed
    if (messageParts.size() < 5)
    {
        cerr << "Invalid message format." << endl;
        return;
    }

    string messageType = messageParts[0];
    string replyCounter = messageParts[1];
    string serverAddress = messageParts[2];
    string serverPort = messageParts[3];
    string replyType = messageParts[4];
    string replyContents = concatenateFromIndex(messageParts, 5, ":");

    ConsoleUI::displaySeparator('=', 41);

    // Switch case for different reply types
    if (replyType == "1")
    {
        replyContents = concatenateFromIndex(messageParts, 9, ":");

        cout << "\nRead request successful: " << replyContents << endl;
        auto currentTime = system_clock::now();
        system_clock::duration epochTime = currentTime.time_since_epoch();
        milliseconds milliseconds = duration_cast<chrono::milliseconds>(epochTime);

        long long millisecondsCountCurrentTime = milliseconds.count();
        string pathName = messageParts[5];

        string offSet = messageParts[6];
        string bytesToRead = messageParts[7];
        string tmserver = messageParts[8];

        CacheEntry entry;
        entry.Tc = millisecondsCountCurrentTime;

        long long Tmserver = std::stoll(tmserver);
        entry.Tmclient = Tmserver;
        entry.content = replyContents;
        string cachePathName = pathName + ":" + offSet + ":" + bytesToRead;
        cache[cachePathName] = entry;

        cout << "\nFile content is cached to client: " << endl;
    }
    else if (replyType == "1e1" || replyType == "1e2" || replyType == "1e3" || replyType == "1e4" ||
             replyType == "2" || replyType == "2e1" || replyType == "2e2" || replyType == "2e3" || replyType == "2e4")
    {
        cout << "\nRead/Insert request failed: " << replyContents << endl;
    }
    else if (replyType == "3")
    {
        cout << "\nMonitor request successful: " << replyContents << endl;
        isMonitoring = true;
    }
    else if (replyType == "3e1")
    {
        cout << "\nMonitor update: " << replyContents << endl;
    }
    else if (replyType == "3e2")
    {
        cout << "\nMonitor request ended: " << replyContents << endl;
        this->isMonitoring = false;
        this->timerFlag = false;
    }
    else if (replyType == "3e3")
    {
        cout << "\nMonitor request failed: " << replyContents << endl;
    }
    else if( replyType =="4"){
        cout << "\nDelete request successful: " << replyContents << endl;
    }
    else if (replyType == "4e1" || replyType == "4e2")
    {
        cout << "\nDelete request failed: " << replyContents << endl;
    }
    else if (replyType == "4e3")
    {
        cout << "\nMonitor stopped: " << replyContents << endl;
        this->isMonitoring = false;
        this->timerFlag = false;
    }
    else if (replyType == "5")
    {
        cout << "\nAppend successfull: " << replyContents << endl;
    }
    else if (replyType == "5e1" || replyType == "5e2" || replyType == "5e3" || replyType == "5e4")
    {
        cout << "\nAppend failed: " << replyContents << endl;
    }
    else if (replyType == "6")
    {
        cout << "\nGet Tmserver successful " << endl;
        try
        {

            string pathName = messageParts[5];
            string offSet = messageParts[6];
            string bytesToRead = messageParts[7];
            string tmserver = messageParts[8];

            auto currentTime = system_clock::now();
            system_clock::duration epochTime = currentTime.time_since_epoch();
            milliseconds milliseconds = duration_cast<chrono::milliseconds>(epochTime);

            long long millisecondsCountCurrentTime = milliseconds.count();

            long long Tmserver = std::stoll(tmserver);

            replyContents = concatenateFromIndex(messageParts, 9, ":");
            string cachePathName = pathName + ":" + offSet + ":" + bytesToRead;

            if (cache.find(cachePathName) != cache.end())
            {
                auto &entry = cache[cachePathName];

                if (entry.Tmclient == Tmserver)
                {
                    cout << "\nEntry is valid. Updating Tc to current time. " << endl;
                    // Entry is valid, retrieving from cache
                    std::cout << "Reading from client cache..." << std::endl;
                    std::cout << "Content : " << entry.content << std::endl;
                    entry.Tc = millisecondsCountCurrentTime;
                }
                else if (entry.Tmclient < Tmserver)
                {
                    cout << "\nEntry is invalidated, A request is sent to server for updated data. " << endl;

                    //Delete entry from cache as invalid.
                    cache.erase(cachePathName);

                    string requestContent = "1:" + pathName + ":" + offSet + ":" + bytesToRead;
                    string replyFromServer = handler->sendOverUDP(requestContent);

                    // Process reply from server
                    processReplyFromServer(replyFromServer);
                }
            }
        }
        catch (exception &e)
        {
            cout << "\nGet Tmserver successful failed" << endl;
            cout << e.what() << endl;
        }
    }
    else if (replyType == "6e1")
    {
        cout << "\nFile timestamp get failed: " << replyContents << endl;
    }
    else
    {
        cout << "Other server reply: " << replyContents << endl;
    }

    ConsoleUI::displaySeparator('=', 41);
    // printCacheContent();
}

/**
 * @brief Concatenates elements from a vector into a single string starting from a given index.
 * @param elements The vector of strings to concatenate.
 * @param startIndex The index from which to start concatenation.
 * @param delimiter The delimiter to use between concatenated elements.
 * @return The concatenated string.
 *
 * This method takes a vector of strings, a starting index, and a delimiter. It concatenates
 * the elements from the vector into a single string, using the specified delimiter between them.
 */
string Client::concatenateFromIndex(vector<string> &elements, int startIndex, string delimiter)
{
    string concatenatedString;
    for (int i = startIndex; i < elements.size(); ++i)
    {
        concatenatedString += elements[i];
        if (i < elements.size() - 1)
            concatenatedString += delimiter;
    }
    return concatenatedString;
}

/**
 * @brief Prints the content of the cache.
 *
 * This method iterates through the cache entries and displays relevant information,
 * including the cache key (pathname), content, and timestamps (Tc and Tmclient) in
 * local time. It provides an overview of the cached data for monitoring purposes.
 */
void Client::printCacheContent()
{
    // Iterate through the cache
    std::cout << "\n\nPrinting Cache :" << std::endl;
    ConsoleUI::displaySeparator('=', 41);
    for (const auto &entry : cache)
    {
        ConsoleUI::displaySeparator('-', 41);
        std::cout << "Key: " << entry.first << std::endl;
        std::cout << "Content: " << entry.second.content << std::endl;

        // Convert Tc to local time
        auto Tc_local = system_clock::to_time_t(system_clock::time_point(milliseconds(entry.second.Tc)));
        std::cout << "Tc (Local Time): " << std::put_time(std::localtime(&Tc_local), "%F %T") << std::endl;

        // Convert Tmclient to local time
        auto Tmclient_local = system_clock::to_time_t(system_clock::time_point(milliseconds(entry.second.Tmclient)));
        std::cout << "Tmclient (Local Time): " << std::put_time(std::localtime(&Tmclient_local), "%F %T") << std::endl;

        std::cout << std::endl;
        ConsoleUI::displaySeparator('-', 41);
    }
    ConsoleUI::displaySeparator('=', 41);
}

/**
 * @brief Monitors the specified duration and stops monitoring.
 * @param monitorMinutes The duration (in minutes) to monitor.
 *
 * This method converts the specified monitoring duration to milliseconds, adds an extra buffer time,
 * and sleeps for that duration. If the timer flag is not set (indicating manual stop), it updates
 * the `isMonitoring` flag to false and prints a message indicating the possible reason for stopping.
 */
void Client::monitorTimer(long monitorMinutes) {
    // Convert monitorMinutes to milliseconds
    long extraBufferTime = 0.25 * 60 * 1000;
    long milliseconds = (monitorMinutes * 60 * 1000) + extraBufferTime;

    // Sleep for the specified duration
    std::this_thread::sleep_for(std::chrono::milliseconds(milliseconds));

    if(!timerFlag) {

        return;
    }
    // Update the isMonitoring flag
    this->isMonitoring = false;
    cout << "\nMonitor stopped by Client: Possible reason could be loss of server packet" << endl;
}