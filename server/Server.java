package server;

public class Server {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        ServerHandler serverHandler = new ServerHandler(PORT, BUFFER_SIZE);

        try {
            while (true) {
                serverHandler.receiveOverUDP();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
