#!/bin/bash -e
# ******************************************************************************
# * FILE PURPOSE: Get Device's ID (MAC Address)
# ******************************************************************************
# * FILE NAME: getdevid.sh
# *
# * DESCRIPTION:
# *  Returns Device's MAC address for the purpose of using it as Device ID
# *  for eSync Client Provisioing
# *
# * USAGE:
# *  ./getdevid.sh
# *
# * Copyright (C) 2022 Excelfore Corporation. All Rights Reserved.
# *
# * Unauthorized copying of this file, via any medium is strictly prohibited.
# * Proprietary and confidential.
# * Its use or disclosure, in whole or in part, without written permission of
# * Excelfore Corp. is prohibited.
# ******************************************************************************
export PATH=$PATH:/sbin:/bin:/usr/sbin:/usr/bin

NETDEV=""
if [ -z ${NETDEV} ];then
    DEFAULT_IFACE=$(ip -4 route show default | awk 'match($0,/dev [^ ]*/){ print substr($0, RSTART+4,RLENGTH-4)}')
else
	DEFAULT_IFACE=${NETDEV}
fi

#if multiple interface came, select the first one as priority
IFACE=$(echo $DEFAULT_IFACE | cut -d ' ' -f 1 )

if [ ! -z $IFACE ] || [ -d /sys/class/net/$IFACE ] || [ -f /sys/class/net/$IFACE/address ];then
    read MAC </sys/class/net/$IFACE/address #reading mac address
    DEVID=${MAC//:} #removing colon
fi
echo $DEVID
