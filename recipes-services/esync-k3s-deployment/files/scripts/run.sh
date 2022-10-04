#!/bin/bash
NETDEV="eth0"
esync_runner_pid=""
uds_updateagent_pid=""
xl4doipd_pid=""

LOG_OUTPUT_DIR="/tmp/xl4/logs"

#####################################################################
# Start eSync Auto Provisioning
#####################################################################

function start_esync_autoprovision()
{
    TREE_DIR=/data/trees
    TOOLS_DIR=/data/tools
    MAC_LIST_DIR=${TOOLS_DIR}/.DEV-MAC-LIST
    ACTIVE_DEVID=${MAC_LIST_DIR}/active
    DO_PROVISION=false

    # Check if 'active' macaddr symlink is valid
    if [ -L ${ACTIVE_DEVID} ] ; then
        CURRENT_ACTIVE_MACADDR=$(basename $(readlink -n -e ${ACTIVE_DEVID}))
        if [ -e ${ACTIVE_DEVID} ] ; then
            echo "Valid DMTree exist [macaddr: ${CURRENT_ACTIVE_MACADDR}]"
        else
            echo "InValid DMTree exist [macaddr: ${CURRENT_ACTIVE_MACADDR}]"
            DO_PROVISION=true
        fi
    elif [ -e ${ACTIVE_DEVID} ] ; then
        echo "An invalid status file exist, deleting..."
        rm -rf ${ACTIVE_DEVID}
        DO_PROVISION=true
    else
        echo "DMTree not yet exist"
        DO_PROVISION=true
    fi

    # Perform Provisioning base on above checking
    if ${DO_PROVISION}; then
        for file in ${MAC_LIST_DIR}/*
        do
            if [ -f $file ] && [ ! -L $file ]; then
                macaddr=$(basename $file)
                echo "Removing OLD DMTree..."
                rm -rf ${TREE_DIR}/dm_tree
                echo "Provisioning new eSync-Client [macaddr: ${macaddr}]..."
                ${TOOLS_DIR}/esync-client-autoprov.sh -m ${macaddr}
                break
            fi
        done
    fi
}

#####################################################################
# Start eSync-Client Core Runner
#####################################################################

function start_esync_core_runner()
{
    # Create Download folders
    mkdir -p /data/sota/tmpl/backup
    mkdir -p /data/sota/tmpl/cache

    esyncrun.py /root/scripts/esyncrun.conf > ${LOG_OUTPUT_DIR}/esyncrun.log 2>&1 &
    esync_runner_pid=$!
}

#####################################################################
# Start UDS Update Agent
#####################################################################

function start_uds_updateagent()
{
    #x4uds-ua needs xl4doipd to start first
    xl4doipd -d ${NETDEV} -m t > ${LOG_OUTPUT_DIR}/xl4doipd.log 2>&1 &
    xl4doipd_pid=$!
    sleep 1

    # Create Download folders
    mkdir -p /data/sota/uds-ua/backup
    mkdir -p /data/sota/uds-ua/cache

    esync-uds-ua \
        -k /data/certs/pdc-aurix1 \
        -c /data/sota/uds-ua/cache \
        -b /data/sota/uds-ua \
        -C /root/scripts/config.xml \
        -vvvv > ${LOG_OUTPUT_DIR}/esync-uds-ua.log 2>&1 &
    uds_updateagent_pid=$!
}

#####################################################################
# Perform Clean-Up
#####################################################################
perform_cleanup()
{
    echo "stopping all process..."
    sleep 1

    if [ ! -z "${uds_updateagent_pid}" ]; then
        echo "Stopping UDS Update Agent [${uds_updateagent_pid}]..."
        kill ${uds_updateagent_pid}
    fi

    if [ ! -z "${xl4doipd_pid}" ]; then
        echo "Stopping XL4 DoIP Daemon [${xl4doipd_pid}]..."
        kill ${xl4doipd_pid}
    fi

    if [ ! -z "${esync_runner_pid}" ]; then
        echo "Stopping eSync Core Runner [${esync_runner_pid}]..."
        kill ${esync_runner_pid}
    fi

    exit 1
}

#####################################################################
# Main
#####################################################################

trap perform_cleanup INT TERM
mkdir -p ${LOG_OUTPUT_DIR}

while true; do
    echo "Checking DMTree and InVehicle Certificates..."
    start_esync_autoprovision

    echo "Running eSync Core InVehicle Components..."
    start_esync_core_runner

    echo "Running UDS Update Agent..."
    start_uds_updateagent

    echo "All applications executed successfully!"
    echo "Waiting fo MAC Address Update Notification..."

    # Monitor .DEV-MAC-LIST directory and wait for 'delete' events only.
    # The service that runs on host to monitor mac address change should
    # follow these sequence:
    # (1) touch /path/to/.DEV-MAC-LIST/<new macaddr>
    # (2) rm /path/to/.DEV-MAC-LIST/<old macaddr>
    #-------------------------------------------------------------------
    inotifywait -q -e delete /data/tools/.DEV-MAC-LIST
    echo ".DEV-MAC-LIST updated"
    echo "---------------------------------------"
    ls -lt /data/tools/.DEV-MAC-LIST
    echo "---------------------------------------"
    perform_cleanup
done
