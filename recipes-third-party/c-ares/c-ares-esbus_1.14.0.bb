# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "c-ares is a C library that resolves names asynchronously."
DESCRIPTION = "This is a recipe of c-ares library customized for esync-bus"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=fb997454c8d62aa6a47f07a8cd48b006"

BRANCH = "main"
GIT_REPO = "github.com/c-ares/c-ares.git;protocol=https"
SRC_URI = "git://${GIT_REPO};branch=${BRANCH}"
SRCREV = "17dc1b3102e0dfc3e7e31369989013154ee17893"

S = "${WORKDIR}/git"

TARGET_CFLAGS += "-fPIC -fvisibility=hidden"
DISABLE_STATIC = ""

# Change install prefix to avoid conflicts with same library of other components
prefix = "/xl4bus/libcares"
exec_prefix = "/xl4bus/libcares"

inherit autotools-brokensep

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = "--enable-static --disable-shared"

do_configure:prepend(){
    cd ${S}
    ./buildconf
}
