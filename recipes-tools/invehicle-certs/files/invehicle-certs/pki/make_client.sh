#!/bin/bash

cd $(dirname $0) || {
    echo "Can not change into my own directory?"
    exit 1;
}

echo "Using $(pwd) directory"

function read_yn() {
    while true; do
        read -p "$1 (y/n) " yn
        if test "$yn" = "y"; then
            return 0
        elif test "$yn" = "n"; then
            return 1
        fi

        echo "I insist you answer explicitly"
    done
}

who=
function is_user() { test "$who" = "user"; }
function is_device() { test "$who" = "device"; }
function is_server() { test "$who" = "server"; }
function is_boota() { test "$who" = "boota"; }

label=

read -p "Enter certificate label: " label
test -z "$label" || echo "$label" | fgrep -q / && {
    echo "Label can not be empty or contain forward slashes"
    exit 1
}

cdir="certs/$label"
mkdir "$cdir" || {
    echo "Can't create directory \"$cdir\""
    echo "May be it already exists? If you want to re-use it, delete it first, please"
    exit 1
}

# is_dmclient=0
# is_broker=0

has_addresses="1.3.6.1.4.1.45473.1.6=ASN1:SEQUENCE:bus_addresses"
bus_address_details=
bus_addresses=
addresses=""
count=1

read -rp "Include DMClient privileges [y/N]? " ans
echo "$ans" | fgrep -iq y && {
    addresses="$has_addresses"
    seq="$count"
    bus_addresses="${bus_addresses}|f$((count++))=SEQUENCE:address_seq_$seq"
    bus_address_details="${bus_address_details}|[address_seq_$seq]|f$((count++))=OID:1.3.6.1.4.1.45473.2.2|f$((count++))=NULL"
}
read -rp "Include Broker privileges [y/N]? " ans
echo "$ans" | fgrep -iq y && {
    addresses="$has_addresses"
    seq="$count"
    bus_addresses="${bus_addresses}|f$((count++))=SEQUENCE:address_seq_$seq"
    bus_address_details="${bus_address_details}|[address_seq_$seq]|f$((count++))=OID:1.3.6.1.4.1.45473.2.1|f$((count++))=NULL"
}

while true; do

    read -rp "Add update agent address [empty to end]: " ans
    test -z "$ans" && break;

    addresses="$has_addresses"
    seq="$count"
    bus_addresses="${bus_addresses}|f$((count++))=SEQUENCE:address_seq_$seq"
    bus_address_details="${bus_address_details}|[address_seq_$seq]|f$((count++))=OID:1.3.6.1.4.1.45473.2.3|f$((count++))=UTF8String:$ans"

done

test -z "$addresses" && {
    # if no addresses were provided, create a "general listener" address, so there is at least something.
    addresses="$has_addresses"
    seq="$count"
    bus_addresses="${bus_addresses}|f$((count++))=SEQUENCE:address_seq_$seq"
    bus_address_details="${bus_address_details}|[address_seq_$seq]|f$((count++))=OID:1.3.6.1.4.1.45473.2.4|f$((count++))=NULL"
}

while true; do

    read -rp "Add group name [empty to end]: " ans
    test -z "$ans" && break;

    groups="1.3.6.1.4.1.45473.1.7=ASN1:SET:bus_groups"
    bus_groups="${bus_groups}|f$((count++))=UTF8String:$ans"

done

cat > "$cdir/ca.conf" << _EOF_

HOME			= .
RANDFILE		= \$ENV::HOME/.rnd
oid_section		= new_oids

[ new_oids ]
tsa_policy1 = 1.2.3.4.1
tsa_policy2 = 1.2.3.4.5.6
tsa_policy3 = 1.2.3.4.5.7

[ ca ]
default_ca	= CA_default		# The default ca section

[ CA_default ]

dir		= $(pwd)/certs
certs		= \$dir/issued_certs		# Where the issued certs are kept
crl_dir		= \$dir/crl		# Where the issued crl are kept
database	= \$dir/ca/index.txt	# database index file.
unique_subject	= no
new_certs_dir	= \$dir/issued_certs

certificate	= \$dir/ca/ca.pem
serial		= \$dir/ca/serial 		# The current serial number
crlnumber	= \$dir/crlnumber	# the current crl number
					# must be commented out to leave a V1 CRL
crl		= \$dir/crl.pem 		# The current CRL
private_key	= \$dir/ca/ca_private.pem
RANDFILE	= \$dir/private/.rand	# private random number file

x509_extensions	= usr_cert		# The extentions to add to the cert
name_opt 	= ca_default		# Subject Name options
cert_opt 	= ca_default		# Certificate field options

default_days	= 365			# how long to certify for
default_crl_days= 30			# how long before next CRL
default_md	= default		# use public key default MD
preserve	= no			# keep passed DN ordering

policy		= policy_match

[ policy_match ]
countryName		= match
stateOrProvinceName	= optional
organizationName	= match
organizationalUnitName	= optional
commonName		= supplied
emailAddress		= optional

[ policy_anything ]
countryName		= optional
stateOrProvinceName	= optional
localityName		= optional
organizationName	= optional
organizationalUnitName	= optional
commonName		= supplied
emailAddress		= optional

[ req ]
default_bits		= 2048
default_md		= sha1
default_keyfile 	= privkey.pem
x509_extensions	= v3_ca	# The extentions to add to the self signed cert
prompt = no
distinguished_name = dn
req_extensions = ext
string_mask = utf8only

req_extensions = v3_req # The extensions to add to a certificate request

[ req_distinguished_name ]
countryName			= Country Name (2 letter code)
countryName_default		= XX
countryName_min			= 2
countryName_max			= 2

stateOrProvinceName		= State or Province Name (full name)
#stateOrProvinceName_default	= Default Province

localityName			= Locality Name (eg, city)
localityName_default	= Default City

0.organizationName		= Organization Name (eg, company)
0.organizationName_default	= Default Company Ltd

# we can do this but it is not needed normally :-)
#1.organizationName		= Second Organization Name (eg, company)
#1.organizationName_default	= World Wide Web Pty Ltd

organizationalUnitName		= Organizational Unit Name (eg, section)
#organizationalUnitName_default	=

commonName                      = Common Name (eg, your name or your server\'s hostname)
commonName_max			= 64

emailAddress			= Email Address
emailAddress_max		= 64

# SET-ex3			= SET extension number 3

[ req_attributes ]
challengePassword		= A challenge password
challengePassword_min		= 4
challengePassword_max		= 20

unstructuredName		= An optional company name

[ usr_cert ]

# These extensions are added when 'ca' signs a request.

# This goes against PKIX guidelines but some CAs do it and some software
# requires this to avoid interpreting an end user certificate as a CA.

basicConstraints=CA:FALSE

# Here are some examples of the usage of nsCertType. If it is omitted
# the certificate can be used for anything *except* object signing.

# This is OK for an SSL server.
# nsCertType			= server

# For an object signing certificate this would be used.
# nsCertType = objsign

# For normal client use this is typical
# nsCertType = client, email

# and for everything including object signing:
# nsCertType = client, email, objsign

# This is typical in keyUsage for a client certificate.
# keyUsage = nonRepudiation, digitalSignature, keyEncipherment

# This will be displayed in Netscape's comment listbox.
nsComment			= "Excelfore device cert"

# PKIX recommendations harmless if included in all certificates.
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer

# This stuff is for subjectAltName and issuerAltname.
# Import the email address.
# subjectAltName=email:copy
# An alternative to produce certificates that aren't
# deprecated according to PKIX.
# subjectAltName=email:move

# Copy subject details
# issuerAltName=issuer:copy

#nsCaRevocationUrl		= http://www.domain.dom/ca-crl.pem
#nsBaseUrl
#nsRevocationUrl
#nsRenewalUrl
#nsCaPolicyUrl
#nsSslServerName

# This is required for TSA certificates.
# extendedKeyUsage = critical,timeStamping

$addresses
$groups

[ bus_addresses ]
$(echo $bus_addresses | tr '|' '\n')

$(echo $bus_address_details | tr '|' '\n')

[ bus_groups ]

$(echo $bus_groups | tr '|' '\n')


[ v3_req ]

# Extensions to add to a certificate request

basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment

[ v3_ca ]


# Extensions for a typical CA


# PKIX recommendation.

subjectKeyIdentifier=hash

authorityKeyIdentifier=keyid:always,issuer

# This is what PKIX recommends but some broken software chokes on critical
# extensions.
#basicConstraints = critical,CA:true
# So we do this instead.
basicConstraints = CA:true

# Key usage: this is typical for a CA certificate. However since it will
# prevent it being used as an test self-signed certificate it is best
# left out by default.
# keyUsage = cRLSign, keyCertSign

# Some might want this also
# nsCertType = sslCA, emailCA

# Include email address in subject alt name: another PKIX recommendation
# subjectAltName=email:copy
# Copy issuer details
# issuerAltName=issuer:copy

# DER hex encoding of an extension: beware experts only!
# obj=DER:02:03
# Where 'obj' is a standard or added object
# You can even override a supported extension:
# basicConstraints= critical, DER:30:03:01:01:FF

[ crl_ext ]

# CRL extensions.
# Only issuerAltName and authorityKeyIdentifier make any sense in a CRL.

# issuerAltName=issuer:copy
authorityKeyIdentifier=keyid:always

[ proxy_cert_ext ]
# These extensions should be added when creating a proxy certificate

# This goes against PKIX guidelines but some CAs do it and some software
# requires this to avoid interpreting an end user certificate as a CA.

basicConstraints=CA:FALSE

# Here are some examples of the usage of nsCertType. If it is omitted
# the certificate can be used for anything *except* object signing.

# This is OK for an SSL server.
# nsCertType			= server

# For an object signing certificate this would be used.
# nsCertType = objsign

# For normal client use this is typical
# nsCertType = client, email

# and for everything including object signing:
# nsCertType = client, email, objsign

# This is typical in keyUsage for a client certificate.
# keyUsage = nonRepudiation, digitalSignature, keyEncipherment

# This will be displayed in Netscape's comment listbox.
nsComment			= "OpenSSL Generated Certificate"

# PKIX recommendations harmless if included in all certificates.
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer

# This stuff is for subjectAltName and issuerAltname.
# Import the email address.
# subjectAltName=email:copy
# An alternative to produce certificates that aren't
# deprecated according to PKIX.
# subjectAltName=email:move

# Copy subject details
# issuerAltName=issuer:copy

#nsCaRevocationUrl		= http://www.domain.dom/ca-crl.pem
#nsBaseUrl
#nsRevocationUrl
#nsRenewalUrl
#nsCaPolicyUrl
#nsSslServerName

[dn]
#CN = $cn
#OU = $group
CN = $label
OU = xl4bus
O = Excelfore
L = Duelmen
C = DE

[ext]

_EOF_

cd "$cdir" || exit 1

# generate a private key for the device.
# we can't really encrypt it because it's to be read
# by code.
echo "--- Generating private key"
openssl genpkey -out private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

# create certificate signing request
echo "--- Generating cert request"
openssl req -new -utf8 -nameopt multiline,utf8 -config ca.conf -key private.pem -out cert.req

# sign the request
echo "--- Signing the request"
read -s -r -p "Enter the CA private key password:" ppwd
batch=
tty >/dev/null || batch="-batch"
openssl ca $batch -config ca.conf -days 365 -key "$ppwd" -out cert.pem -in cert.req

rm cert.req

