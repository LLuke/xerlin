package org.merlotxml.merlot;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import matthew.awt.*;

public class MerlotPreferenceDialog extends JDialog implements MerlotConstants,
    ListSelectionListener, ActionListener {
    protected JDialog _dialog;
    protected XMLEditorSettings _setting = XMLEditor.getSharedInstance().getSettings();
    protected XMLEditorFrame _frame = XMLEditorFrame.getSharedInstance();
    
    JLabel headingLabel = new JLabel(MerlotResource.
        getString(UI,"prefs.heading.label.text")+":");
    JLabel keyLabel = new JLabel(MerlotResource.
        getString(UI,"prefs.key.label.text")+ ":",JLabel.RIGHT);
    JLabel valueLabel = new JLabel(MerlotResource.
        getString(UI,"prefs.value.label.text")+ ":",JLabel.RIGHT);

    JButton setButton = new JButton(MerlotResource.
        getString(UI,"prefs.set.button.text"));
    JButton cancelButton = new JButton(MerlotResource.
        getString(UI,"cancel.button.text"));
    JButton okButton = new JButton(MerlotResource.
        getString(UI,"save.button.text"));
    JButton resetButton = new JButton(MerlotResource.
        getString(UI,"prefs.reset.button.text"));
    
    JTextField valueText = new JTextField();
    JTextField keyText = new JTextField();
    
    
    Properties userProps= _setting.getProperties();
    String filterStr =userProps.getProperty("merlot.prefs.filter");  
    PropsTableModel model = new PropsTableModel(userProps, filterStr);
    JTable table = new JTable(model);
        
    public MerlotPreferenceDialog(JFrame frame) {
        super(frame, MerlotResource.getString(UI,"edit.prefs"));
        _dialog = this;
        setupDialog();  
    }
       
    protected void setupDialog()  {
        
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        setSize(new Dimension(410, 420));
        
        //center panel gui
        JPanel centerPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane();
        GridBagLayout gridBagLayout1 = new GridBagLayout();
        Insets insets = new Insets(5, 5, 5, 5);
        
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        centerPanel.setPreferredSize(new Dimension(400,400));
        centerPanel.add(keyLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
        centerPanel.add(valueLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        centerPanel.add(valueText, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        centerPanel.add(scrollPane, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
        centerPanel.add(headingLabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        centerPanel.add(keyText, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        centerPanel.add(setButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, insets, 0, 0));
        scrollPane.getViewport().add(table, null);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        
        //south panel gui
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridBagLayout());
        southPanel.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
        southPanel.add(okButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,insets,0, 0));
        southPanel.add(resetButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
        contentPane.add(southPanel, BorderLayout.SOUTH);
        
        //selection event         
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   
        ListSelectionModel colSM = table.getSelectionModel();
        colSM.addListSelectionListener(this);         

        //button's event
        setButton.addActionListener(this);
        resetButton.addActionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
  
        //Is properties editable?
        keyText.setEnabled(false);
        String value = _setting.getProperty("merlot.prefs.value.editable");
        if (value.trim().equals("true")){
           valueText.setEditable(true);
        } else {
           valueText.setEditable(false);
        }

        //closing
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                setVisible(false);
                _frame._preferenceDialog = null;
            }
        });
       
    }
    public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;
        
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (!lsm.isSelectionEmpty()) {
            int row = lsm.getMinSelectionIndex();
            String key = (String)table.getModel().getValueAt(row,0);
            keyText.setText( key);
            valueText.setText( (String)(table.getModel().getValueAt(row,1)));          
        }
    }
    public void actionPerformed(ActionEvent e){
        if (e.getSource() == okButton){
            ok_actionPerformed(e);
        } else if (e.getSource() == setButton){
            set_actionPerformed(e);
        } else if (e.getSource() == cancelButton){
            cancel_actionPerformed(e);
        } else if (e.getSource() == resetButton){
            reset_actionPerformed(e);
        }
    }
    protected void cancel_actionPerformed(ActionEvent e){
        _dialog.setVisible(false);
        _frame._preferenceDialog = null;
        keyText.setText("");
        valueText.setText("");
        table.clearSelection();
        model.reset();
    }
    protected void ok_actionPerformed(ActionEvent e){
         _dialog.setVisible(false);
         _frame._preferenceDialog = null;
         keyText.setText("");
         valueText.setText("");
         table.clearSelection();
         model.save();
    }
    protected void reset_actionPerformed(ActionEvent e){
        keyText.setText("");
        valueText.setText("");
        Properties defaultProps = _setting.getDefaultProperties();
        String defaultFilterStr =defaultProps.
            getProperty("merlot.prefs.filter"); 
        model.load(defaultProps,defaultFilterStr);
    }
    protected void set_actionPerformed(ActionEvent e){
        int row = table.getSelectedRow();
        if (row != -1) {
            table.getModel().setValueAt(keyText.getText(),row,0);
            table.getModel().setValueAt(valueText.getText(),row,1);
            table.setRowSelectionInterval(row, row); 
        }
    }
    class PropsTableModel extends AbstractTableModel{
        int rows = 2;
        int columns = 2;
        String[] columnName;
        Object [][] data;;
        Properties _defaultProps; //use to restore props if cancel btn is clicked 
        String _filterStr = "";
        
        public PropsTableModel(Properties defaultProps, String filterStr){
           _defaultProps = defaultProps;
           _filterStr= filterStr;
           columnName = new String [2];
           columnName[0] = MerlotResource.getString(UI,"prefs.key.label.text");
           columnName[1] = MerlotResource.getString(UI,"prefs.value.label.text");
           load(_defaultProps,_filterStr);
        }
        public int getRowCount(){
            return rows;
        }
        public int getColumnCount(){
            return columns;
        }
        public Object getValueAt(int row, int col){
            return data[row][col];
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        public String getColumnName(int column) {
            return columnName[column];
        }
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data[rowIndex][columnIndex]= aValue;
            fireTableDataChanged();
        }
        // load the properties with key start with filterStr 
        public void load(Properties prop, String filterStr) {
            Enumeration enum = prop.propertyNames();
            Vector v = new Vector();
            for (int i=0; enum.hasMoreElements(); i++) {
                 String key = (String)enum.nextElement(); 
                 if (filterStr.trim().equals("")){
                     v.add(key);
                 } else {
                    if (key.startsWith(filterStr)) 
                    v.add(key);
                 }
            }
            Object [] obj =  v.toArray();
            Arrays.sort(obj);
            
            rows = v.size();
            data  = new Object[rows][2];
            for (int j = 0; j <obj.length ; j++) {
              String key = (String)obj[j];
              data[j][0]=key;
              data[j][1]= prop.getProperty(key);
            }      
            fireTableDataChanged();      
        }
        public void reset() {
            load(_defaultProps,_filterStr);            
        }
        public void save() {
            Properties props = new Properties();
            for (int i = 0; i < rows; i++) {
              props.put(data[i][0] ,data[i][1]);
            }
            _defaultProps = props;
            _setting.saveProperties(_defaultProps);
        }  
    }
}
