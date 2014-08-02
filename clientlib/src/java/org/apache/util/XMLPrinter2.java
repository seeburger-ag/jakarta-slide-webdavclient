/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/util/XMLPrinter2.java,v 1.1.2.1 2004/02/05 15:51:20 mholz Exp $
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

import java.io.IOException;
import java.io.Writer;

/**
 * XMLPrinter2 helper class.
 * 
 * @author Dirk Verbeeck
 */
public class XMLPrinter2 {
    
    
    // -------------------------------------------------------------- Constants
    
    
    /**
     * Opening tag.
     */
    public static final int OPENING = 0;
    
    
    /**
     * Closing tag.
     */
    public static final int CLOSING = 1;
    
    
    /**
     * Element with no content.
     */
    public static final int NO_CONTENT = 2;
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Buffer.
     */
    protected StringBuffer buffer = new StringBuffer();
    
    
    /**
     * Writer.
     */
    protected Writer writer = null;
    
	/**
	 * Namespace stack
	 */
    protected NSStack stack = new NSStack();
    
    // ----------------------------------------------------------- Constructors
    
    
    /**
     * Constructor.
     */
    public XMLPrinter2() {
    }
    
    
    /**
     * Constructor.
     */
    public XMLPrinter2(Writer writer) {
        this.writer = writer;
    }
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Retrieve generated XML.
     * 
     * @return String containing the generated XML
     */
    public String toString() {
        return buffer.toString();
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param name Property name
     * @param value Property value
     */
    public void writeProperty(QName name, String value, boolean cdata) {
        writeElement(name, OPENING);
        if (cdata)
            writeData(value);
        else
            writeText(value);
        writeElement(name, CLOSING);
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param name Property name
     * @param value Property value
     */
    public void writeProperty(QName name, String value) {
        writeProperty(name, value, false);
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param namespace Namespace
     * @param name Property name
     */
    public void writeProperty(QName name) {
        writeElement(name, NO_CONTENT);
    }
    
    
    /**
     * Write an element.
     * 
     * @param name Element name
     * @param type Element type
     */
    public void writeElement(QName name, int type) {
        writeElement(name, type, null);
    }

	/**
	 * add a namespace declaration to the current namespace scope
	 */
    public void addNSDeclaration(String prefix, String URI) {
    	stack.addNSDeclaration(prefix, URI);
    }
    
    /**
     * add a namespace declaration (with given prefix) to the current namespace scope
     */
    public String addNSDeclaration(String URI) {
    	return stack.addNSDeclaration(URI);
    }
    

    /**
     * Write an element.
     * 
     * @param name Element name and namespace URI
     * @param type Element type
     * @param additionalNamespaceURIs additional namespace URIs that must be declared on this level
     */
    public void writeElement(QName name, int type, String[] additionalNamespaceURIs)
    {
        String prefix;
        String localName;
    	String xmlns,xmlns1,xmlns2;

    	switch (type) {
    	case OPENING:
    		xmlns1 = stack.getXmlns();
    		stack.pushScope();

    		prefix = addNSDeclaration(name.getNamespaceURI());
    		if (additionalNamespaceURIs!=null)
    		{
    			for (int i=0; i<additionalNamespaceURIs.length ; i++)
    			{
    				addNSDeclaration(additionalNamespaceURIs[i]);
    			}
    		}
    		xmlns2 = stack.getXmlns();
    		stack.pushScope();

    		localName = name.getLocalName();
    		xmlns = xmlns1 + xmlns2;

    		buffer.append("<" + prefix + ":" + localName + xmlns + ">");
    	    break;
    	case CLOSING:
    	    prefix = addNSDeclaration(name.getNamespaceURI());
    	    localName = name.getLocalName();
    	    buffer.append("</" + prefix + ":" + localName + ">");
    		stack.popScope();
    		stack.popScope();
    	    break;
    	case NO_CONTENT:
    	default:
    		xmlns1 = stack.getXmlns();
    		stack.pushScope();
    		prefix = addNSDeclaration(name.getNamespaceURI());
    		xmlns2 = stack.getXmlns();
    		localName = name.getLocalName();
    		xmlns = xmlns1 + xmlns2;
    		buffer.append("<" + prefix + ":" + localName + xmlns + "/>");
    		stack.popScope();
    	}
    }
    
    /**
     * Write text.
     * 
     * @param text Text to append
     */
    public void writeText(String text) {
        buffer.append(text);
    }
    
    
    /**
     * Write data.
     * 
     * @param data Data to append
     */
    public void writeData(String data) {
        buffer.append("<![CDATA[");
        buffer.append(data);
        buffer.append("]]>");
    }
    
    
    /**
     * Write XML Header.
     */
    public void writeXMLHeader() {
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    }
    
    
    /**
     * Send data and reinitializes buffer.
     */
    public void sendData()
        throws IOException {
        if (writer != null) {
            writer.write(buffer.toString());
            buffer = new StringBuffer();
        }
    }
    
    
}
