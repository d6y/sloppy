/*
 * Copyright (C) 2001-2008 Richard Dallaway <richard@dallaway.com>
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

import java.text.NumberFormat;

/**
 * Conntainer for information about a particular bandwidth setting: i.e.,
 * a label such as "28.8k" and a corresponding bytes-per-second measure.
 * 
 * @author $Author$
 * @version $Revison$ $Date$
 */
public class Bandwidth
{

    /** The label shown to the user for this bandwidth setting. */
    private final String label;
    
    /** The maximum bytes per second for this bandwidth setting. */
    private final int bytesPerSecond;
    
    /** For formatting labels automaticallty. */
    private static final NumberFormat nf = NumberFormat.getInstance();
    
    static 
    {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(1);
    }
        
    
    
    /**
     * Create a new container for bandwidth settings.
     * 
     * @param kiloBitsPerSecond the kbps value (e.g., 28.8).
     * @param label the label to show the user (e.g., "28.8k").
     */
    public Bandwidth(final float kiloBitsPerSecond, final String label)
    {
       /*
         * Convert Kb into KBs
         * 
         * Here's the thinking:
         * 	2.8k = 28.8 * 1024 = 29491.2 bits per second
         * 	29491.2 / 8 = 3686.4 bytes per second
         *  Take just 7/8ths of that to allow for control bits
         *    3686.4 * 7/8 = 3225.6 bytes per second
         * 
         * NOTE: this is certainly not a good assumption for modern
         * network protocols, and we should revice this by explicitly
         * providing the bytes per second as a constructor parameter. 
         * 
         */
        this.bytesPerSecond = Math.round(((kiloBitsPerSecond * 1024.0f) / 8.0f) * (7.0f / 8.0f));
 
        this.label = label;
    }
    
    /**
     * Construct a new bandwidth setting, computing the label from the given
     * value.
     *
     * @param	kiloBitsPerSecond	Kilobits per second.
     */
    public Bandwidth(final float kiloBitsPerSecond)
    {
         /*
         * Turn the KB into a label for the user to select.
         * 
         * 512.0 KB -> "512k"
         * 28.8KB -> "28.8k"
         */
        this(kiloBitsPerSecond, nf.format(kiloBitsPerSecond) + "k");
        
     
    }

    /**
     * @return the label to show the user.
     */
    @Override
    public String toString()
    {
        return this.label;
    }

    @Override 
    public boolean equals(final Object that)
    {
        if (that instanceof Bandwidth)
        {
            return ((Bandwidth)that).bytesPerSecond == this.bytesPerSecond;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + this.bytesPerSecond;
        return hash;
    }
    
    
    /**
     * @return Bytes per second for this bandwidth setting.
     */
    public int getBytesPerSecond()
    {
        return this.bytesPerSecond;
    }
}
