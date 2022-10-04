SUMMARY = "Secure Socket Layer"
DESCRIPTION = "Secure Socket Layer (SSL) binary and related cryptographic tools."
HOMEPAGE = "http://www.openssl.org/"
BUGTRACKER = "http://www.openssl.org/news/vulnerabilities.html"
SECTION = "libs/network"

# "openssl | SSLeay" dual license
LICENSE = "openssl"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d57d511030c9d66ef5f5966bee5a7eff"

DEPENDS = "hostperl-runtime-native"

SRC_URI = "http://www.openssl.org/source/openssl-${PV}.tar.gz \
           file://run-ptest \
           file://openssl-c_rehash.sh \
           file://0001-Take-linking-flags-from-LDFLAGS-env-var.patch \
           "

SRC_URI:append:class-nativesdk = " \
           file://environment.d-openssl.sh \
           "

SRC_URI[md5sum] = "48278a48ec9bedb84565f7e741612fb4"
SRC_URI[sha256sum] = "74a2f756c64fd7386a29184dc0344f4831192d61dc2481a93a4c5dd727f41148"

inherit lib_package multilib_header ptest

#| engines/afalg/e_afalg.c: In function 'eventfd':
#| engines/afalg/e_afalg.c:110:20: error: '__NR_eventfd' undeclared (first use in this function)
#|      return syscall(__NR_eventfd, n);
#|                     ^~~~~~~~~~~~
EXTRA_OECONF:append:aarch64 = " no-afalgeng"

#| ./libcrypto.so: undefined reference to `getcontext'
#| ./libcrypto.so: undefined reference to `setcontext'
#| ./libcrypto.so: undefined reference to `makecontext'
EXTRA_OECONF:append:libc-musl = " -DOPENSSL_NO_ASYNC"

# [XL4] Prevent parallel make (i.e. no -j) on both build and install
# [XL4] OpenSSL 1.1.0% still have some issue with parallel make
PARALLEL_MAKE = ""
PARALLEL_MAKEINST = ""

do_configure () {
	os=${HOST_OS}
	case $os in
	linux-gnueabi |\
	linux-gnuspe |\
	linux-musleabi |\
	linux-muslspe |\
	linux-musl )
		os=linux
		;;
	*)
		;;
	esac
	target="$os-${HOST_ARCH}"
	case $target in
	linux-arm)
		target=linux-armv4
		;;
	linux-armeb)
		target=linux-armv4
		;;
	linux-aarch64*)
		target=linux-aarch64
		;;
	linux-sh3)
		target=linux-generic32
		;;
	linux-sh4)
		target=linux-generic32
		;;
	linux-i486)
		target=linux-elf
		;;
	linux-i586 | linux-viac3)
		target=linux-elf
		;;
	linux-i686)
		target=linux-elf
		;;
	linux-gnux32-x86_64 | linux-muslx32-x86_64 )
		target=linux-x32
		;;
	linux-gnu64-x86_64)
		target=linux-x86_64
		;;
	linux-mips)
		# specifying TARGET_CC_ARCH prevents openssl from (incorrectly) adding target architecture flags
		target="linux-mips32 ${TARGET_CC_ARCH}"
		;;
	linux-mipsel)
		target="linux-mips32 ${TARGET_CC_ARCH}"
		;;
	linux-gnun32-mips*)
		target=linux-mips64
		;;
	linux-*-mips64 | linux-mips64)
		target=linux64-mips64
		;;
	linux-*-mips64el | linux-mips64el)
		target=linux64-mips64
		;;
	linux-microblaze*|linux-nios2*)
		target=linux-generic32
		;;
	linux-powerpc)
		target=linux-ppc
		;;
	linux-powerpc64)
		target=linux-ppc64
		;;
	linux-riscv32)
		target=linux-generic32
		;;
	linux-riscv64)
		target=linux-generic64
		;;
	linux-supersparc)
		target=linux-sparcv9
		;;
	linux-sparc)
		target=linux-sparcv9
		;;
	darwin-i386)
		target=darwin-i386-cc
		;;
	esac

	useprefix=${prefix}
	if [ "x$useprefix" = "x" ]; then
		useprefix=/
	fi
	libdirleaf="$(echo ${libdir} | sed s:$useprefix::)"
	perl ./Configure ${EXTRA_OECONF} ${PACKAGECONFIG_CONFARGS} --prefix=$useprefix --openssldir=${libdir}/ssl-1.1 --libdir=$libdirleaf $target
}

do_install () {
	oe_runmake DESTDIR="${D}" MANDIR="${mandir}" MANSUFFIX=ssl install
	oe_multilib_header openssl/opensslconf.h
}

do_install:append:class-native () {
	# Install a custom version of c_rehash that can handle sysroots properly.
	# This version is used for example when installing ca-certificates during
	# image creation.
	install -Dm 0755 ${WORKDIR}/openssl-c_rehash.sh ${D}${bindir}/c_rehash
	sed -i -e 's,/etc/openssl,${sysconfdir}/ssl,g' ${D}${bindir}/c_rehash
}

do_install:append:class-nativesdk () {
	mkdir -p ${D}${SDKPATHNATIVE}/environment-setup.d
	install -m 644 ${WORKDIR}/environment.d-openssl.sh ${D}${SDKPATHNATIVE}/environment-setup.d/openssl.sh
}

do_install_ptest() {
	cp -r * ${D}${PTEST_PATH}

	# Putting .so files in ptest package will mess up the dependencies of the main openssl package
	# so we rename them to .so.ptest and patch the test accordingly
	mv ${D}${PTEST_PATH}/libcrypto.so ${D}${PTEST_PATH}/libcrypto.so.ptest
	mv ${D}${PTEST_PATH}/libssl.so ${D}${PTEST_PATH}/libssl.so.ptest
	sed -i 's/$target{shared_extension_simple}/".so.ptest"/' ${D}${PTEST_PATH}/test/recipes/90-test_shlibload.t
}

PACKAGES =+ "${PN}-engines"

FILES:${PN} =+ "${libdir}/ssl-1.1/*"
FILES:${PN}:append:class-nativesdk = " ${SDKPATHNATIVE}/environment-setup.d/openssl.sh"
FILES:${PN}-engines = "${libdir}/engines-1.1"

RDEPENDS:${PN}-ptest += "perl-module-file-spec-functions bash python"

BBCLASSEXTEND = "native nativesdk"
