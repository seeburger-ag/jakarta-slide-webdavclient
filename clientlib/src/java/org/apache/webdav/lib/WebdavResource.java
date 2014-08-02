/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavResource.java,v 1.3.2.6 2004/03/29 14:55:44 ozeigermann Exp $
 * $Revision: 1.3.2.6 $
 * $Date: 2004/03/29 14:55:44 $
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

package org.apache.webdav.lib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.util.DOMUtils;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.util.WebdavStatus;
import org.apache.webdav.lib.methods.AclMethod;
import org.apache.webdav.lib.methods.AclReportMethod;
import org.apache.webdav.lib.methods.CheckinMethod;
import org.apache.webdav.lib.methods.CheckoutMethod;
import org.apache.webdav.lib.methods.CopyMethod;
import org.apache.webdav.lib.methods.DeleteMethod;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.LabelMethod;
import org.apache.webdav.lib.methods.LockMethod;
import org.apache.webdav.lib.methods.MkWorkspaceMethod;
import org.apache.webdav.lib.methods.MkcolMethod;
import org.apache.webdav.lib.methods.MoveMethod;
import org.apache.webdav.lib.methods.OptionsMethod;
import org.apache.webdav.lib.methods.PropFindMethod;
import org.apache.webdav.lib.methods.PropPatchMethod;
import org.apache.webdav.lib.methods.ReportMethod;
import org.apache.webdav.lib.methods.UncheckoutMethod;
import org.apache.webdav.lib.methods.UnlockMethod;
import org.apache.webdav.lib.methods.UpdateMethod;
import org.apache.webdav.lib.methods.VersionControlMethod;
import org.apache.webdav.lib.properties.AclProperty;
import org.apache.webdav.lib.properties.LockDiscoveryProperty;
import org.apache.webdav.lib.properties.PrincipalCollectionSetProperty;
import org.apache.webdav.lib.properties.ResourceTypeProperty;

/**
 * The class <code>WebdavResource</code> is an abstract representation
 * for WebDAV resource.<p>
 *
 * <pre>
 * A functional comparison of WebdavResource and JDK(It's different a lot).
 * ----------------------------------+-------------------------------------
 *   File class (JDK 1.3.x)          |  WebdavResource class
 * ----------------------------------+-------------------------------------
 *   File(String)                    |  WebdavResource(HttpURL)
 *   File(String, String)            |  X (need to escape)
 *   File(File, String)              |  WebdavResource(HttpURL, String)
 *   getName()                       |  getName()
 *   getParent()                     |  *see getHttpURL() and HttpURL
 *   getParentFile()                 |  X (not yet)
 *   getPath()                       |  getPath()
 *   isAbsolute()                    |  X
 *   getAbsolutePath()               |
 *   getAbsoluteFile()               |  X
 *   getCanonicalPath()              |
 *   getCanonicalFile()              |  X
 *   toURL()                         |  *see HttpURL
 *   canRead()                       |
 *   !canWrite()                     |  !isLocked()
 *   exists()                        |  exists()
 *   isDirectory()                   |  isCollection()
 *   isFile()                        |  !isCollection()
 *   isHidden()                      |  getIsHidden()
 *   lastModified()                  |  getGetLastModified()
 *   length()                        |  getGetContentLength()
 *   createNewFile()                 |  putMethod(String)
 *   delete()                        |  deleteMethod()
 *   deleteOnExit()                  |  X
 *   list()                          |  list()
 *   list(FilenameFilter)            |  X
 *   listFiles()                     |  listWebdavResources()
 *   listFiles(FilenameFilter)       |  X
 *   listFiles(FileFilter)           |  X
 *   mkdir()                         |  mkcolMethod()
 *   mkdirs()                        |  mkcolMethod()
 *   renameTo(File)                  |  moveMethod(String)
 *   setLastModified()               |  X
 *   setReadOnly()                   |  setOverwrite(boolean)
 *   listRoots()                     |  *see WebdavSession
 *   generateFile()                  |
 *   createTempFile(...)             |  setGetTempDir(String)
 *   compareTo(Object)               |  compareTo(Object)
 *   equals(Object)                  |  equals(Object)
 *   hashCode()                      |  X
 * ----------------------------------+-------------------------------------
 *   URL class (JDK 1.3.x)           |  Webdavresource and HttpURL classes
 * ----------------------------------+-------------------------------------
 *   getQuery()                      |  getQuery()
 *   getPath()                       |  getPath()
 *   getUserInfo()                   |  getUserInfo()
 *   getAuthority()                  |  getAuthority()
 *   getPort()                       |  getPort()
 *   getProtocol()                   |  getScheme()
 *   getHost()                       |  getHost()
 *   getFile()                       |  getPath()
 *   getRef()                        |  getFragmenet()
 *   hashCode()                      |  X
 *   sameFile()                      |
 *   toExternalForm()                |  toExternalForm()
 *   openConnection()                |
 *   openStream()                    |
 *   getContent()                    |  getMethodDataAsString()
 * ----------------------------------+-------------------------------------
 *   URLConnection class (JDK 1.3.x) |  HttpClient Library and more
 * ----------------------------------+-------------------------------------
 *   getFileNameMap()                |  X
 *   setFileNameMap()                |  X
 *   connect()                       |
 *   getURL()                        |  HttpURL#getURL()
 *   getContenetLength()()           |
 *   getContentType()                |
 *   getContentEncoding()            |
 *   getExpiration()                 |
 *   getDate()                       |
 *   getLastModified()               |
 *   getHeaderField()                |
 *   getHeaderFieldInt()             |  X
 *   getHeaderFielDate()             |  X
 *   getHeaderFieldKey()             |  X
 *   getHeaderFiled(int)             |  X
 *   getContenet()                   |
 *   getInputStream()                |  WebdavResource#getMethodData()
 *   getOutputStream()               |  WebdavResource#putMethod(...)
 *   setDoInput()                    |  X
 *   getDoInput()                    |  X
 *   setAllowUserInteraction()       |  *see WebdavException and WebdavStatus
 *   getAllowUserInteraction()       |  *see WebdavException and WebdavStatus
 *   setUseCaches()                  |
 *   getUseCaches()                  |
 *   getIfModifiedSince()            |  X
 *   setIfModifiedSince(boolean)     |  X
 *   setRequestProperty(...)         |  X
 *   getRequestProperty(...)         |  X
 *   guessContentTypeFromStream(...) |  X
 * ----------------------------------+-------------------------------------
 * </pre>
 *
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author Dirk Verbeeck
 * @author Remy Maucherat
 */
public class WebdavResource extends WebdavSession {


    // -------------------------------------------------------  Constructors

    /**
     * The default constructor.
     */
    protected WebdavResource() {
    }


    /**
     * The constructor.
     */
    protected WebdavResource(HttpClient client) {
        super();
        this.client = client;
    }

    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @param credentials The credentials to use for authentication.
     * @param action The action to set properties of this resource.
     * @param depth The depth to find properties.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, Credentials credentials, int action,
                          int depth)
        throws HttpException, IOException {

        setCredentials(credentials);
        setHttpURL(httpURL, action, depth);
    }


    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @param action The action to set properties of this resource.
     * @param depth The depth to find properties.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, int action, int depth)
        throws HttpException, IOException {

        setHttpURL(httpURL, action, depth);
    }


    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @param depth The depth to find properties.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, int depth)
        throws HttpException, IOException {

        setHttpURL(httpURL, defaultAction, depth);

    }


    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL)
        throws HttpException, IOException {

        setHttpURL(httpURL);
    }


    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @param proxyHost The hostname of the proxy to use.
     * @param proxyPort The port number of the proxy to use.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, String proxyHost, int proxyPort)
        throws HttpException, IOException {

        setProxy(proxyHost, proxyPort);
        setHttpURL(httpURL);
    }


    /**
     * The constructor.
     *
     * @param httpURL The specified http URL.
     * @param proxyHost The hostname of the proxy to use.
     * @param proxyPort The port number of the proxy to use.
     * @param proxyCredentials Credentials to use for proxy authentication.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, String proxyHost, int proxyPort,
                          Credentials proxyCredentials)
        throws HttpException, IOException {

        setProxy(proxyHost, proxyPort);
        setProxyCredentials(proxyCredentials);
        setHttpURL(httpURL);
    }


    /**
     * The constructor.
     * It must be put an escaped http URL as an argument.
     *
     * @param escapedHttpURL The escaped http URL string.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(String escapedHttpURL)
        throws HttpException, IOException {

        setHttpURL(escapedHttpURL);
    }


    /**
     * The constructor.
     * It must be put an escaped http URL as an argument.
     *
     * @param escapedHttpURL The escaped http URL string.
     * @param credentials The credentials used for Authentication.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(String escapedHttpURL, Credentials credentials)
        throws HttpException, IOException {

        setCredentials(credentials);
        setHttpURL(escapedHttpURL);
    }


    /**
     * The constructor.
     * It must be put an escaped http URL as an argument.
     *
     * @param escapedHttpURL The escaped http URL string.
     * @param proxyHost The hostname of the proxy to use.
     * @param proxyPort The port number of the proxy to use.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(String escapedHttpURL, String proxyHost,
                          int proxyPort) throws HttpException, IOException {

        setProxy(proxyHost, proxyPort);
        setHttpURL(escapedHttpURL);
    }

    /**
     * The constructor.
     * It must be put an escaped http URL as an argument.
     *
     * @param escapedHttpURL The escaped http URL string.
     * @param proxyHost The hostname of the proxy to use.
     * @param proxyPort The port number of the proxy to use.
     * @param proxyCredentials Credentials to use for proxy authentication.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(String escapedHttpURL, String proxyHost,
                          int proxyPort, Credentials proxyCredentials)
        throws HttpException, IOException {

        setProxy(proxyHost, proxyPort);
        setProxyCredentials(proxyCredentials);
        setHttpURL(escapedHttpURL);
    }


    /**
     * The constructor.
     *
     * @param httpURL The http URL.
     * @param additionalPath The added relative path.
     * @exception HttpException
     * @exception IOException
     * @see #setDefaultAction(int)
     */
    public WebdavResource(HttpURL httpURL, String additionalPath)
        throws HttpException, IOException {

        setHttpURL(httpURL, additionalPath);
    }


    // -------------------------------------- Constants for WebDAV properties.


    /**
     * The displayname property.
     */
    public static final String DISPLAYNAME = "displayname";


    /**
     * The getcontentlanguage property.
     */
    public static final String GETCONTENTLANGUAGE = "getcontentlanguage";


    /**
     * The getcontentlength property.
     */
    public static final String GETCONTENTLENGTH = "getcontentlength";


    /**
     * The getlastmodifed property.
     */
    public static final String GETLASTMODIFIED = "getlastmodified";


    /**
     * The creationdate property.
     */
    public static final String CREATIONDATE = "creationdate";


    /**
     * The resourcetype property.
     */
    public static final String RESOURCETYPE = "resourcetype";


    /**
     * The source property.
     */
    public static final String SOURCE = "source";


    /**
     * The getcontenttype property.
     */
    public static final String GETCONTENTTYPE = "getcontenttype";


    /**
     * The getetag property.
     */
    public static final String GETETAG = "getetag";


    /**
     * The ishidden property.
     */
    public static final String ISHIDDEN = "ishidden";


    /**
     * The iscollection property.
     */
    public static final String ISCOLLECTION = "iscollection";


    /**
     * The supportedlock property.
     */
    public static final String SUPPORTEDLOCK = "supportedlock";


    /**
     * The lockdiscovery property.
     */
    public static final String LOCKDISCOVERY = "lockdiscovery";


    // ------------------------------------------------------------ Constants


    /**
     * No action to find properties for this resource.
     */
    public static final int NOACTION = 1;


    /**
     * The action setting only the displayname for this resource.
     */
    public static final int NAME = 2;


    /**
     * The action setting the basic properties for this resource.
     */
    public static final int BASIC = 3;


    /**
     * The action setting the default DAV properties for this resource.
     */
    public static final int DEFAULT = 4;


    /**
     * The action setting the all properties for this resource.
     */
    public static final int ALL = 5;


    /**
     *
     */
    public static final int OPTIONS_WORKSPACE = 8;

    /**
     *
     */
    public static final int OPTIONS_VERSION_HISTORY = 9;

    public static final int LABEL_SET = 10;
    public static final int LABEL_REMOVE = 11;
    public static final int LABEL_ADD = 12;


    /**
     * Owner information for locking and unlocking.
     */
    public static final String defaultOwner = "Slide";


    /**
     * The true constant string.
     */
    public static final String TRUE = "1";


    /**
     * The false constant string.
     */
    public static final String FALSE = "0";


    /**
     * Date formats using for Date parsing.
     */
    public static final SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)
    };


    /**
     * GMT timezone.
     */
    protected final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");


    static {
        for (int i = 0; i < formats.length; i++) {
            formats[i].setTimeZone(gmtZone);
        }
    }


    // --------------------------------------------------- Instance Variables


    /**
     * The HttpURL to represent a WebDAV resource.
     */
    protected HttpURL httpURL;


    /**
     * Table of the hrefs gotten in a collection.
     */
    protected WebdavResources childResources = new WebdavResources();


    /**
     * The default action to find properties.
     */
    protected static int defaultAction = BASIC;


    /**
     * The default depth for WebDAV methods.
     */
    protected static int defaultDepth = DepthSupport.DEPTH_0;


    /**
     * The default temporary directory for the GET method.
     * @deprecated The client is responsible for disk I/O.
     */
    protected static String tempDirForGet;


    /**
     * The flag setter to use the disk for the GET method.
     * @deprecated The client is responsible for disk I/O.
     */
    protected static boolean useDiskForGet = true;


    /**
     * The flag to set the status code by propfind.
     */
    protected boolean thisResource;


    /**
     * The allowed HTTP methods.
     */
    protected Enumeration allowedMethods;


    /**
     * The capabilities of the WebDAV server.
     */
    protected Enumeration davCapabilities;


    /**
     * An WebdavResource flag to check its existence;
     */
    protected boolean exists;


    /**
     * An WebdavResource flag to check overwriting;
     */
    protected boolean overwrite;


    /**
     * An status code performed by HTTP methods at the most recent.
     */
    protected int latestStatusCode;


    /**
     * An status message performed by HTTP methods at the most recent.
     */
    protected String latestStatusMessage = "";


    /**
     * An WebDAV property, displayname.
     */
    protected String displayName = "";


    /**
     * An WebDAV property, getcontentlength.
     */
    protected long getContentLength;


    /**
     * An WebDAV property, getcontenttype.
     */
    protected String getContentType = "";


    /**
     * An WebDAV property, resourcetype.
     */
    protected ResourceTypeProperty resourceType;


    /**
     * An WebDAV property, getlastmodified.
     */
    protected long getLastModified;


    /**
     * An WebDAV property, creationdate.
     */
    protected long creationDate;


    /**
     * An WebDAV property, getetag.
     */
    protected String getEtag = "";

    /**
     * Owner information for locking and unlocking.
     */
    protected String owner = null;


    /**
     * An WebDAV property, ishidden.
     */
    protected boolean isHidden;


    /**
     * An WebDAV property, iscollection.
     */
    protected boolean isCollection;


    /**
     * An WebDAV property, supportedlock.
     */
    protected String supportedLock = "";


    /**
     * An WebDAV property, lockdiscovery.
     */
    protected LockDiscoveryProperty lockDiscovery;


    // --------------------------------------------------------- Basic settings

    /**
     * Generate and add the If header to the specified HTTP method.
     */
    protected void generateIfHeader(HttpMethod method) {

        if (client == null) return;
        if (method == null) return;

        WebdavState state = (WebdavState) client.getState();
        String[] lockTokens = state.getAllLocks(method.getPath());

        if (lockTokens.length == 0) return;

        StringBuffer ifHeaderValue = new StringBuffer();

        for (int i = 0; i < lockTokens.length; i++) {
            ifHeaderValue.append("(<").append(lockTokens[i]).append(">) ");
        }

        method.setRequestHeader("If", ifHeaderValue.toString());

    }


    /**
     * Parse the <code>java.util.Date</code> string for HTTP-date.
     *
     * @return The parsed date.
     */
    protected Date parseDate(String dateValue) {
        // TODO: move to the common util package related to http.
        Date date = null;
        for (int i = 0; (date == null) && (i < formats.length); i++) {
            try {
                synchronized (formats[i]) {
                    date = formats[i].parse(dateValue);
                }
            } catch (ParseException e) {
            }
        }

        return date;
    }


    /**
     * Set only the displayname property for this resource.
     *
     * @param depth The depth to find properties.
     */
    protected void setNameProperties(int depth)
        throws HttpException, IOException {

        Vector properties = new Vector();
        properties.addElement(DISPLAYNAME);

        setNamedProp(depth, properties);
    }


    /**
     * Sets the basic properties on a resource by indirectly issuing a PROPFIND
     * on the resource.
     *
     * <p>Properties retrieved include:
     *
     * <ul>
     *  <li>displayname</li>
     *  <li>getcontentlength</li>
     *  <li>getcontenttype</li>
     *  <li>resourcetype</li>
     *  <li>getlastmodified</li>
     *  <li>lockdiscovery</li>
     * </ul>
     *
     * @param depth The depth to find properties.
     */
    protected void setBasicProperties(int depth)
        throws HttpException, IOException {

        Vector properties = new Vector();
        properties.addElement(DISPLAYNAME);
        properties.addElement(GETCONTENTLENGTH);
        properties.addElement(GETCONTENTTYPE);
        properties.addElement(RESOURCETYPE);
        properties.addElement(GETLASTMODIFIED);
        properties.addElement(LOCKDISCOVERY);

        setNamedProp(depth, properties);
    }


    /**
     * Set the default properties on the resource by indirectly issuing a PROPFIND request
     * for a default set of properties.
     *
     * <p>Properties retrieved include:
     *
     * <ul>
     *  <li>creationdate</li>
     *  <li>displayname</li>
     *  <li>getcontentlanguage</li>
     *  <li>getcontentlength</li>
     *  <li>getcontenttype</li>
     *  <li>getetag</li>
     *  <li>getlastmodified</li>
     *  <li>lockdiscovery</li>
     *  <li>resourcetype</li>
     *  <li>source</li>
     *  <li>supportedlock</li>
     * </ul>
     *
     * @param depth The depth to find properties.
     */
    protected void setDefaultProperties(int depth)
        throws HttpException, IOException {

        Vector properties = new Vector();
        properties.addElement(CREATIONDATE);
        properties.addElement(DISPLAYNAME);
        properties.addElement(GETCONTENTLANGUAGE);
        properties.addElement(GETCONTENTLENGTH);
        properties.addElement(GETCONTENTTYPE);
        properties.addElement(GETETAG);
        properties.addElement(GETLASTMODIFIED);
        properties.addElement(LOCKDISCOVERY);
        properties.addElement(RESOURCETYPE);
        properties.addElement(SOURCE);
        properties.addElement(SUPPORTEDLOCK);

        setNamedProp(depth, properties);
    }


    /**
     * Set the named properties for this resource.
     *
     * @param depth The depth.
     * @param propertyNames The property-names.
     */
    protected void setNamedProp(int depth, Vector propertyNames)
        throws HttpException, IOException {

        Enumeration responses = propfindMethod(depth, propertyNames);
        setWebdavProperties(responses);
    }


    /**
     * Set all properties for this resource.
     *
     * @param depth The depth
     */
    protected void setAllProp(int depth)
        throws HttpException, IOException {

        Enumeration responses = propfindMethod(depth);
        setWebdavProperties(responses);
    }


    /**
     * Set WebDAV properties following to the given http URL.
     * This method is fundamental for getting information of a collection.
     *
     * @param responses An enumeration over {@link ResponseEntity} items, one
     * for each resource for which information was returned via PROPFIND.
     *
     * @exception HttpException
     * @exception IOException The socket error with a server.
     */
    protected void setWebdavProperties(Enumeration responses)
        throws HttpException, IOException {

        // Make the resources in the collection empty.
        childResources.removeAll();
        while (responses.hasMoreElements()) {

            ResponseEntity response =
                (ResponseEntity) responses.nextElement();

            boolean itself = false;
            String href = response.getHref();
            if (!href.startsWith("/"))
                href = URIUtil.getPath(href);
            String httpURLPath = httpURL.getEscapedPath();
            int compared = httpURLPath.compareTo(href);
            // Compare with the href path and requested-path itself.
            if (compared == 0 || compared == -1 && href.endsWith("/") ||
                compared == 1 && httpURLPath.endsWith("/")) {
                // Set the status code for this resource.
                if (response.getStatusCode() > 0)
                    setStatusCode(response.getStatusCode());
                setExistence(true);
                itself = true;
            }

            // Get to know each resource.
            WebdavResource workingResource = null;
            if (itself) {
                workingResource = this;
            }
            else {
                workingResource = createWebdavResource(client);
                workingResource.setDebug(debug);
            }

            // clear the current lock set
            workingResource.setLockDiscovery(null);

            // Process the resource's properties
            Enumeration properties = response.getProperties();
            while (properties.hasMoreElements()) {

                Property property = (Property) properties.nextElement();

                // ------------------------------  Checking WebDAV properties
                workingResource.processProperty(property);
            }

            String displayName = workingResource.getDisplayName();

            if (displayName == null || displayName.trim().equals("")) {
                displayName = getName(href);
            }
            if (!itself) {
                String myURI = httpURL.getEscapedURI();
                char[] childURI = (myURI + (myURI.endsWith("/") ? "" : "/")
                                   + URIUtil.encodePath(getName(href))
                    ).toCharArray();
		 HttpURL childURL = httpURL instanceof HttpsURL
                                           ? new HttpsURL(childURI)
		                           : new HttpURL(childURI);
		childURL.setRawAuthority(httpURL.getRawAuthority());						  
                workingResource.setHttpURL( childURL,
                                           NOACTION, defaultDepth);
                workingResource.setExistence(true);
                workingResource.setOverwrite(getOverwrite());
            }
            workingResource.setDisplayName(displayName);

            if (!itself)
                childResources.addResource(workingResource);
        }
    }


    // ------------------------------------------------------------ Properties


    /**
     * Set the default action for this resource.
     * The default action is set as 'BASIC' for the first time.
     *
     * ex)
     *  WebdavResource.NOACTION
     *  WebdavResource.NAME
     *  WebdavResource.BASIC
     *  WebdavResource.DEFAULT
     *  WebdavResource.ALL
     *
     * @param action The action type.
     */
    public static void setDefaultAction(int action) {
        defaultAction = action;
    }


    /**
     * Get the default action.
     *
     * @return The action type.
     */
    public static int getDefaultAction() {
        return defaultAction;
    }


    /**
     * Set the default action for this resource.
     *
     * ex)
     *   DepthSupport.DEPTH_0
     *   DepthSupport.DEPTH_1
     *   DepthSupport.DEPTH_INFINITY
     *
     * @param depth The depth.
     */
    public static void setDefaultDepth(int depth) {
        defaultDepth = depth;
    }


    /**
     * Get the default action.
     *
     * @return The depth.
     */
    public static int getDefaultDepth() {
        return defaultDepth;
    }


    /**
     * Get the default temporary directory for the GET method.
     *
     * @param tempDir The temporary directory.
     * @deprecated The given directory will not be used.
     */
    public static void setGetTempDir(String tempDir) {
        tempDirForGet = tempDir;
    }


    /**
     * Get the default temporary directory for the GET method.
     * The default temporary directory is "temp/".
     *
     * @return The temporary directory path.
     *         It's set by default, if it returns null.
     * @deprecated The returned directory is not used by the GET method.
     */
    public static String getGetTempDir() {
        return tempDirForGet;
    }



    /**
     * Set the use disk flag for the GET method.
     *
     * @param useDisk The use disk flag.
     * @deprecated This method has no effect.
     */
    public static void setGetUseDisk(boolean useDisk) {
        //useDiskForGet = useDisk;
    }


    /**
     * Get the use disk flag for the GET method.
     *
     * @return The current flag of the use disk.
     *         By default, it's true.
     * @deprecated This method always returns false.
     */
    public static boolean getGetUseDisk() {
        return false;
    }


    /**
     * Test that the httpURL is the same with the client.
     *
     * @return true if the given httpURL is the client for this resource.
     */
    protected synchronized boolean isTheClient() throws URIException {

        Credentials creds = client.getState().getCredentials(null,
                                                             client.getHost());
        String userName = null;
        String password = null;

        if (creds instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials upc = (UsernamePasswordCredentials) creds;
            userName = upc.getUserName();
            password = upc.getPassword();
        }
        HttpURL clientHttpURL =
            new HttpURL(userName, password,
                        client.getHost(), client.getPort());

        return clientHttpURL.getAuthority().equals(httpURL.getAuthority());
    }


    /**
     * Set the client for this resource.
     *
     * @exception IOException
     */
    protected void setClient() throws IOException {
        setClient(httpURL);
    }


    /**
     * Set the client for this resource and the given http URL.
     *
     * @param httpURL The http URL.
     * @exception IOException
     */
    protected synchronized void setClient(HttpURL httpURL) throws IOException {

        if ((client == null) || ((client != null) && !isTheClient())) {
            closeSession();
            client = getSessionInstance(httpURL);
        }
    }


    /**
     * Set the HttpURL for this WebdavResource.
     *
     * @param httpURL the specified HttpURL.
     * @param action The action to decide properties to find.
     * @param depth The depth to find properties.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL(HttpURL httpURL, int action, int depth)
        throws HttpException, IOException {

        this.httpURL = httpURL;
        setClient(httpURL);
        // make its existence false
        setExistence(false);
        setProperties(action, depth);
    }


    /**
     * Set the HttpURL for this WebdavResource.
     *
     * @param httpURL the specified HttpURL.
     * @param depth The depth to find properties.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL(HttpURL httpURL, int depth)
        throws HttpException, IOException {

        // Follow the default action.
        setHttpURL(httpURL, defaultAction, depth);
    }


    /**
     * Set the HttpURL for this WebdavResource.
     * It must be put an escaped path part of the http URL as an argument.
     *
     * @param httpURL The specified HttpURL.
     * @param additionalPath The added relative path.
     * @param action The action to decide properties to find.
     * @param depth The depth.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL
        (HttpURL httpURL, String additionalPath, int action, int depth)
        throws HttpException, IOException {

        setHttpURL(httpURL instanceof HttpsURL
                   ? new HttpsURL((HttpsURL) httpURL, additionalPath)
                   : new HttpURL(httpURL, additionalPath), action, depth);
    }


    /**
     * Set the HttpURL for this WebdavResource.
     * It must be put an escaped path part of the http URL as an argument.
     *
     * @param httpURL The specified HttpURL.
     * @param additionalPath The added relative path.
     * @param action The action to decide properties to find.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL
        (HttpURL httpURL, String additionalPath, int action)
        throws HttpException, IOException {

        setHttpURL(httpURL instanceof HttpsURL
                   ? new HttpsURL((HttpsURL) httpURL, additionalPath)
                   : new HttpURL(httpURL, additionalPath),
                   action, defaultDepth);
    }


    /**
     * Set the HttpURL for this WebdavResource.
     *
     * @param httpURL The specified HttpURL.
     * @param additionalPath The added relative path.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL(HttpURL httpURL, String additionalPath)
        throws HttpException, IOException {

        setHttpURL(httpURL instanceof HttpsURL
                   ? new HttpsURL((HttpsURL) httpURL, additionalPath)
                   : new HttpURL(httpURL, additionalPath),
                   defaultAction, defaultDepth);
    }


    /**
     * Set the HttpURL for this WebdavResource.
     *
     * @param httpURL the specified HttpURL.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL(HttpURL httpURL)
        throws HttpException, IOException {

        setHttpURL(httpURL, defaultDepth);
    }


    /**
     * Set the HttpURL of this WebdavResource.
     * It must be put an escaped http URL as an argument.
     *
     * @param escapedHttpURL The escaped http URL string.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(HttpURL)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setHttpURL(String escapedHttpURL)
        throws HttpException, IOException {

        setHttpURL(escapedHttpURL.startsWith("https")
                   ? new HttpsURL(escapedHttpURL)
                   : new HttpURL(escapedHttpURL));
    }


    /**
     * Get the HttpURL of this WebdavResource.
     *
     * @return httpURL the http URL.
     */
    public HttpURL getHttpURL() {
        return httpURL;
    }


    /**
     * Get the HttpURL except for userinfo.
     *
     * @return httpURL the http URL.
     */
    public HttpURL getHttpURLExceptForUserInfo()
        throws URIException {

        return httpURL instanceof HttpsURL ? new HttpsURL(httpURL.getRawURI())
                                           : new HttpURL(httpURL.getRawURI());
    }


    /**
     * Set the path part of this WebdavResource.
     *
     * @param path the specified path.
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(HttpURL)
     * @see #setHttpURL(java.lang.String)
     * @see #setUserInfo(java.lang.String, java.lang.String)
     */
    public void setPath(String path)
        throws HttpException, IOException {

        httpURL.setPath(path);
        setHttpURL(httpURL);
    }


    /**
     * Get the path part of this WebdavResource.
     * If the decoding of the path fails, this method will not throw an
     * exception but return the escaped path instead.
     *
     * @return the path for this WebdavResource.
     * @see org.apache.commons.httpclient.HttpURL#getPath()
     * @see #setPath(java.lang.String)
     */
    public String getPath() {
        try {
            return httpURL.getPath();
        } catch (URIException e) {
            return httpURL.getEscapedPath();
        }
    }


    /**
     * Get the name of this WebdavResource.
     * If the decoding of the name fails, this method will not throw an
     * exception but return the escaped name instead.
     *
     * @return the name of this WebdavResource.
     * @see org.apache.commons.httpclient.HttpURL#getName()
     */
    public String getName() {
        return getName(httpURL.getEscapedPath());
    }


    /**
     * Get the hostname of this WebdavResource.
     *
     * @return the hostname.
     * @exception URIException
     */
    public String getHost() throws URIException {
        return httpURL.getHost();
    }


    /**
     * Set the userinfo part of this WebdavResource.
     *
     * @exception HttpException
     * @exception IOException
     * @see #setHttpURL(HttpURL)
     * @see #setHttpURL(java.lang.String)
     * @see #setPath(java.lang.String)
     */
    public void setUserInfo(String userName, String password)
        throws HttpException, IOException {

        httpURL.setUserinfo(userName, password);
        setHttpURL(httpURL);
    }


    // ------------------------------------------------ DAV properties checking


    /**
     * Get the value of DAV property, displayname.
     *
     * @return The displayname string.
     */
    public String getDisplayName() {
        return displayName;
    }


    /**
     * Set the value of DAV property, displayname.
     *
     * @param displayName The displayname string.
     */
    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Get the value of DAV property, getcontentlength.
     *
     * @return The getcontentlength value.
     */
    public long getGetContentLength() {
        return getContentLength;
    }


    /**
     * Set the value of DAV property, getcontentlength.
     *
     * @param getContentLength The getcontentlength value.
     */
    protected void setGetContentLength(long getContentLength) {
        this.getContentLength = getContentLength;
    }


    /**
     * Set the value of DAV property, getcontentlength.
     *
     * @param getContentLength The getcontentlength value.
     */
    protected void setGetContentLength(String getContentLength) {
        try {
            this.getContentLength = Long.parseLong(getContentLength);
        } catch (NumberFormatException nfe) {
            // it's ok to ignore this error.
        }
    }


    /**
     * Get the value of DAV property, resourcetype.
     *
     * @return The resourcetype property.
     * @see #isCollection()
     */
    public ResourceTypeProperty getResourceType() {
        return resourceType;
    }


    /**
     * Set the value of DAV property, resourcetype.
     *
     * @param resourceType The resourcetype property.
     */
    protected void setResourceType(ResourceTypeProperty resourceType) {
        this.resourceType = resourceType;
    }


    /**
     * Get the value of DAV property, resourcetype
     *
     * @return The resourcetype string.
     * @see #getResourceType()
     * @see #getIsCollection()
     */
    public boolean isCollection() {
        if (getResourceType() == null) return false;
        return getResourceType().isCollection();
    }


    /**
     * Get the value of DAV property, getcontenttype.
     *
     * @return The getcontenttype string.
     */
    public String getGetContentType() {
        return getContentType;
    }


    /**
     * Set the value of DAV property, getcontenttype.
     *
     * @param getContentType The getcontenttype string.
     */
    protected void setGetContentType(String getContentType) {
        this.getContentType = getContentType;
    }

    /**
     * Set the content-type to use for this resource, for PUTs.
     * @param contentType The content-type string.
     */
    public void setContentType(String contentType) {
        this.getContentType = contentType;
    }

    /**
     * Get the value of DAV property, getlastmodified.
     *
     * @return The getlastmodified value.
     */
    public long getGetLastModified() {
        return getLastModified;
    }


    /**
     * Set the value of DAV property, getlastmodified.
     *
     * @param getLastModified The getlastmodified value.
     * @see #setGetLastModified(java.lang.String)
     */
    protected void setGetLastModified(long getLastModified) {
        this.getLastModified = getLastModified;
    }


    /**
     * Set the value of DAV property, getlastmodified.
     *
     * @param getLastModified The getlastmodified value.
     * @see #setGetLastModified(long)
     */
    protected void setGetLastModified(String getLastModified) {
        Date date = parseDate(getLastModified);
        if (date != null)
            this.getLastModified = date.getTime();
    }


    /**
     * Get the value of DAV property, creationdate.
     *
     * @return The creationdate string.
     */
    public long getCreationDate() {
        return creationDate;
    }


    /**
     * Set the value of DAV property, creationdate.
     *
     * @param creationDate The creationdate string.
     */
    protected void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }


    /**
     * Set the value of DAV property, creationdate.
     *
     * @param creationDate The creationdate string.
     */
    protected void setCreationDate(String creationDate) {
        Date date = parseDate(creationDate);
        if (date != null)
            this.creationDate = date.getTime();
    }


    /**
     * Get the value of DAV property, getetag.
     *
     * @return The getetag string.
     */
    public String getGetEtag() {
        return getEtag;
    }


    /**
     * Set the value of DAV property, getetag.
     *
     * @param getEtag The getetag string.
     */
    protected void setGetEtag(String getEtag) {
        this.getEtag = getEtag;
    }

    /**
     * Get the owner string, as used for locking purposes.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get the value of DAV property, supportedlock.
     *
     * @return The supportedlock string.
     */
    public String getSupportedLock() {
        return supportedLock;
    }


    /**
     * Set the value of DAV property, supportedlock.
     *
     * @param supportedLock The supportedlock string.
     */
    protected void setSupportedLock(String supportedLock) {
        this.supportedLock = supportedLock;
    }


    /**
     * Get the value of DAV property, lockdiscovery.
     *
     * @return The lockdiscovery property.
     */
    public LockDiscoveryProperty getLockDiscovery() {
        return lockDiscovery;
    }

    /**
     * Set the value of DAV property, lockdiscovery.
     *
     * @param lockDiscovery The lockdiscovery property.
     */
    protected void setLockDiscovery(LockDiscoveryProperty lockDiscovery) {
        this.lockDiscovery = lockDiscovery;
    }


    /**
     * Get the activelock owners for this resource.
     *
     * @return An enumeration of owners.
     */
    public Enumeration getActiveLockOwners() {
        if (lockDiscovery == null) return null;
        Lock[] activeLocks = lockDiscovery.getActiveLocks();
        if (activeLocks == null) return null;
        Vector buff = new Vector();
        int count = activeLocks.length;
        for (int i = 0; i < count; i++) {
            buff.addElement(activeLocks[i].getOwner());
        }
        return buff.elements();
    }


    /**
     * Test that this resource is locked.
     *
     * @return true if it's locked.
     */
    public boolean isLocked() {
        if (lockDiscovery == null) return false;
        Lock[] activeLocks = lockDiscovery.getActiveLocks();
        if (activeLocks == null) return false;
        for (int i = 0; i < activeLocks.length; i++) {
            if (activeLocks[i].getLockType() == Lock.TYPE_WRITE) return true;
        }
        return false;
    }


    /**
     * Get the value of DAV property, ishidden.
     *
     * @return true if it is hidden, otherwise false.
     */
    public boolean getIsHidden() {
        return isHidden;
    }


    /**
     * Set the value of DAV property, ishidden.
     *
     * @param isHidden
     */
    protected void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }


    /**
     * Set the value of DAV property, ishidden.
     *
     * @param isHidden
     */
    protected void setIsHidden(String isHidden) {
        this.isHidden = isHidden.equals(TRUE) ? true : false;
    }


    /**
     * Get the value of DAV property, iscollection
     *
     * @return true if it is collection, otherwise false.
     * @see #isCollection()
     */
    public boolean getIsCollection() {
        return isCollection;
    }


    /**
     * Set the value of DAV property, iscollection
     *
     * @param isCollection
     */
    protected void setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }


    /**
     * Set the value of DAV property, iscollection
     *
     * @param isCollection
     */
    protected void setIsCollection(String isCollection) {
        this.isCollection = isCollection.equals(TRUE) ? true : false;
    }


    // --------------------------------------- WebDAV Resource Public Methods


    /**
     * Set the properties for this resource.
     *
     * @param action The action to find properties for this resource.
     * @param depth the depth to which properties shall be found
     */
    public void setProperties(int action, int depth)
        throws HttpException, IOException {

        switch (action) {
            case NAME:
                setNameProperties(depth);
                break;
            case BASIC:
                setBasicProperties(depth);
                break;
            case DEFAULT:
                setDefaultProperties(depth);
                break;
            case ALL:
                setAllProp(depth);
                break;
            case NOACTION:
            default:
                break;
        }
    }


    /**
     * Set the properties for this resource.
     *
     * @param depth the depth to which properties shall be found
     */
    public void setProperties(int depth)
        throws HttpException, IOException {

        setProperties(defaultAction, depth);
    }

    /**
     * Refresh the properties of this resource
     * without changing the status of the previous command
     */
    protected void refresh() throws HttpException, IOException {
        int latestStatusCode = this.latestStatusCode;
        String latestStatusMessage = this.latestStatusMessage;
        setProperties(DepthSupport.DEPTH_0);
        this.latestStatusCode = latestStatusCode;
        this.latestStatusMessage = latestStatusMessage;
    }

    /**
     * Test if it exists.
     * This is a wrapper method for getExistence.
     *
     * @return true if it exists.
     * @see #getExistence()
     */
    public boolean exists() {
        return getExistence();
    }


    /**
     * Set its existence.
     *
     * @param exists The boolean value to be set for its existence.
     */
    protected void setExistence(boolean exists) {
        this.exists = exists;
    }


    /**
     * Get its existence.
     *
     * @return true if it exists.
     */
    public boolean getExistence() {
        return exists;
    }


    /**
     * Set the overwrite flag for COPY and MOVE.
     * Should be set before the method is executed.
     *
     * @param overwrite the overwrite flag
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }


    /**
     * Get the current value of the overwrite flag for COPY and MOVE.
     *
     * @return true if the current flag is overwriting.
     */
    public boolean getOverwrite() {
        return overwrite;
    }


    /**
     * Close the session of this client
     */
    public void close() throws IOException {
        closeSession();
    }


    /**
     * Get the lastest value of the status message by HTTP methods.
     *
     * @return The http status string.
     */
    public String getStatusMessage() {
        return latestStatusMessage;
    }


    /**
     * Get the lastest value of the status code by HTTP methods.
     *
     * @return The http status code.
     */
    public int getStatusCode() {
        return latestStatusCode;
    }


    /**
     * Set the lastest value of the status code by HTTP methods.
     *
     * @param statusCode the HTTP status code.
     */
    protected void setStatusCode(int statusCode) {
        setStatusCode(statusCode, null);
    }


    /**
     * Set the lastest value of the status code by HTTP methods.
     *
     * @param statusCode the HTTP status code.
     * @param message the additional message.
     */
    protected void setStatusCode(int statusCode, String message) {

        latestStatusCode = statusCode;
        latestStatusMessage = WebdavStatus.getStatusText(statusCode) +
            " (" + statusCode + ")" + ((message == null) ? "" : message);
    }


    /**
     * Get the allowed methods, checked by HTTP OPTIONS.
     *
     * @return the allowed HTTP methods.
     * @see #optionsMethod(java.lang.String)
     */
    public Enumeration getAllowedMethods() {
        return allowedMethods;
    }


    /**
     * Get the WebDAV capabilities, checked by HTTP OPTIONS.
     *
     * @return the WebDAV capabilities.
     * @see #optionsMethod(java.lang.String)
     */
    public Enumeration getDavCapabilities() {
        return davCapabilities;
    }


    /**
     * Get all resources in this collection with the depth 1.
     *
     * @return resources in this collection with the depth 1.
     * @exception HttpException
     * @exception IOException
     */
    public WebdavResources getChildResources()
        throws HttpException, IOException {

        setProperties(DepthSupport.DEPTH_1);

        return childResources;
    }


    /**
     * Get an array of resources denoting the WebDAV child resources in the
     * collection of this resources.
     *
     * @return An array of child resources in this resource.
     * @exception HttpException
     * @exception IOException
     */
    public WebdavResource[] listWebdavResources()
        throws HttpException, IOException {

        return getChildResources().listResources();
    }


    /**
     * Get an array of pathnames denoting the WebDAV resources in the
     * collection denoted by this pathname.
     *
     * @return An array of pathnames denoting the resources, null if an
     *         IOException occurs.
     */
    public String[] list() {

        try {
            setNameProperties(DepthSupport.DEPTH_1);
        } catch (IOException e) {
            return null;
        }
        Enumeration hrefs = childResources.getResourceNames();

        // To be atomic.
        Vector hrefList = new Vector();
        while (hrefs.hasMoreElements()) {
            hrefList.addElement((String) hrefs.nextElement());
        }
        // Calculate the size of the string array.
        int num = hrefList.size();
        String[] pathnames = new String[num];
        for (int i = 0; i < num; i++) {
            pathnames[i] = (String) hrefList.elementAt(i);
        }

        return pathnames;
    }


    /**
     * Get an array of pathnames and basic information denoting the WebDAV
     * resources in the denoted by this pathname.
     *
     * array 0: displayname
     * array 1: getcontentlength
     * array 2: iscollection or getcontentype
     * array 3: getlastmodifieddate
     *
     * @return An array of pathnames and more denoting the resources.
     * @exception HttpException
     * @exception IOException
     */
    public Vector listBasic()
        throws HttpException, IOException {

        setBasicProperties(DepthSupport.DEPTH_1);
        Enumeration hrefs = childResources.getResourceNames();

        Vector hrefList = new Vector();
        while (hrefs.hasMoreElements()) {
            try {
                String resourceName = (String) hrefs.nextElement();
                WebdavResource currentResource =
                    childResources.getResource(resourceName);

                String[] longFormat = new String[4];
                // displayname.
                longFormat[0] = currentResource.getDisplayName();

                long length = currentResource.getGetContentLength();
                // getcontentlength
                longFormat[1] = new Long(length).toString();
                // resourcetype
                ResourceTypeProperty resourceTypeProperty =
                    currentResource.getResourceType();
                // getcontenttype
                String getContentType =
                    currentResource.getGetContentType();
                longFormat[2] = resourceTypeProperty.isCollection() ?
                    "COLLECTION" : getContentType ;
                Date date = new Date(currentResource.getGetLastModified());
                // getlastmodified
                // Save the dummy what if failed.
                longFormat[3] = (date == null) ? "-- -- ----" :
                    // Print the local fancy date format.
                    DateFormat.getDateTimeInstance().format(date);
                hrefList.addElement(longFormat);
            } catch (Exception e) {
                // FIXME: After if's gotten an exception, any solution?
                if (debug > 0)
                    e.printStackTrace();
                //log.error(e,e);
            }
        }

        return hrefList;
    }


    /**
     * Set the URL encoding flag for this http URL.
     *
     * @param encodeURLs true if it is encoded.
     * @exception MalformedURLException
     * @exception IOException
     *
     * @deprecated  No longer has any effect.
     */
    public void setEncodeURLs(boolean encodeURLs) {

    }


    // -------------------------- General accessor to use http request methods.


    /**
     * Retrieve the current http client for this resource.
     *
     * @return The http client.
     * @see #executeHttpRequestMethod(HttpClient, HttpMethod)
     */
    public HttpClient retrieveSessionInstance()
        throws IOException {

        setClient();
        return client;
    }


    /**
     * Execute the http request method.  And get its status code.
     *
     * @param client The http client.
     * @param method The http method.
     * @return The status code.
     * @see #retrieveSessionInstance()
     */
    public int executeHttpRequestMethod(HttpClient client, HttpMethod method)
        throws IOException, HttpException {

        client.executeMethod(method);
        return method.getStatusCode();
    }


    // --------------------------------- WebDAV Request-method Public Methods

    /**
     * Updates the resource with a new set of aces.
     *
     * @param path the server relative path of the resource to which the given
     *        ACEs shall be applied
     * @param aces the ACEs to apply
     * @return true if the method succeeded
     */
    public boolean aclMethod(String path, Ace[] aces)
        throws HttpException, IOException {

        setClient();

        AclMethod method = new AclMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        for (int i=0; i<aces.length ; i++) {
            Ace ace = aces[i];
            method.addAce(ace);
        }
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Return the <code>AclProperty</code> for the resource at the given path
     *
     * @param path the server relative path of the resource to request
     * @return acl property, null if the server doesn't respond with
     * <code>AclProperty</code>
     */
    public AclProperty aclfindMethod(String path)
        throws HttpException, IOException {

        setClient();

        AclProperty acl = null;

        Vector properties = new Vector();
        properties.addElement(AclProperty.TAG_NAME);

        // Default depth=0, type=by_name
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   DepthSupport.DEPTH_0,
                                                   properties.elements());
        client.executeMethod(method);

        Enumeration responses = method.getResponses();
        if (responses.hasMoreElements()) {
            ResponseEntity response =
                (ResponseEntity) responses.nextElement();
            String href = response.getHref();

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties =
                method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property =
                    (Property) responseProperties.nextElement();
                if (property instanceof AclProperty) {
                    acl = (AclProperty)property;
                }

            }
        }

        return acl;
    }


    /**
     * Get the <code>PrincipalCollectionSetProperty</code> for the resource.
     *
     * @param path the server relative path of the resource to request
     * @return principal collection set Property, null if the server doesn't
     * respond with a <code>PrincipalCollectionSetProperty</code>
     */
    public PrincipalCollectionSetProperty principalCollectionSetFindMethod(
        String path) throws HttpException, IOException {

        setClient();

        PrincipalCollectionSetProperty set = null;

        Vector properties = new Vector();
        properties.addElement(PrincipalCollectionSetProperty.TAG_NAME);

        // Default depth=0, type=by_name
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   DepthSupport.DEPTH_0,
                                                   properties.elements());
        client.executeMethod(method);

        Enumeration responses = method.getResponses();
        if (responses.hasMoreElements()) {
            ResponseEntity response =
                (ResponseEntity) responses.nextElement();
            String href = response.getHref();

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties =
                method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property =
                    (Property) responseProperties.nextElement();
                if (property instanceof PrincipalCollectionSetProperty) {
                    set = (PrincipalCollectionSetProperty)property;
                }

            }
        }

        return set;
    }




    /**
     * Return the LockDiscoveryProperty for the resource at the given path
     *
     * @param path the server relative path of the resource to request
     * returns null if the server doesn't respond with a LockDiscoveryProperty
     */
    public LockDiscoveryProperty lockDiscoveryPropertyFindMethod(String path)
        throws HttpException, IOException {

        setClient();

        LockDiscoveryProperty set = null;

        Vector properties = new Vector();
        properties.addElement(LockDiscoveryProperty.TAG_NAME);

        // Default depth=0, type=by_name
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   DepthSupport.DEPTH_0,
                                                   properties.elements());
        client.executeMethod(method);

        Enumeration responses = method.getResponses();
        if (responses.hasMoreElements()) {
            ResponseEntity response =
                (ResponseEntity) responses.nextElement();
            String href = response.getHref();

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties =
                method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property =
                    (Property) responseProperties.nextElement();
                if (property instanceof LockDiscoveryProperty) {
                    set = (LockDiscoveryProperty)property;
                }

            }
        }

        return set;
    }


    /**
     * Get InputStream for the GET method.
     *
     * @return InputStream
     * @exception HttpException
     * @exception IOException
     */
    public InputStream getMethodData()
        throws HttpException, IOException {

        return getMethodData(httpURL.getPathQuery());
    }


    /**
     * Get InputStream for the GET method for the given path.
     *
     * @param path the server relative path of the resource to get
     * @return InputStream
     * @exception HttpException
     * @exception IOException
     */
    public InputStream getMethodData(String path)
        throws HttpException, IOException {

        setClient();

        GetMethod method = new GetMethod(URIUtil.encodePathQuery(path));
        client.executeMethod(method);

        int statusCode = method.getStatusLine().getStatusCode();
        setStatusCode(statusCode);

        if(statusCode >= 200 && statusCode < 300)
            return method.getResponseBodyAsStream();
        else
            throw new IOException("Couldn't get file");
    }


    /**
     * Get data as a String for the GET method.
     *
     * @return the contents of this resource as a string
     * @exception HttpException
     * @exception IOException
     */
    public String getMethodDataAsString()
        throws HttpException, IOException {

        return getMethodDataAsString(httpURL.getPathQuery());
    }


    /**
     * Get data as a String for the GET method for the given path.
     *
     * @param path the server relative path of the resource to get
     * @return the contents of the given resource as a string
     * @exception HttpException
     * @exception IOException
     */
    public String getMethodDataAsString(String path)
        throws HttpException, IOException {

        setClient();
        GetMethod method = new GetMethod(URIUtil.encodePathQuery(path));
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return method.getResponseBodyAsString();
    }


    /**
     * Execute the GET method for this WebdavResource path.
     *
     * @param file The local file.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean getMethod(File file)
        throws HttpException, IOException {

        return getMethod(httpURL.getPathQuery(), file);
    }


    /**
     * Execute the GET method for the given path.
     *
     * @param path the server relative path of the resource to get
     * @param file The local file.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean getMethod(String path, File file)
        throws HttpException, IOException {

        setClient();
        GetMethod method = new GetMethod(URIUtil.encodePathQuery(path));
        int statusCode = client.executeMethod(method);

        // Do a simple little loop to read the response back into the passed
        // file parameter.
        InputStream inStream = method.getResponseBodyAsStream();

        FileOutputStream fos = new FileOutputStream(file);
        byte buffer[] = new byte[2048];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) >= 0) {
            fos.write(buffer, 0, bytesRead);
        }
        inStream.close();
        fos.close();

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the PUT method for this resource.
     *
     * @param data The byte array.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(byte[] data)
        throws HttpException, IOException {

        boolean result = putMethod(httpURL.getPathQuery(), data);
        if (result) refresh();
        return result;
    }


    /**
     * Execute the PUT method for the given path.
     *
     * @param path the server relative path to put the data
     * @param data The byte array.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String path, byte[] data)
        throws HttpException, IOException {

        setClient();
        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));
        generateIfHeader(method);
        if (getGetContentType() != null && !getGetContentType().equals(""))
            method.setRequestHeader("Content-Type", getGetContentType());

        method.setRequestHeader("Content-Length", String.valueOf(data.length));
        method.setRequestBody(new ByteArrayInputStream(data));

        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the PUT method for this resource.
     *
     * @param is The input stream.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(InputStream is)
        throws HttpException, IOException {

        return putMethod(httpURL.getPathQuery(), is);
    }


    /**
     * Execute the PUT method for the given path.
     *
     * @param path the server relative path to put the data
     * @param is The input stream.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String path, InputStream is)
        throws HttpException, IOException {

        setClient();
        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));
        generateIfHeader(method);
        if (getGetContentType() != null && !getGetContentType().equals(""))
            method.setRequestHeader("Content-Type", getGetContentType());
        method.setRequestContentLength(PutMethod.CONTENT_LENGTH_CHUNKED);
        method.setRequestBody(is);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the PUT method for this WebdavResource.
     *
     * @param data String</cdoe> data to send.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String data)
        throws HttpException, IOException {

        boolean result = putMethod(httpURL.getPathQuery(), data);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the PUT method for the given path.
     *
     * @param path the server relative path to put the data
     * @param data String to send.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String path, String data)
        throws HttpException, IOException {

        setClient();
        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));
        generateIfHeader(method);
        if (getGetContentType() != null && !getGetContentType().equals(""))
            method.setRequestHeader("Content-Type", getGetContentType());
        method.setRequestBody(data);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the PUT method for this WebdavResource.
     *
     * @param file the filename to get on local.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(File file)
        throws HttpException, IOException {

        boolean result = putMethod(httpURL.getPathQuery(), file);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the PUT method for the given path.
     *
     * @param path the server relative path to put the given file
     * @param file the filename to get on local.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String path, File file)
        throws HttpException, IOException {

        setClient();
        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));
        generateIfHeader(method);
        if (getGetContentType() != null && !getGetContentType().equals(""))
            method.setRequestHeader("Content-Type", getGetContentType());
        long fileLength = file.length();
        method.setRequestContentLength(fileLength <= Integer.MAX_VALUE
                                       ? (int) fileLength
                                       : PutMethod.CONTENT_LENGTH_CHUNKED);
        method.setRequestBody(new FileInputStream(file));
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }



    /**
     * Execute the PUT method for this resource from the given url.
     * It's like a streaming copy about a resource of the specified remote url
     * to another remote url of this resource.
     *
     * @param url The URL to get a resource.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(URL url)
        throws HttpException, IOException {

        boolean result = putMethod(httpURL.getPathQuery(), url);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the PUT method for the given path from the given url.
     *
     * @param path the server relative path to put the data
     * @param url The URL to get a resource.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean putMethod(String path, URL url)
        throws HttpException, IOException {

        setClient();
        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));
        generateIfHeader(method);
        if (getGetContentType() != null && !getGetContentType().equals(""))
            method.setRequestHeader("Content-Type", getGetContentType());
        method.setRequestBody(url.openStream());
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute OPTIONS method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean optionsMethod()
        throws HttpException, IOException {

        return optionsMethod(httpURL.getPath());
    }


    /**
     * Execute OPTIONS method for the given path.
     *
     * @param path the server relative path of the resource to request
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @see #getAllowedMethods()
     */
    public boolean optionsMethod(String path)
        throws HttpException, IOException {

        setClient();
        OptionsMethod method;
        if (path.trim().equals("*"))
            method = new OptionsMethod("*");
        else
            method = new OptionsMethod(URIUtil.encodePath(path));
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        if  (statusCode >= 200 && statusCode < 300) {
            // check if the specific method is possbile
            allowedMethods = method.getAllowedMethods();
            // check WebDAV capabilities.
            davCapabilities = method.getDavCapabilities();
            return true;
        }

        return false;
    }


    /**
     * Execute OPTIONS method for the given path.
     *
     * @param path the server relative path to send the request
     * @param aMethod a method to check it's supported.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean optionsMethod(String path, String aMethod)
        throws HttpException, IOException {

        if (aMethod != null && optionsMethod(path)) {
            while (allowedMethods.hasMoreElements()) {
                if (aMethod.equalsIgnoreCase((String)
                                                 allowedMethods.nextElement()))
                    return true;
            }
        }

        return false;
    }


    /**
     * Execute OPTIONS method for the given http URL.
     *
     * @param httpURL the http URL.
     * @return the allowed methods and capabilities.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration optionsMethod(HttpURL httpURL)
        throws HttpException, IOException {

        HttpClient client = getSessionInstance(httpURL, true);

        OptionsMethod method = new OptionsMethod(httpURL.getEscapedPath());
        client.executeMethod(method);

        Vector options = new Vector();
        int statusCode = method.getStatusLine().getStatusCode();
        if (statusCode >= 200 && statusCode < 300) {
            // check if the specific method is possbile
            Enumeration allowedMethods = method.getAllowedMethods();
            while (allowedMethods.hasMoreElements()) {
                options.addElement(allowedMethods.nextElement());
            }
            // check WebDAV capabilities.
            Enumeration davCapabilities = method.getDavCapabilities();
            while (davCapabilities.hasMoreElements()) {
                options.addElement(davCapabilities.nextElement());
            }
            Enumeration responses = method.getResponses();
            if (responses.hasMoreElements()) {
                ResponseEntity response =
                    (ResponseEntity) responses.nextElement();
                Enumeration workspaces = response.getWorkspaces();
                String sResult="";
                while (workspaces.hasMoreElements()){
                    sResult += workspaces.nextElement().toString();
                }
                Enumeration histories = response.getHistories();
                while (histories.hasMoreElements()){
                    sResult += histories.nextElement().toString();
                }
                // Set status code for this resource.
                if ((thisResource == true) && (response.getStatusCode() > 0))
                    setStatusCode(response.getStatusCode());
                thisResource = false;
                options.addElement(sResult);
            }
        }

        return options.elements();
    }


    /**
     * Execute OPTIONS method for the given http URL, DELTAV
     *
     * @param httpURL the http URL.
     * @return the allowed methods and capabilities.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration optionsMethod(HttpURL httpURL, int type)
        throws HttpException, IOException {

        HttpClient client = getSessionInstance(httpURL, true);

        OptionsMethod method = new OptionsMethod(httpURL.getEscapedPath(),
                                                 type);
        client.executeMethod(method);

        Vector options = new Vector();
        int statusCode = method.getStatusLine().getStatusCode();
        if  (statusCode >= 200 && statusCode < 300) {
            Enumeration responses = method.getResponses();
            if (responses.hasMoreElements()) {
                ResponseEntity response =
                    (ResponseEntity) responses.nextElement();
                // String sResult="";
                if (type == OPTIONS_WORKSPACE){
                    Enumeration workspaces = response.getWorkspaces();
                    while (workspaces.hasMoreElements()){
                        options.add(workspaces.nextElement().toString());
                    }
                } else if (type == OPTIONS_VERSION_HISTORY){
                    Enumeration histories = response.getHistories();
                    while (histories.hasMoreElements()){
                        options.add(histories.nextElement().toString());
                    }
                }

                // Set status code for this resource.
                if ((thisResource == true) && (response.getStatusCode() > 0))
                    setStatusCode(response.getStatusCode());
                thisResource = false;
                // options.addElement(sResult);
            }
        }

        return options.elements();
    }


    /**
     * Execute OPTIONS method for the given path.
     *
     * @param path the server relative path of the resource to request
     * @return the allowed methods and capabilities.
     * @exception HttpException
     * @exception IOException
     * @see #getAllowedMethods()
     */
    public Enumeration optionsMethod(String path, int type)
        throws HttpException, IOException {

        setClient();

        OptionsMethod method = new OptionsMethod(URIUtil.encodePath(path),
                                                 type);
        client.executeMethod(method);

        Vector options = new Vector();
        int statusCode = method.getStatusLine().getStatusCode();
        if  (statusCode >= 200 && statusCode < 300) {
            Enumeration responses = method.getResponses();
            if (responses.hasMoreElements()) {
                ResponseEntity response =
                    (ResponseEntity) responses.nextElement();
                // String sResult="";
                if (type == OPTIONS_WORKSPACE){
                    Enumeration workspaces = response.getWorkspaces();
                    while (workspaces.hasMoreElements()){
                        options.add(workspaces.nextElement().toString());
                    }
                } else if (type == OPTIONS_VERSION_HISTORY){
                    Enumeration histories = response.getHistories();
                    while (histories.hasMoreElements()){
                        options.add(histories.nextElement().toString());
                    }
                }

                // Set status code for this resource.
                if ((thisResource == true) && (response.getStatusCode() > 0))
                    setStatusCode(response.getStatusCode());
                thisResource = false;
                // options.addElement(sResult);
            }
        }

        return options.elements();
    }

    /**
     * Execute a LABEL method on the current path, setting the given label
     *
     * @param labelname the label to set
     * @param type the type of action. One of:
     *        <ul>
     *        <li> LABEL_ADD
     *        <li> LABEL_REMOVE
     *        <li> LABEL_SET
     *        </ul>
     * @return true if the method succeeded
     */
    public boolean labelMethod(String labelname, int type)
        throws HttpException, IOException {
        return labelMethod(httpURL.getPath(), labelname, type);
    }

    /**
     * Execute a LABEL method on the given path, setting the given label
     *
     * @param path the server relative path of the resource to act on
     * @param labelname the label to set
     * @param type the type of action. One of:
     *        <ul>
     *        <li> LABEL_ADD
     *        <li> LABEL_REMOVE
     *        <li> LABEL_SET
     *        </ul>
     * @return true if the method succeeded
     */
    public boolean labelMethod(String path, String labelname, int type)
        throws HttpException, IOException {
        int labeltype=0;

        switch(type) {
            case LABEL_SET:
                labeltype = LabelMethod.LABEL_SET;
                break;
            case LABEL_REMOVE:
                labeltype = LabelMethod.LABEL_REMOVE;
                break;
            case LABEL_ADD:
                labeltype = LabelMethod.LABEL_ADD;
                break;
        }

        setClient();
        LabelMethod method = new LabelMethod(URIUtil.encodePath(path),
                                             labeltype, labelname);

        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }

    /**
     * Execute the REPORT method.
     */
    public Enumeration reportMethod(HttpURL httpURL, int depth)

        throws HttpException, IOException {
        setClient();
        // Default depth=0, type=by_name
        ReportMethod method = new ReportMethod(httpURL.getEscapedPath(),
                                               depth);
        client.executeMethod(method);

        Vector results = new Vector();

        Enumeration responses = method.getResponses();
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity) responses.nextElement();
            String href = response.getHref();
            String sResult = href;

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties = method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property = (Property) responseProperties.nextElement();
                sResult += "\n" + property.getName() + ":\t" +
                    DOMUtils.getTextValue(property.getElement());

            }
            results.addElement(sResult);
        }

        return results.elements();
    }

    public Enumeration reportMethod(HttpURL httpURL, Vector properties)

        throws HttpException, IOException {
        setClient();
        // Default depth=0, type=by_name
        ReportMethod method =
            new ReportMethod(httpURL.getEscapedPath(), DepthSupport.DEPTH_0,
                             properties.elements());
        client.executeMethod(method);

        return method.getResponses();
    }

    public Enumeration reportMethod(HttpURL httpURL, Vector properties,
                                    int depth)

        throws HttpException, IOException {
        setClient();
        // Default depth=0, type=by_name
        ReportMethod method = new ReportMethod(httpURL.getEscapedPath(), depth,
                                               properties.elements());
        client.executeMethod(method);

        /*first draft, does work anyhow
         Enumeration results = method.getAllResponseURLs();

         return results;*/
        /*  Enumeration responses = method.getResponses();
         ResponseEntity response = (ResponseEntity) responses.nextElement();
         String href = (String) response.getHref();
         Enumeration results = method.getResponseProperties(href);

         return results;*/

        Vector results = new Vector();

        Enumeration responses = method.getResponses();
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity) responses.nextElement();
            String href = response.getHref();
            String sResult = href;

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties = method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property = (Property) responseProperties.nextElement();
                sResult += "\n" + property.getName() + ":\t" +
                    DOMUtils.getTextValue(property.getElement());
                // results.addElement(DOMUtils.getTextValue(property.getElement()));
            }
            results.addElement(sResult);
        }

        return results.elements();
    }


    // locate-by-history Report
    public Enumeration reportMethod(HttpURL httpURL, Vector properties,
                                    Vector histUri, int depth)

        throws HttpException, IOException {
        setClient();
        // Default depth=0, type=by_name
        ReportMethod method = new ReportMethod(httpURL.getEscapedPath(), depth,
                                               properties.elements(),
                                               histUri.elements());
        client.executeMethod(method);

        Vector results = new Vector();

        Enumeration responses = method.getResponses();
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity) responses.nextElement();
            String href = response.getHref();
            String sResult = href;

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties = method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property = (Property) responseProperties.nextElement();
                sResult += "\n" + property.getName() + ":\t" +
                    DOMUtils.getTextValue(property.getElement());
            }
            results.addElement(sResult);
        }

        return results.elements();
    }
    // expand-property Report
    public Enumeration reportMethod(HttpURL httpURL, String sQuery, int depth)

        throws HttpException, IOException {
        setClient();
        // Default depth=0, type=by_name
        ReportMethod method = new ReportMethod(httpURL.getEscapedPath(), depth,
                                               sQuery);
        client.executeMethod(method);

        Vector results = new Vector();

        Enumeration responses = method.getResponses();
        while (responses.hasMoreElements()) {
            ResponseEntity response = (ResponseEntity) responses.nextElement();
            //String href = (String) response.getHref();
            String sResult; //= href;

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            sResult = response.toString();
            /*while (responseProperties.hasMoreElements()) {
             Property property = (Property) responseProperties.nextElement();
             sResult += "\t" + DOMUtils.getTextValue(property.getElement());

             }*/
            results.addElement(sResult);
        }

        return results.elements();
    }


    /**
     * Execute PROPFIND method with allprop for this WebdavResource.
     * Get list of all WebDAV properties on this WebDAV resource.
     *
     * <p>Once used this method, the the status code in the 207
     * reponse is need to be set for the method of WebdavResource.
     *
     * <p>The values of DepthSupport.DEPTH_0, DepthSupport.DEPTH_1,
     * DepthSupport.DEPTH_INFINITY is possbile for the depth.
     *
     * @param depth
     * @return an enumeration of <code>ResponseEntity</code>
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(int depth)
        throws HttpException, IOException {

        thisResource = true;
        return propfindMethod(httpURL.getPath(), depth);
    }


    /**
     * Execute PROPFIND method with allprop for the given path.
     * Get list of all WebDAV properties on the given resource.
     *
     * <p>Once used this method, the the status code in the 207
     * reponse is need to be set for the method of WebdavResource.
     *
     * <p>The values of DepthSupport.DEPTH_0, DepthSupport.DEPTH_1,
     * DepthSupport.DEPTH_INFINITY is possbile for the depth.
     *
     * @param path the server relative path of the resource to request
     * @param depth
     * @return an enumeration of <code>ResponseEntity</code>
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(String path, int depth)
        throws HttpException, IOException {

        setClient();
        // Change the depth for allprop
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   depth);
        // Default depth=infinity, type=allprop
        int status = client.executeMethod(method);

        // Set status code for this resource.
        if (thisResource == true) {
            setStatusCode(status);
        }
        // Also accept OK sent by buggy servers.
        if (status != HttpStatus.SC_MULTI_STATUS
            && status != HttpStatus.SC_OK) {
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
        }
        thisResource = false;

        return method.getResponses();
    }



    /**
     * Execute PROPFIND method with by propname for this resource.
     * Get list of named WebDAV properties on this resource.
     *
     * <p>Once used this method, the the status code in the 207
     * reponse is need to be set for the method of WebdavResource.
     *
     * <p>The values of DepthSupport.DEPTH_0, DepthSupport.DEPTH_1
     * DepthSupport.DEPTH_INFINITY is possbile for the depth.
     *
     * @param depth The depth.
     * @param properties The named properties.
     * @return an enumeration of <code>ResponseEntity</code>
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(int depth, Vector properties)
        throws HttpException, IOException {

        thisResource = true;
        return propfindMethod(httpURL.getPath(), depth, properties);
    }


    /**
     * Execute PROPFIND method with by propname for the given path.
     * Get list of named WebDAV properties on the given resource.
     *
     * <p>Once used this method, the the status code in the 207
     * reponse is need to be set for the method of WebdavResource.
     *
     * <p>The values of DepthSupport.DEPTH_0, DepthSupport.DEPTH_1
     * DepthSupport.DEPTH_INFINITY is possbile for the depth.
     *
     * @param path the server relative path of the resource to request
     * @param depth The depth.
     * @param properties The named properties.
     * @return an enumeration of <code>ResponseEntity</code>
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(String path, int depth,
                                      Vector properties)
        throws HttpException, IOException {

        setClient();
        // Change the depth for prop
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   depth,
                                                   properties.elements());
        int status = client.executeMethod(method);

        // Set status code for this resource.
        if (thisResource == true) {
            // Set the status code.
            setStatusCode(method.getStatusLine().getStatusCode());
        }
        // Also accept OK sent by buggy servers.
        if (status != HttpStatus.SC_MULTI_STATUS
            && status != HttpStatus.SC_OK) {
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
        }
        thisResource = false;

        return method.getResponses();
    }


    /**
     * Execute PROPFIND method for this WebdavResource.
     * Get list of given WebDAV properties on this WebDAV resource.
     *
     * @param propertyName the WebDAV property to find.
     * @return Enumeration list of WebDAV properties on a resource.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(String propertyName)
        throws HttpException, IOException {

        Vector property = new Vector();
        property.addElement(propertyName);

        thisResource = true;
        return propfindMethod(httpURL.getPath(), property);
    }


    /**
     * Execute PROPFIND method for the given WebdavResource path.
     * Get list of given WebDAV properties on this WebDAV resource.
     *
     * @param path the server relative path of the resource to request
     * @param propertyName the WebDAV property to find.
     * @return Enumeration list of WebDAV properties on a resource.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(String path, String propertyName)
        throws HttpException, IOException {

        Vector property = new Vector();
        property.addElement(propertyName);

        thisResource = false;
        return propfindMethod(path, property);
    }


    /**
     * Execute PROPFIND method for this WebdavResource.
     * Get list of given WebDAV properties on this WebDAV resource.
     *
     * @param properties the WebDAV properties to find.
     * @return Enumeration list of WebDAV properties on a resource.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(Vector properties)
        throws HttpException, IOException {

        thisResource = true;
        return propfindMethod(httpURL.getPath(), properties);
    }


    /**
     * Execute PROPFIND method for the given path and properties.
     * Get list of given WebDAV properties on the given resource.
     *
     * @param path the server relative path of the resource to request
     * @param properties the WebDAV properties to find.
     * @return Enumeration list of WebDAV properties on a resource.
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration propfindMethod(String path, Vector properties)
        throws HttpException, IOException {

        setClient();
        // Default depth=0, type=by_name
        PropFindMethod method = new PropFindMethod(URIUtil.encodePath(path),
                                                   DepthSupport.DEPTH_0,
                                                   properties.elements());
        int status = client.executeMethod(method);

        // Also accept OK sent by buggy servers.
        if (status != HttpStatus.SC_MULTI_STATUS
            && status != HttpStatus.SC_OK) {
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
        }

        // It contains the results.
        Vector results = new Vector();

        Enumeration responses = method.getResponses();
        if (responses.hasMoreElements()) {
            ResponseEntity response =
                (ResponseEntity) responses.nextElement();
            String href = response.getHref();

            // Set status code for this resource.
            if ((thisResource == true) && (response.getStatusCode() > 0))
                setStatusCode(response.getStatusCode());
            thisResource = false;

            Enumeration responseProperties =
                method.getResponseProperties(href);
            while (responseProperties.hasMoreElements()) {
                Property property =
                    (Property) responseProperties.nextElement();
                results.addElement(property.getPropertyAsString());
            }
        }

        return results.elements();
    }


    /**
     * Execute PROPATCH method for this WebdavResource.
     *
     * @param propertyName the name of the property to set
     * @param propertyValue the value of the property to set
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(String propertyName, String propertyValue)
        throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(),
                                         propertyName, propertyValue, true);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for this resource with the given property.
     *
     * @param propertyName the property name string (in "DAV:" namespace)
     * @param propertyValue the property value string
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's being set, false if it's being removed
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(String propertyName, String propertyValue,
                                   boolean action) throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(),
                                         propertyName, propertyValue, action);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for this WebdavResource.
     *
     * @param propertyName the name of the property to set
     * @param propertyValue the value of the property to set
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(PropertyName propertyName,
                                   String propertyValue)
        throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(),
                                         propertyName, propertyValue, true);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for this resource with the given property.
     *
     * @param propertyName the name of the property to set
     * @param propertyValue the value of the property to set
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's being set, false if it's being removed
     * @return true if the method is succeeded
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(PropertyName propertyName,
                                   String propertyValue, boolean action)
        throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(),
                                         propertyName, propertyValue, action);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for the given WebdavResource.
     *
     * @param path the server relative path of the resource to act on
     * @param propertyName the property name in "DAV:" namespace
     * @param propertyValue the property value string
     * @return true if the method is succeeded
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(String path, String propertyName,
                                   String propertyValue) throws HttpException, IOException {

        Hashtable property = new Hashtable();
        property.put(propertyName, propertyValue);
        return proppatchMethod(path, property, true);
    }


    /**
     * Execute PROPATCH method for the specified resource with the given
     * property.
     *
     * @param path the server relative path of the resource to act on
     * @param propertyName the property name string (in "DAV:" namespace)
     * @param propertyValue the property value string
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's to be set, false if it's to be removed
     * @return true if the method is succeeded
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(String path, String propertyName,
                                   String propertyValue, boolean action)
        throws HttpException, IOException {

        Hashtable property = new Hashtable();
        property.put(propertyName, propertyValue);
        return proppatchMethod(path, property, action);
    }


    /**
     * Execute PROPATCH method for the given WebdavResource.
     *
     * @param path the server relative path of the resource to act on
     * @param propertyName the property name.
     * @param propertyValue the property value.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(String path, PropertyName propertyName,
                                   String propertyValue) throws HttpException, IOException {

        Hashtable property = new Hashtable();
        property.put(propertyName, propertyValue);

        return proppatchMethod(path, property, true);
    }


    /**
     * Execute PROPATCH method for the given resource with the given
     * properties.
     *
     * @param path the server relative path of the resource to act on
     * @param propertyName the property name
     * @param propertyValue the property value string
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's to be set, false if it's to be removed
     * @return true if the method is succeeded
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(String path, PropertyName propertyName,
                                   String propertyValue, boolean action)
        throws HttpException, IOException {

        Hashtable property = new Hashtable();
        property.put(propertyName, propertyValue);
        return proppatchMethod(path, property, action);
    }


    /**
     * Execute PROPATCH method for this WebdavResource.
     *
     * @param properties name and value pairs to set
     * (name can be a String or PropertyName)
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(Hashtable properties)
        throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(), properties, true);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for this resource with the given properties.
     *
     * @param properties the name(= <code>String</code> or <code>PropertyName
     * </code> and value(= <code>String</code>) pairs for proppatch action
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's being set, false if it's being removed
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(Hashtable properties, boolean action)
        throws HttpException, IOException {

        boolean result = proppatchMethod(httpURL.getPath(), properties, action);
        if (result) refresh();

        return result;
    }


    /**
     * Execute PROPATCH method for the given WebdavResource.
     *
     * @param path the server relative path of the resource to act on
     * @param properties name and value pairs to set
     * (name can be a String or PropertyName)
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     * @deprecated it could be removed after the major version changes
     */
    public boolean proppatchMethod(String path, Hashtable properties)
        throws HttpException, IOException {

        return proppatchMethod(path, properties, true);
    }


    /**
     * Execute PROPATCH method for the specified resource with the given
     * properties.
     *
     * @param path the server relative path of the resource to act on
     * @param properties the name(= <code>String</code> or <code>PropertyName
     * </code> and value(= <code>String</code>) pairs for proppatch action
     * If the proppatch action is being removed, the value is null or any.
     * @param action true if it's being set, false if it's being removed
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean proppatchMethod(String path, Hashtable properties,
                                   boolean action) throws HttpException, IOException {

        setClient();
        PropPatchMethod method = new PropPatchMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        Enumeration names = properties.keys();
        boolean hasSomething = false;
        if (names.hasMoreElements()) {
            hasSomething = true;
        }
        while (names.hasMoreElements()) {
            Object item = names.nextElement();
            if (item instanceof String) {
                String name = (String) item;
                String value = (String) properties.get(item);
                if (action) {
                    method.addPropertyToSet(name, value);
                } else {
                    method.addPropertyToRemove(name);
                }
            } else if (item instanceof PropertyName) {
                String name         = ((PropertyName) item).getLocalName();
                String namespaceURI = ((PropertyName) item).getNamespaceURI();
                String value        = (String) properties.get(item);
                if (action) {
                    method.addPropertyToSet(name, value, null, namespaceURI);
                } else {
                    method.addPropertyToRemove(name, null, namespaceURI);
                }
            } else {
                // unknown type, debug or ignore it
            }
        }
        if (hasSomething) {
            int statusCode = client.executeMethod(method);
            // Possbile Status Codes => SC_OK
            // WebdavStatus.SC_FORBIDDEN, SC_CONFLICT, SC_LOCKED, 507
            setStatusCode(statusCode);
            if (statusCode >= 200 && statusCode < 300) {
                return true;
            }
        }
        return false;
    }


    /**
     * Execute the HEAD method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean headMethod()
        throws HttpException, IOException {

        return headMethod(httpURL.getPathQuery());
    }


    /**
     * Execute the HEAD method for the given path.
     *
     * @param path the server relative path of the resource to request
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean headMethod(String path)
        throws HttpException, IOException {

        setClient();
        HeadMethod method = new HeadMethod(URIUtil.encodePathQuery(path));
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the DELETE method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean deleteMethod()
        throws HttpException, IOException {

        boolean result = deleteMethod(httpURL.getPath());
        if (result) {
            setExistence(false);
        }

        return result;
    }


    /**
     * Execute the DELETE method for the given path.
     *
     * @param path the server relative path of the resource to delete
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean deleteMethod(String path)
        throws HttpException, IOException {

        setClient();
        DeleteMethod method = new DeleteMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the MOVE method for this WebdavReource.
     *
     * @param destination the destination to move to as a server relative path
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean moveMethod(String destination)
        throws HttpException, IOException {

        boolean result = moveMethod(httpURL.getPath(), destination);
        if (result) {
            httpURL.setPath(destination);
            refresh();
        }

        return result;
    }


    /**
     * Execute the MOVE method for the given source and destination.
     *
     * @param source the source resource as a server relativ path
     * @param destination the destination to move to as a server relative path
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean moveMethod(String source, String destination)
        throws HttpException, IOException {

        setClient();
        MoveMethod method = new MoveMethod(URIUtil.encodePath(source),
                                           URIUtil.encodePath(destination));
        generateIfHeader(method);
        method.setOverwrite(overwrite);
        int statusCode = client.executeMethod(method);

        // Possbile MOVE Status Codes => SC_CREATED, SC_NO_CONTENT
        // WebdavStatus.SC_FORBIDDEN, SC_CONFLICT, SC_PRECONDITION_FAILED,
        // SC_LOCKED, SC_BAD_GATEWAY
        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the COPY method for the given destination path.
     *
     * @param destination the destination as a server relative path
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean copyMethod(String destination)
        throws HttpException, IOException {

        boolean result = copyMethod(httpURL.getPath(), destination);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the COPY method the given source and destination.
     *
     * @param source the source resource as a server relative path
     * @param destination the destination as a server relative path
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean copyMethod(String source, String destination)
        throws HttpException, IOException {

        setClient();
        CopyMethod method = new CopyMethod(URIUtil.encodePath(source),
                                           URIUtil.encodePath(destination));
        generateIfHeader(method);
        method.setOverwrite(overwrite);
        int statusCode = client.executeMethod(method);

        // Possbile COPY Status Codes => SC_CREATED, SC_NO_CONTENT
        // WebdavStatus.SC_FORBIDDEN, SC_CONFLICT, SC_PRECONDITION_FAILED,
        // SC_LOCKED, SC_BAD_GATEWAY, SC_INSUFFICIENT_STORAGE
        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }



    /**
     * Execute the MKCOL method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean mkcolMethod()
        throws HttpException, IOException {

        boolean result = mkcolMethod(httpURL.getPath());
        if (result) refresh();

        return result;
    }


    /**
     * Execute the MKCOL method for the given path.
     *
     * @param path the server relative path at which to create a new collection
     *        resource
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean mkcolMethod(String path)
        throws HttpException, IOException {

        setClient();
        MkcolMethod method = new MkcolMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        // Possbile MKCOL Status Codes => SC_CREATED
        // WebdavStatus.SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_CONFLICT,
        // SC_LOCKED, SC_UNSUPPORTED_MEDIA_TYPE, SC_INSUFFICIENT_STORAGE
        setStatusCode(statusCode);
        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the LOCK method for this WebdavResource. This method tries to
     * acquire an exclusive write lock with a timeout of 120 seconds.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean lockMethod()
        throws HttpException, IOException {

        String owner = (httpURL.getUser() != null) ?
            httpURL.getUser() : defaultOwner;

        boolean result = lockMethod(httpURL.getPath(), owner, 120);
        if (result) refresh();

        return result;
    }



    /**
     * Execute the LOCK method for this WebdavResource. This method tries to
     * acquire an exclusive write lock with the given timeout value.
     *
     * @param owner the owner string.
     * @param timeout the timeout
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean lockMethod(String owner, int timeout)
        throws HttpException, IOException {

        boolean result = lockMethod(httpURL.getPath(), owner, timeout);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the LOCK method for the given path. This method tries to acquire
     * an exclusive write lock with a timeout of 120 seconds.
     *
     * @param path the server relative path of the resource to lock
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean lockMethod(String path)
        throws HttpException, IOException {

        String owner = (httpURL.getUser() != null) ?
            httpURL.getUser() : defaultOwner;

        return lockMethod(path, owner, 120);
    }


    /**
     * Execute the LOCK method for the given path. This method tries to acquire
     * an exclusive write lock with the given timeout value.
     *
     * @param path the server relative path of the resource to lock
     * @param owner The owner string.
     * @param timeout the timeout value.
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean lockMethod(String path, String owner, int timeout)
        throws HttpException, IOException {

        setClient();
        // default lock type setting
        short lockType = LockMethod.SCOPE_EXCLUSIVE;
        LockMethod method = new LockMethod(URIUtil.encodePath(path), owner,
                                           lockType, timeout);
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);
        String lock = method.getLockToken();
        WebdavState state = (WebdavState) client.getState();
        if (state != null) {
            state.addLock(path, lock);
        }
        this.owner = method.getOwner();

        // Possbile LOCK Status Codes => SC_OK
        // WebdavStatus.SC_SC_PRECONDITION_FAILED, SC_LOCKED
        setStatusCode(statusCode, lock);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the LOCK method for this WebdavResource.
     *
     * @see LockMethod
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public boolean lockMethod(String owner, short timeout)
        throws HttpException, IOException {

        return lockMethod(owner, (int) timeout);
    }


    /**
     * Execute the LOCK method for the given path.
     *
     * @see LockMethod
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public boolean lockMethod(String path, String owner, short timeout)
        throws HttpException, IOException {

        return lockMethod(path, owner, (int) timeout);
    }


    /**
     * Execute the Unlock method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean unlockMethod() throws HttpException, IOException {

        String owner = (httpURL.getUser() != null) ?
            httpURL.getUser() : defaultOwner;

        boolean result = unlockMethod(httpURL.getPath(), owner);
        if (result) refresh();

        return result;
    }


    /**
     * Execute the Unlock method for the given path.
     *
     * @param path the server relative path of the resource to unlock
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean unlockMethod(String path)
        throws HttpException, IOException {

        String owner = (httpURL.getUser() != null) ?
            httpURL.getUser() : defaultOwner;

        return unlockMethod(path, owner);
    }


    /**
     * Execute the Unlock method for the given path.
     *
     * @param path the server relative path of the resource to unlock
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean unlockMethod(String path, String owner)
        throws HttpException, IOException {

        setClient();
        // Get the lock for the given path.
        WebdavState state = (WebdavState) client.getState();
        // Discover the locktoken from the given lock owner
        state = discoverLock(owner, path, state);
        String lock = state.getLock(path);
        if (lock == null) return false;
        // unlock for the given path.
        UnlockMethod method = new UnlockMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        method.setLockToken(lock);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        if (statusCode >= 200 && statusCode < 300) {
            state.removeLocks(path);
            return true;
        }

        return false;
    }


    /**
     * Discover and refresh lock tokens.
     *
     * @exception HttpException
     * @exception IOException
     */
    public void discoverOwnLocks()
    throws HttpException, IOException {

        setClient();
        String owner = (httpURL.getUser() != null) ?
            httpURL.getUser() : defaultOwner;

        WebdavState state = (WebdavState) client.getState();
        state = discoverLock(owner, httpURL.getPath(), state);
        client.setState(state);
    }


    /**
     * Discover the given owner and locktoken and set the locktoken
     *
     * @param owner the activelock owner
     * @param path the server relative path of the resource to request
     * @param state the state to save the locktoken
     * @return state probably having lock information renewly
     */
    protected WebdavState discoverLock(String owner, String path,
                                       WebdavState state) {

        if (lockDiscovery == null) return state;
        Lock[] activeLocks = lockDiscovery.getActiveLocks();
        if (activeLocks == null) return state;
        for (int i = 0; i < activeLocks.length; i++) {
            String activeLockOwner = activeLocks[i].getOwner();
            if (activeLockOwner.equals(owner)) {
                String locktoken = activeLocks[i].getLockToken();
                state.addLock(path, locktoken);
            }
        }
        return state;
    }


    /**
     * Update this resource to the specified target
     *
     * @param target the path of the history element to update this resource
     *        from
     * @return true if the method has succeeded
     * @exception HttpException
     * @exception IOException
     */
    public boolean updateMethod(String target)
        throws HttpException, IOException {

        return updateMethod(httpURL.getPath(), target);
    }


    /**
     * Update the specified resource to the specified target
     *
     * @param path the server relative path of the resource to update
     * @param target path of the target to update from (history resource)
     * @return true if the method has succeeded
     * @exception HttpException
     * @exception IOException
     */
    public boolean updateMethod(String path, String target)
        throws HttpException, IOException {

        setClient();
        UpdateMethod method = new UpdateMethod(URIUtil.encodePath(path),
                                               URIUtil.encodePath(target));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    public boolean versionControlMethod(String path)
        throws HttpException, IOException {

        setClient();

        VersionControlMethod method =
            new VersionControlMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    public boolean versionControlMethod(String path, String target)
        throws HttpException, IOException {

        setClient();

        VersionControlMethod method =
            new VersionControlMethod(URIUtil.encodePath(path),
                                     URIUtil.encodePath(target));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the MKWORKSPACE method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean mkWorkspaceMethod()
        throws HttpException, IOException {

        boolean result = mkWorkspaceMethod(httpURL.getPath());
        if (result) refresh();

        return result;
    }


    /**
     * Execute the MKCOL method for the given path.
     *
     * @param path the server relative path at which to create a new workspace
     *        resource
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean mkWorkspaceMethod(String path)
        throws HttpException, IOException {

        setClient();
        MkWorkspaceMethod method =
            new MkWorkspaceMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        // Possbile MKCOL Status Codes => SC_CREATED
        // WebdavStatus.SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_CONFLICT,
        // SC_LOCKED, SC_UNSUPPORTED_MEDIA_TYPE, SC_INSUFFICIENT_STORAGE

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    // -------------------------------------------------------- Basic Methods


    /**
     * Compare to the WebdavResource object.
     *
     * @param another the other WebdavResource object
     * @return the value 0 if the argument is equal.
     */
    public int compareToWebdavResource(WebdavResource another) {

        try {
            HttpURL anotherUrl = another.getHttpURL();

            String thisHost = httpURL.getHost();
            String anotherHost= anotherUrl.getHost();
            if (!thisHost.equalsIgnoreCase(anotherHost))
                return thisHost.compareTo(anotherHost);

            int thisPort = httpURL.getPort();
            int anotherPort= anotherUrl.getPort();
            if (thisPort != anotherPort)
                return (thisPort < anotherPort) ? -1 : 1;

            boolean thisCollection = isCollection();
            boolean anotherCollection = another.isCollection();
            if (thisCollection && !anotherCollection)
                return -1;
            if (anotherCollection && !thisCollection)
                return 1;

            String thisPath = httpURL.getPathQuery();
            String anotherPath= anotherUrl.getPathQuery();
            return thisPath.compareTo(anotherPath);
        } catch (Exception e) {
            // FIXME: not to return 0.
        }

        return 0;
    }


    /**
     * Compare to the given another object.
     *
     * @param another the other WebdavResource object
     * @return the value 0 if another is equal.
     */
    public int compareTo(Object another) {

        if ((another != null) && (another instanceof WebdavResource)) {
            return compareToWebdavResource((WebdavResource) another);
        }

        String thisUrl = toString();
        String anotherUrl = another.toString();

        return thisUrl.compareTo(anotherUrl);
    }


    /**
     * Test the object.
     *
     * @param obj the other object
     * @return true if it's equal.
     */
    public boolean equals(Object obj) {

        if ((obj != null) && (obj instanceof WebdavResource)) {
            return compareTo(obj) == 0;
        }
        return false;
    }


    /**
     * Return the http URL string.
     *
     * @return the http URL string.
     */
    public String toString() {
        return httpURL.toString();
    }


    /**
     * Execute the CHECKIN method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean checkinMethod()
        throws HttpException, IOException {

        return checkinMethod(httpURL.getPath());
    }


    /**
     * Execute the CHECKIN method for the given path.
     *
     * @param path the server relative path of the resource to check in
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean checkinMethod(String path)
        throws HttpException, IOException {

        setClient();
        CheckinMethod method = new CheckinMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the CHECKOUT method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean checkoutMethod()
        throws HttpException, IOException {

        return checkoutMethod(httpURL.getPath());
    }


    /**
     * Execute the CHECKOUT method for the given path.
     *
     * @param path the server relative path of the resource to check out
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean checkoutMethod(String path)
        throws HttpException, IOException {

        setClient();
        CheckoutMethod method = new CheckoutMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }


    /**
     * Execute the CHECKOUT method for this WebdavResource.
     *
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean uncheckoutMethod()
        throws HttpException, IOException {

        return uncheckoutMethod(httpURL.getPath());
    }




    /**
     * Execute the CHECKOUT method for the given path.
     *
     * @param path the server relative path of the resource to act on
     * @return true if the method is succeeded.
     * @exception HttpException
     * @exception IOException
     */
    public boolean uncheckoutMethod(String path)
        throws HttpException, IOException {

        setClient();
        UncheckoutMethod method =
            new UncheckoutMethod(URIUtil.encodePath(path));
        generateIfHeader(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);

        return (statusCode >= 200 && statusCode < 300) ? true : false;
    }

    /**
     * Create a new WebdavResource object (as a seperate method so that it can
     * be overridden by subclasses.
     *
     * @param client HttpClient to be used by this webdavresource.
     * @return A new WebdavResource object.
     */
    protected WebdavResource createWebdavResource(HttpClient client) {
        WebdavResource resource = new WebdavResource(client);
        resource.setProxy(proxyHost, proxyPort);
        resource.setProxyCredentials(proxyCredentials);
        return resource;
    }

    /**
     * Process a property, setting various member variables depending
     * on what the property is.
     *
     * @param property The property to process.
     */
    protected void processProperty(Property property) {
        if (property.getLocalName().equals(DISPLAYNAME)) {
            displayName = property.getPropertyAsString();
        }
        else if (property.getLocalName().equals(GETCONTENTLENGTH)) {
            String getContentLength = property.getPropertyAsString();
            setGetContentLength(getContentLength);
        }
        else if (property.getLocalName().equals(RESOURCETYPE)) {
            ResourceTypeProperty resourceType =
                (ResourceTypeProperty) property;
            setResourceType(resourceType);
        }
        else if (property.getLocalName().equals(GETCONTENTTYPE)) {
            String getContentType = property.getPropertyAsString();
            setGetContentType(getContentType);
        }
        else if (property.getLocalName().equals(GETLASTMODIFIED)) {
            String getLastModified = property.getPropertyAsString();
            setGetLastModified(getLastModified);
        }
        else if (property.getLocalName().equals(CREATIONDATE)) {
            String creationDate = property.getPropertyAsString();
            setCreationDate(creationDate);
        }
        else if (property.getLocalName().equals(GETETAG)) {
            String getEtag = property.getPropertyAsString();
            setGetEtag(getEtag);
        }
        else if (property.getLocalName().equals(ISHIDDEN)) {
            String isHidden = property.getPropertyAsString();
            setIsHidden(isHidden);
        }
        else if (property.getLocalName().equals(ISCOLLECTION)) {
            String isCollection = property.getPropertyAsString();
            setIsCollection(isCollection);
        }
        else if (property.getLocalName().equals(SUPPORTEDLOCK)) {
            String supportedLock = property.getPropertyAsString();
            setSupportedLock(supportedLock);
        }
        else if (property.getLocalName().equals(LOCKDISCOVERY)) {
            LockDiscoveryProperty lockDiscovery =
                (LockDiscoveryProperty) property;
            setLockDiscovery(lockDiscovery);
        }
    }


    /**
     * Execute REPORT method.
     * This method is for the special Access Control Reports:
     * - acl-principal-prop-set (not supported yet)
     * - principal-match (not supported yet)
     * - principal-property-search
     * - principal-search-property-set (not supported yet)
     *
     * @param path the server relative path of the resource to request
     * @param properties The named properties.
     * @return an enumeration of <code>ResponseEntity</code>
     * @exception HttpException
     * @exception IOException
     */
    public Enumeration aclReportMethod(
        String path,
        Collection properties,
        int reportType)
        throws HttpException, IOException {

        setClient();
        AclReportMethod method =
            new AclReportMethod(
                URIUtil.encodePath(path),
                properties,
                DepthSupport.DEPTH_INFINITY,
                reportType);
        int status = client.executeMethod(method);

        // Set status code for this resource.
        if (thisResource == true) {
            // Set the status code.
            setStatusCode(method.getStatusLine().getStatusCode());
        }

        //slide/tamino delivers status code OK.
        //can be removed when the server sends MULTI_STATUS
        if (status != HttpStatus.SC_MULTI_STATUS && status != HttpStatus.SC_OK) {
            HttpException ex = new HttpException();
            ex.setReasonCode(status);
            throw ex;
        }
        thisResource = false;

        return method.getResponses();
    }


    private static String getName(String uri) {
        String escapedName = URIUtil.getName(
            uri.endsWith("/") ? uri.substring(0, uri.length() - 1): uri);
        try {
            return URIUtil.decode(escapedName);
        } catch (URIException e) {
            return escapedName;
        }
    }
}
