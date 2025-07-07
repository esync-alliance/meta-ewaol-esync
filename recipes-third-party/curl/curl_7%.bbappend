# Curl uses gnutls as default, we need to use openssl for curl in esync.

PACKAGECONFIG ?= "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)} openssl libidn proxy threaded-resolver verbose zlib"
