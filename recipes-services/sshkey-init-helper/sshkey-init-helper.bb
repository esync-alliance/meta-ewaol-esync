# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "SSH Key Init Helper"
DESCRIPTION = "A service that initializes ssh keys on initial boot"
LICENSE = "CLOSED"
RDEPENDS:${PN} = "bash openssh "

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = " \
        file://sshkey-init-helper.service.in \
        file://sshkey-init.sh.in \
        "

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"

inherit systemd

do_configure () {
    # sshkey-init-helper.service configuration
    cp ${WORKDIR}/sshkey-init-helper.service.in ${WORKDIR}/sshkey-init-helper.service
    sed -i -e 's:@ROOT_HOME@:${ROOT_HOME}:' ${WORKDIR}/sshkey-init-helper.service

    # sshkey-init.sh configuration
    cp ${WORKDIR}/sshkey-init.sh.in ${WORKDIR}/sshkey-init.sh
    sed -i -e 's:@ESCLIENT_PRIV_DIR@:${ESYNC_CLIENT_PRIV_HOST_DIR}:' ${WORKDIR}/sshkey-init.sh
    sed -i -e 's:@SSHKEYGEN_BINDIR@:${bindir}:' ${WORKDIR}/sshkey-init.sh
    sed -i -e 's:@ROOT_HOME@:${ROOT_HOME}:' ${WORKDIR}/sshkey-init.sh
}

do_install () {
    # Install workload agent as a service
    install -m 0755 -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshkey-init-helper.service ${D}${systemd_unitdir}/system/

    # Install SSH authorized keys for accessing the SSH service in K3S
    install -d ${D}${ROOT_HOME}/.ssh
    install -m 0611 ${WORKDIR}/sshkey-init.sh ${D}${ROOT_HOME}/.ssh/sshkey-init.sh
}

SYSTEMD_SERVICE:${PN} = "sshkey-init-helper.service"
FILES:${PN} = "${ROOT_HOME}/.ssh/sshkey-init.sh"
