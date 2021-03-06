/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/util/NSDecl.java,v 1.1.2.1 2004/02/05 16:55:43 mholz Exp $
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

/**
 * Utility class to hold a single declaration.
 */
class NSDecl {
  String prefix;
  String URI;

  NSDecl (String prefix, String URI) {
    this.prefix = prefix;
    this.URI = URI;
  }
  
  // MJD - debug
  public String toString()
  {
    return prefix + "->" + URI;
  }
  // MJD - debug
}
