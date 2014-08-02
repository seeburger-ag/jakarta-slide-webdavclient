/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/UnlockMethod.java,v 1.1.2.2 2004/02/06 10:03:55 ib Exp $
 * $Revision: 1.1.2.2 $
 * $Date: 2004/02/06 10:03:55 $
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
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.webdav.lib.WebdavState;

/**
 * UNLOCK Method.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 */
public class UnlockMethod
    extends HttpMethodBase {


    // ----------------------------------------------------- Instance Variables


    private String lockToken = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public UnlockMethod() {
    }


    /**
     * Method constructor.
     */
    public UnlockMethod(String path) {
        super(path);
    }


    /**
     * Method constructor.
     */
    public UnlockMethod(String path, String lockToken) {
        this(path);
        setLockToken(lockToken);
    }


    // ------------------------------------------------------------- Properties


    public void setLockToken(String lockToken) {
        checkNotUsed();
        this.lockToken = lockToken;
    }


    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return "UNLOCK";
    }

    /**
     * Set header, handling the special case of the lock-token header so
     * that it calls {@link #setLockToken} instead.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Lock-Token")){
            setLockToken(headerValue);
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }




    /**
     * Generate additional headers needed by the request.
     *
     * @param state HttpState token
     * @param conn The connection being used to send the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        super.setRequestHeader("Lock-Token", "<" + lockToken + ">");

    }

    protected void processResponseBody(HttpState state, HttpConnection conn) {
        if ((getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) &&
            (state instanceof WebdavState)) {
            ((WebdavState) state).removeLock(getPath(), lockToken);
        }
    }



}
