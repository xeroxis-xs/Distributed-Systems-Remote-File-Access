#ifndef USER_INPUT_READER_HPP
#define USER_INPUT_READER_HPP

#include <string>

class UserInputReader
{
private:
    std::string userInput;

public:
    UserInputReader();

    int getInt();
    long getLong();
    std::string getString();
};

#endif
