#include "Client.hpp"

int main(int argc, char *argv[])
{
    string serverAddress = "127.0.0.1";
    int serverPort = 12345;
    int clientPort = 65535;
    double PACKET_SEND_LOSS_PROB = 0;
    double PACKET_RECV_LOSS_PROB = 0;
    double MONITORING_PACKET_RECV_LOSS_PROB = 0;
    int BUFFER_SIZE = 1024;
    int MAX_RETRIES = 10;
    long freshnessInterval;

    if (argc > 1)
    {
        try
        {
            if (argc == 2)
            {
                serverAddress = argv[1];
            }
            else if (argc == 3)
            {
                serverAddress = argv[1];
                serverPort = stoi(argv[2]);
            }
            else if (argc == 4)
            {
                serverAddress = argv[1];
                serverPort = stoi(argv[2]);
                clientPort = stoi(argv[3]);
            }
            else if (argc == 5)
            {
                serverAddress = argv[1];
                serverPort = stoi(argv[2]);
                clientPort = stoi(argv[3]);
                freshnessInterval = stoi(argv[4]);
            }
            else
            {
                cerr << "Invalid arguments. Please try again." << endl;
                return 1;
            }
        }
        catch (const exception &e)
        {
            cerr << "Invalid arguments. Please try again." << endl;
            return 1;
        }
    }

    cout << "Initialising: Server address set to " << serverAddress << endl;
    cout << "Initialising: Server port set to " << serverPort << endl;

    Client client(clientPort, serverAddress, serverPort, BUFFER_SIZE, PACKET_SEND_LOSS_PROB, PACKET_RECV_LOSS_PROB,MONITORING_PACKET_RECV_LOSS_PROB, MAX_RETRIES, freshnessInterval);
    client.startConnection();

    return 0;
}