/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/ResourceTypeProperty.java,v 1.1.2.1 2004/02/05 15:51:23 mholz Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/02/05 15:51:23 $
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

package org.apache.webdav.lib.properties;

import org.apache.util.DOMUtils;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An interface that describes a standard Resource Type property (as defined by
 * the WebDAV specification).
 *
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author Dirk Verbeeck
 */
public class ResourceTypeProperty extends BaseProperty {

    private boolean initialized=false;
    private boolean isCollection;

    // -------------------------------------------------------------- Constants


    /**
     * The property name.
     */
    public static final String TAG_NAME = "resourcetype";


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor for the property.
     */
    public ResourceTypeProperty(ResponseEntity response, Element element) {
        super(response, element);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Returns true if the resource is a collection.  A collection is indicated
     * by a response like this:
     *
     * <pre>
     * &lt;D:resourcetype&gt;&lt;D:collection/&gt;&lt;/D:resourcetype&gt;
     * </pre>
     */
    public boolean isCollection() {
        init();
        return isCollection;
    }

    private void init()
    {
        // FIXME: only <DAV:collection/> is supported

        if (initialized)
            return;

        NodeList tmp = element.getChildNodes();
        for (int i = 0; tmp != null && i < tmp.getLength(); i++ ) {
            try {
                Element child = (Element) tmp.item(i);
                if ("collection".equals(DOMUtils.getElementLocalName(child))
                     && "DAV:".equals(DOMUtils.getElementNamespaceURI(child)))
                {
                    isCollection=true;
                    initialized=true;
                    return;
                }
            } catch (ClassCastException e) {
            }
        }
        isCollection=false;
        initialized=true;
    }

    /**
     * This method returns the value of the property.
     * For this property "COLLECTION" is returned if
     * this resource is a collection, "" otherwise.
     *
     * WARNING: this will change in the future
     * use isCollection()
     */
    public String getPropertyAsString() {
        init();
        return isCollection?"COLLECTION":"";
    }
}
