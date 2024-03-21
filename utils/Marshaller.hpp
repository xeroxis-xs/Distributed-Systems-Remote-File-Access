#ifndef MARSHALLER_HPP
#define MARSHALLER_HPP

#include <string>
#include <vector>

using namespace std;

class Marshaller
{
public:
    static vector<char> marshal(const string &stringValue);

    static string unmarshal(const vector<char> &data);
};

#endif
