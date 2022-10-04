# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Excelfore eSync MAC address monitor Service"
DESCRIPTION = "eSync service that montors host's mac address"
HOMEPAGE = "http://excelfore.com/"
SECTION = "console/tools"
LICENSE="CLOSED"

RDEPENDS:${PN} = "bash iproute2 openssl-bin curl"

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = "file://esync-macaddr-monitor.service.in \
           file://macaddr-monitor.sh.in \
           file://getdevid.sh.in \
          "

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"
PROVISION_TOOLS_DIR ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/tools"

inherit systemd

do_configure () {
    # esync-macaddr-monitor.service configuration
    cp ${WORKDIR}/esync-macaddr-monitor.service.in ${WORKDIR}/esync-macaddr-monitor.service
    sed -i -e 's:@PROVISION_TOOLS_DIR@:${PROVISION_TOOLS_DIR}:' ${WORKDIR}/esync-macaddr-monitor.service

    # macaddr-monitor.sh configuration
    cp ${WORKDIR}/macaddr-monitor.sh.in ${WORKDIR}/macaddr-monitor.sh
    sed -i -e 's:@ESCLIENT_PRIV_DIR@:${ESYNC_CLIENT_PRIV_HOST_DIR}:' ${WORKDIR}/macaddr-monitor.sh
    sed -i -e 's:@PROVISION_TOOLS_DIR@:${PROVISION_TOOLS_DIR}:' ${WORKDIR}/macaddr-monitor.sh

    # getdevid.sh configuration
    cp ${WORKDIR}/getdevid.sh.in ${WORKDIR}/getdevid.sh
}

do_install () {
    install -m 0755 -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/esync-macaddr-monitor.service ${D}${systemd_unitdir}/system/

    install -d ${D}/${PROVISION_TOOLS_DIR}
    install -m 0755 ${WORKDIR}/macaddr-monitor.sh  ${D}/${PROVISION_TOOLS_DIR}/

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/getdevid.sh ${D}${sbindir}
}

SYSTEMD_SERVICE:${PN} = "esync-macaddr-monitor.service"
FILES:${PN} += "${PROVISION_TOOLS_DIR}"
