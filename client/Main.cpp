#include <string>
#include <iostream>

int main(int argc, char* argv[]) {
    std::string serverAddress = "127.0.0.1";
    int serverPort = 12345;
    int clientPort = 65535;
    double PACKET_SEND_LOSS_PROB = 0;
    double PACKET_RECV_LOSS_PROB = 0;
    int BUFFER_SIZE = 1024;
    int MAX_RETRIES = 10;

    if (argc > 1) {
        try {
            if (argc == 2) {
                serverAddress = argv[1];
            }
            else if (argc == 3) {
                serverAddress = argv[1];
                serverPort = std::stoi(argv[2]);
            }
            else if (argc == 4) {
                serverAddress = argv[1];
                serverPort = std::stoi(argv[2]);
                clientPort = std::stoi(argv[3]);
            }
            else {
                std::cerr << "Invalid arguments. Please try again." << std::endl;
                return 1;
            }
        }
        catch (const std::exception& e) {
            std::cerr << "Invalid arguments. Please try again." << std::endl;
            return 1;
        }
    }

    std::cout << "Initialising: Server address set to " << serverAddress << std::endl;
    std::cout << "Initialising: Server port set to " << serverPort << std::endl;

    Client client(clientPort, serverAddress, serverPort, BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB, MAX_RETRIES);
    client.startConnection();

    return 0;
}