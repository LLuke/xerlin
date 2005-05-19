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
import javax.swing.tree.*;
import org.w3c.dom.*;

import com.sun.javax.swing.*;


/**
 * 
 * JTreeTableModel which has some drag and drop operations
 * 
 * @author Kelly A. Campbell
 *
 * @version $Id: DNDJTreeTableModel.java,v 1.2 2000/03/07 04:46:14 camk Exp $
 *
 */
public abstract class DNDJTreeTableModel extends AbstractTreeTableModel 
	implements MerlotConstants
{
    public DNDJTreeTableModel(Object root) {
        super(root);
    }
    
    /**
     * Takes a data object and inserts it into the tree after the given row.
     * Returns true if the drop is acceptable. Otherwise, returns false;
     * @param row the row affected
     * @param data the data being dropped
     * @param where INTO, BEFORE, or AFTER
     */
    public abstract boolean dropOnRow(int row, Object data, int where);
    
    
    /**
     * Allows the model give a dragndrop transferable for a given selection
     */
    public Transferable getTransferable(TreePath[] paths) 
    {
	return null;
    }
    

}
