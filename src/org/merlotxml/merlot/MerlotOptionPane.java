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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;


/**
 * 
 * Special wrapper interface to JOptionPane that allows this to make the
 * parent frame grab focus after the option pane goes away.
 * 
 * @author Kelly A. Campbell
 *
 */
public class MerlotOptionPane
{
    public static int showInternalConfirmDialog(Component parentComponent,
						Object message) 
    {
	int ret = JOptionPane.showConfirmDialog(parentComponent,
							message);
	focusParent(parentComponent);
	return ret;
		
    }
	
    public static int showInternalConfirmDialog(Component parentComponent,
						Object message, 
						String title, 
						int optionType) 
    {
	int ret = JOptionPane.showConfirmDialog(parentComponent,
							message,
							title,
							optionType);
	focusParent(parentComponent);
	return ret;
    }
	
    public static int showInternalConfirmDialog(Component parentComponent,
						Object message, 
						String title, 
						int optionType, 
						int messageType) 
    {
	int ret = JOptionPane.showConfirmDialog(parentComponent,
							message,
							title,
							optionType,
							messageType);
	focusParent(parentComponent);
	return ret;
    }
	
    public static int showInternalConfirmDialog(Component parentComponent, 
						Object message,
						String title,
						int optionType, 
						int messageType,
						Icon icon) 
    {
	int ret;
	ret = JOptionPane.showConfirmDialog( parentComponent,  
						     message,
						     title,	
						     optionType,
						     messageType,
						     icon);
		
		
	focusParent(parentComponent);
	return ret;
		
    }

    public static String showInternalInputDialog(Component parentComponent,
						 Object message) 
    {
	String ret = JOptionPane.showInputDialog(parentComponent,
							 message);
	focusParent(parentComponent);
	return ret;
    }
	
    public static String showInternalInputDialog(Component parentComponent,
						 Object message, 
						 String title, 
						 int messageType) 
    {
	String ret = JOptionPane.showInputDialog(parentComponent,
							 message,
							 title,
							 messageType);
	focusParent(parentComponent);
	return ret;	
    }
	
    public static Object showInternalInputDialog(Component parentComponent,
						 Object message, 
						 String title, 
						 int messageType, 
						 Icon icon,
						 Object[] selectionValues, 
						 Object initialSelectionValue) 
    {
	Object ret = JOptionPane.showInputDialog(parentComponent,
							 message,
							 title,
							 messageType,
							 icon,
							 selectionValues,
							 initialSelectionValue);
	focusParent(parentComponent);
	return ret;
    }
	
    public static void showInternalMessageDialog(Component parentComponent,
						 Object message) 
    {
	JOptionPane.showMessageDialog(parentComponent,
					      message);
	focusParent(parentComponent);
	
    }
	
    public static void showInternalMessageDialog(Component parentComponent,
						 Object message, 
						 String title, 
						 int messageType)
    {
	JOptionPane.showMessageDialog(parentComponent,
					      message,
					      title,
					      messageType);
	focusParent(parentComponent);
	
    }

    public static void showInternalMessageDialog(Component parentComponent,
						 Object message, 
						 String title,
						 int messageType, 
						 Icon icon) 
    {
	JOptionPane.showMessageDialog(parentComponent,
					      message,
					      title,
					      messageType,
					      icon);
	focusParent(parentComponent);
		
    }
	
    public static int showInternalOptionDialog(Component parentComponent,
					       Object message, 
					       String title, 
					       int optionType, 
					       int messageType,
					       Icon icon, 
					       Object[] options, 
					       Object initialValue) 
    {
	int ret = JOptionPane.showOptionDialog(parentComponent,
						       message,
						       title,
						       optionType,
						       messageType,
						       icon,
						       options,
						       initialValue);
	focusParent(parentComponent);
	return ret;
    }
	

    protected static void focusParent(Component p) 
    {
	p.requestFocus();
	if (p instanceof JInternalFrame) {
	    try {
		((JInternalFrame) p).toFront();
		((JInternalFrame) p).setSelected(true);
	    }
	    catch (Exception ex) {
	    }
			
	}	
    }
	
	
}
