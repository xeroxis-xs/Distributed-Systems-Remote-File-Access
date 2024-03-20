#ifndef MARSHALLER_HPP
#define MARSHALLER_HPP

#include <string>
#include <vector>

class Marshaller {
public:
    static std::vector<char> marshal(const std::string& stringValue);

    static std::string unmarshal(const std::vector<char>& data);
};

#endif 
