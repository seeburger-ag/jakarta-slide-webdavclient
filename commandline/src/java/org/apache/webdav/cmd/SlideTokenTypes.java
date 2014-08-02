// $ANTLR 2.7.3: "Client.g" -> "ClientLexer.java"$

/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/SlideTokenTypes.java,v 1.1.2.3 2004/04/01 08:38:02 ozeigermann Exp $
 * $Revision: 1.1.2.3 $
 * $Date: 2004/04/01 08:38:02 $
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

package org.apache.webdav.cmd;

import java.io.*;
import java.util.*;
import org.apache.util.QName;
import org.apache.webdav.lib.PropertyName;


public interface SlideTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int HELP = 4;
	int QUESTION = 5;
	int EOL = 6;
	int STATUS = 7;
	int SPOOL = 8;
	int STRING = 9;
	int OFF = 10;
	int RUN = 11;
	int ECHO = 12;
	int ON = 13;
	int DEBUG = 14;
	int OPTIONS = 15;
	int CONNECT = 16;
	int OPEN = 17;
	int DISCONNECT = 18;
	int LPWD = 19;
	int PWC = 20;
	int PWD = 21;
	int LCD = 22;
	int CD = 23;
	int CC = 24;
	int LLS = 25;
	int LDIR = 26;
	int OPTIONSTRING = 27;
	int LS = 28;
	int DIR = 29;
	int MKCOL = 30;
	int MKDIR = 31;
	int MOVE = 32;
	int COPY = 33;
	int DELETE = 34;
	int DEL = 35;
	int RM = 36;
	int PROPFIND = 37;
	int PROPGET = 38;
	int QNAME = 39;
	int PROPFINDALL = 40;
	int PROPGETALL = 41;
	int PROPPATCH = 42;
	int PROPSET = 43;
	int GET = 44;
	int PUT = 45;
	int LOCK = 46;
	int UNLOCK = 47;
	int LOCKS = 48;
	int GRANT = 49;
	int TO = 50;
	int DENY = 51;
	int REVOKE = 52;
	int FROM = 53;
	int ACL = 54;
	int PRINCIPALCOLLECTIONSET = 55;
	int VERSIONCONTROL = 56;
	int UPDATE = 57;
	int CHECKIN = 58;
	int CHECKOUT = 59;
	int UNCHECKOUT = 60;
	int REPORT = 61;
	int EREPORT = 62;
	int LREPORT = 63;
	int MKWS = 64;
	int EXIT = 65;
	int QUIT = 66;
	int BYE = 67;
	int SET = 68;
	int CLOSE = 69;
	int CP = 70;
	int MV = 71;
	int PROPPUT = 72;
	int PRINCIPALCOL = 73;
	int WS = 74;
	int CHARS = 75;
	int ALPHANUM = 76;
	int ALPHA = 77;
	int LOWALPHA = 78;
	int UPALPHA = 79;
	int DIGIT = 80;
}
