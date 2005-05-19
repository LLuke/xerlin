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

import java.awt.event.*;
import java.awt.datatransfer.*;
import java.beans.*;

import java.io.*;
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import java.text.*;

import java.util.*;

import com.sun.javax.swing.*;

import org.w3c.dom.*;

public class MerlotUndoableEdit 
    implements UndoableEdit, MerlotConstants
{

    public static final int DELETE = 1;
    public static final int INSERT = 2;
    public static final int MOVE   = 3;
	
    /**
     * For deletes this is the only node,
     * For moves, this is the node being moved. 
     * For inserts this is the node being inserted.
     */
    protected MerlotDOMNode _node;


    /**
     * this is basically a TreePath with child indices instead of object refs of where the
     * node used to be before being deleted or moved. We don't care about location when
     * undoing an insert. We just erase it.
     */
    protected int[] _nodeLocation;
	
    protected DOMTreeTableAdapter _tableModel;
    


    /**
     * Presentation name
     */	
    protected String _presName;
	
    protected int _action;
	

    /*
     * For MerlotUndoables taht take place in a document tree... we need to keep track of 
     * positions and the data itself rather than relating the nodes that are affected to 
     * nodes that previously existed. This will make undoing much easier
     */

	
    public MerlotUndoableEdit(String pname, 
							int action, 
							DOMTreeTableAdapter model,
							MerlotDOMNode nd,
							int[] where)
    {
		_presName = pname;
		_action = action;
		_node = nd;
		_nodeLocation = where;
		_tableModel = model;
	
		
    }
	
	
	
    /**
     * Undo the edit that was made.
     */
    public void undo() throws CannotUndoException
    {
		MerlotDebug.msg("Undo: "+toString());
		switch (_action) {
		case DELETE:
			// add the node back in where it was removed from
			undoDelete();
			
			break;
			
		case INSERT:
			undoInsert();
			break;
			
		case MOVE:
			undoMove();
			break;
		}
		
				
    }

    protected void undoDelete() 
		throws CannotUndoException
    {
		//	MerlotDebug.msg("undoDelete");
	
		MerlotDOMNode parent = getParentFromLocationPath();
		int index = _nodeLocation[_nodeLocation.length - 1];
		MerlotDebug.msg("undoDelete: parent = "+parent+ " index = "+index);
	
		parent.insertChildAt(_node,index);
    }
	
    protected void undoInsert() 
		throws CannotUndoException
    {
		MerlotDebug.msg("undoInsert:  node =" + _node);
		_node.delete();
    }
	
    protected void undoMove() 
		throws CannotUndoException
    {
		MerlotDebug.msg("undo Move: not implemented");
		throw new CannotUndoException();
	
    }
	
    /**
     * This gets the parent node from the location path
     */
    protected MerlotDOMNode getParentFromLocationPath() 
		throws CannotUndoException
    {
		MerlotDOMNode root = (MerlotDOMNode)_tableModel.getRoot();
		int len = _nodeLocation.length-1;
	
		MerlotDOMNode nd = root;
		MerlotDOMNode[] children;
	
		for(int i=1;i < len;i++) {
			children = nd.getChildNodes();
			if (children.length > _nodeLocation[i]) {
				nd = children[_nodeLocation[i]];
			}
			else {
				MerlotDebug.msg("can't undo: node location problem");
				throw new CannotUndoException();
			}
			MerlotDebug.msg(" getParentFromLocationPath: loc["+i+"] = "+_nodeLocation[i]+"  node = "+nd);
	    
		}
		return nd;
    }
    

    /**
     * True if it is still possible to undo this operation
     */
    public boolean canUndo()
    {
		return true;
    }
	

    /**
     * Re-apply the edit, assuming that it has been undone.
     */
    public void redo() throws CannotRedoException
    {
		
    }
	

    /**
     * True if it is still possible to redo this operation
     */
    public boolean canRedo()
    {
		return false;
		
    }
	

    /**
     * May be sent to inform an edit that it should no longer be
     * used. This is a useful hook for cleaning up state no longer
     * needed once undoing or redoing is impossible--for example,
     * deleting file resources used by objects that can no longer be
     * undeleted. UndoManager calls this before it dequeues edits.
     *
     * Note that this is a one-way operation. There is no "undie"
     * method.
     *
     * @see CompoundEdit#die
     */
    public void die()
    {
		
    }
	

    /**
     * This UndoableEdit should absorb anEdit if it can. Return true
     * if anEdit has been incoporated, false if it has not.
     *
     * <p>Typically the receiver is already in the queue of a
     * UndoManager (or other UndoableEditListener), and is being
     * given a chance to incorporate anEdit rather than letting it be
     * added to the queue in turn.</p>
     *
     * <p>If true is returned, from now on anEdit must return false from
     * canUndo() and canRedo(), and must throw the appropriate
     * exception on undo() or redo().</p>
     */
    public boolean addEdit(UndoableEdit anEdit)
    {
		return false;
		
    }
	


    /**
     * Return true if this UndoableEdit should replace anEdit. The
     * receiver should incorporate anEdit's state before returning true.
     *
     * <p>This message is the opposite of addEdit--anEdit has typically
     * already been queued in a UndoManager (or other
     * UndoableEditListener), and the receiver is being given a chance
     * to take its place.</p>
     *
     * <p>If true is returned, from now on anEdit must return false from
     * canUndo() and canRedo(), and must throw the appropriate
     * exception on undo() or redo().</p>
     */
    public boolean replaceEdit(UndoableEdit anEdit)
    {
		return false;
		
    }
	

    /**
     * Return false if this edit is insignificant--for example one
     * that maintains the user's selection, but does not change any
     * model state. This status can be used by an UndoableEditListener
     * (like UndoManager) when deciding which UndoableEdits to present
     * to the user as Undo/Redo options, and which to perform as side
     * effects of undoing or redoing other events.
     */
    public boolean isSignificant()
    {
		return true;
    }
	

    /**
     * Provide a localized, human readable description of this edit
     * suitable for use in, say, a change log.
     */
    public String getPresentationName()
    {
		return _presName;
    }
	
    public void setPresentationName(String s) 
    {
		_presName = s;
    }
    

    /**
     * Provide a localized, human readable description of the undoable
     * form of this edit, e.g. for use as an Undo menu item. Typically
     * derived from getDescription();
     */
    public String getUndoPresentationName()
    {
		return _presName;
		
    }
	

    /**
     * Provide a localized, human readable description of the redoable
     * form of this edit, e.g. for use as a Redo menu item. Typically
     * derived from getPresentationName();
     */
    public String getRedoPresentationName()
    {
		return _presName;
		
    }
	
    public String toString() 
    {
		StringBuffer sb = new StringBuffer("MerlotUndoableEdit [");
		sb.append(_presName+", ");
		sb.append("action="+_action+", ");
		sb.append("node="+_node);
		sb.append("location="+array2String(_nodeLocation)+"]");
		
		return sb.toString();
    }
	
		public String array2String(int[] array) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i=0;i < array.length; i++) {
			sb.append(array[i]);
			if (i+1 < array.length) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
		
	}
	
	
}
