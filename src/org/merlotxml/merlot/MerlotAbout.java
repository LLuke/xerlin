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
Merlot XML Editor (http://www.merlotxml.org/)."
 
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
(http://www.merlotxml.org/)."

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
http://www.merlotxml.org
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
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.border.*;
import org.w3c.dom.*;

import com.sun.javax.swing.*;


/**
 * About screen for merlot with cool animation someday
 */

public class MerlotAbout extends JInternalFrame
    implements Runnable, MerlotConstants
{

    XMLEditorFrame _frame;
    AboutScroller _scroller;
    
    
    public MerlotAbout(XMLEditorFrame frame) 
    {
	super(MerlotResource.getString(UI,"help.about")+" "
	      +XMLEditorSettings.getSharedInstance().getFrameTitle(), false,true);
	_frame = frame;
	setupPanel();
	this.addInternalFrameListener( new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) 
		{
		    if (_scroller != null) {
			_scroller.stop();
		    }
					
		}
	    });
    }
    
    protected void setupPanel() 
    {
	try {
	    ImageIcon pic = MerlotResource.getImage(UI,"about.background");
	    
	    JLabel label = new JLabel(pic);
	    JPanel p = new JPanel(new BorderLayout());
	    p.setBorder(new EmptyBorder(5,5,5,5));
	    
	    p.add(label,BorderLayout.CENTER);
	    
	    _scroller = new AboutScroller();
	    p.add(_scroller, BorderLayout.SOUTH);
	    
	    this.getContentPane().add(p);
	    // figure out where to put this to center it
	    Dimension d = _frame.getSize();
	    this.pack();
	    Dimension e = this.getSize();
	    int x,y;
	    x = (d.width / 2) - (e.width / 2);
	    y = (d.height / 2) - (e.height / 2) - 25;
	    this.setLocation(x,y);
	}
	catch (Exception ex) {
	    MerlotDebug.exception(ex);
	}
    }
	
    public void run () 
    {
	_frame.addInternalFrame(this,false);
	Thread t = new Thread(_scroller);
	t.start();
    }
	
    protected class AboutScroller extends JPanel 
	implements Runnable
    {
	String _stuff[];
			
	final static long FIVE_SECONDS = 5 * 1000;
	boolean _stop = false;
	JPanel _multiLineLabel;
	
	
	public AboutScroller() 
	{
	    super();
	    _stuff = new String[2];
	    _stuff[0] = MerlotResource.getString(UI,"merlot.version.string");
	    _stuff[1] = MerlotResource.getString(UI,"merlot.copyright");
			
	    //setText(_stuff[0]);
	    _multiLineLabel = MerlotUtils.createMultiLineLabel(_stuff[0],50);
	    _multiLineLabel.setBorder(new EmptyBorder(2,2,2,2));
	    
	    add(_multiLineLabel,BorderLayout.WEST);
			
			
	}
		
	public void run() 
	{
	    int i = 0;
	    while (!_stop) {
		try {
		    Thread.sleep(FIVE_SECONDS);
		}
		catch (InterruptedException ex) {
		}
		this.remove(_multiLineLabel);
				
		i = (i + 1) % _stuff.length;
				//	setText(_stuff[i]);
		_multiLineLabel = MerlotUtils.createMultiLineLabel(_stuff[i],50);			
		_multiLineLabel.setBorder(new EmptyBorder(2,2,2,2));
		add(_multiLineLabel,BorderLayout.WEST);
		this.validate();
				

	    }
	}
		
	public void stop() 
	{
	    _stop = true;
	}
    }
}
