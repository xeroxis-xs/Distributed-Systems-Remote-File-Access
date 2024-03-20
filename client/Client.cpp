#include "Client.hpp"
#include "Handler.hpp"

#include <iostream>


Client::Client(int clientPort, std::string serverAddress, int serverPort, int BUFFER_SIZE, double PACKET_SEND_LOSS_PROB, double PACKET_RECV_LOSS_PROB, int MAX_RETRIES) {
    this->clientPort = clientPort;
    this->serverAddress = serverAddress;
    this->serverPort = serverPort;
    handler = new Handler(BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB, MAX_RETRIES);
    inputReader = new UserInputReader();
    isMonitoring = false;
}

void Client::startConnection() {
    try {
        handler->connectToServer(serverAddress, serverPort);
        handler->openPort(clientPort);
        startServices();
    }
    catch (std::exception& e) {
        std::cout << "\nConnection to " << serverAddress << " failed. Please try again." << std::endl;
        std::cout << e.what() << std::endl;
        exit(1);
    }
}


void Client::startServices() {
    int choice;
    do {
        std::cout << "\n";
        std::cout << "+---------------------------------------+" << std::endl;
        std::cout << "|    Welcome to Remote File Service!    |" << std::endl;
        std::cout << "+---------------------------------------+" << std::endl;
        std::cout << "|                                       |" << std::endl;
        std::cout << "| [1] Read a content from a file        |" << std::endl;
        std::cout << "| [2] Insert a content into a file      |" << std::endl;
        std::cout << "| [3] Monitor updates of a file         |" << std::endl;
        std::cout << "| [4] Idempotent service                |" << std::endl;
        std::cout << "| [5] Non-idempotent service            |" << std::endl;
        std::cout << "| [6] Exit                              |" << std::endl;
        std::cout << "|                                       |" << std::endl;
        std::cout << "+---------------------------------------+" << std::endl;
        std::cout << "\nEnter your choice: ";
        choice = inputReader->getInt();

        switch (choice) {
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
                std::cout << "Exiting..." << std::endl;
                exit(0);
                break;
            default:
                std::cout << "Invalid choice. Please try again." << std::endl;
                break;
        }
    } while (choice != 6);
}

void Client::startRead(std::string requestType) {
    std::cout << "\nEnter the pathname of the target file to be read: ";
    std::cout << "\nE.g. server/storage/hello.txt" << std::endl;
    std::string pathname = inputReader->getString();

    std::cout << "\nEnter the offset of the file content (in bytes) to read from: ";
    std::cout << "\nE.g. 0" << std::endl;
    long offset = inputReader->getLong();

    std::cout << "\nEnter the number of bytes to read: ";
    std::cout << "\nE.g. 2" << std::endl;
    int bytesToRead = inputReader->getInt();

    std::cout << "You have selected to read " << bytesToRead << " bytes from " << pathname << " starting from byte " << offset << "." << std::endl;
    std::string requestContent = requestType + ":" + pathname + ":" + std::to_string(offset) + ":" + std::to_string(bytesToRead);

    // Send request and receive reply from server
    std::string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);
}

void Client::startInsert(std::string requestType) {
    std::cout << "\nEnter the pathname of the target file to insert into: ";
    std::cout << "\nE.g. server/storage/hello.txt" << std::endl;
    std::string pathname = inputReader->getString();

    std::cout << "\nEnter the offset of the file content (in bytes) to insert from: ";
    std::cout << "\nE.g. 0" << std::endl;
    long offset = inputReader->getLong();

    std::cout << "\nEnter the content to be inserted into the file: ";
    std::cout << "\nE.g. abc" << std::endl;
    std::string stringToInsert = inputReader->getString();

    std::cout << "You have selected to insert '" << stringToInsert << "' into " << pathname << " starting from byte " << offset << "." << std::endl;
    std::string requestContent = requestType + ":" + pathname + ":" + std::to_string(offset) + ":" + stringToInsert;

    // Send request and receive reply from server
    std::string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);
}


void Client::startMonitor(std::string requestType) {
    std::cout << "\nEnter the pathname of the target file to be monitored: ";
    std::cout << "\nE.g. server/storage/hello.txt" << std::endl;
    std::string pathname = inputReader->getString();

    std::cout << "\nEnter the duration (in minutes) you would like to receive updates for: ";
    std::cout << "\nE.g. 1" << std::endl;
    long monitorMinutes = inputReader->getLong();

    std::cout << "You have selected to monitor " << pathname << " for " << monitorMinutes << " minute(s)" << std::endl;
    std::string requestContent = requestType + ":" + pathname + ":" + std::to_string(monitorMinutes);

    // Send request and receive reply from server
    std::string replyFromServer = handler->sendOverUDP(requestContent);

    // Process reply from server
    processReplyFromServer(replyFromServer);

    while (isMonitoring) {
        // Receives monitoring updates from server
        std::string monitorFromServer = handler->monitorOverUDP();
        // Process monitoring reply from server
        processReplyFromServer(monitorFromServer);
    }
}

void Client::processReplyFromServer(std::string message) {
    // Splitting the message into parts
    std::vector<std::string> messageParts;
    std::string delimiter = ":";
    size_t pos = 0;
    while ((pos = message.find(delimiter)) != std::string::npos) {
        messageParts.push_back(message.substr(0, pos));
        message.erase(0, pos + delimiter.length());
    }
    messageParts.push_back(message); // Push the last part

    // Checking if we have enough parts to proceed
    if (messageParts.size() < 5) {
        std::cerr << "Invalid message format." << std::endl;
        return;
    }

    std::string messageType = messageParts[0];
    std::string replyCounter = messageParts[1];
    std::string serverAddress = messageParts[2];
    std::string serverPort = messageParts[3];
    std::string replyType = messageParts[4];
    std::string replyContents = concatenateFromIndex(messageParts, 5, ":");

    ConsoleUI::displaySeparator('=', 41);

    // Switch case for different reply types
    if (replyType == "1") {
        std::cout << "Read request successful: " << replyContents << std::endl;
    } else if (replyType == "1e1" || replyType == "1e2" || replyType == "1e3" || replyType == "1e4" ||
               replyType == "2" || replyType == "2e1" || replyType == "2e2" || replyType == "2e3" || replyType == "2e4") {
        std::cout << "Read/Insert request failed: " << replyContents << std::endl;
    } else if (replyType == "3") {
        std::cout << "Monitor request successful: " << replyContents << std::endl;
        isMonitoring = true;
    } else if (replyType == "3e1") {
        std::cout << "Monitor update: " << replyContents << std::endl;
    } else if (replyType == "3e2") {
        std::cout << "Monitor request ended: " << replyContents << std::endl;
        isMonitoring = false;
    } else if (replyType == "3e3") {
        std::cout << "Monitor request failed: " << replyContents << std::endl;
    } else {
        std::cout << "Other server reply: " << replyContents << std::endl;
    }

    ConsoleUI::displaySeparator('=', 41);
}

string Client::concatenateFromIndex(vector<string> elements, int startIndex, string delimiter) {
    std::string result;
    for (int i = startIndex; i < elements.size(); ++i) {
        result += elements[i];
        if (i < elements.size() - 1)
            result += delimiter;
    }
    return result;
}