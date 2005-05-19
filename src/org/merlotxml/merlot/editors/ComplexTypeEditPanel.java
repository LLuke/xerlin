package org.merlotxml.merlot.editors;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;

import matthew.awt.StrutLayout;

import org.merlotxml.merlot.MerlotConstants;
import org.merlotxml.merlot.MerlotDOMElement;
import org.merlotxml.merlot.MerlotDOMNode;
import org.merlotxml.merlot.MerlotDOMText;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.MerlotResource;
import org.merlotxml.merlot.XMLEditorSettings;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.merlotxml.util.xml.xerces.Schema;
import org.merlotxml.util.xml.xerces.SchemaGrammarComplexTypeImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * An alternative for GenericDOMEditPanel. Can display error messages for
 * invalid values below an attribute or simple content. The simple content of an
 * element is displayed in the same way as an attribute, but it is named the
 * same as the element. A SimpleTypeEditor for each simple type (attribute or
 * simple content), provides access to the JComponent for editing the simple
 * types, and other methods.
 *
 * @author   Evert Hoff
 */
public class ComplexTypeEditPanel extends JPanel implements ActionListener {

    public final static int ALIGN_BOTTOM = 2;
    public final static int ALIGN_MIDDLE = 1;
    public final static int ALIGN_TOP = 0;
    protected GrammarComplexType _complexType = null;
    protected GrammarSimpleType[] _grammarAttributes = null;
    protected GrammarSimpleType _grammarSimpleContent = null;

    /** The node this editor was created for */
    protected MerlotDOMNode _node;

    /** The DOM Nodes with the values of the attributes */
    protected NamedNodeMap _nodeAttributes = null;
    protected JComponent _previousComponent = null;

    /**
     * Similar to the hidable attributes, except these are displayed, but can't
     * be changed
     */
    protected static Hashtable _readonlyAttrs;

    /** The icon to use for required attribute labels */
    protected static Icon _requiredAttrIcon = null;
    protected Hashtable _simpleTypeEditors = new Hashtable();

    /** A node which is the child #text element for this node */
    protected MerlotDOMText _subtext;
    private final static String HIDE_PROP = "merlot.editor.generic.hide";
    private final static String RO_PROP = "merlot.editor.generic.readonly";

    /** The panel which contains the actual layout of attributes */
    private JPanel _attributePanel;

    private boolean _firstComponent = true;
    private JComponent _firstField = null;

    /**
     * A list of attributes that should be hidden. Key is element.attr. If value
     * is nonnull, then the attribute is hidden in the generic panel. #text can
     * also be hidden if key '#text' is in this hash
     */
    private static Hashtable _hideAttrs;

    /** list of PropertyChangeListeners that can veto editing actions */
    private Vector _vetoListeners = new Vector();

    /**
     * Constructor for the GenericComplexTypeEditPanel object
     *
     * @param node  Description of Parameter
     */
    public ComplexTypeEditPanel(MerlotDOMNode node) {
        super();
        MerlotDebug.msg("ComplexTypeEditPanel");
        _node = node;
        _nodeAttributes = _node.getAttributes();
        _complexType = _node.getGrammarComplexType();
        _grammarAttributes = _node.getGrammarAttributes();
        if (_complexType != null) {
            _grammarSimpleContent = _complexType.getSimpleContent();
            buildPanel();
        }
    }

    /**
     * Adds a feature to the VetoableChangeListener attribute of the
     * ComplexTypeEditPanel object
     *
     * @param l  The feature to be added to the VetoableChangeListener attribute
     */
    public void addVetoableChangeListener(VetoableChangeListener l) {
        _vetoListeners.addElement(l);

    }

    /**
     * To do: Revisit. Here was the original reason for doing this: Gotta
     * implement this stuff ourselves cause the PropertyChangeSupport classes
     * compare the old and new values, and dont' fire if they're equal.
     *
     * @param evt                        Description of Parameter
     * @exception PropertyVetoException  Description of Exception
     */
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

    public void removeVetoableChangeListener(VetoableChangeListener l) {
        _vetoListeners.removeElement(l);
    }

    /**
     * Saves any changes back to the DOM
     *
     * @exception PropertyVetoException  Description of Exception
     */
	public void save() throws PropertyVetoException {
        MerlotDebug.msg("ComplexTypeEditPanel.save()");
        // Save simple content to a sub #text node.
        SimpleTypeEditor simpleContentEditor =
            getSimpleTypeEditor(_grammarSimpleContent);
        if (simpleContentEditor != null && _grammarSimpleContent != null) {
            String oldValue = "";
            if (_subtext != null) {
                oldValue = _subtext.getText();
            }
            String newValue = simpleContentEditor.getValue();
			MerlotDebug.msg(
				" Old Value: '" + oldValue + "' New Value: '" + newValue + "'");
            if (!newValue.equals(oldValue)) {
                MerlotDebug.msg(" simpleContent value: " + newValue);
                FieldNode fieldNode = getFieldNode(_grammarSimpleContent);
                _grammarSimpleContent.setValue(fieldNode, newValue);
                if (newValue.trim().equals("")) {
                    if (_subtext != null) {
                        // it's empty and they didn't create it or edit it
                        // manually... remove it
                        _node.removeChild(_subtext);
                    }
                    if (_complexType.getIsNillable()) {
                        Element el = (Element)_node.getRealNode();
                        el.setAttribute("xsi:nil", "true");
                    }
                } else {
                    if (_subtext == null) {
                        // create a new TextNode
                        MerlotDOMNode nd =
                            _node.newChild(DTDConstants.PCDATA_KEY);
                        if (nd instanceof MerlotDOMText) {
                            _subtext = (MerlotDOMText) nd;
                        }
                    }
                    if (_subtext != null) {
                        _subtext.setText(newValue);
                    }
                    if (_complexType.getIsNillable()) {
                        Element el = (Element)_node.getRealNode();
                        el.removeAttribute("xsi:nil");
                    }
                }
                _node.fireNodeChanged();
            }
        }

        // Save attributes.
        HashMap attributes = new HashMap();
        for (int i = 0; i < _grammarAttributes.length; i++) {
            GrammarSimpleType simpleType = _grammarAttributes[i];
            String key = simpleType.getName();
            Node oldNode = _nodeAttributes.getNamedItem(key);
            String oldValue;
            if (oldNode != null) {
                oldValue = oldNode.getNodeValue();
			} else {
                oldValue = "";
            }
            SimpleTypeEditor attributeEditor = getSimpleTypeEditor(simpleType);
            String newValue = null;
            if (attributeEditor != null)
                newValue = attributeEditor.getValue();
            if (newValue != null && newValue.trim().equals("")) {
                newValue = null;
            }
            if (newValue == null
                && simpleType.getDefaultType() == DTDConstants.REQUIRED) {
                String err[] = new String[2];
                err[0] = _node.getNodeName();
                err[1] = key;
				throw new PropertyVetoException(
					MessageFormat.format(
						MerlotResource.getString(
							MerlotConstants.ERR,
							"required.field"),
						err),
                    new PropertyChangeEvent(_node, key, oldValue, newValue));
            }

			//            String valid = simpleType.getValidationMessage(newValue);
			//if (valid!=null && valid.length()>0) {
			//    throw new PropertyVetoException(valid, new PropertyChangeEvent(_node,key,oldValue,newValue));
			//}

			MerlotDebug.msg(
				" Saving attribute "
					+ key
					+ " old='"
					+ oldValue
					+ "' new='"
					+ newValue
					+ "'");

			fireVetoableChange(
				new PropertyChangeEvent(_node, key, oldValue, newValue));

            // check if the attribute is already set in the hashtable
            if (!attributes.containsKey(key)) {
                attributes.put(key, newValue);
            }
        }
        _node.setAttributes(attributes);
    }

    protected void addSimpleType(GrammarSimpleType simpleType) {
        if (simpleType == null) {
            MerlotDebug.msg("Trying to add null simpleType.");
            return;
        }
        SimpleTypeEditor editor = getSimpleTypeEditor(simpleType);
        String value = editor.getValue();
        FieldNode fieldNode = getFieldNode(simpleType);
        String message = simpleType.getValidationMessage(fieldNode);

        String name = simpleType.getName();
        if (name.equals("#text"))
            name = _complexType.getName();
        MerlotDebug.msg("Adding simple type: " + name + " value: " + value);

        JLabel label = new JLabel(name + ":", JLabel.RIGHT);
        if (simpleType.getDefaultType() == DTDConstants.REQUIRED) {
            Icon reqIcon = getRequiredAttrIcon();
            if (reqIcon != null) {
                label.setIcon(reqIcon);
            }
        }

        JComponent component = editor.getComponent();

        if (component == null) {
            MerlotDebug.msg("Component is null.");
            return;
        }
        //_attrComponents.put(a.getName(),c);

        String s = _node.getNodeName() + "." + simpleType.getName();
        String t = (String) _readonlyAttrs.get(s);
        if (t != null) {
            MerlotDebug.msg("Disabling component for: " + s);
            editor.setEditable(false);
        }
        addSimpleTypeComponent(label, component, ALIGN_MIDDLE);

        if (!message.equals("")) {
            JLabel messageHeading = new JLabel(" ");
            Font f = new Font("DEFAULT", Font.PLAIN, 10);
            messageHeading.setFont(f);
            int colon = message.indexOf(':');
            message = message.substring(colon + 1);
            JLabel messageContent = new JLabel(message);
            messageContent.setFont(f);
            messageContent.setForeground(Color.red);
			addSimpleTypeComponent(
				messageHeading,
				messageContent,
				ALIGN_MIDDLE);
        }
    }

    protected FieldNode getFieldNode(GrammarSimpleType simpleType) {
        String name = simpleType.getName();
        if (name == null)
            name = "#text";
        Element element = (Element) _node.getRealNode();
        Node node = null;
        if (name.equalsIgnoreCase("#text")) {
            if (!_complexType.isEmptyType() || !_complexType.isMixedType()) {
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

    protected Node getTextNode(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("#text"))
                return child;
        }
        return null;
    }

    protected void addSimpleTypeComponent(JLabel l, JComponent c, int align) {

        StrutLayout.StrutConstraint strut;
        StrutLayout.StrutConstraint strut2;

        if (_firstComponent) {
			strut =
				new StrutLayout.StrutConstraint(
					_previousComponent,
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

            _firstField = c;
            _firstComponent = false;

		} else {
			strut2 =
				new StrutLayout.StrutConstraint(
					_previousComponent,
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

        _previousComponent = c;
    }

    protected void buildPanel() {
        setupSimpleTypeEditors();
        setupReadonlyTable();
        setupHideTable();
        initPanelLayout();
        setupPanel();
    }

    protected Icon getRequiredAttrIcon() {
        if (_requiredAttrIcon == null) {
            _requiredAttrIcon =
				MerlotResource.getImage(
					MerlotConstants.UI,
					"editor.required.attr.icon");
        }
        return _requiredAttrIcon;
    }

    protected SimpleTypeEditor getSimpleTypeEditor(GrammarSimpleType simpleType) {
        if (simpleType == null)
            return null;
        return (SimpleTypeEditor) _simpleTypeEditors.get(simpleType);
    }

    /**
     * Gets all the simple types, including all the attributes and the simple content.
     */
    protected GrammarSimpleType[] getSimpleTypes() {
        int num = _grammarAttributes.length;
        if (_grammarSimpleContent != null)
            num++;
        GrammarSimpleType[] ret = new GrammarSimpleType[num];
        if (_grammarSimpleContent != null)
            ret[0] = _grammarSimpleContent;
        for (int i = 0; i < _grammarAttributes.length; i++) {
            ret[i] = _grammarAttributes[i];
        }
        return ret;
    }

    protected void initPanelLayout() {
        _firstComponent = true;
        _firstField = null;
        _attributePanel = new JPanel();
        _attributePanel.setMinimumSize(new Dimension(4, 4));
        _attributePanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        StrutLayout slay = new StrutLayout();
		StrutLayout.setDefaultStrutLength(10);

        _attributePanel.setLayout(slay);

        // get the root element for the strut layout.. it will be the icon of the node
        JLabel iconLabel = new JLabel(_node.getIcon());
        _previousComponent = iconLabel;
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

    protected void setupPanel() {
        MerlotDebug.msg("Setting up ComplexTypeEditPanel.");

        if (_node instanceof MerlotDOMElement) {
            GrammarComplexType complexType = _node.getGrammarComplexType();
            
            // Test if the grammar is schema
            if (complexType instanceof SchemaGrammarComplexTypeImpl)
                addDocumentation();
                
            String message = "";
            //message = complexType.getValidationMessage(_node.getRealNode());
            MerlotDOMElement element = (MerlotDOMElement)_node;
            if (message.equals("") && !element.getIsLocationValid())
                message = "The location of this node is not valid.";
            if (message.equals("") && !element.getIsComplete())
				message =
					"This node is not complete. The following child is required - "
						+ complexType.getFirstMissingChildName(
							(Element) _node.getRealNode());
            if (message.equals("") && !element.getIsEachChildValid())
                message = "This node has a child node that is not valid.";
            if (!message.equals("")) {
                JLabel messageHeading = new JLabel(" ");
                Font f = new Font("DEFAULT", Font.PLAIN, 10);
                messageHeading.setFont(f);
                int colon = message.indexOf(':');
                message = message.substring(colon + 1);
                JLabel messageContent = new JLabel(message);
                messageContent.setFont(f);
                messageContent.setForeground(Color.red);
                addSimpleTypeComponent(
                    messageHeading,
                    messageContent,
                    ALIGN_MIDDLE);
            }
        }

		if (_grammarSimpleContent != null
			&& _complexType.getIsSimpleContentAllowed())
            addSimpleType(_grammarSimpleContent);
        else
			MerlotDebug.msg(
				"Can't add simple content: _grammarSimpleContent is null.");

        for (int i = 0; i < _grammarAttributes.length; i++) {
            GrammarSimpleType simpleType = _grammarAttributes[i];
            if (!suppressAttribute(simpleType))
                addSimpleType(simpleType);
        }
    }
    
    /**
     * Displays the documentation that is contained in the XSD for this element.
     */
    protected void addDocumentation() {
        Schema schema = Schema.getInstance();
        String documentation = schema.getDocumentation(_node);
        if (documentation == null || documentation.equals(""))
            return;
            
        JLabel label = new JLabel("Documentation:", JLabel.RIGHT);
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        JEditorPane ep = new JEditorPane();
        ep.setEditable(false);
        ep.setEditorKit(htmlKit);
        BufferedReader in = new BufferedReader(
         new StringReader(documentation));
        try {
            htmlKit.read(in, ep.getDocument(),0);
        } catch (IOException e) {
        } catch (BadLocationException e) {
        }
        JScrollPane scrollPane =
            new JScrollPane(
                ep,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(350, 100));
        scrollPane.setMinimumSize(new Dimension(60, 30));
        addSimpleTypeComponent(
            label,
            scrollPane,
            ALIGN_MIDDLE);
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

    protected void setupSimpleTypeEditors() {
        MerlotDebug.msg("ComplexTypeEditPanel.setupSimpleTypeEditors()");
        MerlotDebug.msg(" _grammarSimpleContent: " + _grammarSimpleContent);
		MerlotDebug.msg(
			" isSimpleContentAllowed: "
				+ _complexType.getIsSimpleContentAllowed());
		if (_grammarSimpleContent != null
			&& _complexType.getIsSimpleContentAllowed()) {
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
			SimpleTypeEditor editor =
				createSimpleTypeEditor(_grammarSimpleContent);
            String value = null;
            if (_subtext != null)
                value = _subtext.getText();
            editor.setValue(value);
            editor.addActionListener(this);
            _simpleTypeEditors.put(_grammarSimpleContent, editor);
        }
        for (int i = 0; i < _grammarAttributes.length; i++) {
            GrammarSimpleType simpleType = _grammarAttributes[i];
            SimpleTypeEditor editor = createSimpleTypeEditor(simpleType);
            Node attr = _nodeAttributes.getNamedItem(simpleType.getName());
            if (attr != null)
                editor.setValue(attr.getNodeValue());
            editor.addActionListener(this);
            _simpleTypeEditors.put(simpleType, editor);
        }
    }

    protected SimpleTypeEditor createSimpleTypeEditor(GrammarSimpleType simpleType) {
        SimpleTypeEditor ret = null;
        int type = simpleType.getType();
        switch (type) {
            case DTDConstants.IDREF :
                ret = new KeyRefEditor(this, simpleType);
                break;
            case DTDConstants.TOKEN_GROUP :
                ret = new EnumerationEditor(this, simpleType);
                break;
            default :
                ret = new TextEditor(this, simpleType);
        }
        return ret;
    }

    /**
     * returns true if the attribute doesn't show in the editor display
     *
     * @param simpleType  Description of Parameter
     * @return            Description of the Returned Value
     */
    protected boolean suppressAttribute(GrammarSimpleType simpleType) {
        String s = _node.getNodeName() + "." + simpleType.getName();
        String t = (String) _hideAttrs.get(s);
        if (t != null) {
            MerlotDebug.msg("Suppressing attribute: " + s);
            return true;
        }
        return false;
    }

    public void actionPerformed(ActionEvent event) {
        MerlotDebug.msg("ComplexTypeEditPanel.actionPerformed(" + event + ")");
        try {
            save();
        } catch (PropertyVetoException ex) {
        }
        rebuildPanel();
    }

    protected void rebuildPanel() {
        this.removeAll();
        initPanelLayout();
        setupPanel();
        //buildPanel();
        revalidate();
    }

    public static class ScrollablePanel extends JPanel implements Scrollable {

        private boolean _trackHeight = true;
        private boolean _trackWidth = true;

        /**
         * Constructor for the ScrollablePanel object
         *
         * @param trackWidth   Description of Parameter
         * @param trackHeight  Description of Parameter
         */
        public ScrollablePanel(boolean trackWidth, boolean trackHeight) {
            _trackWidth = trackWidth;
            _trackHeight = trackHeight;
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

		public int getScrollableBlockIncrement(
			Rectangle visibleRect,
            int orientation,
            int direction) {
            return 10;
        }

        public boolean getScrollableTracksViewportHeight() {
            return _trackHeight;
        }

        public boolean getScrollableTracksViewportWidth() {
            return _trackWidth;
        }

		public int getScrollableUnitIncrement(
			Rectangle visibleRect,
            int orientation,
            int direction) {
            return 1;
        }

    }
}
