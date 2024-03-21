#include "UserInputReader.hpp"
#include <iostream>

UserInputReader::UserInputReader() {}

int UserInputReader::getInt()
{
    try
    {
        getline(cin, userInput);
        return stoi(userInput);
    }
    catch (const exception &e)
    {
        cout << "Invalid input. Please enter an integer" << endl;
        return getInt();
    }
}

long UserInputReader::getLong()
{
    try
    {
        getline(cin, userInput);
        return stol(userInput);
    }
    catch (const exception &e)
    {
        cout << "Invalid input. Please enter an integer" << endl;
        return getLong();
    }
}

string UserInputReader::getString()
{
    try
    {
        getline(cin, userInput);
        while (userInput.empty())
        {
            cout << "Empty input. Please try again" << endl;
            getline(cin, userInput);
        }
        return userInput;
    }
    catch (const exception &e)
    {
        cout << "Invalid input. Please try again" << endl;
        return getString();
    }
}
