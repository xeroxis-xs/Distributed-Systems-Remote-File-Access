package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Server's IP address

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            File fileToSend = new File("client/downloads/hello.txt");
            byte[] fileBytes = readFileToBytes(fileToSend);

            DatagramPacket sendPacket = new DatagramPacket(fileBytes, fileBytes.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            System.out.println("File sent to server.");

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readFileToBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fis.read(fileBytes);
        fis.close();
        return fileBytes;
    }
}
