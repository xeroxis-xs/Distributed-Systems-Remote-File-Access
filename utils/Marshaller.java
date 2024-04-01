package utils;

import java.nio.charset.StandardCharsets;

class ByteArrayOutputStream {

    private byte[] buffer;
    private int size;

    public ByteArrayOutputStream() {
        // Default size is 32 bytes
        this(32);
    }

    public ByteArrayOutputStream(int initialSize) {
        this.buffer = new byte[initialSize];
        this.size = 0;
    }

    public void writeUTF(String stringValue) {
        // Convert string to byte array using UTF-8 encoding
        byte[] stringBytes = stringValue.getBytes(StandardCharsets.UTF_8);
        // Reserve 2 bytes for length + actual length of string bytes
        int minCapacity = 2 + stringBytes.length;
        ensureCapacity(minCapacity);
        // Write the length of the string in 2 bytes
        writeShort(stringBytes.length);
        // Write the actual string bytes
        write(stringBytes, 0, stringBytes.length);
    }

    private void ensureCapacity(int minCapacity) {
        // Adjust the capacity of the buffer
        if (minCapacity > buffer.length) {
            int newCapacity = Math.max(buffer.length << 1, minCapacity);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, size);
            buffer = newBuffer;
        }
    }

    private void writeShort(int value) {
        // Write the length of the string in 2 bytes in Big-endian format
        // value = 4, buffer = [0, 4]
        buffer[size++] = (byte) (value >> 8); // MSB
        buffer[size++] = (byte) value; // LSB
    }

    private void write(byte[] bytes, int offset, int length) {
        // Copy bytes to buffer byte array
        System.arraycopy(bytes, offset, buffer, size, length);
        // Update size of buffer
        size += length;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[size];
        // Copy buffer byte array to result
        System.arraycopy(buffer, 0, result, 0, size);
        return result;
    }
}

class ByteArrayInputStream {

    private byte[] buffer;
    private int pos;

    public ByteArrayInputStream(byte[] buffer) {
        this.buffer = buffer;
        this.pos = 0;
    }

    public String readUTF() {
        int length = readShort();
        byte[] stringBytes = new byte[length];
        read(stringBytes, 0, length);
        // Convert byte array to string using UTF-8 encoding
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    private int readShort() {
        // Read the length of the string in 2 bytes in Big-endian format
        int ch1 = buffer[pos++] & 0xFF; // Convert byte to unsigned int
        int ch2 = buffer[pos++] & 0xFF; // Convert byte to unsigned int
        return (ch1 << 8) + ch2;
    }

    private void read(byte[] bytes, int offset, int length) {
        // Copy bytes from buffer byte array to bytes
        System.arraycopy(buffer, pos, bytes, offset, length);
        pos += length;
    }
}

public class Marshaller {

    // Marshalling: Convert String into a byte array representation
    public static byte[] marshal(String stringValue) {
        ByteArrayOutputStream customOutputStream = new ByteArrayOutputStream();
        customOutputStream.writeUTF(stringValue);
        return customOutputStream.toByteArray();
    }

    // Unmarshalling: Convert byte array representation into String
    public static String unmarshal(byte[] data) {
        ByteArrayInputStream customInputStream = new ByteArrayInputStream(data);
        return customInputStream.readUTF();
    }
}