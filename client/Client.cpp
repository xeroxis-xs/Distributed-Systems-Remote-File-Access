#include "Client.hpp"
#include "Handler.hpp"

#include <algorithm>
#include <iomanip>
#include <sstream>
using std::replace;

Client::Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES, long freshnessInterval)
{
    this->clientPort = clientPort;
    this->serverAddress = serverAddress;
    this->serverPort = serverPort;
    this->freshnessInterval = freshnessInterval;
    handler = new Handler(BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB, MAX_RETRIES);
    inputReader = new UserInputReader();
    isMonitoring = false;
}

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
        cout << "| [4] Idempotent service                |" << endl;
        cout << "| [5] Non-idempotent service            |" << endl;
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
            startIdempotent("4");
            break;
        case 5:
            startNonIdempotent("5");
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
}

void Client::startRead(string requestType)
{
    cout << "\nEnter the pathname of the target file to be read: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    cout << "\nEnter the offset of the file content (in bytes) to read from: ";
    cout << "\nE.g. 0" << endl;
    long offset = inputReader->getLong();

    cout << "\nEnter the number of bytes to read: ";
    cout << "\nE.g. 2" << endl;
    int bytesToRead = inputReader->getInt();

    cout << "You have selected to read " << bytesToRead << " bytes from " << pathname << " starting from byte " << offset << "." << endl;
    string pathnameOffsetBytesToReady = pathname + ":" + to_string(offset) + ":" + to_string(bytesToRead);
    string requestContent = requestType + ":" + pathnameOffsetBytesToReady;

    if (cache.find(pathnameOffsetBytesToReady) != cache.end())
    {
        auto &entry = cache[pathnameOffsetBytesToReady];
        auto currentTime = system_clock::now();
        system_clock::duration epochTime = currentTime.time_since_epoch();
        milliseconds milliseconds = duration_cast<chrono::milliseconds>(epochTime);

        long long millisecondsCount = milliseconds.count();

        auto timeSinceLastValidated = millisecondsCount - entry.Tc;

        if (timeSinceLastValidated < (freshnessInterval * 1000))
        {
            // Content is fresh, retrieve from cache
            std::cout << "Reading from client cache..." << std::endl;
            std::cout << "Content : " << entry.content << std::endl;
        }
        else
        {
            // Issue getattr call to server to obtain Tmserver
            string requestTmserverContent = "6:" + pathnameOffsetBytesToReady;
            string replyFromServer = handler->sendOverUDP(requestTmserverContent);
            processReplyFromServer(replyFromServer);
        }
    }
    else
    {
        cout << "requestContent send to Server: " << requestContent << endl;

        // Send request and receive reply from server
        string replyFromServer = handler->sendOverUDP(requestContent);

        // Process reply from server
        processReplyFromServer(replyFromServer);
    }
}

void Client::startInsert(string requestType)
{
    cout << "\nEnter the pathname of the target file to insert into: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    cout << "\nEnter the offset of the file content (in bytes) to insert from: ";
    cout << "\nE.g. 0" << endl;
    long offset = inputReader->getLong();

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

void Client::startMonitor(string requestType)
{
    cout << "\nEnter the pathname of the target file to be monitored: ";
    cout << "\nE.g. server/storage/hello.txt" << endl;
    string pathname = inputReader->getString();

    cout << "\nEnter the duration (in minutes) you would like to receive updates for: ";
    cout << "\nE.g. 1" << endl;
    long monitorMinutes = inputReader->getLong();

    cout << "You have selected to monitor " << pathname << " for " << monitorMinutes << " minute(s)" << endl;
    string requestContent = requestType + ":" + pathname + ":" + to_string(monitorMinutes);

    // Send request and receive reply from server
    string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);

    while (isMonitoring)
    {
        // Receives monitoring updates from server
        string monitorFromServer = handler->monitorOverUDP();
        // Process monitoring reply from server
        processReplyFromServer(monitorFromServer);
    }
}

void Client::startIdempotent(string requestType)
{
    // Implementation for idempotent service
}

void Client::startNonIdempotent(string requestType)
{
    // Implementation for non-idempotent service
}

void Client::processReplyFromServer(string message)
{
    // Splitting the message into parts
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
        cout << "\nRead request successful: " << replyContents << endl;
        auto currentTime = system_clock::now();
        system_clock::duration epochTime = currentTime.time_since_epoch();
        milliseconds milliseconds = duration_cast<chrono::milliseconds>(epochTime);

        long long millisecondsCountCurrentTime = milliseconds.count();
        string pathName = messageParts[5];

        string offSet = messageParts[6];
        string bytesToRead = messageParts[7];
        string tmserver = messageParts[8];

        replyContents = concatenateFromIndex(messageParts, 9, ":");

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
        isMonitoring = false;
    }
    else if (replyType == "3e3")
    {
        cout << "\nMonitor request failed: " << replyContents << endl;
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

    printCacheContent();
}

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