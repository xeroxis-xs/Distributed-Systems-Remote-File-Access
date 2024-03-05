package utils;

import java.util.Scanner;

public class InputGetter {

    private Scanner sc;

    public InputGetter() {
        sc = new Scanner(System.in);
    }

    public float getFloat() {
        if (sc.hasNextFloat()) {
            return sc.nextFloat();
        } else {
            sc.nextLine();
            return -1;
        }
    }

    public int getInt() {
        if (sc.hasNextInt()) {
            return sc.nextInt();
        } else {
            sc.nextLine();
            return -1;
        }
    }

    public long getLong() {
        if (sc.hasNextLong()) {
            return sc.nextLong();
        } else {
            sc.nextLine();
            return -1;
        }
    }

    public String getString() {
        String output;
        do {
            output = sc.nextLine();
        } while (output.equals(""));

        return output;
    }
}
