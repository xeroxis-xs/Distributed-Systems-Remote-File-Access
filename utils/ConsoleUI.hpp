#ifndef CONSOLEUI_HPP
#define CONSOLEUI_HPP

#include <string>

using namespace std;

class ConsoleUI
{
public:
    static void displaySeparator(char separatorChar, int length);
    static void displayMessage(const string &message);
    static void displayPrompt(const string &prompt);
    static void displayBox(const string &text);

private:
    static void printHorizontalLine(int length);
};

#endif
