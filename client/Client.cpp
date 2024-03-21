#include "Client.hpp"
#include "Handler.hpp"

#include <iostream>

Client::Client(int clientPort, string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES)
{
    this->clientPort = clientPort;
    this->serverAddress = serverAddress;
    this->serverPort = serverPort;
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
    string requestContent = requestType + ":" + pathname + ":" + to_string(offset) + ":" + to_string(bytesToRead);

    cout << "requestContent send to Server: " << requestContent << endl;

    // Send request and receive reply from server
    string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);
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
        cout << "Read request successful: " << replyContents << endl;
    }
    else if (replyType == "1e1" || replyType == "1e2" || replyType == "1e3" || replyType == "1e4" ||
             replyType == "2" || replyType == "2e1" || replyType == "2e2" || replyType == "2e3" || replyType == "2e4")
    {
        cout << "Read/Insert request failed: " << replyContents << endl;
    }
    else if (replyType == "3")
    {
        cout << "Monitor request successful: " << replyContents << endl;
        isMonitoring = true;
    }
    else if (replyType == "3e1")
    {
        cout << "Monitor update: " << replyContents << endl;
    }
    else if (replyType == "3e2")
    {
        cout << "Monitor request ended: " << replyContents << endl;
        isMonitoring = false;
    }
    else if (replyType == "3e3")
    {
        cout << "Monitor request failed: " << replyContents << endl;
    }
    else
    {
        cout << "Other server reply: " << replyContents << endl;
    }

    ConsoleUI::displaySeparator('=', 41);
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