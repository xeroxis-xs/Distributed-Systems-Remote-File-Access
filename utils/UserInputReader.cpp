#include "UserInputReader.hpp"
#include <iostream>

UserInputReader::UserInputReader() {}

int UserInputReader::getInt()
{
    try
    {
        std::getline(std::cin, userInput);
        return stoi(userInput);
    }
    catch (const std::exception &e)
    {
        std::cout << "Invalid input. Please enter an integer" << std::endl;
        return getInt();
    }
}

long UserInputReader::getLong()
{
    try
    {
        std::getline(std::cin, userInput);
        return stol(userInput);
    }
    catch (const std::exception &e)
    {
        std::cout << "Invalid input. Please enter an integer" << std::endl;
        return getLong();
    }
}

std::string UserInputReader::getString()
{
    try
    {
        std::getline(std::cin, userInput);
        while (userInput.empty())
        {
            std::cout << "Empty input. Please try again" << std::endl;
            std::getline(std::cin, userInput);
        }
        return userInput;
    }
    catch (const std::exception &e)
    {
        std::cout << "Invalid input. Please try again" << std::endl;
        return getString();
    }
}
