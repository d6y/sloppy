/*
 * Copyright (C) 2001 Richard Dallaway <richard@dallaway.com>
 * 
 * This file is part of Sloppy.
 * 
 * Sloppy is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Sloppy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sloppy; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.dallaway.sloppy.test;

import com.dallaway.sloppy.SloppyGUI;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test of some of the helper methods in the Sloppy GUI.
 * 
 * @author	$Author$
 * @version $Revision$ $Date$
 */
public class TestSloppyGUI extends TestCase
{


	/**
	 * Test of the helper used to clean up URLs entered
	 * by end users.
	 */
	public void testURLCleanUP()
	{
		// A valid URL
		assertEquals("http://www.newscientist.com/", SloppyGUI.clean("http://www.newscientist.com/").toExternalForm());
		
		// Missing the protocol
		assertEquals("http://newscientist.com", SloppyGUI.clean("newscientist.com").toExternalForm());	
			
	}

	/**
	 * @see junit.framework.TestCase#TestCase(java.lang.String)
	 */
	public TestSloppyGUI(String name)
	{
		super(name);
	}
	
	/**
	 * @return This test as a suite.
	 */
	public static TestSuite suite()
	{
		return new TestSuite(TestSloppyGUI.class);	
	}

	/**
	 * Runs this test suit using JUnit's text UI.
	 * 
	 * @param	args	Ignored.
	 */
	public static void main(String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}
}
