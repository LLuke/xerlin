/**
 *
 *  SizableJPopupMenu - This popup menu provide the functionality to
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

public class SizeableJPopupMenuByHeight extends JPopupMenu {

    /**
     *  Take into account most window managers have a task or system
     *  bar always on top on the bottom of the screen.  Empirically
     *  determined value.
     */
    public static final int TASKBAR_HEIGHT = 55;


    /**
     *  More Menu Text - makes changing text easier later
     */
    public static final String MORE="more";
    

    /** 
     * Manually keep track of height - getPreferredSize/getSize seems 
     * to only work properly after the menu has been dispayed at least
     * once.
     */
    protected double myHeight;

    /** 
     * The maximum number height allowed in a menu
     */
    protected double maximumHeight;
    
    /**
     *  "more->" menu - recursive object allows for arbitrarily deep
     *  more menus.
     */
    JMoreMenu moreMenu;
    
    public SizeableJPopupMenuByHeight() {
	super();

	// Arbitrary Default
	maximumHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight() - TASKBAR_HEIGHT;
	moreMenu = null;
    }
    

    public SizeableJPopupMenuByHeight(String label) {
	super(label);
	maximumHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight()- TASKBAR_HEIGHT;
	moreMenu = null;
    }


    /**
     *  Additional work needed to finalize and free up memory.
     */
    public void finalize() {
	// Placeholder
    }


    /** 
     *  Convenience helper function
     */
    private void createMoreMenu() {
	if (moreMenu == null) {
	    moreMenu = new JMoreMenu(MORE);
	    moreMenu.setMaximumHeight(maximumHeight);
	    super.add(moreMenu);
	}
    }

    /**
     *  TEMPORARY - Move to State model once you get height algorithm working
     *  Differences between JMenu and JPopup menu make the algorithm somewhat
     *  tempermental and a bit unpredictable.  Move into a STATE pattern with
     *  message forwarding once you get the algorithm working.
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
	    createMoreMenu();
	    // Add item to the More Menu.
	    retVal = moreMenu.add(a);
	}
	return retVal;
    }


    /**
     *  Override the JPopupMenu functionality, try to provide illusion of a 
     *  single menu.
     *  TEMPORARY - Two methods are temporary, move to a STATE pattern
     *  once you get the height algorithm working.
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
	    createMoreMenu();
	    retVal = moreMenu.add(menuItem);
	}
	return retVal; 
    }
    

    /**
     *  Override the JPopupMenu functionality ....
     *  same disclaimer as above
     */
    public JMenuItem add(String string) {
	// Strings do not have a getPreferredSize call
	// Therefore, the only way to determine the height 
	// needed is to forcibly insert the item, then remove it.
	JMenuItem retVal = null;
	
	// Use locals for a convenient reference point when 
	// debugging
	double preferredHeight = getPreferredSize().getHeight();
	double menuItemHeight = 0;
	JMenuItem tempMenuItem = super.add(string);
	if (tempMenuItem != null) {
	    menuItemHeight = tempMenuItem.getPreferredSize().getHeight();
	}
	
	super.remove(tempMenuItem);
	
	if ((preferredHeight + menuItemHeight) < maximumHeight) {
	    retVal = super.add(string);
	}
	else {
	    createMoreMenu();
	    retVal = moreMenu.add(string);
	}
	return retVal;
    }


    /**
     *  Override the JPopup Menu
     */
    public void addSeparator() {
	// Ignore height of separator
	// Not ideal - but typical
	super.addSeparator();
    }

    /**
     *  Override the JPopupMenu functionality
     */
    public int getComponentIndex(Component c) {
	int retVal = -1;

	Component[] components = getComponents();
	retVal = super.getComponentIndex(c);

	if (retVal >= 0) {
	    ; // Nothing to do - already found the item
	}
	else {
	    // Account for more Menu;
	    int moreLocation = moreMenu.getComponentIndex(c);
	    if (moreLocation != -1) {
		// Account for the more menu item - hence the '-1'
		retVal = (components.length-1) + moreMenu.getComponentIndex(c);
	    }
	}
	return retVal;
    }
    

    /**
     *  Override the JPopupMenu functionality
     */
    public Component getComponentAtIndex(int i) {
	Component retVal = null;
	Component[] components = getComponents();

	// I'm not certain what getComponentIndex does here, test.
	if (i < components.length) {
	    retVal = super.getComponentAtIndex(i);
	}
	else {
	    // The extra steps are to make debugging easier.
	    retVal = moreMenu.getComponentAtIndex(i-components.length);
	}
	return retVal;
    }

    /**
     *  Override the JPopupMenu functionality
     */
    public MenuElement[] getSubElements() {
	
	// Test - Use getComponentCount - had problems with it before
        // not working correctly if menu had not been shown at least
	// once.  If successful - replace appropriate getComponents[] call 
	// with itemCount style call
	int itemCount = getComponentCount();
	MenuElement[] retVal = null;

	if (moreMenu==null) {
	    retVal = super.getSubElements();
	}
	else {
	    // Concatenate moreMenu and my elements, but don't
	    // include the "more" button item, hence "length-1"
	    Vector elements = new Vector();
	    MenuElement[] subElements = super.getSubElements();
	    for (int i = 0; i < subElements.length-1; i++) {
		elements.add(subElements[i]);
	    }
	    MenuElement[] moreElements = {};
	    if (moreMenu != null) {
		moreElements = moreMenu.getSubElements();
	    }
	    for (int j = 0; j < moreElements.length; j++) {
		elements.add(moreElements[j]);
	    }
	    // Casting caused wierd problems, so we do it the hard way.
	    MenuElement[] elementsToReturn = new MenuElement[elements.size()];
	    for(int k = 0; k < elements.size(); k++) {
		elementsToReturn[k] = (MenuElement)elements.elementAt(k);
	    }
	    retVal = elementsToReturn;
	}
	return retVal;
    }

    /**
     * Override the JPopupMenu functionality
     * NOTE - CODE A MUCH MORE COMPLEX THAN I WOULD LIKE.  CONTINUE TO SEARCH
     * FOR A BETTER WAY TO INTEGRATE JPOPUPMENU AND JMENU.  
     * The subtle differences between the two is what made this code a 
     * nightmare to code up and debug!!!!
     */
  
    public void insert(Action a, int index) {
	int itemCount = getComponentCount();

	// Actions do not have a getPreferred size call, so we have
	// to forcibly insert it, then ask the result JMenuItem
	// what size it wanted.
	JMenuItem tempItem = super.add(a);
	super.remove(tempItem);
	insert((Component)tempItem,index);
    }
    

    /**
     * Override the JPopupMenu functionality
     */
    public void insert(Component c, int index) {
	int itemCount = getComponentCount();

	if (index < itemCount) {
	    super.insert(c,index);
	    double componentHeight = c.getPreferredSize().getHeight();
	    itemCount += 1; // New Component
	    myHeight += componentHeight;
	    if (myHeight < maximumHeight) {
		// Nothing left to do - Leave this code in this is a 
		// good placeholder for debugging code if necessary
	    }
	    else {
		Component componentToMove = null;
		do {
		    // Get the component closest to the moreMenu item
		    // menu is 0 based, and component is 1 back
		    // hence -2
		    componentToMove = getComponentAtIndex(itemCount - 2);
		    moreMenu.insert(componentToMove,0);
		    myHeight -= componentToMove.getPreferredSize().getHeight();
			
		} while (myHeight > maximumHeight);
	    }
	}
	else {
	    createMoreMenu();
	    moreMenu.insert(c, index - itemCount);
	}
    }

    /**
     *  Convenience method.  JPopupMenu does not have a insert(String)
     *  however, it makes things very convenient
     */
    public void insert(String string, int index) {
	// Strings do not have a getPreferredSize call, hence the onlye
	// way to get their preferred size is to forcibly insert them
	// then ask the resulting JMenuItem what it's size is
	
	int itemCount = getComponentCount();
	JMenuItem tempItem = super.add(string);
	double itemHeight = tempItem.getPreferredSize().getHeight();
	super.remove(tempItem);
	insert((Component)tempItem,index);
    }

    /**
     *  Override the JPopupMenu functionality.  For simplicity, do not
     *  reshuffle the menu item for now.  Remove is not used as often as
     *  add/insert.
     */
    public void remove(Component c) {
	if (getComponentIndex(c) != -1) {
	    // We have the component in the main
	    // menu
	    super.remove(c);
	}
	else {
	    // one of the more menus has it.
	    moreMenu.remove(c);
	    // If the more menu is now empty - remove it.
	    if (moreMenu.getItemCount() == 0) {
		super.remove(moreMenu);
		moreMenu = null;
	    }
	}
    }

    /**
     *  Override the JPopupMenu functionality
     *  For simplicities sake, do not reshuffle menu for now.
     *  Remove is not used as often as add/insert algorithms
     */
    public void remove(int pos) {
	int itemCount = getComponentCount();
	if (pos < itemCount) {
	    super.remove(pos);
	}
	else {
	    moreMenu.remove(pos-itemCount);
	    // If the more menu is now empty - remove it.
	    if (moreMenu.getItemCount() == 0) {
		super.remove(moreMenu);
		moreMenu = null;
	    }
	}
    }

    /**
     *  Override JPopupMenu functionality.  Take into account aut
     */
    public void removeAll() {
	if (moreMenu != null) {
	    moreMenu.removeAll();
	    moreMenu = null;
	}
	super.removeAll();
	myHeight = 0;
    }


    /**
     *  Added functionality - dynamically restucture the menu to allow menus to
     *  be a maximum height or by number on the screen.  Provided for convenience
     *  only.  Height algorithms are more desireable but tend to be quite
     *  tempermental and unpredictable requiring testing out the yin-yang to get
     *  right.
     */
    
    /**
     *  Get all Components **MINUS THE MORE BUTTONS**
     *  Use vectors, they are easier to work with when working with 
     *  heavily dynamic stuff.
     */
    public Vector getAllSubComponents() {
	Vector componentVector = new Vector();
	Component[] componentArray = getComponents();

	for (int i = 0; i < componentArray.length; i++) {
	    // Skip over the more menu item
	    if (componentArray[i] instanceof JMenuItem) { // sanity check
		if ((JMenuItem)componentArray[i] == moreMenu) {
		    componentVector.add(componentArray[i]);
		}
	    }
	    else {
		componentVector.add(componentArray[i]);
	    }
	}
	if (moreMenu != null) {
	    Vector moreComponents = moreMenu.getAllSubComponents();
	    
	    for (int j = 0; j < moreComponents.size(); j++) {
		componentVector.add(moreComponents.elementAt(j));
	    }
	}
	return componentVector;
    }


    /**
     *  Show - Over the JPopupMenu functionality - try to guard against
     *  menu going off the screen.  If the max Height is larger than
     *  screen area however, we're fried, set the menu size smaller.
     *
     *  WARNING - The code here is not very complex, but a bit deceiving
     *            Getting this code **CORRECT** is a lot tricker than
     *            it looks, primarily due to the fact you don't have
     *            easy access to the internal co-ordinate system and
     *            associated maniuplation routines.  In addition, the
     *            getPreferredSize call doesn't seem to work properly 
     *            until the menu has been displayed at least once.
     *            If you don't believe, uncomment out the System.out.println
     *            calls and watch what happens (left in for debugging later).
     *
     *            Most of this code is work-around of known problems in Swing 
     *            with a couple of sequence dependent items. (must be called
     *            at the appropriate time).
     *
     *            When making mods, proceed with MUCH CAUTION!.
     */
    public void show(Component invoker, int x, int y) {
        pack(); // Desparation to find out why
                // getPreferredSize() is not  returning correct values

        // WORKAROUND
        // KNOWN PROBLEM IN SWING convertPointToScreen routine, convertPointToScreen only
        // converts one component of the point, not both.
        Point screenWorkAround = invoker.getLocationOnScreen();
	Point screenLocation = new Point((int)screenWorkAround.getX()+x,(int)screenWorkAround.getY() + y);
        // END WORKAROUND
	Dimension preferredSize = getPreferredSize();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	//SwingUtilities.convertPointToScreen(screenLocation,invoker);

        //  TEST CODE
        //System.out.println("DEBUG Popup Menu Size = " + preferredSize);
        //System.out.print("Invoker Location = " + x);
        //System.out.println("," + y);
        //System.out.println("Screen Location = " + screenLocation);

        //System.out.println("My Height = " + myHeight);
        //  TEST CODE
        // KNOWN PROBLEM IN getPreferredSize()
        MenuElement elements[] = getSubElements();
        //System.out.println("Elements Size = " + elements.length);
        Component c;
        Dimension componentPreferredSize;
        Dimension componentActualSize;
        double computedHeight = 0;
        double computedWidth = 0;
        for (int i = 0; i < elements.length; i++) {
           c = elements[i].getComponent();
           componentPreferredSize = c.getPreferredSize();
           componentActualSize = c.getSize(); 
           //System.out.print("Component " + i);
           //System.out.println("PreferredSize = " + componentPreferredSize);
           //System.out.println("ActualSize = " + componentActualSize);
           computedHeight += componentPreferredSize.getHeight();
           if (componentPreferredSize.getWidth() > computedWidth) {
              computedWidth = componentPreferredSize.getWidth();
           }
        }
        //System.out.println("Computed Height = " + computedHeight);
        //System.out.println("Computed Width = " + computedWidth);
  
        //  END TEST CODE
	// Check X location and width 
	// if off the screen 
	//    reposition the menu
	int maxLocationX = (int)screenLocation.getX() + (int)computedWidth;
	if (maxLocationX > (int)screenSize.getWidth()) {
	    // Reposition menu - 25 pixels from the edge just to be safe
	    x = (int)screenSize.getWidth() - (int)computedWidth - 25; 
	}
	else {
	    // Nothing to do - save raw screen location for later
	    x = (int)screenLocation.getX();
	}

	// Check Y location and width 
	// if off the screen
	//    reposition the menu
	int maxLocationY = (int)screenLocation.getY() + (int)computedHeight;

	if (maxLocationY > (int)screenSize.getHeight() -TASKBAR_HEIGHT ) {
	    // Reposition - 55 pixels from edge just to be safe
	    // Use 55 to accomodate really big Windows Task Bars
            y = (int)(screenSize.getHeight() - (computedHeight + TASKBAR_HEIGHT));
	}
	else {
	    // Nothing to do - save raw screen location for later
	    y = (int)screenLocation.getY();
	}
	
	// Final Check - if top of menu is off the screen
	// place menu at the top of the screen
        // Unfortunately, if the final menu is taller 
        // than the screen size, we're just plain screwed.
        // Set the maximum height to a smaller height.
	

	screenLocation.setLocation(x,y);
	SwingUtilities.convertPointFromScreen(screenLocation,invoker);
	super.show(invoker,(int)screenLocation.getX(),
		   (int)screenLocation.getY());
    }


    /**
     * Override the JPopupMenu functionality
     */
    public void setSelected(Component c) {
	if (getComponentIndex(c) != -1) {
	    // We have the component in the main menu
	    super.setSelected(c);
	}
	else {
	    // one of the more menus has it.
	    moreMenu.setSelected(c);
	}
    }

    /**
     * Override the JPopupMenu functionality
     */
    public void setBorderPainted(boolean b) {
	super.setBorderPainted(b);
	if (moreMenu != null) {
	    moreMenu.setBorderPainted(b);
	}
    }

    /**
     *  I ran out of ideas on how to get it to be invisible, so I added
     *  some little helpers.  Get Item count but do not include the
     *  automatically created more menus.  I could not get the 
     *  getComponentCount to work transparently.
     */
    public int SizeableGetItemCount() {
	int retVal = 0;
	if (moreMenu != null) {
	    retVal =  getComponentCount();
	}
	else {
	    int itemCount = getComponentCount();
	    retVal = itemCount + moreMenu.SizeableGetItemCount();
	}
	return retVal;
    }

    /**
     *  Set the maximum height of this menu
     */
    public void setMaximumHeight(double aHeight) {
	maximumHeight = aHeight;
	if (moreMenu != null) {
	    moreMenu.setMaximumHeight(aHeight);
	}
    }

    /**
     *  Convenience helper - get one of the sub-menus if you
     *  want to manipulate it directly (For Example - insert a static
     *  item.)
     */
    public JMoreMenu getMoreMenu() {
	return moreMenu;
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
	
	if ((myHeight + componentHeight) < maximumHeight) {
	    // Force the component to go below the moreMenu
	    super.add(c);
	}
	else {
	    int itemCount = getComponentCount();
	    Component componentToMove = null;
	    do {
		// indexes are 0 based, the standard item is 1 before
		// the more menu hence -2
		int index = getComponentIndex(moreMenu);
		componentToMove = getComponentAtIndex(index-1);
		JMenuItem item = (JMenuItem)componentToMove;
		moreMenu.insert(componentToMove,0);
		super.remove(componentToMove);
		myHeight -= componentToMove.getPreferredSize().getHeight();
		itemCount = getComponentCount();
	    } while ((myHeight+componentHeight) > maximumHeight);
	    super.add(c);
	    myHeight += componentHeight;
	}
    }

}

