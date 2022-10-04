# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "eSync Bus Library"

LICENSE = "CLOSED"
DEPENDS = "openssl jansson cjose-esbus json-c-esbus c-ares-esbus mbedtls-esbus pkgconfig-native"

RDEPENDS:${PN} = "openssl jansson"

BRANCH = "main"
GIT_REPO = "github.com/esync-alliance/esync-bus.git;protocol=https"
SRC_URI = "git://${GIT_REPO};branch=${BRANCH}"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit cmake

# When building esync-bus in yocto need to pass to the following cmake options
# using EXTRA_OECMAKE:
EXTRA_OECMAKE = "-DENABLE_YOCTO_BUILD:BOOL=ON \
                 -DBITBAKE_STAGING_DIR:PATH=${STAGING_DIR_HOST} \
                "

# esync-bus uses object files of cjose, json-c and c-ares when building
# static libraries so we need extract these object files from their
# respective static archives before do_configure()
do_configure:prepend(){
    mkdir -p ${STAGING_DIR_HOST}/xl4bus/libcjose/obj
    mkdir -p ${STAGING_DIR_HOST}/xl4bus/libjson-c/obj
    mkdir -p ${STAGING_DIR_HOST}/xl4bus/libcares/obj
    mkdir -p ${STAGING_DIR_HOST}/xl4bus/libmbedtls/obj
    cd ${STAGING_DIR_HOST}/xl4bus/libcjose/obj
    ${AR} -x ${STAGING_DIR_HOST}/xl4bus/libcjose/${base_libdir}/libcjose.a
    cd ${STAGING_DIR_HOST}/xl4bus/libjson-c/obj
    ${AR} -x ${STAGING_DIR_HOST}/xl4bus/libjson-c/${base_libdir}/libjson-c.a
    cd ${STAGING_DIR_HOST}/xl4bus/libcares/obj
    ${AR} -x ${STAGING_DIR_HOST}/xl4bus/libcares/${base_libdir}/libcares.a
    cd ${STAGING_DIR_HOST}/xl4bus/libmbedtls/obj
    ${AR} -x ${STAGING_DIR_HOST}/xl4bus/libmbedtls/${base_libdir}/libmbedx509.a
    ${AR} -x ${STAGING_DIR_HOST}/xl4bus/libmbedtls/${base_libdir}/libmbedcrypto.a
    cd ${B}
}
