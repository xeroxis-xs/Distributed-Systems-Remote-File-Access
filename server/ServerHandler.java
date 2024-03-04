package server;

import java.io.*;

public class ServerHandler {

    public byte[] processRequest(int messageHeader, String[] messageBody) {
        byte[] replyData = new byte[0];

        switch (messageHeader) {
            case 1:
                System.out.println("Server: Request Type: " + messageHeader + ". Read a content from a file");
                replyData = startRead(messageBody);
                break;
            case 2:
                System.out.println("Server: Request Type: " + messageHeader + ". Insert a content into a file");
                replyData = startInsert(messageBody);
                break;
            case 3:
                System.out.println("Server: Request Type: " + messageHeader + ". Monitor updates of a file");
                replyData = startMonitor(messageBody);
                break;
            case 4:
                System.out.println("Server: Request Type: " + messageHeader + ". Idempotent service");
                replyData = startIdempotent(messageBody);
                break;
            case 5:
                System.out.println("Server: Request Type: " + messageHeader + ". Non-idempotent service");
                replyData = startNonIdempotent(messageBody);
                break;
            default:
                System.out.println("Server: Invalid request type.");
        }
        return replyData;
    }

    private byte[] startRead(String[] messageBody) {
        String filePath = messageBody[0];
        long offset = Long.parseLong(messageBody[1]);
        int bytesToRead = Integer.parseInt(messageBody[2].trim());
        System.out.println("Server: Filepath: " + filePath);
        System.out.println("Server: Offset: " + offset);
        System.out.println("Server: Bytes: " + bytesToRead);

        File file = new File(filePath);
        byte[] replyData = new byte[0];

        if (file.exists()) {
            System.out.println("Server: File found!");
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(offset);
                byte[] buffer = new byte[bytesToRead];
                int bytesRead = raf.read(buffer);
                replyData = new byte[bytesRead];
                System.arraycopy(buffer, 0, replyData, 0, bytesRead);
                System.out.println("Server: File read!");
            }
            catch (IOException e) {
                System.out.println("Server: Error reading file!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Server: File not found!");
            replyData = "File not found".getBytes();
        }
        return replyData;
    }

    private byte[]  startInsert(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startMonitor(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startIdempotent(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private byte[] startNonIdempotent(String[] messageBody) {
        byte[] replyData = new byte[0];
        return replyData;
    }

    private void sendOverUDP(int number, String message) {

    }

}
