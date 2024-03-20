package utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Marshaller {

    // Marshalling: Convert fields into a byte array representation
    public static byte[] marshal(String stringValue) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            // Write string value
            dataOutputStream.writeUTF(stringValue);
            dataOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }
    // Unmarshalling: Convert byte array representation into fields
    public static String unmarshal(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        String stringValue = null;
        
        try {
            // Read string value
            stringValue = dataInputStream.readUTF();
            dataInputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return stringValue;
    }
}
