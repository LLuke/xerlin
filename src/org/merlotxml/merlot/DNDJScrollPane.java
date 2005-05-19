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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;

import javax.swing.*;


/**
 * 
 * JScrollPane that delegates drag and drop events to it's viewport component
 * if the viewport supports drag and drop. This is usefull for things like 
 * tables or trees that might not fill their scrollpane, but you still want
 * to be able to drop stuff within the full pane and have it go to the
 * component.
 *
 * XXX I don't thinks this works currently
 * 
 * @author Kelly A. Campbell
 *
 * @version $Id: DNDJScrollPane.java,v 1.2 2000/03/07 04:46:14 camk Exp $
 *
 */
public class DNDJScrollPane extends JScrollPane
	implements DropTargetListener
{
    
    protected DropTargetListener _delegate = null;
    protected DropTarget _dropTarget;
    
    
    public DNDJScrollPane() 
    {
	super();
    }
    
    public DNDJScrollPane(Component c) 
    {
	super(c);
	if (c instanceof DropTargetListener) {
	    _delegate = (DropTargetListener)c;
	    _dropTarget = new DropTarget(this,this);
	}	
    }
    
    public DNDJScrollPane(Component c, int hp, int vp)
    {
	super(c,hp,vp);
	
	if (c instanceof DropTargetListener) {
	    _delegate = (DropTargetListener)c;
	    _dropTarget = new DropTarget(this,this);
	}
    }

    public  DNDJScrollPane(int hp, int vp)
    {
	super(hp,vp);
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {
	if (_delegate != null) {
	    // highlight the scrollpane
	    MerlotDebug.msg("SP DragEnter");
	}
    }
    
    public void dragOver(DropTargetDragEvent dtde)
    {
	// nothing
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde)
    {	
    }
    
    
    public void dragExit(DropTargetEvent dte)
    {
	// unhighlight
    }
    
    
    public void drop(DropTargetDropEvent dtde)
    {
	// pass on the drop to the delegate if we need to
	if (_delegate != null) {
	    // now see if the event was within the viewport's bounds
	    
	    // if not, pass the event on up, translating the drop point
	    // to -1, -1
	    MerlotDebug.msg("SP drop");
	    
	}
    }
    
    
}
