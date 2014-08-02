/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/util/NSStack.java,v 1.1.2.1 2004/02/05 16:55:43 mholz Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/02/05 16:55:43 $
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
 *
 */ 

package org.apache.util;

import java.io.*;
import java.util.*;

/**
 * This class implements a namespace stack for XML apps to use. If
 * you need to keep track of namespaces in scope, then this class is
 * for you.  Every time you enter a new element and wish to add some
 * namespace declarations that are to be visible within that
 * element, you should call <tt>pushScope</tt> to create a new
 * scope. Then, call <tt>addNSDeclaration</tt> any number of times to
 * add new declarations for that scope. Scopes nest inside out; that
 * is, any NS declaration added into a scope is visible from any
 * scopes that are pushed later. When you want to see whether an NS
 * declaration has already been made for a certain URI, you should
 * call <tt>getPrefixInScopeForURI</tt> to get the prefix that has
 * been bound to that URI. There is a covenience version of
 * <tt>addNSDecalration</tt> which can be used if you want me to give
 * you a not-so-random, yet unique, prefix for your namespace
 * declaration.
 *
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class NSStack {
  Vector nss = new Vector (); // vector holding vectors of ns decls (stack)
  int nssCount = 0;           // number of items on the stack
  Vector tos = new Vector();  // the vector @ the top of the stack
  private final static String nsPrefixPrefix = "ns";
  private int nsPrefixCount = 1;

  /**
   * Enter a new scope: after calling this I'm ready to accept new
   * declarations into that scope.
   */
  public void pushScope () {
    nss.addElement (tos = new Vector ());
    nssCount++;
  }

  /**
   * Leave a scope: this removes any NS declarations that were added
   * in the last scope. Note that I don't bother to validate that you
   * don't call popScope too many times; that's your problem.
   */
  public void popScope () {
    nss.removeElementAt (--nssCount);
    tos = (nssCount != 0) ? (Vector) nss.elementAt (nssCount-1) : null;
  }

  /**
   * Add a new declaration to the current scope. This is visible within
   * the current scope as well as from any nested scopes. 
   *
   * @param prefix the prefix to be used for this namespace
   * @param URI the namespace name of this namespace.
   */
  synchronized public void addNSDeclaration (String prefix, String URI) {
    tos.addElement (new NSDecl (prefix, URI));
  }

  /**
   * Add a new declaration to the current scope using a unique prefix
   * and return the prefix. This is useful when one just wants to add a
   * decl and doesn't want to have to deal with creating unique prefixes.
   * If the namespace name is already declared and in scope, then the 
   * previously declared prefix is returned.
   *
   * @param URI the namespace name of this namespace
   * @return the unique prefix created or previously declared
   *         for this namespace
   */
  synchronized public String addNSDeclaration (String URI) {
    String uniquePrefix = getPrefixFromURI (URI);
    if (uniquePrefix == null) {
      do {
              uniquePrefix = nsPrefixPrefix + nsPrefixCount++;
      } while (getURIFromPrefix (uniquePrefix) != null);
      addNSDeclaration (uniquePrefix, URI);
    }
    return uniquePrefix;
  }

  /**
   * Return the prefix associated with the given namespace name by
   * looking thru all the namespace declarations that are in scope.
   *
   * @param URI the namespace name for whom a declared prefix is desired
   * @return the prefix or null if namespace name not found
   */
  public String getPrefixFromURI (String URI) {
    for (int i = nssCount-1; i >= 0; i--) {
      Vector scope = (Vector) nss.elementAt (i);
      for (Enumeration e = scope.elements (); e.hasMoreElements (); ) {
        NSDecl nsd = (NSDecl) e.nextElement ();
        if (nsd.URI.equals (URI)) {
          return nsd.prefix;
        }
      }
    }
    return null;
  }

  /**
   * Return the prefix associated with the given namespace name by
   * looking thru all the namespace declarations that are in scope.
   * If the namespace declaration is not found, create one and
   * return the generated prefix.
   *
   * @param URI the namespace name for whom a declared prefix is desired
   * @return the prefix (will never return null)
   */
  synchronized public String getPrefixFromURI (String namespaceURI,
                                               Writer sink)
    throws IOException {
    String prefix = getPrefixFromURI (namespaceURI);

    if (prefix == null) {
      prefix = addNSDeclaration (namespaceURI);

      sink.write (" xmlns:" + prefix + "=\"" + namespaceURI + '\"');
    }

    return prefix;
  }

  /**
   * Return the namespace name associated with the given prefix by
   * looking thru all the namespace declarations that are in scope.
   *
   * @param prefix the prefix for whom a declared namespace name is desired
   * @return the namespace name or null if prefix not found
   */
  public String getURIFromPrefix (String prefix) {
    for (int i = nssCount-1; i >= 0; i--) {
      Vector scope = (Vector) nss.elementAt (i);
      for (Enumeration e = scope.elements (); e.hasMoreElements (); ) {
        NSDecl nsd = (NSDecl) e.nextElement ();
        if (nsd.prefix.equals (prefix)) {
          return nsd.URI;
        }
      }
    }
    return null;
  }
  
  /**
   * Returns a xmlns declaration string for the namespace scope
   */
   public String getXmlns()
   {
		StringBuffer buffer = new StringBuffer();
		for (Enumeration e = tos.elements(); e.hasMoreElements(); ) {
			NSDecl nsd = (NSDecl) e.nextElement();
			buffer.append(" xmlns:" + nsd.prefix + "=\"" + nsd.URI + "\"");
		}
		return buffer.toString();
   }

  // MJD - debug
  public String toString()
  {
    return nss.toString();
  }
  // MJD - debug
}

