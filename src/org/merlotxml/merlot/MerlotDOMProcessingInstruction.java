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

package org.merlotxml.merlot;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;


/**
 *
 *
 */

public class MerlotDOMProcessingInstruction extends MerlotDOMNode 
{
	protected boolean _visible = false;
	protected ProcessingInstruction  _pi = null;
	


	public MerlotDOMProcessingInstruction (Node c, 
					       XMLFile doc) 
	{
		super(c,doc);
		if (c instanceof ProcessingInstruction) {
		    //_pi = (ProcessingInstruction)c;
            _pi = (ProcessingInstruction)_theNode;
		}
		else {
		    _pi = null;
		}
		
		
	}

    public String getNodeName() 
    {
	return "?pi?";
    }
    

	public void setVisible(boolean tf) 
	{
		_visible = tf;
	}
	
	public boolean isVisible() 
	{
		return _visible;
	}
	
        public void setTarget(String s)
        {
	    // Placeholder - There is currently a limitation in the DOM
	    // that does not allow you to set the target, you must create
	    // a new processing instruction node and call replaceChild
	    
	}

        public String getTarget() 
        { 
	    if (_pi == null) {
		return "";
	    }
	    return _pi.getTarget();
        } 
    
	public String getText() 
	{
	    if (_pi == null) {
		return "";
	    }
	    return _pi.getData();
	    
	}
	
	public void setText(String s) 
	{
	    if (_pi != null) {
		_pi.setData(s);
	    }
	    
	}

        //  Currently these are used to work around a limitation in the
        //  DOM that does not allow you to manually set the target of the 
        //  processing instruction
        public ProcessingInstruction getNode()
        {
	    return _pi;
	}  

        public void setNode(ProcessingInstruction node) 
        { 
	    _pi = node;
        }

	public boolean isSpecialNode () {
		return true;
	}

    public Object clone() {
        Object clone = super.clone();
        ((MerlotDOMProcessingInstruction)clone)._pi =
            (ProcessingInstruction)(((MerlotDOMProcessingInstruction)clone)._theNode);
        return clone;
    }


}


