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

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class CalendarUtil {

	public static Calendar firstTimeOfDay(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.set(Calendar.HOUR, cal.getMinimum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		return cal;
	}
	
	public static Calendar firstDayOfMonth(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR, cal.getMinimum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		return cal;
	}
	
	public static Calendar firstDayOfYear(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.set(Calendar.MONTH, cal.getMinimum(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR, cal.getMinimum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		return cal;
	}

	public static Calendar lastTimeOfDay(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.PM);
		cal.set(Calendar.HOUR, cal.getMaximum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		return cal;
	}
	
	public static Calendar lastDayOfMonth(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.PM);
		cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR, cal.getMaximum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		return cal;
	}
	
	public static Calendar lastDayOfYear(){
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.AM_PM, Calendar.PM);
		cal.set(Calendar.MONTH, cal.getMaximum(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR, cal.getMaximum(Calendar.HOUR));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		return cal;
	}
	
	public static int getCurrentYear(){
		Calendar cal = new GregorianCalendar();
		return cal.get(Calendar.YEAR);
	}

	public static Date getCurrentDate(){
		Calendar cal = new GregorianCalendar();
		return cal.getTime();
	}

	public boolean isGregorianCalendar(Object obj){
		return obj.getClass().isAssignableFrom(GregorianCalendar.class);
	}

	public boolean isCalendar(Object obj){
		return obj.getClass().isAssignableFrom(Calendar.class);
	}
	
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
 
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }            
    
    public static BeanUtilsBean getBeanUtils() {
    	Converter dtConverter = new Converter() {			
			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				try {
					if (value == null)
						return null;
					if (value instanceof java.util.List)
						value = ((java.util.List)value).get(0);	
					if (String.valueOf(value).length() == 0)
						return null;
					return StringUtil.CMIS_DATEFORMAT.parse(String.valueOf(value));
				} catch (ParseException e) {
					return null;
				}
			}
		};
        ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
        convertUtilsBean.deregister(Date.class);
        convertUtilsBean.register(dtConverter, Date.class);
        return new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
    }
}
