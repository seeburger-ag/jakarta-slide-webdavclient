/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/MoveMethod.java,v 1.1.2.1 2004/02/05 15:51:22 mholz Exp $
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
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;


/**
 * MOVE Method.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class MoveMethod
    extends XMLResponseMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public MoveMethod() {
    }


    /**
     * Method constructor.
     */
    public MoveMethod(String source) {
        super(source);
    }


    /**
     * Method constructor.
     */
    public MoveMethod(String source, String destination) {
        this(source);
        setDestination(destination);
    }


    /**
     * Method constructor.
     */
    public MoveMethod(String source, String destination, boolean overwrite) {
        this(source, destination);
        setOverwrite(overwrite);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Destination.
     */
    private String destination;


    /**
     * Overwrite.
     */
    private boolean overwrite = true;


    // ------------------------------------------------------------- Properties



    /**
     * Set a header value, redirecting the special case of the Overwrite and Destination
     * headers to {@link #setOverwrite} and {@link #setDestination} as appropriate.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Overwrite")){
            setOverwrite(! (headerValue.equalsIgnoreCase("F") ||
                           headerValue.equalsIgnoreCase("False") ) );
        }
        else if(headerName.equalsIgnoreCase("Destination")){
            setDestination(headerValue);
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }



    /**
     * Destination setter.
     *
     * @param destination New destination value
     */
    public void setDestination(String destination) {
        checkNotUsed();
        this.destination = destination;
    }


    /**
     * Destination getter.
     *
     * @return String destination value
     */
    public String getDestination() {
        return destination;
    }


    /**
     * Overwrite setter.
     *
     * @param overwrite New overwrite value
     */
    public void setOverwrite(boolean overwrite) {
        checkNotUsed();
        this.overwrite = overwrite;
    }


    /**
     * Overwrite getter.
     *
     * @return boolean Overwrite value
     */
    public boolean isOverwrite() {
        return overwrite;
    }


    /**
     * Overwrite getter.
     *
     * @return boolean Overwrite value
     */
    public boolean getOverwrite() {
        return overwrite;
    }


    // --------------------------------------------------- WebdavMethod Methods


    public String getName() {
        return "MOVE";
    }

    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
     * @param conn The connection being used to make the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        String absoluteDestination =
            conn.getProtocol().getScheme() + "://" + conn.getHost() + ":"
            + conn.getPort() + destination;
        super.setRequestHeader("Destination", absoluteDestination);

        if (!isOverwrite())
            super.setRequestHeader("Overwrite", "F");

    }


}

