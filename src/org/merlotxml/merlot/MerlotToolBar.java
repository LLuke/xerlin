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

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Toolbar class that can hide the button text
 */

public class MerlotToolBar extends JToolBar
{
	/**
	 * when false, this acts just like the default JToolbar class
	 */
	protected boolean _hideText = true;
	
	public MerlotToolBar () 
	{
		super();
	}
	
	public MerlotToolBar(int orientation) 
	{
		super(orientation);
	}
	
	public JButton add(Action a) 
	{
		JButton b = super.add(a);
		if (a.getValue(Action.SMALL_ICON) != null) {
			if (_hideText) {
				b.setText(null);
			}
			b.setMargin(new Insets(3,3,3,3));
			b.setBorderPainted(false);
			MouseListener m = new ToolbarButtonMouseListener(b);
			
			b.addMouseListener(m);
			
		}
		String s;
		try {
			s = (String) a.getValue(Action.SHORT_DESCRIPTION);
			if (s != null) {
				b.setToolTipText(s);
			}
			
		}
		catch (ClassCastException ex) {
		}
		return b;
		
	}


    protected PropertyChangeListener createActionChangeListener(JButton b) {
        return new GTBActionChangedListener(b);
    }

    private class GTBActionChangedListener implements PropertyChangeListener {
        JButton button;
        
        GTBActionChangedListener(JButton b) {
            super();
            setTarget(b);
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)){
				if (!_hideText) {
					String text = (String) e.getNewValue();
					button.setText(text);
					button.repaint();
				}
				
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                button.setEnabled(enabledState.booleanValue());
                button.repaint();
            } else if (e.getPropertyName().equals(Action.SMALL_ICON)) {
                Icon icon = (Icon) e.getNewValue();
                button.setIcon(icon);
                button.invalidate();
                button.repaint();
            } 
        }
		public void setTarget(JButton b) {
			this.button = b;
		}
    }
	
	public  class ToolbarButtonMouseListener extends MouseAdapter 
	{
		protected JButton _button;
		
		public ToolbarButtonMouseListener (JButton b) 
		{
			_button = b;
		}
		
		public void mouseEntered (MouseEvent evt)
		{
			if (_button.isEnabled()) {
				_button.setBorderPainted(true);
			}
			
		}
		
		public void mouseExited(MouseEvent evt) 
		{
			_button.setBorderPainted(false);
		}
		
		
	}
	
}
