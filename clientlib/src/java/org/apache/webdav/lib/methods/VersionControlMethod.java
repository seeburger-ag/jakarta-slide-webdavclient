/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/VersionControlMethod.java,v 1.1.2.1 2004/02/05 15:51:22 mholz Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/02/05 15:51:22 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.webdav.lib.methods;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.util.WebdavStatus;
import org.apache.util.XMLPrinter;


/**

 */
public class VersionControlMethod
    extends XMLResponseMethodBase implements DepthSupport {


    // -------------------------------------------------------------- Constants



    // ----------------------------------------------------- Instance Variables
        private String sComment, sCreatorDisplayName;

        private String sTarget = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public VersionControlMethod() {
        sComment ="none";
        sCreatorDisplayName ="unknown";
    }


    /**
     * Method constructor.
     */
    public VersionControlMethod(String path) {
        super(path);
    }

    public VersionControlMethod(String path, String sTarget) {
        super(path);
        this.sTarget = sTarget;

    }





    // ------------------------------------------------------------- Properties


    public int getDepth(){
        return 0;
    }

    public void setDepth(int depth){
    /*azblm: don't know if needed and / or allowed */
    }


    public void setRequestHeader(String headerName, String headerValue) {
        super.setRequestHeader(headerName, headerValue);
        if (sTarget != null) {
            // set the default utf-8 encoding, if not already present
            if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        }
    }



    // --------------------------------------------------- WebdavMethod Methods


    public String getName() {
        return "VERSION-CONTROL";
    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (sTarget != null){
            XMLPrinter printer = new XMLPrinter();
            printer.writeXMLHeader();

            printer.writeElement("D", "DAV:", "version-control", XMLPrinter.OPENING);
            printer.writeElement("D", "version", XMLPrinter.OPENING);

            printer.writeElement("D", "href", XMLPrinter.OPENING);
            printer.writeText(sTarget);
            printer.writeElement("D", "href", XMLPrinter.CLOSING);
            printer.writeElement("D", "version", XMLPrinter.CLOSING);
            printer.writeElement("D", "version-control", XMLPrinter.CLOSING);

            return printer.toString();
        }
        else
            return "";

    }

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        try
        {
            int code = getStatusLine().getStatusCode();
            if (code == WebdavStatus.SC_CONFLICT     ||
                code == WebdavStatus.SC_FORBIDDEN ) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }


}
