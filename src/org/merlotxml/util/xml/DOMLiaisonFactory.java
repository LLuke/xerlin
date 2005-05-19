package org.merlotxml.util.xml;

/**
 * Convenience class to make getting a DOM Liaison easier
 * 
 * @author Tim McCune
 */
public class DOMLiaisonFactory {
	
	//Methods
	
	public static DOMLiaison getDOMLiaison() throws ClassNotFoundException,
	IllegalAccessException, InstantiationException
	{
		//return new org.merlotxml.util.xml.xml4j.DOMLiaison();
		return new org.merlotxml.util.xml.xerces.DOMLiaison();
	}
}
