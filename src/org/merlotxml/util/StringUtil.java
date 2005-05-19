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


// Copyright 1998, ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.util;

/**
 * String utilities
 * 
 * @author Tim McCune
 * @version $Id: StringUtil.java,v 1.4 2000/05/02 23:21:54 camk Exp $
 */

public class StringUtil {
	
	public static final int RECURSION_THRESHOLD = 10;
	

	/**
		Search a string for all instances of a substring and replace
		it with another string.  Amazing that this is not a method
		of java.lang.String since I use it all the time.

		@param search Substring to search for
		@param replace String to replace it with
		@param source String to search through
		@return The source with all instances of <code>search</code>
			replaced by <code>replace</code>
	*/
	public static String sReplace(String search, String replace, String source) {

		int spot;
		String returnString;
		String origSource = new String(source);

		spot = source.indexOf(search);
		if (spot > -1) returnString = "";
		else returnString = source;
		while (spot > -1) {
			if (spot == source.length() + 1) {
				returnString = returnString.concat(source.substring(0, source.length() - 1).concat(replace));
				source = "";
			}
			else if (spot > 0) {
				returnString = returnString.concat(source.substring(0, spot).concat(replace));
				source = source.substring(spot + search.length(), source.length());
			}
			else {
				returnString = returnString.concat(replace);
				source = source.substring(spot + search.length(), source.length());
			}
			spot = source.indexOf(search);
		}
		if (! source.equals(origSource)) {
			return returnString.concat(source);
		}
		else {
			return returnString;
		}
	}
	

    /** 
     * Match a file glob style expression without ranges.
     * '*' matches zero or more chars.
     * '?' matches any single char.
     *
     * @param pattern           A glob-style pattern to match
     * @param input             The string to match
     *
     * @return whether or not the string matches the pattern.
     */
    public static boolean match(String pattern, String input) {
	int patternIndex = 0;
	int inputIndex   = 0;
	int patternLen   = pattern.length();
	int inputLen     = input.length();
	int[] stack      = new int[100];
	int stacktop     = 0;

	for (;;) {
	    if (patternIndex == patternLen) {
		if (inputIndex == inputLen) {
		    return true;
		}

	    } else {
		char patternChar = pattern.charAt(patternIndex);

		if (inputIndex < inputLen) {
		    if (patternChar == '*') {
			stack[stacktop++] = patternIndex;
			stack[stacktop++] = inputIndex + 1;
			patternIndex++;
			continue;

		    } else if (patternChar == '?' || 
			patternChar == input.charAt(inputIndex)) {
			patternIndex++;
			inputIndex++;
			continue;
		    }

		} else if (patternChar == '*') {
		    patternIndex++;
		    continue;
		}
	    }

	    if (stacktop == 0) {
		return false;
	    }
		
	    inputIndex   = stack[--stacktop];
	    patternIndex = stack[--stacktop];
	}
    }
    public static interface KeyFinder 
    {
       String lookupString(String key);
    }
    

    /**
     * This looks up {% %} delimted keys in a string and replaces them. This
     * is used by resource catalog, TreeConfig, and several other components.
     */
    public static String lookupKeysInString(String str, KeyFinder finder) 
    {
        return lookupKeysInString(str,0,finder);
    }
    
    public static String lookupKeysInString(String str, int recurselvl,KeyFinder finder) 
    {
        if (recurselvl > RECURSION_THRESHOLD) {
            throw new RuntimeException("Recursion Threshold reached");
        }
	
        // this is where all those years of c/c++ pay off in java
        //  boolean foundkey = false;
	
        char[] sb = str.toCharArray();
        int len = sb.length;

        // now go through the string looking for "{%"
        StringBuffer newsb = null;
       
        int lastKeyEnd = 0;
	
        for (int i=0;i<len;i++) {
            char c = sb[i];
            if ((c == '{') && (i+2 < len) && (sb[i+1] == '%')) {
				// we got a potential key
		
                int endkey = -1;
                StringBuffer key = new StringBuffer();
                for  (int j=i+2; j+1 < len && endkey < 0; j++) {
                    if (sb[j] == '%' && sb[j+1] == '}') {
                        endkey = j-1;
                    }
                    else {
                        key.append(sb[j]);
                    }
                }
                if (endkey > 0) {
                    String val = finder.lookupString(key.toString());
                    String s = lookupKeysInString(val,recurselvl+1,finder);
                    if (s != null) {
                        if (newsb == null) {
                            newsb = new StringBuffer(len);
                            for (int k = 0; k < i; k++) {
                                newsb.append(sb[k]);
                            }
                        }
                        else {
                            for (int k = lastKeyEnd+1; k < i; k++) {
                                newsb.append(sb[k]);
                            }
                        }
                        newsb.append(s);
                        i = endkey+2;
                        lastKeyEnd = i;
			
                    }
                }
            }
        }
        if (lastKeyEnd == 0 && newsb == null) {
            return str;
        }
        if (lastKeyEnd > 0 && lastKeyEnd+1 < len) {
            for (int k = lastKeyEnd+1; k < len; k++) {
                newsb.append(sb[k]);
            }
        }
        return newsb.toString();
	
    }

}
