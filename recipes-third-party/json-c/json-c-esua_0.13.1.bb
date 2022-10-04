# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
require recipes-third-party/json-c/json-c.inc

FILESEXTRAPATHS:prepend := "${THISDIR}/files/${PN}:"

SRC_URI += "\
    file://json-c-${PV}/json-c-rename.h \
    "

# Change install prefix to avoid conflicts with same library of other components
prefix = "/updateagent/json-c"
exec_prefix = "/updateagent/json-c"
