package client_java;

import java.util.Scanner;

/**
 * Input class to get user input
 */
public class InputGetter {

    private Scanner sc;

    public InputGetter() {
        sc = new Scanner(System.in);
    }

	/**
	 * Get an integer from the user input
	 * @return integer value
	 */
    public int getInt() {
		try {
			//DO NOT use nextint because it does not read \n
			return Integer.parseInt(sc.nextLine());
		} catch (Exception e) {
			System.out.println("Invalid input. Please enter an integer");
			return getInt();
		}
    }

	/**
	 * Get a long from the user input
	 * @return long value
	 */
    public long getLong() {
		try {
			return Long.parseLong(sc.nextLine());
		} catch (Exception e) {
			System.out.println("Invalid input. Please enter an integer");
			return getLong();
		}
    }

	/**
	 * Get a String from the user input
	 * @return string value
	 */
    public String getString() {
		try {
			String userInput = sc.nextLine();
			while (userInput.equals("")) {
				System.out.println("Empty input. Please try again");
				userInput = sc.nextLine();
			}
			return userInput;
		} catch (Exception e) {
			System.out.println("Invalid input. Please try again");
			return getString();
		}
    }
}
