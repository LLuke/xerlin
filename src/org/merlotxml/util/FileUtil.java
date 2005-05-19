/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
====================================================================

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions 
are met:

1. Redistribution of source code must retain the above copyright 
notice, this list of conditions and the following disclaimer. 

2. Redistribution in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the 
documentation and/or other materials provided with the distribution.

3. All advertising materials mentioning features or use of this 
software must display the following acknowledgment:  "This product 
includes software developed by ChannelPoint, Inc. for use in the 
Merlot XML Editor (http://www.channelpoint.com/merlot/)."
 
4. Any names trademarked by ChannelPoint, Inc. must not be used to 
endorse or promote products derived from this software without prior
written permission. For written permission, please contact
legal@channelpoint.com.

5.  Products derived from this software may not be called "Merlot"
nor may "Merlot" appear in their names without prior written
permission of ChannelPoint, Inc.

6. Redistribution of any form whatsoever must retain the following
acknowledgment:  "This product includes software developed by 
ChannelPoint, Inc. for use in the Merlot XML Editor 
(http://www.channelpoint.com/merlot/)."

THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ====================================================================

For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
For information on the Merlot project, please see 
http://www.channelpoint.com/merlot.
*/


// Copyright 1997-1998 ChannelPoint, Inc. All Rights Reserved.
// $Id: FileUtil.java,v 1.3 2000/03/10 05:31:17 camk Exp $

package org.merlotxml.util;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * File utilities
 *
 * @author	Rick Boykin
 * @version $Id: FileUtil.java,v 1.3 2000/03/10 05:31:17 camk Exp $
 */

public class FileUtil {

    private FileUtil() {
    }

  
    /**
     * Utility method to copy a file from one directory to another
     */
    public static void
    copyFile(String fileName, String fromDir, String toDir) throws 
    IOException {

	copyFile(new File(fromDir + File.separator + fileName),
		new File(toDir + File.separator + fileName));

    }
    /**
     * Utility method to copy a file from one directory to another
     */
    public static void
    copyFile(File from, File to) throws IOException {

	if (!from.canRead()) {
	    throw new IOException("Cannot read file '" + from + "'.");
	}

	if (to.exists() && (!to.canWrite())) {
	    throw new IOException("Cannot write to file '" + 
		    to + "'.");
	}

	FileInputStream fis = new FileInputStream(from);
	FileOutputStream fos = new FileOutputStream(to);

	byte[] buf = new byte[1024];

	int bytesLeft;
	while ((bytesLeft = fis.available()) > 0) {
	    if (bytesLeft >= buf.length) {
		fis.read(buf);
		fos.write(buf);
	    } else {
		byte[] smallBuf = new byte[bytesLeft];
		fis.read(smallBuf);
		fos.write(smallBuf);
	    }
	}

	fos.close();
	fis.close();
    }
	
	
	public static InputStream getInputStream(File file, Class c) throws FileNotFoundException {
	
		InputStream rtn;
		String s;
		if (file != null) {
			try {
				return new FileInputStream(file);
			}
			catch (FileNotFoundException e) {
				s = file.toString();
				int i = s.indexOf(File.separator);
				if (i >= 0) {
					s = s.substring(i);
					s = StringUtil.sReplace("\\", "/", s);
					if ( (rtn = c.getResourceAsStream(s)) != null) {
						return rtn;
					}
				}
				throw e;
			}
		}
		return null;
	}
	
}
