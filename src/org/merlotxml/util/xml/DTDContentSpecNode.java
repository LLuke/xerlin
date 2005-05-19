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


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.util.xml;


/**
 * Interface to specify a ContentSpecification node.
 * <P>
 * This is used to describe the content 
 * specification of what a DTD element can contain.
 * <P> 
 * Example from a DTD: (el1?, el2+ (el3 | el4 | el5)*)
 * <P>
 * Each node is part of a tree and has a type. CONTENT_GROUP nodes
 * correspond to a ( ) delimted container, a CONTENT_SINGLE node
 * would be a single element leaf with no ordinality specified. (like 'el3' 
 * in the above example)
 * <P>
 * <PRE>
 * Possible types:
 * CONTENT_GROUP:    ( stuff between parens ) getLeft returns the first node
 *                                            getRight returns null
 * CONTENT_OR:       el3 | el4                getLeft returns the first leaf
 *                                            getRight returns a leaf or
 *                                              another non-leaf
 * CONTENT_CONCAT    el1?, el2+               getLeft, getRight acts the same
 *                                              as in CONTENT_OR
 *
 * Possible ordinalities:
 * CONTENT_SINGLE:   node with no ordinality  i.e. 'el3'
 * CONTENT_STAR:     node with zero to many   i.e. '(el3 | el4 | el5)*'
 * CONTENT_PLUS:     node with at least one   i.e. 'el2+'
 * CONTENT_ONEMAX:   node with at most one    i.e. 'el1?'
 * </PRE>
 *
 * @author Kelly A. Campbell
 */

public interface DTDContentSpecNode
{
	public String getName();
	
	public int getType();
	
	public int getOrdinality();
	
	public boolean isLeaf();
	
	public DTDContentSpecNode getLeft();
	
	public DTDContentSpecNode getRight();
		
}
