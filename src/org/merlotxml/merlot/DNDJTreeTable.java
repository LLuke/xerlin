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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import com.sun.javax.swing.JTreeTable;
import com.sun.javax.swing.TreeTableModel;


/**
 * 
 * JTreeTable which implements drag and drop operations
 * 
 * @author Kelly A. Campbell
 *
 */
public class DNDJTreeTable extends JTreeTable implements DragGestureListener, DropTargetListener
{
    protected DragSource _dragSource = DragSource.getDefaultDragSource();
	
    protected DropTarget _dropTarget;

    protected int _lastrow = -1;
    protected Rectangle _oldrp = null;
	
    protected Rectangle _dndhint = null;
	
    protected Rectangle _hintBig = null;
    protected Rectangle _hintSmall = null;
    protected boolean   _inside = false;
    protected boolean   _before = false;
	
	

    protected TreeTableModel _model;
	
    public DNDJTreeTable (TreeTableModel m) 
    {
	super(m);
	_model = m;
	_dragSource.createDefaultDragGestureRecognizer(this, 
						       DnDConstants.ACTION_COPY_OR_MOVE,
						       this);
	_dropTarget = new DropTarget(this,this);
    }
	

    // hint painter
    public void paint(Graphics g) 
    {
	super.paint(g);
	if (_lastrow >= 0) {
	    // paint a line where the tree node is
	    g.setColor(Color.black);
	    g.setXORMode(Color.white);
	    int x,y,w,h;
	    x = _dndhint.x;
	    y = _dndhint.y;
	    w = _dndhint.width;
	    h = _dndhint.height;
	    //	MerlotDebug.msg("painting line: x="+x+" y="+y+" w="+w+" h="+h);
			
	    g.drawRect(x,y,w,h);
	    g.setPaintMode();
			
	}
    }
	

    // DragGestureListener

    public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
	// first make sure this isn't a popup menu event that triggered it
	InputEvent evt = dragGestureEvent.getTriggerEvent();
	if (evt instanceof MouseEvent) {
	    if (((MouseEvent)evt).isPopupTrigger()) {
		return;
	    }
	}
	
	JTreeTable.TreeTableCellRenderer tree = (JTreeTable.TreeTableCellRenderer)
	    getDefaultRenderer(TreeTableModel.class);
	TreePath[] paths = tree.getSelectionPaths();
	if (paths == null) {
	    // Nothing selected, nothing to drag
	    System.out.println ("Nothing selected - beep");
	    getToolkit().beep();
	} else {
		
	    Object selection = null;
	    Transferable transfer = null;
			
	    if (_model instanceof DNDJTreeTableModel) {
				// let the model give us what it wants us to transfer 
				// for the given selection
		transfer = ((DNDJTreeTableModel)_model).getTransferable(paths);
		if (transfer != null) {
		    selection = transfer;
		}
	    }
	    if (selection instanceof Transferable) {
		_dragSource.startDrag(dragGestureEvent,
				      DragSource.DefaultCopyDrop,
				      (Transferable)selection,
				      _dragSourceListener);
	    }
			
	}
    }
	

    final static DragSourceListener _dragSourceListener
	= new MyDragSourceListener();
	 
    static class MyDragSourceListener
	implements DragSourceListener {
	public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent) {
	}
	public void dragEnter(DragSourceDragEvent DragSourceDragEvent) {
	}
	public void dragExit(DragSourceEvent DragSourceEvent) {
	}
	public void dragOver(DragSourceDragEvent DragSourceDragEvent) {
	}
	public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent) {
	}
    }

    // DropListenerInterface methods

    public void dragEnter (DropTargetDragEvent dropTargetDragEvent) {
	dropTargetDragEvent.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
    }
	
    public void dragExit (DropTargetEvent dropTargetEvent) {
	resetDragHint();
    }
	
    public void dragOver (DropTargetDragEvent dropTargetDragEvent) {
	// draw some stuff where we're dragging
	Point loc = dropTargetDragEvent.getLocation();
	int row = rowAtPoint(loc);

	if (row >= 0 && row != _lastrow) {
	    // XXX see what type the drag data is, and if it's something
	    // that can be a sibling or child. 
	    // -- watch for the rare case of both, when we should make
	    // it a child if the drag is within a smaller bounds of the
	    // possible parent, Otherwise, put it in as a sibling
		
	    _hintBig = getTree().getRowBounds(row);
	    Rectangle tablerect = this.getBounds();
	    _hintBig.width = tablerect.width - (_hintBig.x - tablerect.x) - 10;
			
	    _hintSmall = new Rectangle(_hintBig);
	    _hintSmall.grow(0,-6);
		
	}
	// now highlight a rectangle or a line depending on where we're at
	if (_hintSmall.contains(loc)) {
	    _inside = true;
	    _dndhint = _hintBig;
	}
	else {
	    _inside = false;
	    if (loc.y < _hintSmall.y) {
		_before = true;
	    }
	    else {
		_before = false;
	    }

        // Make sure the row before current row and
        // the row after current are VISIBLE by auto scrolling.
        if (row+1< getRowCount()) {
            scrollRectToVisible(getTree().getRowBounds(row+1));
        }

        if (row>0){
            scrollRectToVisible(getTree().getRowBounds(row-1));
        }

		
	    int x,y;
	    x = _hintBig.x;
	    if (_before) {
		y = _hintBig.y;
	    }
	    else {
		y = _hintBig.y + _hintBig.height;
	    }
	    _dndhint = new Rectangle(x,y,_hintBig.width,0);
		
	}
	Rectangle r = getTree().getRowBounds(row);
	Rectangle rp = new Rectangle(_dndhint.x-1,_dndhint.y-1,_dndhint.width+2,_dndhint.height+2);
	repaint(rp);
	if (_oldrp != null) {
	    repaint(_oldrp);
	}
	_oldrp = rp;
	_lastrow = row;
    }
    
    public void resetDragHint()
    {
	_lastrow = -1;
	if (_oldrp != null) {
	    repaint(_oldrp);
	    _oldrp = null;		
	}
    }
	
	

    public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent) {
    }
	
    public synchronized void drop (DropTargetDropEvent dropTargetDropEvent) {
	try {
	    Transferable tr = dropTargetDropEvent.getTransferable();
	    if (tr.isDataFlavorSupported(MerlotDOMNode.DOM_TREENODE_FLAVOR)) {
		Object userObject = tr.getTransferData(MerlotDOMNode.DOM_TREENODE_FLAVOR);
		MerlotDebug.msg("Drop: userObject = "+userObject);
				// see if the tree model will take this
		    
		boolean acceptdrop = false;
		if (_model instanceof DNDJTreeTableModel) {
		    // get the row we're dropping it on 
		    int row = rowAtPoint(dropTargetDropEvent.getLocation());
		    int where;
		    if (_inside) {
			where = DNDJTreeTableModel.INTO;
		    }
		    else if (_before) {
			where = DNDJTreeTableModel.BEFORE;
		    }
		    else {
			where = DNDJTreeTableModel.AFTER;
		    }
					
		    MerlotDebug.msg("dropOnRow("+row+" userObject:"+userObject+" where:"+where+")");
					
		    acceptdrop = ((DNDJTreeTableModel)_model).dropOnRow(row,userObject,where);
		    if (acceptdrop) {
										
			dropTargetDropEvent.acceptDrop (DnDConstants.ACTION_COPY);
		    }
		    else {
			dropTargetDropEvent.rejectDrop();
		    }
		}
		else {
		    dropTargetDropEvent.rejectDrop();
		}
				
		dropTargetDropEvent.getDropTargetContext().dropComplete(true);
	    } else if (tr.isDataFlavorSupported (DataFlavor.stringFlavor)) {
		dropTargetDropEvent.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
		String string = (String)tr.getTransferData (DataFlavor.stringFlavor);
							
		dropTargetDropEvent.getDropTargetContext().
		    dropComplete(true);

	    } else if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
		dropTargetDropEvent.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
		java.util.List fileList = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
		Iterator iterator = fileList.iterator();
		while (iterator.hasNext()) {
		    File file = (File)iterator.next();
		    Hashtable hashtable = new Hashtable();
		    hashtable.put("name", file.getName());
		    hashtable.put("url", file.toURL().toString());
		}
		dropTargetDropEvent.getDropTargetContext().dropComplete(true);
	    } else {
		System.err.println ("Rejected");
		dropTargetDropEvent.rejectDrop();
	    }
	} catch (IOException io) {
	    io.printStackTrace();
	    dropTargetDropEvent.rejectDrop();
	} catch (UnsupportedFlavorException ufe) {
	    ufe.printStackTrace();
	    dropTargetDropEvent.rejectDrop();
	}
	resetDragHint();
    }
}	

