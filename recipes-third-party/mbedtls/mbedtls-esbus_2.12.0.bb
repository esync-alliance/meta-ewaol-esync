# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Lightweight crypto and SSL/TLS library"
DESCRIPTION = "This is a recipe of mbedtls library customized for esync-bus"

HOMEPAGE = "https://tls.mbed.org/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=302d50a6369f5f22efdb674db908167a"

SECTION = "libs"

SRC_URI = "https://tls.mbed.org/download/mbedtls-${PV}-apache.tgz"
SRC_URI[md5sum] = "90b55ca8c726f6612de8a31a2a090e94"
SRC_URI[sha256sum] = "a2bed048f41a19ec7b4dd2e96649145bbd68a6955c3b51aeb7ccbf8908c3ce97"

S = "${WORKDIR}/mbedtls-${PV}"

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
