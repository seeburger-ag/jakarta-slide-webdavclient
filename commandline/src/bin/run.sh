#!/bin/sh
# ---------------------------------------------------------------------------
# run.sh - Start Script for the Slide Client
#
# $Id: run.sh,v 1.2 2004/01/20 11:23:36 ib Exp $
# ---------------------------------------------------------------------------

SLIDE_CLIENT_HOME=`dirname $0`/..

CP=${CLASSPATH}
for lib in ${SLIDE_CLIENT_HOME}/lib/*.jar; do
    CP=${CP}:${lib};
done

if [ -d ${SLIDE_CLIENT_HOME}/classes ] ; then
    CP=${CP}:${SLIDE_CLIENT_HOME}/classes
fi

MAINCLASS=org.apache.webdav.cmd.Slide
java -classpath ${CP} ${MAINCLASS} $*
