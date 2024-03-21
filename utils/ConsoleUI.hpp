#ifndef CONSOLEUI_HPP
#define CONSOLEUI_HPP

#include <string>

class ConsoleUI
{
public:
    static void displaySeparator(char separatorChar, int length);
    static void displayMessage(const std::string &message);
    static void displayPrompt(const std::string &prompt);
    static void displayBox(const std::string &text);

private:
    static void printHorizontalLine(int length);
};

#endif
