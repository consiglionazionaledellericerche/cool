package it.cnr.cool.util;

import java.io.Serializable;
import java.math.BigDecimal;

public class StrServ
    implements Serializable
{

    public StrServ()
    {
    }

    public static final String asGetter(String s)
    {
        if(s == null)
        {
            return "";
        } else
        {
            return "get" + firstUppercase(s);
        }
    }

    public static final String asSetter(String s)
    {
        if(s == null)
        {
            return "";
        } else
        {
            return "set" + firstUppercase(s);
        }
    }

    public static final String asString(String s)
    {
        if(s == null)
            return "";
        else
            return s;
    }

    public static final String firstUppercase(String s)
    {
        if(s == null)
            return "";
        else
            return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
    }

    public static final Class getEJSPersisterFromEJBPKName(String s)
        throws Exception
    {
        String s1 = rightOfLast(s, ".");
        String s2 = leftOfLast(s1, "Key");
        String s3 = leftOfLast(s, ".");
        Class class1 = Class.forName(s3 + ".EJSJDBCPersister" + s2 + "Bean");
        return class1;
    }

    public static final BigDecimal getMaxNum(int i)
    {
        if(i <= 0)
            return BigDecimal.ZERO;
        else
            return new BigDecimal(lpad("", i, "9"));
    }

    public static final String leftOfLast(String s, String s1)
    {
        if(s == null)
            return "";
        if(s1 == null)
            return "";
        if(s1.length() == 0)
            return s;
        if(s.lastIndexOf(s1) == -1)
            return "";
        else
            return s.substring(0, s.lastIndexOf(s1));
    }

    public static final String lpad(String s, int i)
    {
        return lpad(s, i, "0");
    }

    public static final String lpad(String s, int i, String s1)
    {
        if(s == null)
            return "";
        if(s1 == null)
            return "";
        if(s1.length() != 1)
            return s;
        if(i <= 0)
            return s;
        String s2 = "";
        for(int j = 0; j < i; j++)
            s2 = s2 + s1;

        s2 = s2 + s;
        return s2.substring(s.length(), s2.length());
    }

    public static final String replace(String s, String s1, String s2)
    {
        if(s == null)
            return "";
        String s3;
        if((s3 = s1) == null)
            return s;
        String s4 = s2 != null ? s2 : "";
        int i = 0;
        String s5 = s;
        String s6 = "";
        while((i = s5.indexOf(s3)) >= 0) 
        {
            s6 = s6 + s5.substring(0, i) + s4;
            s5 = s5.substring(i + s3.length(), s5.length());
        }
        s6 = s6 + s5;
        return s6;
    }

    public static final String rightOfLast(String s, String s1)
    {
        if(s == null)
            return "";
        if(s1 == null)
            return "";
        if(s1.length() == 0)
            return s;
        if(s.lastIndexOf(s1) == -1)
            return "";
        else
            return s.substring(s.lastIndexOf(s1) + s1.length(), s.length());
    }
}