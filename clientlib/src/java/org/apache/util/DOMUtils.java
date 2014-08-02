/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/util/DOMUtils.java,v 1.1.2.1 2004/02/05 15:51:20 mholz Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/02/05 15:51:20 $
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

package org.apache.util;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * This class provides some basic utility methods for working with
 * XML Document objects.  Many of these utilities provide JAXP 1.0 "brute
 * force" implementations of functions that are available in JAXP 1.1.
 *
 * @author <a href="mailto:bcholmes@interlog.com>B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com>Park, Sung-Gu</a>
 * @author Dirk Verbeeck
 * @version $Revision: 1.1.2.1 $
 */
public class DOMUtils {

    protected static Class[] getElementsByNSParameterTypes =
        { String.class, String.class };


	public static boolean isDocumentBuilderDOM2Compliant() {
		try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);

	        DocumentBuilder builder = factory.newDocumentBuilder();
			String data="<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:displayname/></D:prop></D:propfind>";
	        Document document = builder.parse(new InputSource(new StringReader(data)));
			Element root = document.getDocumentElement();
			return isDOM2Compliant(root);
		}
		catch (Throwable ex) {
		}
		return false;
	}

    /**
     * This method checks a DOM node to see if the implementation
     * is DOM version 2 compliant (and hence, supports namespaces).
     */
    public static boolean isDOM2Compliant(Node node) {
        if (node == null) {
            throw new IllegalArgumentException
                ("The input node cannot be null");
        }
        Document document = node.getOwnerDocument();
        boolean isDOM2 = false;
        try {
            Class documentClass = document.getClass();
            Method method = documentClass.getMethod(
                "getElementsByTagNameNS",
                getElementsByNSParameterTypes );

            // can method ever be null?
            if (method != null) {
                isDOM2 = true;
            }
        } catch (NoSuchMethodException e) {
        }
        return isDOM2;
    }


    /**
     *  Determine the namespace prefix being used for DAV.
     *  Generally, DAV responses say something like:
     *
     *  <PRE>
     *  &lt;D:multistatus xmlns:D="DAV:"&gt;
     *  </PRE>
     *
     *  <P>  In this case, the "D:" is the prefix for DAV.
     */
    public static String findDavPrefix(Document document) {
        Element multistatus = document.getDocumentElement();
        NamedNodeMap list = multistatus.getAttributes();
        String prefix = "DAV:";
        for (int i = 0; i < list.getLength(); i++) {
            try {
                Attr attr = (Attr) list.item(i);
                if (attr.getName() != null &&
                    attr.getName().startsWith("xmlns") &&
                    attr.getValue().equals("DAV:")) {
                    int indx = attr.getName().indexOf(":");
                    if ((indx >= 0) && (indx < attr.getName().length()-1)) {
                        prefix = attr.getName().substring(indx + 1) + ":";
                    } else {
                        prefix = "";
                    }
                }
            } catch (ClassCastException e) {
            }
        }
        return prefix;
    }


    /**
     *  Scan all immediate children of a node, and append all
     *  text nodes into a string.  Consider the following example
     *
     *  <PRE>
     *  &lt;customer&gt;Joe Schmoe&lt;/customer&gt;
     *  </PRE>
     *
     *  <P>  In this case, calling this method on the
     *  <CODE>customer</CODE> element returns "Joe Schmoe".
     */
    public static String getTextValue(Node node) {

        // I *thought* that I should be able to use element.getNodeValue()...

        String text = "";
        NodeList textList = node.getChildNodes();
        for (int i = 0; i < textList.getLength(); i++) {
            try {
                text += ((Text) textList.item(i)).getData();
            } catch (ClassCastException e) {
                // we don't care about non-Text nodes
            }
        }
        return text;
    }

    /**
     *  Get the status code out of the normal status response.
     *
     *  <P>  Each <code>DAV:propstat</code> node contains a
     *  status line, such as:
     *
     *  <PRE>
     *  &lt;DAV:status&gt;HTTP/1.1 200 OK&lt;/DAV:status&gt;
     *  </PRE>
     *
     *  <P>  In this case, calling this method on the
     *  text string returns 200.
     */
     public static int parseStatus(String statusString) {
        int status = -1;
        if (statusString != null) {
            StringTokenizer tokenizer = new StringTokenizer(statusString);
            if (tokenizer.countTokens() >= 2) {
                Object dummy = tokenizer.nextElement();
                String statusCode = tokenizer.nextElement().toString();
                try {
                    status = Integer.parseInt(statusCode);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Status code is not numeric");
                }
            } else {
                throw new IllegalArgumentException(
                    "There aren't enough words in the input argument");
            }
        }
        return status;
    }

    /**
     *
     */
    public static String getElementNamespaceURI(Element element) {
        String namespace = null;

        if (element == null) {
            throw new IllegalArgumentException(
                "The element cannot be null");
        } else if (isDOM2Compliant(element)) {
/*
            try {
                Method method = element.getClass().getMethod(
                    "getNamespaceURI", null );
                if (method != null) {
                    namespace = (String) method.invoke(element, null);
                } else {
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
*/
              namespace = element.getNamespaceURI();
        } else {

            String tagName = element.getTagName();
            String attribute = "xmlns";
            int index = tagName.indexOf(":");
            if (index > 0 && index < (tagName.length()-1)) {
                attribute += (":" + tagName.substring(0,index));
            }

            boolean found = false;
            for (Node node = element; !found && node != null;
                 node = node.getParentNode()) {

                try {
                    String tmp = ((Element) node).getAttribute(attribute);
                    if (tmp != null && !tmp.equals("")) {
                        namespace = tmp;
                        found = true;
                    }
                } catch (ClassCastException e) {
                    // this will happen for Documents
                }
            }
        }

        return namespace;
    }


    /**
     *
     */
    public static String getElementLocalName(Element element) {
        String localName = null;

        if (element == null) {
            throw new IllegalArgumentException(
                "The element cannot be null");
        } else if (isDOM2Compliant(element)) {
/*
            try {
                Method method = element.getClass().getMethod(
                    "getLocalName", null );
                if (method != null) {
                    localName = (String) method.invoke(element, null);
                } else {
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
*/
              localName = element.getLocalName();
        } else {

            localName = element.getTagName();
            int index = localName.indexOf(":");
            if (index > 0 && index < (localName.length()-1)) {
                localName = localName.substring(index + 1);
            }
        }

        return localName;
    }

    /**
     *
     */
    public static NodeList getElementsByTagNameNS(
        Node node, String tagName, String namespace) {

        NodeList list = null;

        if (node == null) {
        } else if (!(node instanceof Document) &&
            !(node instanceof Element)) {
            throw new IllegalArgumentException(
                "The node parameter must be an Element or a Document node");
        } else if (isDOM2Compliant(node)) {
            /*
            try {
                Method method = node.getClass().getMethod(
                    "getElementsByTagNameNS", getElementsByNSParameterTypes );
                if (method != null) {
                    Object[] params = { namespace, tagName };
                    list = (NodeList) method.invoke(node, params);
                } else {
                }
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
            */
              list = ((Element) node).getElementsByTagNameNS(namespace, tagName);
        } else {
            Vector vector = new Vector();
            getChildElementsByTagNameNS(vector, node, tagName, namespace);
            list = new NodeListImpl(vector);
        }
        return list;
    }


    protected static void getChildElementsByTagNameNS(
        Vector vector, Node node, String tagName, String namespace) {

        NodeList list = node.getChildNodes();
        for (int i = 0; list != null && i < list.getLength(); i++) {
            try {
                Element element = (Element) list.item(i);

                if (tagName.equals(DOMUtils.getElementLocalName(element)) &&
                    namespace.equals(
                        DOMUtils.getElementNamespaceURI(element))) {

                    vector.addElement(element);
                } else {
                    // RECURSIVE!  DANGER, WILL ROBINSON!
                    getChildElementsByTagNameNS(vector, element,
                                                tagName, namespace);
                }
            } catch (ClassCastException e) {
            }
        }
    }


    /**
     * Get the first element matched with the given namespace and name.
     *
     * @param node The node.
     * @param namespac The namespace.
     * @param name The name.
     * @return The wanted first element.
     */
    public static Element getFirstElement(Node node, String namespace,
                                          String name) {
        NodeList children = node.getChildNodes();
        if (children == null)
            return null;
        for (int i = 0; i < children.getLength(); i++) {
            try {
                Element child = (Element) children.item(i);
                if (name.equals(DOMUtils.getElementLocalName(child)) &&
                    namespace.equals(DOMUtils.getElementNamespaceURI(child))) {
                    return child;
                }
            } catch (ClassCastException e) {
            }
        }
        return null;
    }


    // ---------------------------------------------------------- Inner Classes


    /**
     * This class provides an implementation of NodeList, which is used by
     * the getElementsByTagNameNS() method.
     */
    static class NodeListImpl implements NodeList {
        private Vector vector = null;

        NodeListImpl(Vector vector) {
            this.vector = vector;
        }

        public int getLength() {
            return vector.size();
        }

        public Node item(int i) {
            return (Node) vector.elementAt(i);
        }
    }
}
