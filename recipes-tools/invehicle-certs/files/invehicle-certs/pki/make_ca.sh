#!/bin/bash

set -e

cd "$(dirname $0)"

mkdir -p certs

mkdir certs/ca || {

    echo "Your CA directory ('${pwd}/ca') already exists (or can not be created)"
    echo "It may contain a CA certificate, if you create a new one"
    echo "You won't be able to use previously issued certs"
    echo "Therefore, I'm refusing to overwrite anything"
    echo "Remove '${pwd}/ca' directory before running me again"
    exit 1

}

mkdir certs/issued_certs || {
    echo "Your certs directory ('${pwd}/issued_certs') already exists"
    echo "It may contain certificates that were issued to the devices"
    echo "You may need them in case you need to revoke any of them before"
    echo "Remove '${pwd}/issued_certs' directory before running me again"
    exit 1
}

read -s -r -p "Enter CA private key password:" ppwd
echo
read -s -r -p "Re-enter CA private key password:" ppwd2
echo

if test "$ppwd" != "$ppwd2"; then
    echo "Passwords don't match"
    rmdir certs/ca
    rmdir certs/issued_certs
    exit 1
fi

# you can change:
# - key size
# - encryption cipher

openssl genpkey -out certs/ca/ca_private.pem -pass pass:"$ppwd" -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -aes-128-cbc

# you can change:
# - validity of cert (in days)
# - subject
# There are other cert things you can mock with, see man req(1)
openssl req -key certs/ca/ca_private.pem -passin pass:"$ppwd" -new -x509 -days 36500 -out certs/ca/ca.pem -sha256 -subj "/C=DE/L=Duelmen/O=Excelfore/OU=xl4bus/CN=CA/emailAddress=pawel@excelfore.com"

touch certs/ca/index.txt
openssl x509 -in certs/ca/ca.pem -noout -serial | awk -F= '{print $2}' > certs/ca/serial

