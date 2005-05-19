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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import matthew.awt.StrutLayout;



/**
 * This abstract class implements a node editor that has a table for a set of it's child elements
 *
 */


public abstract class ChildTableEditor
    implements MerlotDOMEditor, MerlotConstants

{
    /**
     * The type of child that shows up in the edit table
     */
    private Hashtable _edit_child_types;
	
    private String[] _titles;
    private String[] _attrnames;
    private int[]    _col_widths = null;
    private int      _table_height = -1;
    private int      _min_children = 0;
    private Class[]  _col_types = null;
	
	
    public ChildTableEditor () 
    {
	super();
	_edit_child_types = new Hashtable();
		
    }
	
    public void addEditChildType(String parenttype, String childtype) 
    {
	_edit_child_types.put(parenttype, childtype);
    }
	
    /**
	 * sets the child columns for the table of child nodes.
	 * <P>
	 * Params must be the same length. One is the displayed
	 * title. the other is the attribute name on the nodes.
	 */
    public void setColumns(String[] titles, String[] attrnames) 
    {
	_titles = titles;
	_attrnames = attrnames;
    }
	
    public void setColumnWidths(int[] colwidths) 
    {
	_col_widths = colwidths;
    }
	
    public void setColumnTypes(Class[] coltypes) 
    {
	_col_types = coltypes;
    }
	
	
    public void setPreferredTableHeight(int height) 
    {
	_table_height = height;
    }
	
    public void setMinChildren (int m) 
    {
	_min_children = m;
    }
	

    public JPanel getEditPanel(MerlotDOMNode node) 
    {
	return new GenericChildTableEditPanel(node);
    }

    public void grabFocus(JPanel p) 
    {
	if (p instanceof GenericChildTableEditPanel) {
	    ((GenericChildTableEditPanel)p).grabFocus();
	}
    }
	

    public void savePanel(JPanel p)
	throws PropertyVetoException
    {
	if (p instanceof GenericChildTableEditPanel) {
	    ((GenericChildTableEditPanel)p).save();
	}
	else if (p instanceof ChildTableEditPanel) {
	    ((ChildTableEditPanel)p).save();
	}
	else {
	    MerlotDebug.msg("Cannot save! Panel is not the right type.");
	}
		
    }
	
    protected class GenericChildTableEditPanel extends JPanel
    {
	private MerlotDOMNode _parent;
	private JPanel  _editPanel;
	private ChildTableEditPanel      _childPanel;
		
	public GenericChildTableEditPanel (MerlotDOMNode nd) 
	{
	    // super(VERTICAL_SPLIT, true);
	    super();
	    
	    _parent = nd;
	    setupPanel();
	}
		
	public void setupPanel() 
	{
	    _editPanel = new GenericDOMEditor().getEditPanel(_parent);
	    String nodetype  = _parent.getNodeName();
			
	    String childtype = (String)_edit_child_types.get(nodetype);
	    if (childtype != null) {
		_childPanel = new ChildTableEditPanel(_parent,childtype);
		_childPanel.setPreferredSize(new Dimension(450,100));
				
		_editPanel.setBorder(new TitledBorder("Edit "+_parent.getNodeName()));
		_editPanel.setMinimumSize(new Dimension(4,4));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
						      true, _editPanel, _childPanel);
		
		StrutLayout lay = new StrutLayout();
		this.setLayout(lay);
		this.add(splitPane);
		lay.setSprings(splitPane, StrutLayout.SPRING_BOTH);

		splitPane.setOneTouchExpandable(true);
		
		// see if we're using jdk1.3 or later and set the weighting
		try {
		    Class spclass = JSplitPane.class;
		    Class[] paramClass = {double.class};
		    Object[] params = {new Double(0.66)};
		    
		    java.lang.reflect.Method m = spclass.getDeclaredMethod("setResizeWeight",paramClass);
		    if (m != null) {
			m.invoke(splitPane,params);
		    }
		}
		catch (NoSuchMethodException nsm) {
		}
		catch (Exception ex) {
		    MerlotDebug.exception(ex);
		}		
	    }
	    else {
		
		// give them a generic editor
		StrutLayout lay = new StrutLayout();
		this.setLayout(lay);
		this.add(_editPanel);
		lay.setSprings(_editPanel, StrutLayout.SPRING_BOTH);

	    }
	}
		
	public void grabFocus() 
	{
	    ((GenericDOMEditPanel)_editPanel).grabFocus();
			
	}
		
	protected void save() 
	    throws PropertyVetoException
	{
	    if (_editPanel != null) {
		((GenericDOMEditPanel)_editPanel).save();
	    }
	    if (_childPanel != null) { 
		_childPanel.save();
	    }
	}								  
    }
	
	
    public class ChildTableEditPanel extends JPanel
	implements ActionListener
    {
	private MerlotDOMNode _parent;
	private JTable  _table;
	private ChildTableModel _tableModel;
		
	private JButton     _removeButton;
	private JButton     _addButton;
	private JButton     _orderUpButton;
	private JButton     _orderDownButton;
	    

	public ChildTableEditPanel (MerlotDOMNode nd, String childtype) 
	{
	    super();
	    _parent = nd;
	    setupPanel(childtype);
	    this.setBorder(new TitledBorder(childtype));

	}
		
	protected JTable getTable() 
	{
	    return _table;
	}
		
		
	protected void setupPanel(String childtype) 
	{
	    StrutLayout lay = new StrutLayout();
		    
	    this.setLayout(lay);
		    			
	    _tableModel = new ChildTableModel(_parent,childtype);
	    VetoableChangeListener listener = new GenericDOMEditor.GenericSanityCheckListener();
			
	    _tableModel.addVetoableChangeListener(listener);
			
	    _table = new JTable(_tableModel);
	    _table.setMinimumSize(new Dimension(10,10));
			
	    JScrollPane sp = new JScrollPane(_table);
	    int width = 250;
	    int height = 100;
	    if (_col_widths != null) {
		width = 0;
		TableColumnModel col_model = _table.getColumnModel();
		for (int i=0;i<_col_widths.length;i++) {
		    width += _col_widths[i] + 2; // +2 for column header borders
		    TableColumn col = col_model.getColumn(i);
		    col.setPreferredWidth(_col_widths[i]);
		}
	    }
	    if (_table_height > 0) {
		height = _table_height;
	    }
	    sp.setPreferredSize(new Dimension(width,height));
	    sp.setMinimumSize(new Dimension(10,10));

	    this.add(sp);
	    lay.setSprings(sp, StrutLayout.SPRING_BOTH);
	    lay.setDebug(sp,true, "ScrollPane");
	    Dimension s = lay.preferredLayoutSize(this);
		// System.out.println("s = "+s);
					
	    _removeButton = new JButton("Remove");
	    _addButton    = new JButton("Add");

	    ImageIcon up   = MerlotResource.getImage(MerlotConstants.UI, "editor.childtable.up.icon");
	    ImageIcon down = MerlotResource.getImage(MerlotConstants.UI, "editor.childtable.down.icon");

	    _orderUpButton = new JButton(up);
	    _orderDownButton = new JButton(down);
		
	    _removeButton.setActionCommand("REMOVE");
	    _addButton.setActionCommand("ADD");
			
	    _orderUpButton.setActionCommand("ORDERUP");
	    _orderDownButton.setActionCommand("ORDERDOWN");
		
	    _removeButton.addActionListener(this);
	    _addButton.addActionListener(this);
	    _orderUpButton.addActionListener(this);
	    _orderDownButton.addActionListener(this);
				   
	    JLabel emptySpaceR = new JLabel("");
	    JLabel emptySpaceL = new JLabel("");


	    StrutLayout buttonLay = new StrutLayout();
	    JPanel buttonPanel = new JPanel(buttonLay);
	    buttonPanel.add(emptySpaceL);
	    buttonPanel.add(_orderDownButton, new StrutLayout.StrutConstraint(emptySpaceL,
									      StrutLayout.MID_RIGHT,
									      StrutLayout.MID_LEFT,
									      StrutLayout.EAST,
									      10));
	    buttonPanel.add(_orderUpButton, new StrutLayout.StrutConstraint(_orderDownButton,
									      StrutLayout.MID_RIGHT,
									      StrutLayout.MID_LEFT,
									      StrutLayout.EAST,
									      10));
	    buttonPanel.add(_removeButton, new StrutLayout.StrutConstraint(_orderUpButton,
									      StrutLayout.MID_RIGHT,
									      StrutLayout.MID_LEFT,
									      StrutLayout.EAST,
									      10));
	    buttonPanel.add(_addButton, new StrutLayout.StrutConstraint(_removeButton,
									      StrutLayout.MID_RIGHT,
									      StrutLayout.MID_LEFT,
									      StrutLayout.EAST,
									      10));


	    StrutLayout.StrutConstraint strut;
	    strut = new StrutLayout.StrutConstraint(sp,
						    StrutLayout.MID_BOTTOM,
						    StrutLayout.MID_TOP,
						    StrutLayout.SOUTH,
						    5);
	    this.add(buttonPanel,strut);
	    lay.setSprings(buttonPanel,StrutLayout.SPRING_HORIZ);
	    this.setMinimumSize(new Dimension(10,10));
	}
		
	public void save() 
	    throws PropertyVetoException
	{
	    //	MerlotDebug.msg("child table save");
			
	    //	((GenericDOMEditPanel)_editPanel).save();
	    if (_table.isEditing()) {
		TableCellEditor tce = _table.getCellEditor();
		tce.stopCellEditing();
	    }
			
	}								  
			

	public void actionPerformed(ActionEvent evt) 
	{
	    String cmd = evt.getActionCommand();
	    int row = _table.getSelectedRow();
		ListSelectionModel selectionModel = _table.getSelectionModel();
		
	    if (cmd.equals("ADD")) {
			_tableModel.addRow();
			row = _tableModel.getRowCount() - 1;
			selectionModel.setSelectionInterval(row, row);
		}
	    else if (cmd.equals("REMOVE") && row >= 0) {
			// remove the selected textbox
			_tableModel.removeRow(row);
	    }	
	    else if (cmd.equals("ORDERUP") && row >= 0) {
			_tableModel.moveRowUp(row);
			if (row > 0) row--;
			selectionModel.setSelectionInterval(row, row);
	    }
	    else if (cmd.equals("ORDERDOWN") && row >= 0) {
			_tableModel.moveRowDown(row);
			if (row < (_tableModel.getRowCount()-1)) row++;
			selectionModel.setSelectionInterval(row, row);
    }
			
			
	}
		
    }

    protected class ChildTableModel extends AbstractTableModel
	implements MerlotNodeListener
    {
	private boolean _updating = false;
	MerlotDOMNode _parent;
	Vector      _children;
	String      _childtype;
	private Vector _vetoListeners;
		
		
	public ChildTableModel (MerlotDOMNode parent, String childtype) 
	{
	    _parent = parent;
	    _childtype = childtype;
		
		_parent.addMerlotNodeListener(this);
	    init();
	}
		
	private void init() 
	{
	    _children = new Vector();
	    _vetoListeners = new Vector();
			
	    MerlotDOMNode[] ch = _parent.getChildNodes();
	    MerlotDOMNode nd;
			
	    if (ch != null) {
		for (int i=0; i<ch.length; i++) {
		    nd = ch[i];
		    if (nd.getNodeName().equals(_childtype)) {
			_children.addElement(nd);
		    }
		}
	    }
	    if (_children.size() < _min_children) {
				// need to add a new textbox
		for (int i=0; i<_min_children; i++) {
		    addRow();
		}
				
	    }
	}
		
	public int getRowCount() 
	{
	    return _children.size();
	}
		
	public int getColumnCount() 
	{
	    return _titles.length;
	}
		
	public boolean isCellEditable(int row, int col) 
	{
	    return true;
	}
		
	public String getColumnName(int col) 
	{
	    return _titles[col];
	}
		
	public Class getColumnClass(int col) 
	{
	    if (_col_types != null) {
		return _col_types[col];
	    }
	    else {
		return super.getColumnClass(col);
	    }
			
			
	}
		

	public Object getValueAt(int row, int col) 
	{
	    Object ret = null;
	    Object o = _children.elementAt(row);
	    MerlotDOMElement el;
	    if (o instanceof MerlotDOMElement) {
		el = (MerlotDOMElement)o;
		String attr = el.getAttribute(_attrnames[col]);
		ret = attr;
		if (_col_types != null) {
		    Class c = _col_types[col];
		    if (c == Boolean.class) {
			ret = Boolean.valueOf(attr);
		    }
		}
	    }
	    return ret;
			
	}
		
	public void setValueAt(Object obj, int row, int col) 
	{
	    //		MerlotDebug.msg("ChildTableEditor.setValueAt()");
			
	    String s = null;
	    if (obj instanceof Boolean) {
		s = ((Boolean)obj).toString();
	    }
	    else {
		s = obj.toString().trim();
	    }
	    if (s.equals("")) {
		s = null;		// BUG01409
	    }
			
	    if (row < _children.size()) { // BUG01445
		Object o = _children.elementAt(row);
		MerlotDOMElement el;
		if (o instanceof MerlotDOMElement) {
		    el = (MerlotDOMElement)o;
		    String key = _attrnames[col];
		    String oldval = el.getAttribute(key);
		    String newval = s;
		    try {
			fireVetoableChange(new PropertyChangeEvent(el,key,oldval,newval));
						
			el.setAttribute(_attrnames[col],s);
		    }
		    catch (PropertyVetoException ex) {
			MerlotError.msg(ex.getMessage(), MerlotResource.getString(ERR,"error"));
		    }
					
		}
	    }
			
	}
		
	public void addRow() 
	{
		_updating = true;
	    MerlotDOMNode	nd = _parent.newChild(_childtype);
	    _children.addElement(nd);
	    fireTableDataChanged();
		_updating = false;
	}
		
	public void removeRow(int row)
	{
		_updating = true;
	    if (row < _children.size()) {
		Object o = _children.elementAt(row);
		if (o instanceof MerlotDOMNode) {
		    ((MerlotDOMNode)o).delete();
		    _children.remove(row);
		    fireTableDataChanged();
		}
	    }
		_updating = false;	
	}
		
	public void moveRowUp(int row) 
	{
	    if (row > 0 && row < _children.size()) {
		Object o = _children.elementAt(row);
		Object p = _children.elementAt(row-1);
		if (o instanceof MerlotDOMNode && p instanceof MerlotDOMNode) {
		    ((MerlotDOMNode)o).delete();
		    //_children.remove(row);
		    ((MerlotDOMNode)o).insertBefore((MerlotDOMNode)p);
		    //_children.add(row-1,o);
		    fireTableDataChanged();
		}
	    }
			
	}
		
		
	/**
	 * move the element at the selected row down one row
	 */
	public void moveRowDown(int row) 
	{
	    if (row < getRowCount() - 1) {
		Object o = _children.elementAt(row);
		Object p = _children.elementAt(row+1);
		if (o instanceof MerlotDOMNode && p instanceof MerlotDOMNode) {
		    ((MerlotDOMNode)o).delete();
		    //_children.remove(row);
		    ((MerlotDOMNode)o).insertAfter((MerlotDOMNode)p);
		    //_children.add(row+1,o);
		    fireTableDataChanged();
		}
	    }
	}
		   
	public void addVetoableChangeListener (VetoableChangeListener l) 
	{
	    //	_propChanger.addVetoableChangeListener(l);
	    _vetoListeners.addElement(l);
		
	}
	public void removeVetoableChangeListener (VetoableChangeListener l) 
	{
	    //_propChanger.removeVetoableChangeListener(l);
	    _vetoListeners.removeElement(l);
	}
	
	// gotta implement this stuff ourselves cause the PropertyChangeSupport 
	// classes compare the old and new values, and dont' fire if they're equal
	public void fireVetoableChange(PropertyChangeEvent evt) 
	    throws PropertyVetoException
	{
	    //	System.out.println("fireVetoableChange: "+evt);
		
	    Enumeration e = _vetoListeners.elements();
	    while (e.hasMoreElements()) {

		VetoableChangeListener l = (VetoableChangeListener)e.nextElement();
				//		System.out.println("  listener: "+l);
		l.vetoableChange(evt);
	    }
	}
	
		public void nodeInserted(MerlotDOMNode parent, int[] indices, MerlotDOMNode[] children) {
			if (!_updating) {
				init();
				fireTableDataChanged();
			}
		}
		
		public void nodeRemoved(MerlotDOMNode parent, int[] indices, MerlotDOMNode[] children) {
			if (!_updating) {
				init();
				fireTableDataChanged();
			}
		}
			
		public void nodeDeleted(MerlotDOMNode node) {
			if (!_updating) {
				init();
				fireTableDataChanged();
			}
		}
		
		public void nodeChanged(MerlotDOMNode parent, int[] indices, MerlotDOMNode[] children) {
			if (!_updating) {
				init();
				fireTableDataChanged();
			}
		}
    }
}
