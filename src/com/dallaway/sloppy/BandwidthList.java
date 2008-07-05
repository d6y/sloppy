/*
 * Copyright (C) 2008 Richard Dallaway <richard@dallaway.com>
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

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * Conntainer for the list of {@link Bandwidth} options.
 * 
 * @author $Author: dallaway $
 * @version $Revison$ $Date: 2007-05-26 17:47:58 +0100 (Sat, 26 May 2007) $
 */
public class BandwidthList extends AbstractListModel implements ComboBoxModel
{

    // The available options:
    private List<Bandwidth> options = new ArrayList<Bandwidth>();
    
    // The selected option, defaulted in the constructor:
    private int selectedIndex ;
    
    /**
     * Construct a new list of bandwith options, selecting the one that matches
     * the supplied kbps value.
     * 
     * @param selected_kbps the value to be selected (e.g., 
     */
    public BandwidthList(final int desired_bytes_per_second)
    {
              
        float[] kiloBitesPerSecond = {9.6f, 14.4f, 28.8f, 56f, 128f, 256f, 512f};

        for(float kbps : kiloBitesPerSecond)
        {
            options.add( new Bandwidth(kbps) );
        }
        
        
	// Have something selected by default:
        selectedIndex = 3;
        assert kiloBitesPerSecond != null && kiloBitesPerSecond.length > 3;
        
        // Try to find and select the option based on the value passed in:
        for(int i=0, n=options.size(); i<n; i++)
        {
            if (options.get(i).getBytesPerSecond() == desired_bytes_per_second)
            {
                selectedIndex = i;
                break;
            }			
        }
    }
    
    /** {@inheritDoc} */
    public int getSize()
    {
        return options.size();
    }

    /** {@inheritDoc} */
    public Object getElementAt(int index)
    {
        return options.get(index);
    }

    /** {@inheritDoc} */
    public void setSelectedItem(Object anItem)
    {
        int pos = options.indexOf(anItem);
        
        if (pos != -1)
        {
            this.selectedIndex = pos;
        }
         
    }

    /** {@inheritDoc} */
    public Object getSelectedItem()
    {
        return options.get(selectedIndex);
    }

}
