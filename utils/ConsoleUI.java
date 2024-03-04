package utils;

public class ConsoleUI {
    public static void displaySeparator(char separatorChar, int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(separatorChar);
        }
        System.out.println();
    }

    public static void displayMessage(String message) {
        System.out.println(message);
    }

    public static void displayPrompt(String prompt) {
        System.out.print(prompt + ": ");
    }

    // Method to print a box with given text
    public static void displayBox(String text) {
        int length = text.length();
        printHorizontalLine(length);
        System.out.println("| " + text + " |");
        printHorizontalLine(length);
    }

    // Method to print a horizontal line of dashes
    private static void printHorizontalLine(int length) {
        System.out.print("+");
        for (int i = 0; i < length + 2; i++) {
            System.out.print("-");
        }
        System.out.println("+");
    }
}

