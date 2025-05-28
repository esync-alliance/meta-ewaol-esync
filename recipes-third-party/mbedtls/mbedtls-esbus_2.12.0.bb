# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Lightweight crypto and SSL/TLS library"
DESCRIPTION = "This is a recipe of mbedtls library customized for esync-bus"

HOMEPAGE = "https://tls.mbed.org/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=302d50a6369f5f22efdb674db908167a"

SECTION = "libs"

SRC_URI = "https://github.com/Mbed-TLS/mbedtls/archive/refs/tags/mbedtls-${PV}.tar.gz"
SRC_URI[md5sum] = "ecb3b11a27ee99a969c13f7a59b6845f"
SRC_URI[sha256sum] = "05b126f25d4438f206d062b48cd2f2db2a1cd11bda58b21afe40b9b7cf6fca48"

S = "${WORKDIR}/mbedtls-mbedtls-${PV}"

TARGET_CFLAGS += "-fPIC -fvisibility=hidden"

# Change install prefix to avoid conflicts with same library of other components
prefix = "/xl4bus/libmbedtls"
exec_prefix = "/xl4bus/libmbedtls"

inherit cmake

EXTRA_OECMAKE = "-DENABLE_TESTING=OFF -DLIB_INSTALL_DIR:STRING=${libdir}"

do_configure:prepend(){
    cd ${S}
    ./scripts/config.pl set MBEDTLS_PLATFORM_MEMORY
    cd ${B}
}
