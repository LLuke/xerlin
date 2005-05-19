package org.merlotxml.merlot.editors;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.merlotxml.util.xml.xerces.SchemaGrammarSimpleTypeImpl;

/**
 * @author everth
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TextEditor extends SimpleTypeEditor {
    
    JTextField _textField = null;
    JTextArea _textArea = null;
    Vector _actionListeners = null;
    static int _eventId = 0;

    /**
     * Constructor for TextEditor.
     * @param complexTypeEditPanel
     * @param grammarSimpleType
     */
    public TextEditor(
        ComplexTypeEditPanel complexTypeEditPanel,
        GrammarSimpleType grammarSimpleType) {
        super(complexTypeEditPanel, grammarSimpleType);
    }

    public void addActionListener(ActionListener listener) {
        JComponent component = getComponent();
        if (component instanceof JTextField) {
            ((JTextField)component).addActionListener(listener);
        } else {
            if (_actionListeners == null)
                _actionListeners = new Vector();
            _actionListeners.add(listener);
        }
    }
    
    public void removeActionListener(ActionListener listener) {
        JComponent component = getComponent();
        if (component instanceof JTextField) {
            ((JTextField)component).removeActionListener(listener);
        } else {
            if (_actionListeners == null)
                _actionListeners = new Vector();
            _actionListeners.remove(listener);
        }
    }
    
    void fireActionPerformed() {
        if (_actionListeners == null)
            return;
        ActionEvent event = new ActionEvent(this, _eventId++, "ValueChanged");
        for (int i = 0; i < _actionListeners.size(); i++) {
            ActionListener listener = (ActionListener)_actionListeners.get(i);
            listener.actionPerformed(event);
        }
    }

    /**
     * Gets the component for editing this simple type.
     *
     * @return   The component value
     */
    public JComponent getComponent() {
        if (_component == null) {
            int maxLength = -1;
            if (_grammarSimpleType instanceof SchemaGrammarSimpleTypeImpl) {
                SchemaGrammarSimpleTypeImpl schemaSimpleType =
                    (SchemaGrammarSimpleTypeImpl) _grammarSimpleType;
                maxLength = schemaSimpleType.getMaxLength();
            }
            MerlotDebug.msg("MaxLength: " + maxLength);
            if (maxLength <= 128) {
                _textField = new JTextField();
                //_textField.addActionListener(new TextFieldKeyListener());
                _component = _textField;
            } else {
                _textArea = new JTextArea();
                _textArea.addKeyListener(new TextAreaKeyListener());
                _textArea.setLineWrap(true);
                _textArea.setWrapStyleWord(true);
                //_textArea.setRows(5);
                //_textArea.setPreferredSize(new Dimension(350, 50));
                //_textArea.setMinimumSize(new Dimension(40, 40));
                //_textArea.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
                JScrollPane scrollPane =
                    new JScrollPane(
                        _textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setPreferredSize(new Dimension(350, 40));
                //scrollPane.setMinimumSize(new Dimension(40, 40));
                //scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
                _component = scrollPane;
                //_component = _textArea;
            }
        }
        return _component;
    }

    public String getValue() {
        getComponent();
        String ret = null;
        if (_component instanceof JTextField) {
            ret = _textField.getText();
        } else {
            ret = _textArea.getText();
        }
        if (ret == null)
            ret = "";
        return ret;
    }
    
    public void setValue(String value) {
        getComponent();
        if (value == null)
            value = _grammarSimpleType.getDefaultValue();
        if (_textField != null) {
            _textField.setText(value);
        }
        if (_textArea != null) {
            _textArea.setText(value);
        }
        if (_grammarSimpleType.getDefaultType() == DTDConstants.FIXED) {
            setEditable(false);
        }
    }

    public void setEditable(boolean editable) {
        if (_textField != null) {
            _textField.setEditable(editable);
            _textField.setEnabled(editable);
        } if (_textArea != null) {
            _textArea.setEditable(editable);
            _textArea.setEnabled(editable);
        }
    }
    
    class TextAreaKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent ke) {
            int code = ke.getKeyCode();
            int modifiers = ke.getModifiers();
            if (code == KeyEvent.VK_ENTER) {
                if (modifiers == KeyEvent.CTRL_MASK
                    || modifiers == KeyEvent.ALT_MASK
                    || modifiers == KeyEvent.SHIFT_MASK) {
                    _textArea.append("\r\n");
                } //else {
                    //fireActionPerformed();
                    //ke.consume();
                //}
            }
        }
    }
}
