#!/bin/bash
# ******************************************************************************
# * FILE PURPOSE: eSync Client DMTree and Certificate Auto-Provisioning
# ******************************************************************************
# * FILE NAME: esync-client-autoprov.sh
# *
# * DESCRIPTION:
# *  Configures and sets up eSync Client's DMTree and Certificate base on
# *  device's MAC address
# *
# * USAGE:
# *  ./esync-client-autoprov.sh
# *
# * DEPENDENCIES:
# *  [getdevid.sh] - script to get device's MAC address
# *  [provision-secrets] - config files contains info required to contact
# *                        provisioning server
# *  [invehicle-certs] - invehicle certs scripts and configs needed to generate
# *                      certificates.
# *
# * Copyright (C) 2022 Excelfore Corporation. All Rights Reserved.
# *
# * Unauthorized copying of this file, via any medium is strictly prohibited.
# * Proprietary and confidential.
# * Its use or disclosure, in whole or in part, without written permission of
# * Excelfore Corp. is prohibited.
# *****************************************************************************

###############################################################################
# Default option
###############################################################################
# Path to eSync DM Tree
: ${TREE_DIR:="/data/trees"}
# Path to eSync In-Vehicle Certificates
: ${CERTS_DIR:="/data/certs"}
# Path to eSync SOTA Directory
: ${SOTA_DIR:="/data/sota"}
# Path to eSync Provisioning Tools
: ${TOOLS_DIR:="/data/tools"}

###############################################################################
# Runtime Option and Initial config Checking
###############################################################################

function usage() {
    echo "Usage: $0 [<options>]"
    echo
    echo "options:"
    echo "  -m <macaddr> : MAC address string (w/out the colon) to use for provisioning (e.g: For 80:ee:73:dc:cf:db input '80ee73dccfdb')"
    echo "                 If not provided, MAC address will be extracted using the getdevid.sh script"
    echo "  -h           : Display this help usage)"
    echo
    exit -1
}

# parse options and positional argument
declare -a args=()
while [ $OPTIND -le "$#" ]; do
    if getopts m:h opt; then
        case $opt in
            m) macaddr=$OPTARG ;;
            h) usage ;;
        esac
    else
        args+=("${!OPTIND}")
        ((OPTIND++))
    fi
done

if [ -z "${macaddr}" ]; then
    macaddr=$(${TOOLS_DIR}/getdevid.sh)
    if [ -z "${macaddr}" ]; then
      echo "Error: get devid mac address failed"
      exit 1
    fi
fi

secrets=${TOOLS_DIR}/provision-secrets
if [ ! -f "${secrets}" ]; then
  echo "Error: secrets are not set"
  exit 1
fi

echo "dmtree auto provision ID=$macaddr"
temp_dir=$(mktemp -d /tmp/esync-download-dmtree.XXXXXX)

###############################################################################
# Deploy SOTA Directory
###############################################################################

function deploy_sota_dir()
{
    mkdir -p ${SOTA_DIR}
}

###############################################################################
# Download DMTree
###############################################################################

function download_dmtree()
{
    # Download DMTree Base on 'provision-secrets'
    source ${secrets}

    # Get Access Token
    RET=1
    until [ ${RET} -eq 0 ]; do
        token_curl=$(curl -k -X POST -d "grant_type=password&api_key=${apikey}&secret=${secret}" ${domain}/oauth/token) && RET=$? || RET=$?
        if [ "${RET}" -eq 0 ]; then
            token=$(echo $token_curl | jq -r ".access_token") && RET=$? || RET=$?
        fi
        sleep 1
    done

    # Get Download URL
    RET=1
    until [ ${RET} -eq 0 ]; do
        url=$(curl -k "${domain}/api/v1/devices/download_package?access_token=${token}&mac=${macaddr}") && RET=$? || RET=$?
        sleep 1
    done

    # Download DMTree from the Download URL
    if [ -z "${url}" ]; then
        echo "E: unable to obtain provisioning url" && exit 1;
    else
        cd ${temp_dir}
        RET=1
        until [ ${RET} -eq 0 ]; do
            curl ${url} --output dmtree.zip && RET=$? || RET=$?
            sleep 1
        done
    fi
}

###############################################################################
# Deploy DMTree
###############################################################################

function deploy_dmtree()
{
    # Deploy DMTree if download is successful
    if [ -f ${temp_dir}/dmtree.zip ]; then
        mkdir -p ${TREE_DIR}
        unzip ${temp_dir}/dmtree.zip -d ${TREE_DIR}  &> /dev/null; res=$?
        if [ $res -ne 0 ]; then
            echo "unzip to ${TREE_DIR} failed [res=${res}]"
        else
            # dmtree might be unzipped into ${TREE_DIR}/devid (SDK) or
            # ${TREE_DIR} (ZF) so check for both
            devid=$(ls ${TREE_DIR});
            if [[ ${devid} == dm_tree_* ]]; then
                mv ${TREE_DIR}/${devid} ${TREE_DIR}/dm_tree
            else
                mv ${TREE_DIR}/${devid}/dm_tree_${devid} ${TREE_DIR}/dm_tree
                rm -rf ${TREE_DIR}/${devid}
            fi
            echo "unzip successful [dmtree: ${devid}]"

            # add a link for monitoring mac address
            mkdir -p ${TOOLS_DIR}/.DEV-MAC-LIST
            cd ${TOOLS_DIR}/.DEV-MAC-LIST
            touch ${macaddr} && ln -sf ${macaddr} active
            echo "DMTree [${devid}] linked to MAC Address successfully"
        fi
    fi
}

###############################################################################
# Generate and Deploy In-Vehicle Certificates
###############################################################################

function gen_invehicle_certs()
{
    # Reset and (Re)-Generate In-Vehicle Certificates
    cd ${TOOLS_DIR}/InVehicle-Certs
    ./generate_node_certs.sh --reset
    mkdir -p ${CERTS_DIR}
    rm -rf ${CERTS_DIR}/*
    echo "DM_TREE_PATH=${TREE_DIR}/dm_tree" > config/script.config
    echo "NODE_EDITOR=/usr/local/bin/node_editor" >> config/script.config
    echo "COMP_DLOAD_DIR=/data/sota/tmp" >> config/script.config
    ./generate_node_certs.sh -c
    cp -rf pki/certs/* ${CERTS_DIR}
    echo "In-Vehicle Certificates Generated successfully"
}

###############################################################################
# Main
###############################################################################

function main()
{
    deploy_sota_dir
    download_dmtree
    deploy_dmtree
    gen_invehicle_certs
}

main
