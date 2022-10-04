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

BRANCH = "main"
GIT_REPO = "github.com/esync-alliance/esync-ua.git;protocol=https"
SRC_URI = "git://${GIT_REPO};branch=${BRANCH}"
SRCREV = "${AUTOREV}"

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
