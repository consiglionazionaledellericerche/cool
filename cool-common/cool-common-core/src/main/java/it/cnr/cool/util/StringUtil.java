package it.cnr.cool.util;

import it.cnr.cool.exception.CoolException;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class StringUtil {
	public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
	public static final SimpleDateFormat DATETIMEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.ITALY);
	public static final String MD5 = "MD5";

	public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }		
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}	
	
	public static int romanConvert(String roman)
	{
	    int decimal = 0;

	    String romanNumeral = roman.toUpperCase();
	    for(int x = 0;x<romanNumeral.length();x++)
	    {
	        char convertToDecimal = roman.charAt(x);

	        switch (convertToDecimal)
	        {
	        case 'M':
	            decimal += 1000;
	            break;

	        case 'D':
	            decimal += 500;
	            break;

	        case 'C':
	            decimal += 100;
	            break;

	        case 'L':
	            decimal += 50;
	            break;

	        case 'X':
	            decimal += 10;
	            break;

	        case 'V':
	            decimal += 5;
	            break;

	        case 'I':
	            decimal += 1;
	            break;
	        }
	    }
	    if (romanNumeral.contains("IV"))
	    {
	        decimal-=2;
	    }
	    if (romanNumeral.contains("IX"))
	    {
	        decimal-=2;
	    }
	    if (romanNumeral.contains("XL"))
	    {
	        decimal-=10;
	    }
	    if (romanNumeral.contains("XC"))
	    {
	        decimal-=10;
	    }
	    if (romanNumeral.contains("CD"))
	    {
	        decimal-=100;
	    }
	    if (romanNumeral.contains("CM"))
	    {
	        decimal-=100;
	    }
	    return decimal;
	}	
	
	public static final SimpleDateFormat CMIS_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ITALY)
	{
	    private static final long serialVersionUID = -8275126788734707527L;

	    @Override
		public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos)
	    {            
	        final StringBuffer buf = super.format(date, toAppendTo, pos);
	        buf.insert(buf.length() - 2, ':');
	        return buf;
	    };

	    @Override
		public Date parse(String source) throws java.text.ParseException {
	        final int split = source.length() - 2;
	        return super.parse(source.substring(0, split - 1) + source.substring(split)); // replace ":" du TimeZone
	    };
	};

	public static String getMd5(InputStream is) {
		try {
			byte[] buffer = IOUtils.toByteArray(is);
			return getMd5(buffer);
		}
		catch (IOException e) {
			throw new CoolException("Error: ", e);
		}
	}

	public static String getMd5(byte [] buffer) {
		return getHexDigest(buffer, MD5);
	}

	private static String getHexDigest(byte[] buffer, String algorithmName) {
		byte[] digest = null;
		try {

			java.security.MessageDigest algorithm = java.security.MessageDigest
					.getInstance(algorithmName);
			algorithm.reset();
			algorithm.update(buffer);
			digest = algorithm.digest();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new CoolException("Algoritmo non supportato: "
					+ algorithmName, e);
		}
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			String hex = "0" + Integer.toHexString(0xFF & digest[i]);
			hexString.append(hex.substring(hex.length() - 2));
		}
		return hexString.toString();
	}

}