# -----------------------------------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
#
# Note:
# - This include is not inteded to be used for recipes that wants to install
#   json-c on the image. The official json-c in poky/meta should be used for
#   that purpose.
# - Instead this include file is used to customize json-c library to build
#   inside the eSync InVehicle libraries/binaries with hidden symbols and
#   renamed public APIs.
# - Recipes that wish to include this file should append the path to
#   json-c-rename.h file in SRC_URI as well as setting the desired prefix and
#   exec_prefix variable for bitbake staging directory.
# -----------------------------------------------------------------------------
SUMMARY = "C bindings for apps which will manipulate JSON data"
DESCRIPTION = "This is a recipe of json-c library customized for eSync Components"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=de54b60fbbc35123ba193fea8ee216f2"

SRC_URI = "\
    https://s3.amazonaws.com/json-c_releases/releases/json-c-${PV}.tar.gz \
    "
SRC_URI[sha256sum] = "b87e608d4d3f7bfdd36ef78d56d53c74e66ab278d318b71e6002a369d36f4873"

S = "${WORKDIR}/json-c-${PV}"

TARGET_CFLAGS += "-fPIC -fvisibility=hidden -Wno-implicit-fallthrough"
TARGET_CPPFLAGS += "-include json-c-rename.h"

inherit pkgconfig autotools-brokensep

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = "--enable-static --disable-shared"
