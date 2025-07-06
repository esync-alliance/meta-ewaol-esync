# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "eSync Update Agent Library Python Bindings"

LICENSE = "CLOSED"
DEPENDS = "swig-native esync-bus esync-ua"

RDEPENDS:${PN} = " \
       esync-bus \
       esync-ua \
       python3 \
       python3-jsonschema \
       python3-yamlloader \
       python3-pyyaml "

BRANCH = "wa-c-v1"
GIT_REPO = "git@github.com/esync-alliance/esync-ua.git"
SRC_URI = "git://${GIT_REPO};protocol=ssh;branch=${BRANCH}"
SRCREV = "${AUTOREV}"
#SRCREV = "d26b950ba8d2607c5a2a3f6d603ed3e1b2d3344b"

SRC_URI += " file://0001-SWIG-4.0.2-migration-support.patch"

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
