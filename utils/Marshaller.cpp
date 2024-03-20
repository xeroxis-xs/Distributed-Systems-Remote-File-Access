#include "Marshaller.hpp"
#include <sstream>
#include <stdexcept>

std::vector<char> Marshaller::marshal(const std::string& stringValue) {
    std::vector<char> byteArray;
    try {
        // Write string value
        std::stringstream ss;
        ss << stringValue;
        std::string temp = ss.str();
        byteArray.insert(byteArray.end(), temp.begin(), temp.end());
    }
    catch (const std::exception& e) {
        // Handle exceptions
        throw std::runtime_error(e.what());
    }
    return byteArray;
}

std::string Marshaller::unmarshal(const std::vector<char>& data) {
    std::string stringValue;
    try {
        // Read string value
        stringValue.assign(data.begin(), data.end());
    }
    catch (const std::exception& e) {
        // Handle exceptions
        throw std::runtime_error(e.what());
    }
    return stringValue;
}
