/**
 *
 *  JMoreMenu - This popup menu provide the functionality to
 *  limit the size of the popup menu, providing a "more" submenu for
 *  more items.  Similar to the functionality of the Motif pick-list
 *  you find in UNIX netscape functionality.  This is a very simple
 *  implementation that checks for vertically overrunning the screen
 *  only, checking for horizontally overrunning the screen should also
 *  be done (eventually). 
 *
 *  NOTE - There is a small TEST of getComponentCount and getComponent
 *  in this version.  I had some problems with  getComponentCount 
 *  returning 0 if the menu had not been shown at least once, similiar
 *  to the problem with getPreferredSize.  If the getComponentCount works
 *  properly - replace the getComponents().length style with a 
 *  getComponentCount call.
 *
 *  DISCLAIMER -
 *
 *  JPopupMenu and JMenu are not direct class/subclass relationships.  They
 *  are siblings, both derived from JComponent.  This made things a little
 *  difficult during development.  Any ideas on how to simplify the code
 *  would be welcome.  Example - JMenu has no prepend functionality.  Menu
 *  prepend forces a removeAll then re-insertion of everything.
 * 
 *  A more efficient version of this class could undoubtedly be written
 *  but I was going for simplicity, understandability, and correctness
 *  first.
 *
 *  I tried to make this a drop in replacement for JPopupMenu, but it
 *  will undoubtedly need far far more work and testing before this 
 *  becomes the case.  Things such as createActionListener, fire*Event, 
 *  menuSelectionChanged, processKeyEvent, processMouseEvent, etc. probably
 *  need to be re-written before things behave quite as expected.
 *
 */


package org.merlotxml.merlot;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

// SubClass JMenu to provide the same "more" functionality as Sizeable
// JPopupMenu 

public class JMoreMenu extends JMenu {
    /**
     *  More Menu Text - makes changing text easier later
     */
    public static final String MORE="more";

    /**
     *  Take into account most window managers have a task or system
     *  bar always on top on the bottom of the screen.  Empirically
     *  determined value.
     */
    public static final int TASKBAR_HEIGHT = 55;
    
    /**
     *  The maximum height in screen pixles a menu is allowed to be.
     */
    protected double maximumHeight;
    
    /** 
     * Manually keep track of height - getPreferredSize/getSize seems 
     * to only work properly after the menu has been dispayed at least
     * once. 
     */
    protected double myHeight;
    
    
    /**
     *  Used to provide arbitrarily deep more menus.
     */
    JMoreMenu moreMenu;


    /**
     *  Default constructor
     */
    public JMoreMenu() {
	super();
        maximumHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() - TASKBAR_HEIGHT;    
    }
    
    /**
     *  Constructor - override JMenu constructor set the default height
     */
    public JMoreMenu(String label) {
	super(label);
        maximumHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() - TASKBAR_HEIGHT;    
    }
    

    /**
     *  Constructor - override JMenu constructor, set the default height.
     */
    public JMoreMenu(String label,boolean b) {
	super(label,b);
        maximumHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() - TASKBAR_HEIGHT;    
    }
    

    /**
     *  Override of JMenu::add(Action);
     */
    public JMenuItem add(Action a) {
	// Actions do not have a getPreferredSize call
	// Therefore, the only way to determine the desired height 
	// of an action to be inserted is to forcibly insert
	// it and then ask the resulting menu item that
	// is returned for its preferred height.  Then based
	// on that height, determine if we can add it to the
	// base menu or if we must 
	
	JMenuItem retVal = null;
	
	// Forcibly insert the item into the menu
	JMenuItem tempMenuItem = super.add(a);
	//super.remove(tempMenuItem);
	
	// Determine if we can insert this into the primary menu
	// or if we must insert into the more menu.
	// Use locals for convenient reference points when debugging
	double preferredHeight = getPreferredSize().getHeight();
	double menuItemHeight = tempMenuItem.getPreferredSize().getHeight();
	
	if ((preferredHeight + menuItemHeight) < maximumHeight) {
	    retVal = tempMenuItem;
	}
	else {
	    // Create the more menu if necessary
        super.remove(tempMenuItem);
	    createSubMoreMenu();
	    // Add item to the More Menu.
	    retVal = moreMenu.add(a);
	}
	
	return retVal;
    }

    /**
     *  Override of JMenu::add(JMenuItem)
     */
    public JMenuItem add(JMenuItem menuItem) {	    
	JMenuItem retVal = null;

	// Use locals for a convenient reference point
	// to check when debugging.
	double menuItemSize = menuItem.getPreferredSize().getHeight();
	
	if ((myHeight + menuItemSize) < maximumHeight) {
	    retVal = super.add(menuItem);
	    myHeight += menuItemSize;
	}
	else {
	    // Create the more menu if necessary
	    createSubMoreMenu();
	    retVal = moreMenu.add(menuItem);
	}
	return retVal; 
    }


    /**
     *  Convenience add on - allows for easy integration to
     *  SizeableJPopupMenu.  There are a differences between
     *  JPopupMenu and JMenu that make them not completely straightforward
     *  to integrate.
     */
    public Component addComponent(Component c) {
	Component retVal = null;
	
	// Use locals for a convenient reference point
	// to check when debugging.
	double componentSize = c.getPreferredSize().getHeight();
	
	if ((myHeight + componentSize) < maximumHeight) {
	    retVal = super.add(c);
	    myHeight += componentSize;
	}
	else {
	    // Create the more menu if necessary
	    createSubMoreMenu();
	    retVal = moreMenu.addComponent(c);
	}
	return retVal; 
    }

    /**
     *  Override of JMenuItem::add(string)
     */
    public JMenuItem add(String string) {
	// Strings do not have a getPreferredSize call
	// Therefore, the only way to determine the height 
	// needed is to forcibly insert the item, then remove it.
	JMenuItem retVal = null;
	
	// Use locals for a convenient reference point when 
	// debugging
	double menuItemHeight = 0;
	JMenuItem tempMenuItem = super.add(string);
	if (tempMenuItem != null) {
	    menuItemHeight = tempMenuItem.getPreferredSize().getHeight();
	}
	
	super.remove(tempMenuItem);
	
	if ((myHeight + menuItemHeight) < maximumHeight) {
	    retVal = super.add(string);
	}
	else {
	    createSubMoreMenu();
	    retVal = moreMenu.add(string);
	}
	return retVal;
    }

    /**
     *  Override of JMenu getComponentIndex(Component).  Automatically
     *  skips over menu genereated "more>" item.
     */
    public int getComponentIndex(Component c) {
	Component[] components = getMenuComponents();
	int retVal = -1;
	
	for (int i = 0; i < components.length; i++) {
	    if (components[i] == c) {
		retVal = i;
		break;
	    }
	}
	if (retVal == -1) {
	    // Account for nested More menu
	    int moreLocation = moreMenu.getComponentIndex(c);
	    if (moreLocation != -1) {
		retVal = (components.length-1) + moreLocation;
	    }
	}
	return retVal;
    }

    /**
     *  Override of JMenu::getComponentAtIndex(int).  Automatically
     *  skip over the menu generated "more>" item.
     */
    public Component getComponentAtIndex(int index) {
	Component retVal = null;
	Component[] components = getMenuComponents();
       
	if (index < components.length) {
	    retVal = components[index];
	}
	else {
	    retVal = moreMenu.getComponentAtIndex(index-components.length);
	}
	return retVal;
    }
    
    /**
     *  Override of the JMenu::setSelected,  includes a 
     *  "do something intelligent" check to help ease integration
     *  with SizeableJPopupMenu
     */
    public void setSelected(Component c) {
	Component[] components = getComponents();
	Component selectedComponent = null;
	
	for (int i = 0; i < components.length; i++) {
	    if (components[i] == c) {
		selectedComponent = components[i];
		break;
	    }
	}
	if (selectedComponent == null) {		
	    // Account for More menu
	    moreMenu.setSelected(c);
	}
	else {
	    // do something intelligent
	    // This is a place where the difference between
	    // JPopupMenu and JMenu makes things a little difficult
	    setSelected(true);
	}
    }
    
    /**
     *  insert at index (if index > menusize  append item)
     *  JMenus do not have a insert(Component, index)
     *  Hence removeAll, then re-add everything inserting the new one
     *  when appropriate.
     */
    public void insert(Component c, int index) {
	Vector components = getAllSubComponents();
	removeAll();
	
	// Boundary Condition - insert degrading into an add
	if (index >= components.size())  {
	    addComponent(c);
	}
	for (int i = 0; i < components.size(); i++) {
	    if (i == index) {
		addComponent(c);
	    }
	    addComponent((Component)components.elementAt(i));
	}
    }

    /**
     *  Override JMenu::insert(Action, int)  Automatically restuctures
     *  menu by height.
     */
    public JMenuItem insert(Action a, int index) {
	JMenuItem retVal = null;
	
	// Actions do not have a getPreferred size call, so we have
	// to forcibly insert it, then ask the result JMenuItem
	// what size it wanted.
	
	int itemCount = getMenuComponentCount();
	JMenuItem tempItem = super.add(a);
	super.remove(tempItem);
	insert((Component)tempItem,index);
	return retVal;
    }
    
    /**
     *  Override JMenu::insert(String, int)  Automatically restuctures
     *  menu by height.
     */
    public void insert(String string, int index) {
	// Strings do not have a getPreferredSize, so the only
	// way to determine the height is to forcibly insert
	// it, then ask the resulting JMenuItem what it's
	// preferedSize is.
	
	JMenuItem tempItem = super.add(string);
	super.remove(tempItem);
	insert((Component)tempItem,index);
    }

    /**
     *  Override JMoreMenu remove. Automatically take "more>" structure
     *  into account.
     */
    public void remove(int index) {
	Component[] components = getMenuComponents();
	
	if (index < components.length) {
	    super.remove(index);
	}
	else {
	    if (moreMenu != null) {
		moreMenu.remove(index-components.length);
	    }
	    else {
		// Assertion point - More Menu should
		// not be null here
	    }
	}
    }

    /**
     *  Override JMoreMenu removeAll. Automatically take "more>" structure
     *  into account.
     */
    public void removeAll() {
	if (moreMenu != null) {
	    moreMenu.removeAll();
	    super.remove(moreMenu);
	    moreMenu = null;
	}
	super.removeAll();
	myHeight = 0;
    }
    
    /**
     *  Convenience add on
     */
    public int SizeableGetItemCount() {
	int retVal = 0;
	int itemCount = getMenuComponentCount();
	
	if (moreMenu == null) {
	    retVal = itemCount;
	}
	else  {
	    // Account for the more menu item - hence the '-1'
	    retVal = (itemCount-1) + 
		moreMenu.SizeableGetItemCount();
	}
	return retVal;
    }
    
    /** 
     *  Convenience helper function - same functionality but 
     *  use a local helper in case I have to change things
     *  later
     */
    private void createSubMoreMenu() {
	if (moreMenu == null) {
	    moreMenu = new JMoreMenu(MORE);
	    super.add(moreMenu);
	    moreMenu.setMaximumHeight(maximumHeight);
	}
    }


    /**
     *  Set the maximum allowable height if default screenheight-taskbar
     *  is insufficient.
     */
    public void setMaximumHeight(double aHeight) {
	maximumHeight = aHeight;
	if (moreMenu != null) {
	    moreMenu.setMaximumHeight(aHeight);
	}
    }

    /**
     *  Accessor
     */
    public double getMaximumHeight() {
	return maximumHeight;
    }
    
    
    /**
     *  Get all Components **MINUS THE MORE BUTTONS**
     *  Use vectors, they are easier to work with when dynamically appending
     */
    public Vector getAllSubComponents() {
	Vector components = new Vector();
	Component[] componentArray = getMenuComponents();
	
	for (int i = 0; i < componentArray.length; i++) {
	    if (componentArray[i] instanceof JMenuItem) { // sanity check
		// Skip over more menu item.
		if ((JMenuItem)componentArray[i] != moreMenu) {
		    components.add(componentArray[i]);
		}
	    }
	    else {
		components.add(componentArray[i]);
	    }
	}
	if (moreMenu != null) {
	    Vector moreComponents = moreMenu.getAllSubComponents();
	    for (int j = 0; j < moreComponents.size(); j++) {
		components.add(moreComponents.elementAt(j));
	    }
	}
	
	return components;
    }

    /**
     *  Used to forcibly insert items at the end of the primary
     *  menu.  Useful if you have items that you don't want 
     *  who knows how many levels deep in the more structure.
     */
    public void insertStatic(Component c) {
	// Determine if the Component can fit already
	// If yes
	//   Just do a regular insert, we're done.
	// else
	//   do
	//      Put the standard item closest to the more
	//      menu in the more menu
	//   until the static component (c) can fit in this menu
	//   do a regular insert(component)

	double componentHeight = c.getPreferredSize().getHeight();
	
	if ((myHeight + componentHeight) < maximumHeight - componentHeight) {
	    // Force the component to go below the moreMenu
	    super.add(c);
            myHeight += c.getPreferredSize().getHeight();
	}
	else {
	    int itemCount = getMenuComponentCount();
	    Component componentToMove = null;
	    do {
		// the standard item is 1 before
		// the more menu hence -1
		int index = getComponentIndex(moreMenu);
		componentToMove = getComponentAtIndex(index-1);


		moreMenu.insert(componentToMove,0);
		//super.remove(componentToMove);
		myHeight -= componentToMove.getPreferredSize().getHeight();
		itemCount = getMenuComponentCount();
	    } while ((componentHeight + myHeight) >= maximumHeight);
	    super.add(c);
	    myHeight += c.getPreferredSize().getHeight();
	}
    }

    /**
     *  Set the position of the menus - try guard against the menus going
     *  off the screen.
     *
     *  WARNING - The code here is not very complex, but a bit deceiving
     *            Getting this code **CORRECT** is a lot tricker than
     *            it looks, primarily due to the fact you don't have 
     *            easy access to the internal co-ordinate system and
     *            associated maniuplation routines.  Most of this code
     *            is work-around of known problems in Swing with a
     *            couple of sequence dependent items. (must be called
     *            at the appropriate time).
     *            When making mods, proceed with MUCH CAUTION!.
     */
    public void setPopupMenuVisible(boolean b) {
	Point location = getLocation();

	int oldX = (int)location.getX();
	int oldY = (int)location.getY();


	// WORK AROUND
	// SwingUtilities convertPointTo/FromScreen Utilities do not
	// work very well, do it myself.  convertPointFromScreen has 
	// known a bug in it only converts the Y location, not the X location
	// get the screen location.x in a another way.
	Point screenWorkAround = this.getLocationOnScreen();
	int screenY = (int)screenWorkAround.getY();
	int screenX = (int)screenWorkAround.getX();

	// WORK AROUND KNOWN getPreferredSize() problem
	Component c = getComponentAtIndex(0);
	int componentWidth = (int)getSize().getWidth();

	// Compute new Location on Screen
	int newScreenY = screenY;
	int newScreenX = screenX + componentWidth;
	// END WORKAROUND

	// Try to guard against going off the screen
	double screenHeight = 
	    Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	double screenWidth = 
	    Toolkit.getDefaultToolkit().getScreenSize().getWidth();

	if ((newScreenY+myHeight) > (screenHeight - TASKBAR_HEIGHT)){
	    newScreenY = (int)screenHeight - ((int)myHeight + TASKBAR_HEIGHT);
	}
	if ((newScreenX + componentWidth) > screenWidth) {
	    newScreenX =(int)(screenWidth - componentWidth);
	}

	// These two items *MUST* be called last, otherwise something
	// inside swing resets the values to default swing-calculated
	// values.
	super.setPopupMenuVisible(b);  // Sequence dependent
	setMenuLocation(newScreenX,newScreenY);  // sequence dependent
    }
    

    /**
     *  Get sub-menu
     */
    JMoreMenu getMoreMenu() {
	return moreMenu;
    }
}
