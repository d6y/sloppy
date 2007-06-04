/*
 * Copyright (C) 2001-2007 Richard Dallaway <richard@dallaway.com>
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
package com.dallaway.sloppy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test of some of the helper methods in the Sloppy GUI.
 * 
 * @author	$Author:dallaway $
 * @version $Revision:12 $ $Date:2007-05-26 17:45:22 +0100 (Sat, 26 May 2007) $
 */
public class TestSloppyGUI 
{


	/**
	 * Test of the helper used to clean up URLs entered
	 * by end users.
	 */
	@Test public void testURLCleanUP()
	{
		// A valid URL
		assertEquals("http://www.newscientist.com/", SloppyGUI.clean("http://www.newscientist.com/").toExternalForm());
		
		// Missing the protocol
		assertEquals("http://newscientist.com", SloppyGUI.clean("newscientist.com").toExternalForm());	
			
	}

	
}
