#!/bin/sh

. ./setenv.sh

if [ $# -ne 3 ]
then
	echo "Usage: $0 <key_store_name> <key_password> <store_password>"
	exit
fi

KEY_STORE=$1
KEY_PASSWORD=$2
STORE_PASSWORD=$3

if [ "x$JAVA_HOME" != "x" ]; then
	KEYTOOL="$JAVA_HOME/bin/keytool"
else
	KEYTOOL="keytool"
fi

KEYTOOL_OPTION="-genkey -keyalg RSA -noprompt -alias jetty"
#KEYTOOL_OPTION="$KEYTOOL_OPTION -dname CN=commonName -dname OU=OrganizationUnit -dname  O=OrganizaionName -dname  L=Local -dname  S=State -dname  C=Country(2 letter)"
KEYTOOL_OPTION="$KEYTOOL_OPTION -dname CN= -dname OU= -dname  O= -dname  L= -dname  S= -dname  C="
KEYTOOL_OPTION="$KEYTOOL_OPTION -keystore $KEY_STORE -storepass $STORE_PASSWORD -keypass $KEY_PASSWORD"

$KEYTOOL $KEYTOOL_OPTION