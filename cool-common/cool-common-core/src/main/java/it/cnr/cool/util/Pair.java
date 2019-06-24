/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.util;

import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;

/**
 * Utility class for containing two things that aren't like each other
 */
public final class Pair<F extends Comparable<F> & Serializable, S extends Serializable> implements Serializable, Comparable<Pair<F, S>>
{
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Pair NULL_PAIR = new Pair(null, null);
    
    private static final long serialVersionUID = -7406248421185630612L;

    /**
     * The first member of the pair.
     */
    @Expose(serialize=true)
    private F first;
    
    /**
     * The second member of the pair.
     */
    @Expose(serialize=true)
    private S second;
    
    /**
     * Make a new one.
     * 
     * @param first The first member.
     * @param second The second member.
     */
    public Pair(F first, S second)
    {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Get the first member of the tuple.
     * @return The first member.
     */
    public F getFirst()
    {
        return first;
    }
    
    /**
     * Get the second member of the tuple.
     * @return The second member.
     */
    public S getSecond()
    {
        return second;
    }
    
    public void setFirst(F first)
    {
        this.first = first;
    }
    
    public void setSecond(S second)
    {
        this.second = second;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || !(other instanceof Pair<?, ?>))
        {
            return false;
        }
        Pair<?, ?> o = (Pair<?, ?>)other;
        return nullSafeEquals(this.first, o.first) &&
               nullSafeEquals(this.second, o.second);
    }
    
    public static boolean nullSafeEquals(Object left, Object right)
    {
        return left == right || left != null && right != null && left.equals(right);
    }
    
    @Override
    public int hashCode()
    {
        return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }

    /**
     * Ensure that previously-serialized instances don't fail due to the member name change.
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException
    {
        GetField fields = is.readFields();
        if (fields.defaulted("first"))
        {
            // This is a pre-V3.3
            this.first = (F) fields.get("fFirst", null);
            this.second = (S) fields.get("fSecond", null);
        }
        else
        {
            this.first = (F) fields.get("first", null);
            this.second = (S) fields.get("second", null);
        }
    }

	@Override
	public int compareTo(Pair<F, S> arg0) {		
		return first.compareTo(arg0.getFirst());
	}
}