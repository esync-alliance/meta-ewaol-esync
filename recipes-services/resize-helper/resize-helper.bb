# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "Useful script to expand rootfs partition on initial boot"
SECTION = "console/tools"
LICENSE="CLOSED"

RDEPENDS:${PN} = "e2fsprogs-resize2fs gptfdisk parted util-linux"

# Use different resize-helper script when target is AWS EC2 instance
FILESEXTRAPATHS:prepend := "${@bb.utils.contains('PACKAGECONFIG', \
                                                 'aws-ec2', \
                                                 '${THISDIR}/files/aws-ec2:', \
                                                 '${THISDIR}/files/default:', \
                                                 d)}"
# SRC filenames are the same for all targets
SRC_URI = "file://resize-helper.service \
           file://resize-helper \
          "

inherit systemd

do_configure[noexec] = "1"

do_install () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/resize-helper.service ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/resize-helper ${D}${sbindir}
}

SYSTEMD_SERVICE:${PN} = "resize-helper.service"
