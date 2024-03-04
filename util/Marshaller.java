package util;

import java.nio.charset.StandardCharsets;

public class Marshaller {
    // Marshalling: Convert fields into a byte array representation
    public static byte[] marshal(int number, String message) {

        // Convert integer to byte array (4 bytes for int)
        byte[] numberBytes = new byte[4];
        numberBytes[0] = (byte) (number >> 24);
        numberBytes[1] = (byte) (number >> 16);
        numberBytes[2] = (byte) (number >> 8);
        numberBytes[3] = (byte) number;

        // Convert string to byte array using UTF-8 encoding
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // Create byte array
        byte[] result = new byte[numberBytes.length + messageBytes.length];

        // Concatenate byte arrays
        System.arraycopy(numberBytes, 0, result, 0, numberBytes.length);
        System.arraycopy(messageBytes, 0, result, numberBytes.length, messageBytes.length);

        return result;
    }

    // Unmarshalling: Convert byte array representation into fields
    public static Object[] unmarshal(byte[] data) {
        // Extract integer from byte array
        int number = (data[0] << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

        // Extract string from byte array using UTF-8 encoding
        String message = new String(data, 4, data.length - 4, StandardCharsets.UTF_8);

        return new Object[]{number, message};
    }
}
