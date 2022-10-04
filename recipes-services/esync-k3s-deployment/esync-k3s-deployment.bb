# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Excelfore eSync-Client k3s deployment"
DESCRIPTION = "Service that deploys Container Base eSync-Client to k3s server"
HOMEPAGE = "http://excelfore.com/"
SECTION = "console/tools"
LICENSE="CLOSED"

RDEPENDS:${PN} = "bash k3s esync-macaddr-monitor invehicle-certs"

FILESEXTRAPATHS:prepend = "${THISDIR}/files:"
SRC_URI = "file://esync-deployment-helper.service.in \
           file://kubeconfigs/ \
           file://scripts/ \
           file://ssh-priv/ \
           file://docker-priv/ \
           file://data/ \
          "

ESYNC_CLIENT_PRIV_HOST_DIR ?= "/mnt/esync"
PROVISION_TOOLS_DIR ?= "${ESYNC_CLIENT_PRIV_HOST_DIR}/data/tools"

inherit systemd

do_configure () {
    # esync-deployment-helper.service configuration
    cp ${WORKDIR}/esync-deployment-helper.service.in ${WORKDIR}/esync-deployment-helper.service
    sed -i -e 's:@ESCLIENT_PRIV_DIR@:${ESYNC_CLIENT_PRIV_HOST_DIR}:' ${WORKDIR}/esync-deployment-helper.service

    # deploy.sh configuration
    cp ${WORKDIR}/kubeconfigs/deploy.sh.in ${WORKDIR}/kubeconfigs/deploy.sh
    sed -i -e 's:@ESCLIENT_PRIV_DIR@:${ESYNC_CLIENT_PRIV_HOST_DIR}:' ${WORKDIR}/kubeconfigs/deploy.sh
    sed -i -e 's:@ROOT_HOME@:${ROOT_HOME}:' ${WORKDIR}/kubeconfigs/deploy.sh
}

do_install () {
    # Checked that required Auto-Provision Variables are set
    if [ -z "${PROVISION_API_KEY}" ] || [ -z "${PROVISION_SECRET}" ] || [ -z "${PROVISION_DOMAIN}" ]; then
        bbfatal "Auto-Provision settings incomplete!!!"
    fi

    # Install eSync Deployment Helper Service
    install -m 0755 -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/esync-deployment-helper.service ${D}${systemd_unitdir}/system/

    # Install k3s yaml files
    install -d ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/kubeconfigs
    install -m 0664 ${WORKDIR}/kubeconfigs/*.yaml ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/kubeconfigs/
    install -m 0755 ${WORKDIR}/kubeconfigs/deploy.sh ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/kubeconfigs/

    # Install Provisioning Tools and Secrets
    install -d ${D}/${PROVISION_TOOLS_DIR}
    install -m 0755 ${WORKDIR}/data/tools/esync-client-autoprov.sh ${D}/${PROVISION_TOOLS_DIR}/
    install -m 0755 ${WORKDIR}/data/tools/getdevid.sh ${D}/${PROVISION_TOOLS_DIR}/

    echo apikey=\'${PROVISION_API_KEY}\' >  ${D}/${PROVISION_TOOLS_DIR}/provision-secrets
    echo secret=\'${PROVISION_SECRET}\' >> ${D}/${PROVISION_TOOLS_DIR}/provision-secrets
    echo domain=\'${PROVISION_DOMAIN}\' >> ${D}/${PROVISION_TOOLS_DIR}/provision-secrets

    # Install scripts to be run inside the container.
    # (minimum requirement is a run.sh file)
    install -d ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/scripts
    install -m 0664 ${WORKDIR}/scripts/*.xml ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/scripts/
    install -m 0664 ${WORKDIR}/scripts/*.conf ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/scripts/
    install -m 0755 ${WORKDIR}/scripts/run.sh ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/scripts/

    # Install SSH authorized keys for accessing the SSH service in K3S
    # (if user does not provide one just install the template)
    install -d ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/ssh-priv
    if [ -f "${WORKDIR}/ssh-priv/authorized_keys" ]; then
        install -m 0664 ${WORKDIR}/ssh-priv/authorized_keys ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/ssh-priv/
    else
        install -m 0664 ${WORKDIR}/ssh-priv/authorized_keys.in ${D}/${ESYNC_CLIENT_PRIV_HOST_DIR}/ssh-priv/authorized_keys
    fi

    # Install Docker Registry Auth Config only if user provided it
    # (If docker config is not provided, deploy script will not update registry
    #  credential secret)
    install -d ${D}${ROOT_HOME}/.docker
    if [ -f "${WORKDIR}/docker-priv/config.json" ]; then
        install -m 0600 ${WORKDIR}/docker-priv/config.json ${D}${ROOT_HOME}/.docker/
    fi
}

SYSTEMD_SERVICE:${PN} = "esync-deployment-helper.service"
FILES:${PN} += "\
    ${ROOT_HOME}/.docker/ \
    ${ESYNC_CLIENT_PRIV_HOST_DIR} \
    ${ESYNC_CLIENT_PRIV_HOST_DIR}/kubeconfigs \
    ${ESYNC_CLIENT_PRIV_HOST_DIR}/scripts \
    ${ESYNC_CLIENT_PRIV_HOST_DIR}/ssh-priv \
    ${PROVISION_TOOLS_DIR} \
    "
