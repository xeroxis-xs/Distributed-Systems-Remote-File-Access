#ifndef USER_INPUT_READER_HPP
#define USER_INPUT_READER_HPP

#include <string>

using namespace std;

class UserInputReader
{
private:
    string userInput;

public:
    UserInputReader();

    int getInt();
    long getLong();
    string getString();
};

#endif
