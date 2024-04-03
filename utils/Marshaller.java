package utils;

import java.nio.charset.StandardCharsets;

/*
 * ByteArrayOutputStream class is used to write data that is written into a byte array
 */
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

    /**
     * Write a string value into the byte array
     * @param stringValue string value to be written
     */
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

    /**
     * Ensure the capacity of the buffer can accommodate the data
     * And adjust the capacity of the buffer if needed
     * @param minCapacity the length of the data
     */
    private void ensureCapacity(int minCapacity) {
        // Adjust the capacity of the buffer
        if (minCapacity > buffer.length) {
            int newCapacity = Math.max(buffer.length << 1, minCapacity);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, size);
            buffer = newBuffer;
        }
    }

    /**
     * Write the length of the data into the first 2 bytes of the byte array
     * @param value the length of the data
     */
    private void writeShort(int value) {
        // Write the length of the string in 2 bytes in Big-endian format
        // value = 4, buffer = [0, 4]
        buffer[size++] = (byte) (value >> 8); // MSB
        buffer[size++] = (byte) value; // LSB
    }

    /**
     * Write the data into the byte array
     * @param bytes the data to be written in byte array
     * @param offset the starting index of the data
     * @param length the length of the data
     */
    private void write(byte[] bytes, int offset, int length) {
        // Copy bytes to buffer byte array
        System.arraycopy(bytes, offset, buffer, size, length);
        // Update size of buffer
        size += length;
    }

    /**
     * Return the marshalled byte array
     * @return the byte array
     */
    public byte[] toByteArray() {
        byte[] result = new byte[size];
        // Copy buffer byte array to result
        System.arraycopy(buffer, 0, result, 0, size);
        return result;
    }
}

/**
 * ByteArrayInputStream class is used to read data from a byte array
 */
class ByteArrayInputStream {

    private byte[] buffer;
    private int pos;

    public ByteArrayInputStream(byte[] buffer) {
        this.buffer = buffer;
        this.pos = 0;
    }

    /**
     * Read a string value from the byte array in UTF-8 encoding
     * @return the string value
     */
    public String readUTF() {
        int length = readShort();
        byte[] stringBytes = new byte[length];
        read(stringBytes, 0, length);
        // Convert byte array to string using UTF-8 encoding
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * Read the length of the string from the first 2 bytes of the byte array
     * @return the length of the string
     */
    private int readShort() {
        // Read the length of the string in 2 bytes in Big-endian format
        int ch1 = buffer[pos++] & 0xFF; // Convert byte to unsigned int
        int ch2 = buffer[pos++] & 0xFF; // Convert byte to unsigned int
        return (ch1 << 8) + ch2;
    }

    /**
     * Read the data from the byte array
     * @param bytes the byte array to store the data
     * @param offset the starting index of the byte array
     * @param length the length of the data
     */
    private void read(byte[] bytes, int offset, int length) {
        // Copy bytes from buffer byte array to bytes
        System.arraycopy(buffer, pos, bytes, offset, length);
        pos += length;
    }
}

/**
 * Marshaller class is used to marshal and unmarshal data
 */
public class Marshaller {

    /**
     * Marshal the string value into a byte array representation
     * @param stringValue the string value to be marshalled
     * @return
     */
    public static byte[] marshal(String stringValue) {
        ByteArrayOutputStream customOutputStream = new ByteArrayOutputStream();
        customOutputStream.writeUTF(stringValue);
        return customOutputStream.toByteArray();
    }

    /**
     * Unmarshal the byte array data into a string value
     * @param data the byte array data to be unmarshalled
     * @return
     */
    public static String unmarshal(byte[] data) {
        ByteArrayInputStream customInputStream = new ByteArrayInputStream(data);
        return customInputStream.readUTF();
    }
}