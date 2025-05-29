# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "eSync Update Agent Library"

LICENSE = "CLOSED"
DEPENDS = "libxml2 libzip esync-bus json-c-esua"

RDEPENDS:${PN} = "libxml2 libzip esync-bus"

BRANCH = "fix/python-libua-segfault"
GIT_REPO = "git@github.com/esync-alliance/esync-ua.git"
SRC_URI = "git://${GIT_REPO};protocol=ssh;branch=${BRANCH}"
# Use specific commit with segfault and OTA callback fixes instead of AUTOREV for stability
# Commit: fix: resolve callback registration issue causing OTA inactivity
SRCREV = "aceb0cbf7d77acf997817c3abe56b14d4ee58634"

S = "${WORKDIR}/git"

inherit cmake

# When building esync-ua in yocto need to pass to the following cmake options
# using EXTRA_OECMAKE:
EXTRA_OECMAKE = "-DENABLE_YOCTO_BUILD:BOOL=ON \
                 -DBITBAKE_STAGING_DIR:PATH=${STAGING_DIR_HOST} \
                "

# Disable deprecated warnings to avoid build failures with newer libzip
TARGET_CFLAGS += " -Wno-deprecated-declarations"

PACKAGECONFIG ??= "bintest"
PACKAGECONFIG[bintest] = "-DWITH_BINTEST:BOOL=ON"

do_configure:prepend(){
    # esync-ua uses object files of json-c when building
    # static libraries so we need extract these object files from its
    # static archives before do_configure()
    mkdir -p ${STAGING_DIR_HOST}/updateagent/json-c/obj
    cd ${STAGING_DIR_HOST}/updateagent/json-c/obj
    ${AR} -x ${STAGING_DIR_HOST}/updateagent/json-c/lib/libjson-c.a

    # enable USE_LEGACY_API in config.cmk if legacy-ua feature is enabled
    cp ${S}/linux_port/config.cmk.tmpl ${S}/linux_port/config.cmk
    if ${@bb.utils.contains('XL4_ESYNC_FEATURES', 'legacy-ua', 'true', 'false', d)}; then
        sed -i "s/^#set(USE_LEGACY_API/set(USE_LEGACY_API/" ${S}/linux_port/config.cmk
    fi
    cd ${B}
}
