# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Excelfore eSync Workload Agent Service"
DESCRIPTION = "Service that runs the Workload Agent"
HOMEPAGE = "http://excelfore.com/"
LICENSE = "CLOSED"
RDEPENDS:${PN} = "python-libua esync-wa"

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = "file://esync-workload-agent.service.in \
           file://run-esync-wa.sh.in"

inherit systemd
inherit python3-dir

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"
CLIENT_CERTS_PATH ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/certs/"
CLIENT_SOTA_PATH ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/"
RPI_IP_ADDR ?= "127.0.0.1"
CLIENT_PORTNO ?= "31933"
SSH_USER_NAME ?= "root"
SSH_PORTNO ?= "30022"
TIMEOUT_VAL ?= "0"
RETRY_COUNT ?= "30"

do_configure () {
    # esync-workload-agent.service configuration
    cp ${WORKDIR}/esync-workload-agent.service.in ${WORKDIR}/esync-workload-agent.service
    sed -i -e 's:@WA_RUNNER_PATH@:${bindir}:' ${WORKDIR}/esync-workload-agent.service

    # run-esync-wa.sh configuration
    cp ${WORKDIR}/run-esync-wa.sh.in ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@WA_BIN_PATH@:${bindir}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@AGENTDIR@:${datadir}/esync-wa:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@CLIENT_CERTS_DIR@:${CLIENT_CERTS_PATH}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@CLIENT_SOTA_DIR@:${CLIENT_SOTA_PATH}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@CLIENT_IP@:${RPI_IP_ADDR}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@CLIENT_PORT@:${CLIENT_PORTNO}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@SSH_USER@:${SSH_USER_NAME}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@SSH_PORT@:${SSH_PORTNO}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@TIMEOUT@:${TIMEOUT_VAL}:' ${WORKDIR}/run-esync-wa.sh
    sed -i -e 's:@RETRIES@:${RETRY_COUNT}:' ${WORKDIR}/run-esync-wa.sh
}

do_install () {
    # Install workload agent service that will run the esync-wa at startup
    install -m 0755 -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/esync-workload-agent.service ${D}${systemd_unitdir}/system/
    # Install runner script
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/run-esync-wa.sh ${D}${bindir}/run-esync-wa.sh
}
SYSTEMD_SERVICE:${PN} = "esync-workload-agent.service"
