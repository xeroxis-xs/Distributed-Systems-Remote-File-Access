package server;
import java.io.*;
import java.net.*;

public class FileServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);

            System.out.println("Server: Server started...");

            URL url = new URL("http://checkip.amazonaws.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String publicIP = in.readLine();
            System.out.println("Server: Public IP: " + publicIP);

            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Server: Private IP: " + localHost.getHostAddress());
            
            while (true) {
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String filename = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Server: Client " + clientAddress + " request for file: " + filename);

                String filePath = "server/storage/" + filename;
                File file = new File(filePath);

                if (file.exists()) {
                    System.out.println("Server: File found!");
                    byte[] sendData = new byte[BUFFER_SIZE];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    int bytesRead;
                    while ((bytesRead = bis.read(sendData, 0, sendData.length)) != -1) {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, bytesRead, clientAddress, clientPort);
                        socket.send(sendPacket);
                    }
                    bis.close();
                } else {
                    String errorMessage = "Server: File not found!";
                    DatagramPacket errorPacket = new DatagramPacket(errorMessage.getBytes(), errorMessage.length(), clientAddress, clientPort);
                    socket.send(errorPacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
