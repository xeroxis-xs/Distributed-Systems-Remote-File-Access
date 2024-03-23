#include "ConsoleUI.hpp"
#include <iostream>

void ConsoleUI::displaySeparator(char separatorChar, int length)
{
    for (int i = 0; i < length; i++)
    {
        std::cout << separatorChar;
    }
    std::cout << std::endl;
}

void ConsoleUI::displayMessage(const std::string &message)
{
    std::cout << message << std::endl;
}

void ConsoleUI::displayPrompt(const std::string &prompt)
{
    std::cout << prompt << ": ";
}

void ConsoleUI::displayBox(const std::string &text)
{
    int length = text.length();
    printHorizontalLine(length);
    std::cout << "| " << text << " |" << std::endl;
    printHorizontalLine(length);
}

void ConsoleUI::printHorizontalLine(int length)
{
    std::cout << "+";
    for (int i = 0; i < length + 2; i++)
    {
        std::cout << "-";
    }
    std::cout << "+" << std::endl;
}
