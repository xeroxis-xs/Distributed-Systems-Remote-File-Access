package client;


public class Client {

    public static void main(String[] args) {

        ClientUI ui = new ClientUI();
        ui.startClient();

        while (ui.isRunning) {
            while (ui.isConnected) {
                ui.clientHandler.receiveOverUDP();
            }
        }

    }

}
