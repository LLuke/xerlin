// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
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
Merlot XML Editor (http://www.merlotxml.org/)."
 
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
(http://www.merlotxml.org/)."

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
http://www.merlotxml.org/
*/




package org.merlotxml.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A LayoutManager that allows you to set the component's sizes as a percentage
 * of the total layout size. Based somewhat on StrutLayout by Matthew Phillips
 *
 * @author Kelly A. Campbell
 *
 */
public class PercentLayout implements LayoutManager2
{
    public static final int NONE = -1;
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;
    public static final int BOTH = 3;
        
    public boolean DEBUG = false;
    
    //  protected Hashtable _componentsHash;
    protected ArrayList _components;
    
    protected int _orient = HORIZONTAL;
    protected int _defaultSpace = 0;
    
    protected boolean _invalid = true;
    protected Dimension _preferredSize;
    
    
    public PercentLayout () 
    {
	//	_componentsHash = new Hashtable();
	_components = new ArrayList();
	_orient = HORIZONTAL;
	
    }
    public PercentLayout (int orientation) 
    {
	this();
	setOrientation(orientation);
	
    }
    public PercentLayout (int orientation, int space) 
    {
	this(orientation);
	_defaultSpace = space;
    }
    
    public void setDefaultSpacing(int space) 
    {
	_defaultSpace = space;
    }
    public void setOrientation(int orient) 
    {
	if (orient != HORIZONTAL && orient != VERTICAL) 
	    throw new RuntimeException("Bad orientation: "+orient);
	_orient = orient;
	
    }
    

    // LayoutManager interface
    /**
     * Adds the specified component with the specified name to the layout.
     * <p>
     * This form adds the component with default constraints which just 
     * uses the components preferred size.
     */
    public void addLayoutComponent(String name, Component comp) 
    {
	addLayoutComponent(comp,new PercentLayout.Constraints(NONE,BOTH));
    }

    /**
     * Adds the specified component to the layout, using the 
     * specified constraint object.
     */
   public  void addLayoutComponent(Component comp, Object constraints) 
    {
	if (constraints == null) {
	    constraints = new PercentLayout.Constraints(NONE,BOTH);
	}
	if (constraints instanceof PercentLayout.Constraints) {
	    //    _componentsHash.put(comp,constraints);
	    ComponentInfo ci = new ComponentInfo();
	    ci.component = comp;
	    ci.constraints = (Constraints)constraints;
	    
	    _components.add(ci);
	}
	
	       
    }
    
    /**
     * Lays out the container in the specified panel.
     */
    public void layoutContainer(Container parent) 
    {
	if (_invalid) {
	    recalculateLayout(parent);
	}
	
    }
    
    /**
     * Calculates the minimum size dimensions for the specified panel
     * given the components in the specified parent
     * container.  
     */
    public Dimension minimumLayoutSize(Container parent) 
    {
	return preferredLayoutSize(parent);
    }

    /**
     * Calculates the preferred size dimensions for the specified 
     * panel given the components in the specified parent
     * container.
     */
    public Dimension preferredLayoutSize(Container parent) 
    {
	if (_invalid) {
	    recalculateLayout(parent);
	    _invalid = false;
	}
	return new Dimension(_preferredSize);
	
    }
    
    /**
     * Removes the specified component from the layout.
     */               
    public void removeLayoutComponent(Component comp)    
    {
	
    }
    

    
     /**
     * Returns the alignment along the x axis.
     */
    public float getLayoutAlignmentX(Container target) 
    {
	return (float)0.0;
    }
    
    /**
     * Returns the alignment along the y axis.
     */
    public float getLayoutAlignmentY(Container target) 
    {
	return (float)0.0;
	
    }
    
    /**
     * Invalidates the layout, indicating that if the layout manager 
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container target) 
    {
	_invalid = true;
    }
    
    /**
     * Returns the maximum size of this component.
     */
    public Dimension maximumLayoutSize(Container target) 
    {
	return preferredLayoutSize(target);
    }
    
    protected void recalculateLayout(Container container) 
    {
	Dimension size = container.getSize();
	
	int xsum = 0;
	int ysum = 0;
	int xmax = 0;
	int ymax = 0;
	int pcttotal = 0;
	
	Iterator it;
	Insets insets = container.getInsets ();

	if (DEBUG) System.out.println("[PercentLayout] recalculateLayout");
	
	assignPreferredSizes(container);
	if (size.width <= 0 || size.height <= 0) {
	    // no size is set yet, calculate one
	    // first get the entire size so we can calculate individual sizes
	    switch (_orient) {
	    case HORIZONTAL:
		 it = _components.iterator();
		while (it.hasNext()) {
		    ComponentInfo ci = (ComponentInfo)it.next();
		    Dimension ps = ci.preferredSize;
		    xsum += ps.width;
		    if (ps.height > ymax) ymax = ps.height;
		    pcttotal += ci.constraints.percent;
		    
		}
		break;
	    case VERTICAL:
		 it = _components.iterator();
		while (it.hasNext()) {
		    ComponentInfo ci = (ComponentInfo)it.next();
		    Dimension ps = ci.preferredSize;
		    ysum += ps.height;
		    if (ps.width > xmax) xmax = ps.width;
		    pcttotal += ci.constraints.percent;
		}
		break;
	    default:
		throw new RuntimeException("Bad orientation: "+_orient);
	    }
	    // set our preferred size
	    if (xsum > 0) {
		_preferredSize = new Dimension(xsum + (_defaultSpace * (_components.size() - 1))
					       + insets.right + insets.left,
					       ymax + insets.top + insets.bottom);
	    }
	    else if (ysum > 0) {
		_preferredSize = new Dimension(xmax + insets.right + insets.left,
					       ysum + (_defaultSpace * (_components.size() - 1))
					       + insets.top + insets.bottom);
	    }
	    else {
		_preferredSize = new Dimension(insets.right + insets.left, insets.top + insets.bottom);
	    }
	    size = new Dimension(_preferredSize);
	    
	    
	    if (DEBUG) System.out.println("[PercentLayout] preferredSize = "+_preferredSize + "  size = "+size);
	    
	}
	size.width -= (insets.right + insets.left);
	size.height -= (insets.top + insets.bottom);
	
	// go through the components and calculate their bounds
	int x = 0;
	int y = 0;
	int width = 0;
	int height = 0;
	int paddedsize;
	
	if (pcttotal == 0) pcttotal = 100;
	
	 it = _components.iterator();
	while (it.hasNext()) {
	    ComponentInfo ci = (ComponentInfo)it.next();
	    Component c = ci.component;
	    Dimension ps = c.getPreferredSize();
	    Dimension ms = c.getMinimumSize();
	    Dimension xs = c.getMaximumSize();
	    
	    switch (_orient) {
	    case HORIZONTAL:
		paddedsize = size.height;
		width = ci.constraints.percent * 
		    (size.width - ((_components.size() - 1) * _defaultSpace)) 
		    / pcttotal;
		if (DEBUG) System.out.println("[PercentLayout] width = percent("+ci.constraints.percent+") / pcttotal("+pcttotal+") * size.width("+size.width+")   width="+width);
		
		int prefheight;

		if (ci.constraints.resize == VERTICAL || ci.constraints.resize == BOTH) {
		    prefheight = paddedsize;
		}
		else {
		    prefheight = ps.height;
		}
		if (DEBUG) System.out.println("[PercentLayout] prefheight = "+prefheight);
		
		if (prefheight < ps.height) {
		    if (prefheight > ms.height) {
			height = prefheight;
		    }
		    else {
			height = ms.height;
		    }
		    if (DEBUG) System.out.println("[PercentLayout] 1: height = "+height+" ps.height = "+ps.height);
		    
		}
		/* max height comes out bad
		else if (prefheight > xs.height) {
		    height = xs.height;
		    if (DEBUG) System.out.println("[PercentLayout] 2: height = xs.height = "+height);
		}
		*/
		else {
		    height = prefheight;
		    if (DEBUG) System.out.println("[PercentLayout] 3: height = prefheight = "+height);
		    
		}
		break;
	    case VERTICAL:
		paddedsize = size.width;// - (2 * _defaultSpace);
		height = ci.constraints.percent * 
		    (size.height - ((_components.size() - 1) * _defaultSpace)) 
		    / pcttotal;
		int prefwidth;

		if (ci.constraints.resize == HORIZONTAL || ci.constraints.resize == BOTH) {
		    prefwidth = paddedsize;
		}
		else {
		    prefwidth = ps.width;
		}
		
		if (prefwidth < ps.width) {
		    if (prefwidth > ms.width) {
			width = prefwidth;
		    }
		    else {
			width = ms.width;
		    }
		}
		/*
		else if (prefwidth > xs.width) {
		    width = xs.width;
		}
		*/
		else {
		    width = prefwidth;
		}
		break;
		
	    }
	    c.setBounds(x+insets.left,y+insets.top,width,height);
	    if (DEBUG) System.out.println("[PercentLayout] setting bounds on "+c+" to x:"+x+" y:"+y+" width:"+width+" height:"+height);
	    
	    if (_orient == HORIZONTAL) {
		x += (width + _defaultSpace);
	    }
	    else {
		y += (height + _defaultSpace);
	    }
	}   

	// adjust positions for insets and alignment
	//	translateComponents (insets.left, insets.top );
	
	
    }
    
    protected void assignPreferredSizes(Container c)
    {
	if (DEBUG) System.out.println("[PercentLayout] assignPreferredSizes");
	
	Iterator it = _components.iterator();
	while (it.hasNext()) {
	    ComponentInfo ci = (ComponentInfo)it.next();
	    Dimension preferredSize;
	    
	    if (ci.preferredSize == null) {
		preferredSize = ci.component.getPreferredSize();
		ci.preferredSize = preferredSize;
		
	    }
	    else {
		preferredSize = ci.preferredSize;
	    }
	    ci.width  = preferredSize.width;
	    ci.height = preferredSize.height;
	    
	}
    }
    
  /**
     Translate all component locations by xdelta, ydelta.
   */
  protected void translateComponents (int xdelta, int ydelta)
  {
    Iterator it = _components.iterator ();

    while (it.hasNext ())
    {
      ComponentInfo componentInfo = (ComponentInfo)it.next ();
      componentInfo.x += xdelta;
      componentInfo.y += ydelta;
    }
  }


    public static class Constraints 
    {
	int percent;
	int resize;
	
	public Constraints (int percent, int resize) 
	{
	    this.percent = percent;
	    this.resize  = resize;
	}
	
    }

    public class ComponentInfo extends Rectangle
    {
	Constraints constraints = null;
	Component component = null;
	Dimension preferredSize = null;
	

    }
    

}
