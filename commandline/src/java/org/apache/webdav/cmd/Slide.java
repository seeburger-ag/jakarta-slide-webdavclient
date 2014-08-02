/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/Slide.java,v 1.1.2.2 2004/04/01 09:31:28 ozeigermann Exp $
 * $Revision: 1.1.2.2 $
 * $Date: 2004/04/01 09:31:28 $
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

/**
 * The Slide client, the command line version for WebDAV client.
 *
 * @author <a href="mailto:jericho@thinkree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @author Dirk Verbeeck
 */
public class Slide {

    /**
     * The version information for the Slide client.
     */
    public final static String version = "Slide client @VERSION@";

    public static void main(String[] args) {
        Client client = new Client(System.in,System.out);

        ////////////  BEGIN Command line arguments //////////////
        String argOptions = null;

        // parse arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (argOptions != null)
                    argOptions += args[i].substring(1);
                else
                    argOptions = args[i];
            } else {
//                slide.stringUrl = args[i];
                client.connect(args[i]);
            }
        }

        // print options
        if (argOptions != null) {
            char option;
            for (int i = 0; i < argOptions.length(); i++) {
                option = argOptions.charAt(i);
                switch (option) {
                    case '-':
                        break;
                    case 'h':
                        printCmdLineUsage();
                        break;
                    case 'v':
                        System.out.println(version);
                        break;
                    case 'd':
                        client.setDebug(Client.DEBUG_ON);
                        break;
                    default:
                        System.exit(-1);
                }
            }
        }
        ////////////  END Command line arguments //////////////

        client.run();
    }

    /**
     * Print the commands options from startup
     */
    private static void printCmdLineUsage()
    {

        System.out.println("Usage: Slide [-vdh] " +
            "http://hostname[:port][/path]");
        System.out.println
            ("  Default protocol: http, port: 80, path: /");
        System.out.println("Options:");
        System.out.println("  -v: Print version information.");
        System.out.println("  -d: Debug.");
        System.out.println("  -h: Print this help message.");
        System.out.println(
            "Please, email bug reports to slide-user@jakarta.apache.org");
    }
}

