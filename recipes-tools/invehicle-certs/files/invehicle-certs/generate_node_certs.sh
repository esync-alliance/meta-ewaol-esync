#!/bin/bash
set -e
usage() {
cat <<USAGE
Usage options :
 $ generate_node_certs.sh
	             -c       # Create certs by reading configuration files
	             --reset  # Remove the existing certs and create new certs.
	             -h       # Help / Usage Options
  E.g., $ './generate_node_certs.sh -c'
USAGE
}

##SCRIPT-CONFIG#####
THIS_DIR="$( cd "$( dirname "$0" )" && pwd )"
RUN_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
CONFIG_DIR="$THIS_DIR/config"
PKI_DIR="$THIS_DIR/pki"
mkdir -p "$PKI_DIR/certs/status"
TMP_DIR="$PKI_DIR/certs/status"
CA_PWD_FILE="$CONFIG_DIR/CA_PWD"
CERTS_DIR_PATH="$PKI_DIR/certs"
CERTS_GENERATED_FILE="$THIS_DIR/pki/certs/status/certificate-generated.txt"
SCRIPT_CFG_FILE="$CONFIG_DIR/script.config"
COMP_DLOAD_DIR=$ESYNC_COMP_DLOAD_DIR
####################

read_script_config()
{
 if [ -f "$SCRIPT_CFG_FILE" ]; then
    source $SCRIPT_CFG_FILE
    echo "**Using script's configuration file !"
 else
    NODE_EDITOR=node_editor
    DM_TREE_PATH=$ESYNC_DIR/InVehicle-Certs/dm_tree
fi
}
read_script_config;

make_ca()
{
 #echo "---Creating CA certificate"
 if [ -f $CA_PWD_FILE ]; then
    source $CA_PWD_FILE
      if [ -z $PASS_WD ]; then
        echo "Password not set in $CA_PWD_FILE, auto generating a password..."
        #tmp_pwd=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c12)
        tmp_pwd=$(date +%F | md5sum; echo -n)
        fin_pwd=$(echo -n "$tmp_pwd" | awk '{print $1}')
        echo -n "PASS_WD=$fin_pwd" > $CA_PWD_FILE
      fi
 else
   echo "Couldn't load CA_PWD file, creating file and auto generating a password..."
   touch $CA_PWD_FILE
   #tmp_pwd=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -n 2)
   tmp_pwd=$(date +%F | md5sum; echo -n)
   fin_pwd=$(echo -n "$tmp_pwd" | awk '{print $1}')
   echo -n "PASS_WD=$fin_pwd" > $CA_PWD_FILE
 fi

 source $CA_PWD_FILE
 if [ -d $THIS_DIR/pki/certs/ca ]; then
   echo "CA certificate already exists, Using the existing CA private key !"
 else
   echo "Generating new CA private key"
   echo -e "$PASS_WD\n$PASS_WD" | $PKI_DIR/make_ca.sh
 fi
}

make_cert_dmclient()
{
 echo "Creating certificate for type: $CERT_TYPE with label:$LABEL_NAME"
 if [ -f $TMP_DIR/$LABEL_NAME ]; then
    echo "cert already exists, skipping!!"
 else
    echo -e "$LABEL_NAME\ny\nn\n\n\n$PASS_WD\ny\ny" | $PKI_DIR/make_client.sh
    touch $TMP_DIR/$LABEL_NAME
    unset CERT_TYPE
 fi
}

make_cert_broker()
{
 echo "Creating certificate for type: $CERT_TYPE with label:$LABEL_NAME"
 if [ -f $TMP_DIR/$LABEL_NAME ]; then
    echo "cert already exists, skipping!!"
 else
    echo -e "$LABEL_NAME\nn\ny\n\n\n$PASS_WD\ny\ny" | $PKI_DIR/make_client.sh
    touch $TMP_DIR/$LABEL_NAME
    unset CERT_TYPE
 fi
}

make_cert_ua_gen_label(){
	   CERT_DATA=()
           CERT_NAME="UA_ADDR"
           LABLE="$LABEL_NAME\nn\nn"
           PASSWORD="\n\n\n$PASS_WD\ny\ny"
           START_FROM=0
           END_ROTATION=$(grep -c $CERT_NAME $1)
 echo "Creating certificate for type: $CERT_TYPE with label:$LABEL_NAME"
 if [ -f $TMP_DIR/$LABEL_NAME ]; then
    echo "cert already exists, skipping!!"
 else
        while [ $START_FROM -lt $END_ROTATION  ]
	do
	       CERT_NO="$START_FROM"
               COMBINE_CERT=$CERT_NAME$CERT_NO
               CERT_DATA[$START_FROM]+=$(eval "echo \$$COMBINE_CERT")
	       NODE_TYPE=$NODE_TYPE"\n"${CERT_DATA[$START_FROM]}
	       START_FROM=$((START_FROM+1))
	done
        command="$LABLE$NODE_TYPE$PASSWORD"
        echo -e "$command" | $PKI_DIR/make_client.sh
        touch $TMP_DIR/$LABEL_NAME
    	unset CERT_TYPE
        unset NODE_TYPE
 fi
}
make_cert_group_gen_label(){
           CERT_DATA=()
           CERT_NAME="GROUP"
           LABLE="$LABEL_NAME\nn\nn\n"
           PASSWORD="\n\n$PASS_WD\ny\ny"
           START_FROM=1
           END_ROTATION=$(grep -c $CERT_NAME $1)
echo "Creating certificate for type: $CERT_TYPE with label:$LABEL_NAME"
 if [ -f $TMP_DIR/$LABEL_NAME ]; then
    echo "cert already exists, skipping!!"
 else
        while [ $START_FROM -lt $END_ROTATION  ]
	do
	       CERT_NO="$START_FROM"
               COMBINE_CERT=$CERT_NAME$CERT_NO
               CERT_DATA[$START_FROM]+=$(eval "echo \$$COMBINE_CERT")
	       NODE_TYPE=$NODE_TYPE"\n"${CERT_DATA[$START_FROM]}
	       START_FROM=$((START_FROM+1))
	done
        command="$LABLE$NODE_TYPE$PASSWORD"
        echo -e "$command" | $PKI_DIR/make_client.sh
        touch $TMP_DIR/$LABEL_NAME
    	unset CERT_TYPE
        unset NODE_TYPE
 fi

}
reset_all_certs()
{
 rm -rf $THIS_DIR/pki/certs
 rm -f $CA_PWD_FILE > /dev/null
 echo "[Success !]: Existing certificates are revoked successfully, Rerun with -c to create certs again"
}

provision_dmtree()
{
 if [ -f $TMP_DIR/provision_ok ]; then
     echo "DM_tree provisioning already done, skipping!!"
 else
     echo "Provisioning dm_tree @ $DM_TREE_PATH with latest dmclient-node certificates...."
     if [ ! -d "$DM_TREE_PATH/WSApp" ]; then
        echo "[Error !]: $DM_TREE_PATH : Not a valid dm_tree directory ! Please set a valid path to DM_TREE_PATH"
        exit 1
     fi
     if [ ! -d "$CERTS_DIR_PATH" ]; then
        echo "[Error !]: Need a valid path to PKI certs directory !"
        exit 1
     fi
     export LD_LIBRARY_PATH=${ESYNC_LIB}
     #To enable server download report messages set 'WSApp/Config/ServerDownloadReport' to true.
     $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Config/ServerDownloadReport -T bool -V "false"

     #To enable serial updates set 'WSApp/Config/SerializeReadyUpdate' to true.
     $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Config/SerializeReadyUpdate -T bool -V "true"

     #To enable downloads from CDN set 'WSApp/Config/PublicURLs' to true.
     $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Config/PublicURLs -T bool -V "false"

     #Set the payload download path 'WSApp/TmpDir'
     $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/TmpDir -T chr -V "$COMP_DLOAD_DIR"

     #Specify the address for broker to run from e.g., "tcp://localhost:9133"
     $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Broker/Address -NV -T chr -V tcp://localhost:9133

     #Set the node certificates for dmclient.
     cat $CERTS_DIR_PATH/ca/ca.pem | $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Broker/CABundle -NV -T bin -V -
     cat $CERTS_DIR_PATH/dmclient/cert.pem | $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Broker/Certificate -NV -T bin -V -
     cat $CERTS_DIR_PATH/dmclient/private.pem| $NODE_EDITOR -t $DM_TREE_PATH -W WSApp/Broker/PrivateKey -NV -T bin -V -
     touch $TMP_DIR/provision_ok
 fi
}
loop_cfg_files()
{
for cfg_file in $CONFIG_DIR/*.cfg
  do
    source $cfg_file
    if [[ $CERT_TYPE == "DMCLIENT" ]]; then
        make_cert_dmclient;
    elif [[ $CERT_TYPE == "BROKER" ]]; then
        make_cert_broker;
    elif  [[ $CERT_TYPE == *"GROUP"* ]];then
        make_cert_group_gen_label $cfg_file
    elif [[ $CERT_TYPE == *"_AGENT"* ]]; then
        make_cert_ua_gen_label $cfg_file
    else
	echo "$CERT_TYPE is not valid please provide valid type of cert " && exit 1
    fi
  done
}

case "$1" in
  -c)    make_ca;
         loop_cfg_files;
         provision_dmtree;
;;
  --reset)
      reset_all_certs;
;;
  -h) echo  "Help Options :"
      usage;
;;
  *)  echo "[Error !]: Invalid option ! : $1 "
      usage;
;;
esac
