# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "eSync Update Agent Library Python Bindings"

# NOTE: Using fix/python-libua-segfault branch to resolve segmentation faults
# in workloadagent.py caused by memory management issues in C bindings
LICENSE = "CLOSED"
DEPENDS = "swig-native esync-bus esync-ua"

RDEPENDS:${PN} = " \
       esync-bus \
       esync-ua \
       python3 \
       python3-jsonschema \
       python3-yamlloader \
       python3-pyyaml "

BRANCH = "fix/python-libua-segfault"
GIT_REPO = "git@github.com/esync-alliance/esync-ua.git"
SRC_URI = "git://${GIT_REPO};protocol=ssh;branch=${BRANCH}"
# Use specific commit with segfault and OTA callback fixes instead of AUTOREV for stability
# Commit: fix: resolve callback registration issue causing OTA inactivity
SRCREV = "aceb0cbf7d77acf997817c3abe56b14d4ee58634"

# NOTE: Removed 0001-SWIG-4.0.2-migration-support.patch as it's already included in the segfault fix branch
# SRC_URI += " file://0001-SWIG-4.0.2-migration-support.patch"

# Python 3.12 compatibility will be addressed in future updates
# Current focus is on callback registration fixes which resolve the main OTA issue

S = "${WORKDIR}/git"
B = "${WORKDIR}/git"

inherit setuptools3_legacy

do_compile:prepend() {
    if ${@bb.utils.contains('XL4_ESYNC_FEATURES', 'legacy-ua', 'false', 'true', d)}; then
        export LIBUA_API_VER="LIBUA_VER_2_0"
    fi
}

do_install:prepend() {
    if ${@bb.utils.contains('XL4_ESYNC_FEATURES', 'legacy-ua', 'false', 'true', d)}; then
        export LIBUA_API_VER="LIBUA_VER_2_0"
    fi
}
