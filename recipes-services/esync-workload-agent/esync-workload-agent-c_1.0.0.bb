# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Excelfore eSync Workload Agent Service (C Implementation)"
DESCRIPTION = "Service that runs the C Workload Agent"
HOMEPAGE = "http://excelfore.com/"
LICENSE = "CLOSED"
RDEPENDS:${PN} = "esync-ua esync-wa"

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = "file://esync-workload-agent-c.service.in"

inherit systemd

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"
CLIENT_CERTS_PATH ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/certs/python-agent"
CLIENT_SOTA_CACHE_PATH ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/cache"
CLIENT_SOTA_BACKUP_PATH ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/backup"
ESYNC_HANDLER_TYPE ?= "/SDK/PY_AGENT"
ESYNC_HOST ?= "127.0.0.1"
ESYNC_PORT ?= "31933"
SSH_USER_NAME ?= "root"
SSH_PORTNO ?= "30022"
WA_INSTALL_DIR ?= "${datadir}/esync-wa"
WA_TIMEOUT ?= "0"
WA_RETRIES ?= "30"
ESYNC_EXTRA_ARGS ?= ""

do_configure () {
    # esync-workload-agent-c.service configuration
    cp ${WORKDIR}/esync-workload-agent-c.service.in ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@BINDIR@:${bindir}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_CERTS_DIR@:${CLIENT_CERTS_PATH}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_CLIENT_PRIV_HOST_DIR@:${ESYNC_CLIENT_PRIV_HOST_DIR}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_CACHE_DIR@:${CLIENT_SOTA_CACHE_PATH}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_BACKUP_DIR@:${CLIENT_SOTA_BACKUP_PATH}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_HANDLER_TYPE@:${ESYNC_HANDLER_TYPE}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_HOST@:${ESYNC_HOST}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_PORT@:${ESYNC_PORT}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@SSH_USER@:${SSH_USER_NAME}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@SSH_PORT@:${SSH_PORTNO}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@WA_INSTALL_DIR@:${WA_INSTALL_DIR}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@WA_TIMEOUT@:${WA_TIMEOUT}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@WA_RETRIES@:${WA_RETRIES}:' ${WORKDIR}/esync-workload-agent-c.service
    sed -i -e 's:@ESYNC_EXTRA_ARGS@:${ESYNC_EXTRA_ARGS}:' ${WORKDIR}/esync-workload-agent-c.service
}

do_install () {
    # Install C workload agent service
    install -m 0755 -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/esync-workload-agent-c.service ${D}${systemd_unitdir}/system/
}

pkg_postinst_ontarget:${PN} () {
    # Create necessary directory structure on target
    mkdir -p ${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmp
    mkdir -p ${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/cache
    mkdir -p ${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/backup

    # Create symlink so workload agent can find files in both locations
    if [ ! -L ${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/cache/tmp ]; then
        ln -sf ../../tmp ${ESYNC_CLIENT_PRIV_HOST_DIR}/data/sota/tmpl-py/cache/tmp
    fi
}

SYSTEMD_SERVICE:${PN} = "esync-workload-agent-c.service"

# Provide alternative to the Python version
PROVIDES = "esync-workload-agent-impl"
RPROVIDES:${PN} = "esync-workload-agent-impl"

# Conflict with Python version to avoid running both simultaneously
RCONFLICTS:${PN} = "esync-workload-agent"
