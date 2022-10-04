# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Implementation of JOSE for C/C++"
DESCRIPTION = "This is recipe of cjose library customized for esync-bus"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7249e2f9437adfb8c88d870438042f0e"
DEPENDS = "openssl libcheck jansson"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BP}:"

SRC_URI = " \
    git://github.com/veselov/cjose.git;protocol=https;branch=no-key \
    file://0001-Updated-EVP-function-calls-to-use-OpenSSL-Engines.patch \
    "
SRCREV = "8b9416e0993f7dad0369a8de24c24f3361311a56"

S = "${WORKDIR}/git"

TARGET_CFLAGS += "-fPIC -fvisibility=hidden"

# Change install prefix to avoid conflicts with same library of other components
prefix = "/xl4bus/libcjose"
exec_prefix = "/xl4bus/libcjose"

inherit pkgconfig autotools-brokensep

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = "--enable-static --disable-shared"
