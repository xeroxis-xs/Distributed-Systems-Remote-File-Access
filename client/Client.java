package client;
import java.io.*;
import java.net.*;


public class Client {
    private static final int SERVER_PORT = 12345;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        // if (args.length != 1) {
        //     System.out.println("Usage: java FileClient <file_name>");
        //     return;
        // }

        ClientUI ui = new ClientUI();
        ui.startClient();

        String fileName = args[0];
        try {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            // Open UDP Socket
            DatagramSocket socket = new DatagramSocket();

            // Request
            byte[] requestData = marshall((byte) 0, fileName);
            DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);

            // Receive
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            byte[] replyData = receivePacket.getData();
            byte statusCode = replyData[0];
            int dataLength = bytesToInt(replyData, 1);
            byte[] fileData = new byte[dataLength];
            System.arraycopy(replyData, 5, fileData, 0, dataLength);

            if (statusCode == 0) {
                try (FileOutputStream fos = new FileOutputStream("downloaded_" + fileName)) {
                    fos.write(fileData);
                    System.out.println("File downloaded successfully.");
                }
            } else {
                System.out.println("File not found on server.");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] marshall(byte requestType, String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(requestType);
        byte[] fileNameBytes = fileName.getBytes();
        baos.write((fileNameBytes.length >> 8) & 0xFF);
        baos.write(fileNameBytes.length & 0xFF);
        try {
            baos.write(fileNameBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) << 24 |
                (bytes[offset + 1] & 0xFF) << 16 |
                (bytes[offset + 2] & 0xFF) << 8 |
                bytes[offset + 3] & 0xFF;
    }
}
