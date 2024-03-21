#include "ConsoleUI.hpp"
#include <iostream>

void ConsoleUI::displaySeparator(char separatorChar, int length)
{
    for (int i = 0; i < length; i++)
    {
        cout << separatorChar;
    }
    cout << endl;
}

void ConsoleUI::displayMessage(const string &message)
{
    cout << message << endl;
}

void ConsoleUI::displayPrompt(const string &prompt)
{
    cout << prompt << ": ";
}

void ConsoleUI::displayBox(const string &text)
{
    int length = text.length();
    printHorizontalLine(length);
    cout << "| " << text << " |" << endl;
    printHorizontalLine(length);
}

void ConsoleUI::printHorizontalLine(int length)
{
    cout << "+";
    for (int i = 0; i < length + 2; i++)
    {
        cout << "-";
    }
    cout << "+" << endl;
}
