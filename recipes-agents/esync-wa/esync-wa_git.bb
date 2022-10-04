# ---------------------------------------------------
# Copyright Copyright (C) 2022 Excelfore Corporation
# ---------------------------------------------------
SUMMARY = "eSync Workload Agent Implementation"
LICENSE = "CLOSED"
DEPENDS = "python-libua "

RDEPENDS:${PN} = " \
	python-libua \
	python3 \
	python3-jsonschema \
	python3-yamlloader \
	python3-pyyaml "

BRANCH = "main"
GIT_REPO = "github.com/esync-alliance/esync-wa.git;protocol=https"
SRC_URI = "git://${GIT_REPO};branch=${BRANCH}"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

FILES:${PN} = "${bindir}/workloadagent.py ${datadir}/esync-wa/*"

do_install () {
    install -m 0755 -d ${D}${bindir}
	install -m 0755 -d ${D}${datadir}/esync-wa
	install ${S}/wa-schema.json ${D}${datadir}/esync-wa/
	install ${S}/workloadagent.py ${D}${bindir}/
}
