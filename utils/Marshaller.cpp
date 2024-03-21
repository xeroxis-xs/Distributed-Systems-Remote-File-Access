#include "Marshaller.hpp"
#include <sstream>
#include <stdexcept>

vector<char> Marshaller::marshal(const string &stringValue)
{
    vector<char> byteArray;
    try
    {
        // Write string value
        // stringstream ss;
        // ss << stringValue;
        // string temp = ss.str();
        // byteArray.insert(byteArray.end(), temp.begin(), temp.end());
        short length = static_cast<short>(stringValue.size());
        byteArray.push_back((length >> 8) & 0xFF);
        byteArray.push_back(length & 0xFF);

        // Write string characters
        byteArray.insert(byteArray.end(), stringValue.begin(), stringValue.end());
    }
    catch (const exception &e)
    {
        // Handle exceptions
        throw runtime_error(e.what());
    }
    return byteArray;
}

string Marshaller::unmarshal(const vector<char> &data)
{
    string stringValue;
    try
    {
        // Read string value
        // stringValue.assign(data.begin(), data.end());

        short length = (static_cast<unsigned char>(data[0]) << 8) | static_cast<unsigned char>(data[1]);

        // Read string characters
        stringValue.assign(data.begin() + 2, data.begin() + 2 + length);
    }
    catch (const exception &e)
    {
        // Handle exceptions
        throw runtime_error(e.what());
    }
    return stringValue;
}
