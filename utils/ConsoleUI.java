package utils;
/**
 * ConsoleUI class is used to display messages and prompts to the console
 */
public class ConsoleUI {

    /**
     * Print a separator with given character and length
     * @param separatorChar character to be used as separator
     * @param length length of the separator
     */
    public static void displaySeparator(char separatorChar, int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(separatorChar);
        }
        System.out.println();
    }
}

