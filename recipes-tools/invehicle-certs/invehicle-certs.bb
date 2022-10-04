# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "DM provision scripts to create In-Vehicle certificates for esync agents"
DESCRIPTION = "DM provision scripts to auto generate In-Vehicle certificates"
HOMEPAGE = "http://excelfore.com/"
SECTION = "console/tools"
LICENSE = "CLOSED"

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = "file://invehicle-certs/ "

SRC_URI[md5sum] = "3e55dcf82abdb9566c640a589f6b72f7"
SRC_URI[sha256sum] = "ed24e49d17d1cccaae9106f3c915b787fdd327b8b7735b2a5e0ce4fc62c4c351"
PR = "r0"

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"
PROVISION_TOOLS_DIR ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/tools"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
PACKAGES = "${PN}"
ALLOW_EMPTY:${PN} = "1"

FILES:${PN} += "${PROVISION_TOOLS_DIR}/InVehicle-Certs"

do_install () {
    install -d ${D}/${PROVISION_TOOLS_DIR}/InVehicle-Certs
	install -m 0755 ${WORKDIR}/invehicle-certs/generate_node_certs.sh ${D}/${PROVISION_TOOLS_DIR}/InVehicle-Certs/
	install -m 0644 ${WORKDIR}/invehicle-certs/README ${D}/${PROVISION_TOOLS_DIR}/InVehicle-Certs/
	cp -r ${WORKDIR}/invehicle-certs/pki ${D}/${PROVISION_TOOLS_DIR}/InVehicle-Certs/
	cp -r ${WORKDIR}/invehicle-certs/config ${D}/${PROVISION_TOOLS_DIR}/InVehicle-Certs/
}

do_package_qa(){
    echo "Supressing do_package_qa errors!"
}
