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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import matthew.awt.StrutLayout;

import org.merlotxml.util.xml.DTDAttribute;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This is a generic node editing panel which provides a component for each attribute
 * listed with the element it's created to edit, along with a text box for PCDATA.
 *  <P>
 * This class can be extended to change what the user sees for each attribute field.
 * Typically the easiest methods to overload for this type of custom editors are getEditComponent()
 * and sometimes save().
 *
 * @author Kelly Campbell
 */

public class GenericDOMEditPanel extends JPanel implements MerlotConstants {

    private static final String HIDE_PROP = "merlot.editor.generic.hide";
    private static final String RO_PROP = "merlot.editor.generic.readonly";

    public static final int ALIGN_TOP = 0;
    public static final int ALIGN_MIDDLE = 1;
    public static final int ALIGN_BOTTOM = 2;

    public static final boolean NAMESPACES = false;

    /**
     * The icon to use for required attribute labels
     */
    protected static Icon _requiredAttrIcon = null;

    /**
     * The node this editor was created for
     */
    protected MerlotDOMNode _node;
    /**
     * A node which is the child #text element for this node
     */
    protected MerlotDOMText _subtext;

    /**
     * The attributes and their values from this node
     */
    protected NamedNodeMap _node_attributes;

    /**
     * Map of attribute names to their DTDAttribute declaration
     */
    protected Hashtable _dtd_attributes;

    /**
     * Map of attribute names to attribute components (key is String, val is JComponent)
     */
    protected Hashtable _attrComponents;

    private boolean _first_component = true;
    protected JComponent _prev = null;
    private JComponent _first_field = null;

    protected JTextArea _text = null;
    protected JTextField _target = null;

    /**
     * A list of attributes that should be hidden. Key is element.attr. If value is nonnull,
     * then the attribute is hidden in the generic panel. #text can also be hidden if key
     * '#text' is in this hash
     */
    private static Hashtable _hideAttrs;

    /**
     * Similar to the hidable attributes, except these are displayed, but can't be changed
     */
    protected static Hashtable _readonlyAttrs;

    /**
     * list of PropertyChangeListeners that can veto editing actions 
     */
    private Vector _vetoListeners;

    /**
     * The panel which contains the actual layout of attributes
     */
    private JPanel _attributePanel;

    public GenericDOMEditPanel() {
        super();
    }

    public GenericDOMEditPanel(MerlotDOMNode node) {
        super();
        _node = node;

        buildPanel();
    }

    protected void buildPanel() {
        init();
        setupReadonlyTable();
        setupHideTable();

        _vetoListeners = new Vector();

        initPanelLayout();
        setupPanel();
        addVetoableChangeListener(new StandardAttributeChecker());
    }

    protected void init() {
        _node_attributes = _node.getAttributes();
        _dtd_attributes = new Hashtable();
        _attrComponents = new Hashtable();
    }

    protected void initPanelLayout() {
        _attributePanel = new JPanel();
        _attributePanel.setMinimumSize(new Dimension(4, 4));
        _attributePanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        StrutLayout slay = new StrutLayout();
        StrutLayout.setDefaultStrutLength(10);

        _attributePanel.setLayout(slay);

        // get the root element for the strut layout.. it will be the icon of the node
        JLabel iconLabel = new JLabel(_node.getIcon());
        _prev = iconLabel;
        _attributePanel.add(iconLabel);

        // wrap a ScrollPane around it
        ScrollablePanel scrollPanel = new ScrollablePanel(true, false);
        StrutLayout lay = new StrutLayout();
        scrollPanel.setLayout(lay);
        scrollPanel.add(_attributePanel);
        lay.setSprings(_attributePanel, StrutLayout.SPRING_BOTH);
        scrollPanel.setMinimumSize(new Dimension(4, 4));
        scrollPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

        JScrollPane sp =
            new JScrollPane(
                scrollPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sp.setMinimumSize(new Dimension(4, 4));
        sp.getViewport().setMinimumSize(new Dimension(4, 4));

        lay = new StrutLayout();
        this.setLayout(lay);
        this.add(sp);
        lay.setSprings(sp, StrutLayout.SPRING_BOTH);

    }

    protected void setupPanel() {
        MerlotDebug.msg("SetupPanel");

        JComponent prev = null;
        boolean newsubtext = false;

        DTDAttribute a;

        // get all the attributes and create a set of text fields,
        // and labels
        Enumeration e = _node.getDTDAttributes();
        if (e != null) {
            //	MerlotDebug.msg("Got attributes");

            int i = 0;

            while (e.hasMoreElements()) {
                a = (DTDAttribute) e.nextElement();
                _dtd_attributes.put(a.getName(), a);
                i++;
            }

            i = 0;

            // hack to make sure name and label attributes show up first in the list
            // XXX make this a configurable hack someday 
            a = (DTDAttribute) _dtd_attributes.get("name");
            if (a != null) {
                addAttribute(a);
                i++;
            }

            a = (DTDAttribute) _dtd_attributes.get("label");
            if (a != null) {
                addAttribute(a);
                i++;
            }

            e = _dtd_attributes.elements();
            while (e.hasMoreElements()) {
                a = (DTDAttribute) e.nextElement();
                if (a.getName().equals("name")
                    || a.getName().equals("label")) {
                    // skip it 
                } else {
                    if (!suppressAttribute(a)) {
                        addAttribute(a);
                        i++;
                    } else if (a.getDefaultType() == DTDConstants.FIXED) {
                        // Hidden FIXED components should still be saved - Hack, Hack
                        _attrComponents.put(a.getName(), new JLabel());
                    }
                }
                //	MerlotDebug.msg("Added "+c+" to panel");
            }
        }

        addTextPanel();
    }

    /**
     * Add a text editing panel if required
     */
    protected void addTextPanel() {
        boolean newsubtext = false;
        // see if this node has a #text node possible under it
        MerlotDOMNode[] children = _node.getChildNodes();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof MerlotDOMText) {
                MerlotDOMText textNode = (MerlotDOMText) children[i];
                //  System.out.println("child["+i+"] = "+children[i]+" nodeValue='"+textNode.getText()+"'");
                if (textNode.isVisible()) {
                    _subtext = textNode;
                    break;
                }
            }
        }
        // see if #text is a posibility
        if (_subtext == null) {
            // Current GrammarAccess code does not provide information
            // about text nodes...
            /*
            Enumeration en = _node.getAppendableElements();
            DTDElement el;
            String nm;
            
            while (en != null && en.hasMoreElements()) {
            	el = (DTDElement)en.nextElement();
            	if (el != null) {
            		nm = el.getName();
            		if (nm != null && nm.equals(DTDConstants.PCDATA_KEY)) {
            			newsubtext = true;
            			break;
            		}
            	}
            }
            */

            GrammarComplexType gtc = _node.getGrammarComplexType();
            if (gtc != null)
                newsubtext = gtc.getIsSimpleContentAllowed();

        }
        if (_subtext != null || newsubtext) {
            String s = "";
            if (!newsubtext) {
                s = _subtext.getText();
            }

            _text = new JTextArea(s);
            _text.setLineWrap(true);
            _text.setWrapStyleWord(true);
            _text.setMinimumSize(new Dimension(4, 4));
            Locale locale = XMLEditorSettings.getSharedInstance().getLocale();
            _text.setComponentOrientation(
                ComponentOrientation.getOrientation(locale));

            JScrollPane sp =
                new JScrollPane(
                    _text,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setPreferredSize(new Dimension(350, 200));
            JLabel l = new JLabel(DTDConstants.PCDATA_KEY + ":", JLabel.RIGHT);

            addAttributeComponent(l, sp, ALIGN_TOP);
        }

    }

    /**
     * returns true if the attribute doesn't show in the editor display
     */
    protected boolean suppressAttribute(DTDAttribute a) {

        String s = _node.getNodeName() + "." + a.getName();
        String t = (String) _hideAttrs.get(s);
        if (t != null) {
            return true;
        }
        return false;

    }

    protected void setupHideTable() {
        String hidetypes =
            XMLEditorSettings.getSharedInstance().getProperty(HIDE_PROP);
        _hideAttrs = new Hashtable();
        if (hidetypes != null) {
            // parse it
            StringTokenizer tok = new StringTokenizer(hidetypes, ", ");
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                _hideAttrs.put(s, "yes");
            }
        }

    }

    protected void setupReadonlyTable() {
        String rotypes =
            XMLEditorSettings.getSharedInstance().getProperty(RO_PROP);
        //	System.out.println("rotypes = "+rotypes);

        _readonlyAttrs = new Hashtable();
        if (rotypes != null) {
            // parse it
            StringTokenizer tok = new StringTokenizer(rotypes, ", ");
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                MerlotDebug.msg("adding " + s + " to readonly attributes");

                _readonlyAttrs.put(s, "yes");
            }
        }

    }

    protected void addAttribute(DTDAttribute a) {
        if (a != null) {
            JLabel l = new JLabel(a.getName() + ":", JLabel.RIGHT);
            if (a.getDefaultType() == DTDAttribute.REQUIRED) {
                Icon reqIcon = getRequiredAttrIcon();
                if (reqIcon != null) {
                    l.setIcon(reqIcon);
                }
            }

            JComponent c = getEditComponent(a);

            if (c == null) {
                return;
            }
            _attrComponents.put(a.getName(), c);

            String s = _node.getNodeName() + "." + a.getName();
            String t = (String) _readonlyAttrs.get(s);
            if (t != null) {
                //	System.out.println("disabling component: "+c);

                if (c instanceof JTextComponent) {
                    ((JTextComponent) c).setEditable(false);
                }
                c.setEnabled(false);
            }
            addAttributeComponent(l, c, ALIGN_MIDDLE);

        }
    }

    protected Icon getRequiredAttrIcon() {
        if (_requiredAttrIcon == null) {
            _requiredAttrIcon =
                MerlotResource.getImage(UI, "editor.required.attr.icon");
        }
        return _requiredAttrIcon;
    }

    protected void addAttributeComponent(JLabel l, JComponent c, int align) {

        StrutLayout.StrutConstraint strut;
        StrutLayout.StrutConstraint strut2;

        if (_first_component) {
            strut =
                new StrutLayout.StrutConstraint(
                    _prev,
                    StrutLayout.MID_RIGHT,
                    StrutLayout.MID_LEFT,
                    StrutLayout.EAST,
                    20);
            strut2 =
                new StrutLayout.StrutConstraint(
                    l,
                    StrutLayout.MID_RIGHT,
                    StrutLayout.MID_LEFT,
                    StrutLayout.EAST);
            _attributePanel.add(l, strut);
            _attributePanel.add(c, strut2);

            _first_field = c;

            _first_component = false;

        } else {
            strut2 =
                new StrutLayout.StrutConstraint(
                    _prev,
                    StrutLayout.BOTTOM_LEFT,
                    StrutLayout.TOP_LEFT,
                    StrutLayout.SOUTH);
            switch (align) {
                case ALIGN_TOP : // top
                    strut =
                        new StrutLayout.StrutConstraint(
                            c,
                            StrutLayout.TOP_LEFT,
                            StrutLayout.TOP_RIGHT,
                            StrutLayout.WEST);
                    break;
                default :
                case ALIGN_MIDDLE : // middle
                    strut =
                        new StrutLayout.StrutConstraint(
                            c,
                            StrutLayout.MID_LEFT,
                            StrutLayout.MID_RIGHT,
                            StrutLayout.WEST);
                    break;
                case ALIGN_BOTTOM : // bot
                    strut =
                        new StrutLayout.StrutConstraint(
                            c,
                            StrutLayout.BOTTOM_LEFT,
                            StrutLayout.BOTTOM_RIGHT,
                            StrutLayout.WEST);
                    break;
            }

            _attributePanel.add(c, strut2);
            _attributePanel.add(l, strut);

        }
        if (c instanceof JTextField) {
            ((StrutLayout) _attributePanel.getLayout()).setSprings(
                c,
                StrutLayout.SPRING_HORIZ);
        }
        if (c instanceof JScrollPane) {
            ((StrutLayout) _attributePanel.getLayout()).setSprings(
                c,
                StrutLayout.SPRING_BOTH);
        }

        _prev = c;

    }

    /**
     * Create a component based on the attribute type, and get the default from
     * the node, or if the node doesn't have it set, get the default value from
     * the attribute definition itself
     */
    protected JComponent getEditComponent(DTDAttribute attr) {
        MerlotDebug.msg("getEditComponent(" + attr + ")");

        int t = attr.getType();
        JComponent ret = null;
        String value = null;
        Attr a = null;

        if (_node_attributes != null) {
            a = (Attr) _node_attributes.getNamedItem(attr.getName());
        }

        if (a != null) {
            value = a.getValue();
        }

        switch (t) {
            case DTDConstants.NMTOKEN :
            case DTDConstants.CDATA :
                ret = new JTextField();
                if (value != null) {
                    ((JTextField) ret).setText(value);
                } else {
                    value = attr.getDefaultValue();
                    if (value != null) {
                        ((JTextField) ret).setText(value);
                    }
                }
                if (attr.getDefaultType() == DTDConstants.FIXED) {
                    ((JTextField) ret).setEditable(false);
                }
                break;
            case DTDConstants.NMTOKENS :
                ret = getNmtokensComponent(attr);
                break;
            case DTDConstants.IDREFS :
                ret = getIdRefsComponent(attr);
                break;
            case DTDConstants.ID :
                ret = getIdComponent(_node, attr.getName());
                break;
            case DTDConstants.IDREF :
                ret = getIdRefComponent(_node, attr.getName());
                break;
            case DTDConstants.TOKEN_GROUP :
                Enumeration e = attr.getTokens();

                boolean checkbox = true;

                if (e != null) {
                    Vector v = new Vector();
                    if (attr.getDefaultValue() == null
                        && attr.getDefaultType()!=DTDConstants.REQUIRED) {
                        v.addElement("");
                    }
                    while (e.hasMoreElements()) {
                        MerlotDebug.msg("v = " + v + " e = " + e);
                        Object o = e.nextElement();
                        String s = o.toString();

                        checkbox =
                            checkbox && (s.equals("true") || s.equals("false"));

                        v.addElement(o);
                    }
                    if (v.size() == 2 && checkbox) {
                        ret = new JCheckBox();
                        ((JCheckBox) ret).setSelected(
                            value != null && value.equals("true"));
                        break;
                    }

                    ret = new JComboBox(v);
                    ((JComboBox) ret).setEditable(false);
                    int i = getIndexInVector(v, value);
                    if (i < 0) {
                        i = getIndexInVector(v, attr.getDefaultValue());
                    }
                    if (i >= 0) {
                        ((JComboBox) ret).setSelectedIndex(i);
                    }
                }
                break;
            default :
                JLabel l =
                    new JLabel(
                        MerlotResource.getString(ERR, "xml.attr.type.unknown"));
                ret = l;
                break;

        }
        return ret;
    }

    /** Saves any changes back to the DOM */
    public void save() throws PropertyVetoException {
        HashMap t = new HashMap();
        save(t);
    }

    protected void save(HashMap attributes) throws PropertyVetoException {
        if (_text != null) {
            if (_text.getText().trim().equals("")) {
                if (_subtext != null) {
                    // it's empty and they didn't create it or edit it
                    // manually... remove it
                    _node.removeChild(_subtext);
                }
            } else {
                if (_subtext == null) {
                    // create a new TextNode
                    MerlotDOMNode nd = _node.newChild(DTDConstants.PCDATA_KEY);
                    if (nd instanceof MerlotDOMText) {
                        _subtext = (MerlotDOMText) nd;
                    }
                }
                if (_subtext != null) {
                    _subtext.setText(_text.getText());
                }
            }
        }

        // put together a hashtable of attributes to pass back to the node
        Enumeration e = _attrComponents.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            DTDAttribute dtdAttr = (DTDAttribute) _dtd_attributes.get(key);
            Node oldnode = _node_attributes.getNamedItem(key);
            String oldval;
            if (oldnode != null) {
                oldval = oldnode.getNodeValue();
            } else {
                oldval = "";
            }
            JComponent c = (JComponent) _attrComponents.get(key);
            String newval = null;
            if (c instanceof JTextField) {
                newval = ((JTextField) c).getText();
            } else if (c instanceof JComboBox) {
                Object item = ((JComboBox) c).getSelectedItem();
                if (item != null) {
                    newval = item.toString().trim();
                }
            } else if (c instanceof JCheckBox) {
                newval = ((JCheckBox) c).isSelected() ? "true" : "false";
            } else {
                // nothing to do now
                MerlotDebug.msg(
                    "Unknown editing component in GenericDOMEditPanel.save: "
                        + c);
                if (attributes.containsKey(key)) {
                    newval = (String) attributes.get(key);
                } else if (dtdAttr.getDefaultType() == DTDConstants.FIXED) {
                    // Case of a hidden attribute - may need saving
                    newval = dtdAttr.getDefaultValue();
                }
            }

            // Required atts can not be deleted - others may be able to be
            if (newval != null && newval.trim().equals("")) {
                newval = null;
            }

            if (newval == null
                && dtdAttr != null
                && dtdAttr.getDefaultType() == DTDAttribute.REQUIRED) {
                String err[] = new String[2];
                err[0] = _node.getNodeName();
                err[1] = key;
                throw new PropertyVetoException(
                    MessageFormat.format(
                        MerlotResource.getString(ERR, "required.field"),
                        err),
                    new PropertyChangeEvent(_node, key, oldval, newval));
            }

            fireVetoableChange(
                new PropertyChangeEvent(_node, key, oldval, newval));

            // check if the attribute is already set in the hashtable
            if (!attributes.containsKey(key)) {
                attributes.put(key, newval);
            }

        }
        _node.setAttributes(attributes);
        //Check that each attribute is valid
        GrammarSimpleType[] simpleType = _node.getGrammarAttributes();
        String error;
        for (int i = 0; i < simpleType.length; i++) {
            String name = simpleType[i].getName();
            error =
                simpleType[i].getValidationMessage(getFieldNode(simpleType[i]));
            if (error.length() > 0) {
                String newValue = ((MerlotDOMElement) _node).getAttribute(name);
                throw new PropertyVetoException(
                    error,
                    new PropertyChangeEvent(
                        _node,
                        simpleType[i].getName(),
                        newValue,
                        newValue));
            }
        }
    }

    public void grabFocus() {
        if (_first_field != null) {
            _first_field.grabFocus();
        }
    }

    // search through the vector for the value we want
    private int getIndexInVector(Vector v, String s) {
        if (v != null && s != null) {
            int len = v.size();
            for (int i = 0; i < len; i++) {
                Object o = v.elementAt(i);
                if (o.equals(s)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
        _vetoListeners.addElement(l);

    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        _vetoListeners.removeElement(l);
    }

    // gotta implement this stuff ourselves cause the PropertyChangeSupport 
    // classes compare the old and new values, and dont' fire if they're equal
    public void fireVetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
        //	System.out.println("fireVetoableChange: "+evt);

        Enumeration e = _vetoListeners.elements();
        while (e.hasMoreElements()) {

            VetoableChangeListener l = (VetoableChangeListener) e.nextElement();
            //	System.out.println("  listener: "+l);
            l.vetoableChange(evt);
        }

    }

    protected IDManager getIdManager() {
        return _node.getIdManager();
    }

    /**
     * Returns a component aimed at editing the ID attribute from a DOM node.
     * 
     * @param node the node for which to generate the ID editing component
     * @param attrName the name of the ID attribute for which to generate the ID editing component
     */
    protected JComponent getIdComponent(MerlotDOMNode node, String attrName) {
        String value = null;

        Attr attr = (Attr) node.getAttributes().getNamedItem(attrName);
        if (attr != null) {
            value = attr.getValue();
        }

        JTextField ret = new JTextField();
        if (value == null) {
            value = getIdManager().getDefaultIdValue(node, attrName);
        }
        if (value != null) {
            ret.setText(value);
        }
        return ret;
    }

    /**
     * Returns a component aimed at editing the IDREF attribute from a DOM node.
     * 
     * @param node the node for which to generate the IDREF editing component
     * @param attrName the name of the IDREF attribute for which to generate the ID editing component
     */
    protected JComponent getIdRefComponent(
        MerlotDOMNode targetNode,
        String targetAttrName) {
        String value = null;

        Attr targetAttr =
            (Attr) targetNode.getAttributes().getNamedItem(targetAttrName);
        if (targetAttr != null) {
            value = targetAttr.getValue();
        }

        JComboBox choices = new JComboBox();
        choices.setRenderer(new IDREFComboBoxRenderer());
        choices.setEditable(true);
        int selectedValue = 0;

        Map idAttrs =
            getIdManager().getIDAttrs(targetNode, targetAttrName);
        if (idAttrs != null) {
            Iterator e = idAttrs.keySet().iterator();
            int i = 0;
            while (e.hasNext()) {
                Attr attr = (Attr) e.next();
                MerlotDOMNode node = (MerlotDOMNode) idAttrs.get(attr);
                String id = getIdForNode(node);
                if (IdAttributesAreCompatible(targetNode,
                    targetAttrName,
                    node,
                    attr.getName())) {
                    IDObject idObject =
                        new IDObject(
                            id,
                            getDisplayTextForAttribute(
                                targetNode,
                                targetAttrName,
                                node,
                                attr.getName()));
                    choices.addItem(idObject);
                    if (id.equals(value))
                        selectedValue = i;
                }
                i++;
            }
            if (idAttrs.size() != 0)
                choices.setSelectedIndex(selectedValue);
        }
        return choices;
    }

    protected JComponent getIdRefsComponent(DTDAttribute attr) {
        MerlotDebug.msg("getEditComponent(" + attr + ")");

        int t = attr.getType();
        JComponent ret = null;
        String value = null;
        Attr a = null;

        if (_node_attributes != null) {
            a = (Attr) _node_attributes.getNamedItem(attr.getName());
        }

        if (a != null) {
            value = a.getValue();
        }

        final DefaultListModel model = new DefaultListModel();
        int row = 0;
        if (value != null) {
            StringTokenizer tokens = new StringTokenizer(value, " ");
            while (tokens.hasMoreTokens()) {
                model.add(row++, tokens.nextToken());
            }
        } else {
            value = attr.getDefaultValue();
            if (value != null) {
                model.add(row++, value);
            }
        }

        final JList list = new JList(model);

        //buttons
        Image img =
            MerlotResource
                .getImage(UI, "list.add.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton add = new JButton(new ImageIcon(img));
        add.setToolTipText(MerlotResource.getString("ui", "list.add.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.remove.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton remove = new JButton(new ImageIcon(img));
        remove.setToolTipText(MerlotResource.getString("ui", "list.remove.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.moveUp.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton moveUp = new JButton(new ImageIcon(img));
        moveUp.setToolTipText(MerlotResource.getString(UI, "list.moveUp.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.moveDown.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton moveDown = new JButton(new ImageIcon(img));
        moveDown.setToolTipText(
            MerlotResource.getString(UI, "list.moveDown.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.updateRow.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton updateRow = new JButton(new ImageIcon(img));
        updateRow.setToolTipText(
            MerlotResource.getString(UI, "list.updateRow.tt"));

        JPanel rPanel = new JPanel();
        rPanel.setLayout(new GridLayout(5, 1, 5, 5));
        rPanel.setPreferredSize(new Dimension(50, 140));
        rPanel.add(add);
        rPanel.add(remove);
        rPanel.add(updateRow);
        rPanel.add(moveUp);
        rPanel.add(moveDown);

        MerlotDOMNode targetNode = _node;
        String targetAttrName = attr.getName();

        final JComboBox cbo = new JComboBox();
        cbo.setRenderer(new IDREFComboBoxRenderer());
        cbo.setEditable(true);
        int selectedValue = 0;

        Map idAttrs =
            getIdManager().getIDAttrs(targetNode, targetAttrName);
        if (idAttrs != null) {
            Iterator e = idAttrs.keySet().iterator();
            int i = 0;
            while (e.hasNext()) {
                Attr _attr = (Attr) e.next();
                MerlotDOMNode node = (MerlotDOMNode) idAttrs.get(_attr);
                String id = getIdForNode(node);
                if (IdAttributesAreCompatible(targetNode,
                    targetAttrName,
                    node,
                    _attr.getName())) {
                    IDObject idObject =
                        new IDObject(
                            id,
                            getDisplayTextForAttribute(
                                targetNode,
                                targetAttrName,
                                node,
                                _attr.getName()));
                    cbo.addItem(idObject);
                    if (id.equals(value))
                        selectedValue = i;
                }
                i++;
            }
            if (idAttrs.size() != 0)
                cbo.setSelectedIndex(selectedValue);
        }

        cbo.setEditable(false);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                JList theList = (JList) e.getSource();
                if (theList.isSelectionEmpty()) {
                    cbo.setSelectedIndex(0);
                } else {
                    int index = theList.getSelectedIndex();
                    String target =
                        theList.getModel().getElementAt(index).toString();
                    int row = 0;
                    for (int i = 0; i < cbo.getItemCount(); i++) {
                        if (target.equals(cbo.getItemAt(i).toString())) {
                            row = i;
                            break;
                        }
                    }
                    cbo.setSelectedIndex(row);
                }
            }
        });

        updateRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1 && cbo.getSelectedItem() != null) {
                    model.set(row, cbo.getSelectedItem().toString());
                }
            }
        });

        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = model.getSize();
                if (row != -1 && cbo.getSelectedItem() != null) {
                    model.addElement(cbo.getSelectedItem());
                    list.setSelectedIndex(model.size() - 1);
                    list.scrollRectToVisible(
                        list.getCellBounds(
                            model.getSize() - 1,
                            model.getSize() - 1));
                }
            }
        });

        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1) {
                    model.remove(row);
                }
            }
        });

        moveUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1 && (row - 1) >= 0) {
                    Object prevItem = model.getElementAt(row - 1);
                    Object currItem = model.getElementAt(row);
                    model.set(row - 1, currItem);
                    model.set(row, prevItem);
                    list.setSelectedIndex(row - 1);
                }
            }
        });

        moveDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1 && (row + 1) < model.getSize()) {
                    Object nextItem = model.getElementAt(row + 1);
                    Object currItem = model.getElementAt(row);
                    model.set(row + 1, currItem);
                    model.set(row, nextItem);
                    list.setSelectedIndex(row + 1);
                }
            }
        });

        //list
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(5, 5));
        p.add(cbo, BorderLayout.NORTH);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(rPanel, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(350, 120));
        p.setPreferredSize(new Dimension(350, 140));
        //return p;

        return new GenericComponent(list, p);
        // addAttributeComponent(new JLabel(),new JLabel(),ALIGN_MIDDLE);
        // addAttributeComponent(l,p,ALIGN_TOP);  
    }

    /**
     *  Gets the nmtokensComponent attribute of the GenericDOMEditPanel object
     *
     *@param  attr  Description of the Parameter
     *@return       The nmtokensComponent value
     */
    protected JComponent getNmtokensComponent(DTDAttribute attr) {
        MerlotDebug.msg("getEditComponent(" + attr + ")");

        int t = attr.getType();
        JComponent ret = null;
        String value = null;
        Attr a = null;

        if (_node_attributes != null) {
            a = (Attr) _node_attributes.getNamedItem(attr.getName());
        }

        if (a != null) {
            value = a.getValue();
        }

        final DefaultListModel model = new DefaultListModel();
        int row = 0;

        if (value != null) {
            StringTokenizer tokens = new StringTokenizer(value, " ");
            while (tokens.hasMoreTokens()) {
                model.add(row++, tokens.nextToken());
            }
        } else {
            value = attr.getDefaultValue();
            if (value != null) {
                model.add(row++, value);
            }
        }

        final JList list = new JList(model);
        //buttons
        Image img =
            MerlotResource
                .getImage(UI, "list.add.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton add = new JButton(new ImageIcon(img));
        add.setToolTipText(MerlotResource.getString("ui", "list.add.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.remove.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton remove = new JButton(new ImageIcon(img));
        remove.setToolTipText(MerlotResource.getString("ui", "list.remove.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.moveUp.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton moveUp = new JButton(new ImageIcon(img));
        moveUp.setToolTipText(MerlotResource.getString(UI, "list.moveUp.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.moveDown.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton moveDown = new JButton(new ImageIcon(img));
        moveDown.setToolTipText(
            MerlotResource.getString(UI, "list.moveDown.tt"));

        img =
            MerlotResource
                .getImage(UI, "list.updateRow.icon")
                .getImage()
                .getScaledInstance(
                15,
                15,
                Image.SCALE_FAST);
        JButton updateRow = new JButton(new ImageIcon(img));
        updateRow.setToolTipText(
            MerlotResource.getString(UI, "list.updateRow.tt"));

        JPanel rPanel = new JPanel();
        rPanel.setLayout(new GridLayout(5, 1, 5, 5));
        rPanel.setPreferredSize(new Dimension(50, 140));
        rPanel.add(add);
        rPanel.add(remove);
        rPanel.add(updateRow);
        rPanel.add(moveUp);
        rPanel.add(moveDown);
        final JTextField text = new JTextField(10);

        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                JList theList = (JList) e.getSource();
                if (theList.isSelectionEmpty()) {
                    text.setText("");
                } else {
                    int index = theList.getSelectedIndex();
                    text.setText(
                        list.getModel().getElementAt(index).toString());
                }
            }
        });

        updateRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1) {
                    if (checkNmtokenChars(text.getText()) != 0) {
                        JOptionPane.showMessageDialog(
                            XMLEditorFrame.getSharedInstance(),
                            MerlotResource.getString(ERR, "invalid.nmtokens"));
                        text.selectAll();
                    } else {
                        model.set(row, text.getText());
                    }
                }
            }
        });

        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = model.size();
                if (row != -1 && text.getText().trim().length() > 0) {
                    if (checkNmtokenChars(text.getText()) != 0) {
                        JOptionPane.showMessageDialog(
                            XMLEditorFrame.getSharedInstance(),
                            MerlotResource.getString(ERR, "invalid.nmtokens"));
                        text.selectAll();
                    } else {
                        model.addElement(text.getText());
                        list.setSelectedIndex(model.getSize() - 1);
                        list.scrollRectToVisible(
                            list.getCellBounds(
                                model.getSize() - 1,
                                model.getSize() - 1));
                    }
                }
            }
        });

        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1) {
                    model.remove(row);
                }
            }
        });

        moveUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1 && (row - 1) >= 0) {
                    Object prevItem = model.getElementAt(row - 1);
                    Object currItem = model.getElementAt(row);
                    model.set(row - 1, currItem);
                    model.set(row, prevItem);
                    list.setSelectedIndex(row - 1);
                }
            }
        });

        moveDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = list.getSelectedIndex();
                if (row != -1 && (row + 1) < model.getSize()) {
                    Object nextItem = model.getElementAt(row + 1);
                    Object currItem = model.getElementAt(row);
                    model.set(row + 1, currItem);
                    model.set(row, nextItem);
                    list.setSelectedIndex(row + 1);
                }
            }
        });

        //list
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(5, 5));
        p.add(text, BorderLayout.NORTH);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(rPanel, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(350, 120));
        p.setPreferredSize(new Dimension(350, 140));

        return new GenericComponent(list, p);
    }

    public String getIdForNode(MerlotDOMNode node) {
        // Get the first attribute of type "ID"
        Enumeration e = node.getDTDAttributes();
        // Set the default to name "id" in case it is not found in the DTD
        String idAttributeName = "id";
        if (e != null) {
            while (e.hasMoreElements()) {
                DTDAttribute dtdAttr = (DTDAttribute) e.nextElement();
                if (dtdAttr.getType() == DTDConstants.ID) {
                    idAttributeName = dtdAttr.getName();
                    break;
                }
            }
        }
        String id =
            ((String) ((org.w3c.dom.Element) (node.getRealNode()))
                .getAttribute(idAttributeName))
                .trim();
        return id;
    }

    /**
     * Returns whether or not the value of an ID attribute can be used as
     * a value of a target IDREF attribute.
     * By default in XML 1.0 specification, all ID values can be used for IDREFs.
     * This decision can be constrained by subclassing this method.
     *
     * @param idRefNode node containing the IDREF attribute
     * @param idRefAttrName  name of the IDREF attribute
     * @param idNode node containing the ID attribute
     * @param idAttrName name of the ID attribute
     */
    public boolean IdAttributesAreCompatible(
        MerlotDOMNode idRefNode,
        String idRefAttrName,
        MerlotDOMNode IdNode,
        String idAttrName) {
        return true;
    }

    /**
     * Returns the text which represents the referenced node in an IDREF comboBox
     */
    protected String getDisplayTextForAttribute(
        MerlotDOMNode idRefNode,
        String idRefAttrName,
        MerlotDOMNode idNode,
        String idAttrName) {
        Attr attr = (Attr) idNode.getAttributes().getNamedItem(idAttrName);
        String ret = "";
        if (attr != null) {
            ret = attr.getValue();
        }
        return ret;
    }

    // A special renderer for the combobox to enable a different value
    // to be displayed from the value that gets saved.
    class IDREFComboBoxRenderer extends JLabel implements ListCellRenderer {
        public IDREFComboBoxRenderer() {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            IDObject idObject = (IDObject) value;
            String display =
                (idObject != null) ? idObject.getDisplayText() : null;
            setText((display == null) ? "" : display);
            return this;
        }
    }

    class IDObject {
        protected String _idValue = "";
        protected String _displayText = "";
        public IDObject(String idValue, String displayText) {
            if (idValue != null)
                _idValue = idValue.trim();
            if (displayText != null)
                _displayText = displayText.trim();
        }
        public String getIdValue() {
            return _idValue;
        }
        public String toString() {
            return _idValue;
        }
        public String getDisplayText() {
            return _displayText;
        }
    }

    /**
     * Checks attributes according to their type. E.g. NMTOKEN is only of the characters specified
     * by the XML spec
     */
    protected class StandardAttributeChecker
        implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {

            Object n = evt.getSource();
            if (!(n instanceof MerlotDOMNode)) {
                return;
            }
            MerlotDOMNode node = (MerlotDOMNode) n;

            String attributeName = evt.getPropertyName();
            Object o = evt.getNewValue();
            String value = "";
            if (o instanceof String) {
                value = ((String) o).trim();
            }

            // [4]    NameChar    ::=    Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender 
            // [5]    Name        ::=    (Letter | '_' | ':') (NameChar)* 
            // [6]    Names       ::=    Name (S Name)* 
            // [7]    Nmtoken     ::=    (NameChar)+ 
            // [8]    Nmtokens    ::=    Nmtoken (S Nmtoken)*

            DTDAttribute attribute =
                (DTDAttribute) _dtd_attributes.get(attributeName);
            if (attribute != null) {
                int t = attribute.getType();
                char invalid = 0;
                switch (t) {
                    case DTDConstants.NMTOKEN :
                        // inspect the value and make sure it conforms to the restrictions on NMTOKEN's
                        if (value.length() == 0
                            && attribute.getDefaultType() == DTDAttribute.REQUIRED) {
                            String args[] = new String[1];
                            args[0] = attribute.getName();
                            throw new PropertyVetoException(
                                MessageFormat.format(
                                    MerlotResource.getString(
                                        ERR,
                                        "null.length.nmtoken"),
                                    args),
                                evt);
                        }
                        invalid = checkNmtokenChars(value);
                        break;

                    case DTDConstants.CDATA :
                        break;

                    case DTDConstants.ID :
                        //Ensure that the ID is unique in this document
                        Map idAttrs =
                            getIdManager().getIDAttrs(
                                _node,
                                attribute.getName());
                        Iterator e = idAttrs.keySet().iterator();
                        Attr att = null;
                        if (e != null) {
                            while (e.hasNext()) {
                                att = (Attr) e.next();
                                if (att.getValue().equals(value)
                                    && !idAttrs.get(att).equals(_node)) {
                                    String args[] = new String[1];
                                    args[0] = value;
                                    throw new PropertyVetoException(
                                        MessageFormat.format(
                                            MerlotResource.getString(
                                                ERR,
                                                "id.duplicate"),
                                            args),
                                        evt);
                                }
                            }
                        }
                        invalid = checkIdChars(value);
                        break;
                    case DTDConstants.IDREF :
                        break;
                    case DTDConstants.TOKEN_GROUP :
                        break;

                }
                if (invalid != 0) {
                    String[] err = new String[3];
                    err[0] = node.getNodeName();
                    err[1] = attribute.getName();
                    err[2] = "'" + invalid + "'";

                    throw new PropertyVetoException(
                        MessageFormat.format(
                            MerlotResource.getString(
                                ERR,
                                "illegal.value.attr.char"),
                            err),
                        evt);

                }

            }
        }

    }

    /*
     * [7] Nmtoken ::= (NameChar)+
     */
    /**
     * Check to see if a string is a valid Nmtoken according to [7]
     * in the XML 1.0 Recommendation
     *
     * @param nmtoken string to checj
     * @return the first invalid char or 0
     */
    // borrowed from Xerces cause we need to know what char is invalid
    public static char checkNmtokenChars(String nmtoken) {
        for (int i = 0; i < nmtoken.length(); i++) {
            char ch = nmtoken.charAt(i);
            if (!org.apache.xerces.util.XMLChar.isName(ch)) {
                return ch;
            }
        }
        return 0;
        // Need to reimplement, not in Xerces 2 --Evert
        /*
        org.apache.xerces.utils.XMLCharacterProperties.initCharFlags();
        
        for (int i = 0; i < nmtoken.length(); i++) {
        char ch = nmtoken.charAt(i);
        if (ch > 'z') {
            if ((org.apache.xerces.utils.XMLCharacterProperties.fgCharFlags[ch] & org.apache.xerces.utils.XMLCharacterProperties.E_NameCharFlag) == 0)
        	return ch;
            
        } else if (org.apache.xerces.utils.XMLCharacterProperties.fgAsciiNameChar[ch] == 0) {
            return ch;
        }
        }
        return 0;
        */
    }

    /*
     * from the namespace rec
     * [4] NCName ::= (Letter | '_') (NCNameChar)*
     */
    /**
     * Check to see if a string is a valid NCName according to [4]
     * from the XML Namespaces 1.0 Recommendation
     *
     * @param name string to check
     * @return true if name is a valid NCName
     */
    public static char checkNCNameChars(String ncName) {
        char ch = 0;
        if (ncName.length() == 0)
            return 0;
        for (int i = 0; i < ncName.length(); i++) {
            ch = ncName.charAt(i);
            if (!org.apache.xerces.util.XMLChar.isNCName(ch)) {
                return ch;
            }
        }
        return ch;
    }

    /*
     * [5] Name ::= (Letter | '_' | ':') (NameChar)*
     * OR
     * from the namespace rec
     * [4] NCName ::= (Letter | '_') (NCNameChar)*
     */
    /**
     * Check to see if a string is a valid ID according to [5]
     * in the XML 1.0 Recommendation
     *
     * @param nmtoken string to check
     * @return the first invalid char or 0
     */
    // borrowed from Xerces cause we need to know what char is invalid
    public static char checkIdChars(String id) {
        // This case gets trapped elsewhere
        if (id.length() == 0)
            return 0;
        char ch = id.charAt(0);
        if (NAMESPACES) {
            if (!org.apache.xerces.util.XMLChar.isNCNameStart(ch))
                return ch;
            return checkNmtokenChars(id.substring(1));
        } else {
            if (!org.apache.xerces.util.XMLChar.isNameStart(ch))
                return ch;
        }

        if (id.length() > 1)
            return checkNmtokenChars(id.substring(1));
        return 0;

        // Need to reimplement, not in Xerces 2 --Evert
        /*
        org.apache.xerces.utils.XMLCharacterProperties.initCharFlags();
        if (id.length()>0) {
            char ch = id.charAt(0);
            if (org.apache.xerces.utils.XMLCharacterProperties.fgAsciiInitialNameChar[ch] == 0)
                return ch;
            if (id.length()>1)
                return checkNmtokenChars(id.substring(1));
        }
        return 0;
        */
    }

    public static class ScrollablePanel extends JPanel implements Scrollable {

        private boolean _trackHeight = true;
        private boolean _trackWidth = true;

        public ScrollablePanel(boolean trackWidth, boolean trackHeight) {
            _trackWidth = trackWidth;
            _trackHeight = trackHeight;
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction) {
            return 1;
        }

        public int getScrollableBlockIncrement(
            Rectangle visibleRect,
            int orientation,
            int direction) {
            return 10;
        }

        public boolean getScrollableTracksViewportWidth() {
            return _trackWidth;
        }

        public boolean getScrollableTracksViewportHeight() {
            return _trackHeight;
        }

    }

    protected FieldNode getFieldNode(GrammarSimpleType simpleType) {
        String name = simpleType.getName();
        GrammarComplexType complexType = _node.getGrammarComplexType();
        if (name == null)
            name = "#text";
        org.w3c.dom.Element element = (org.w3c.dom.Element) _node.getRealNode();
        Node node = null;
        if (name.equalsIgnoreCase("#text")) {
            if (!complexType.isEmptyType() || !complexType.isMixedType()) {
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child instanceof Text) {
                        node = child;
                        break;
                    }
                }
            }
        } else {
            node = element.getAttributeNode(name);
        }
        return FieldNode.getFieldNode(element, node, name);
    }

    public class GenericComponent extends JComponent {
        JComponent attribute;
        JComponent display;
        public GenericComponent(JComponent attribute, JPanel display) {
            this.attribute = attribute;
            this.display = display;
        }

        public JComponent getAttributeComponent() {
            return attribute;
        }
        public JComponent getDisplayComponent() {
            return display;
        }
    }
}
