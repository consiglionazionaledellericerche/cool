package it.cnr.mock;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

public class ISO8601DateFormatMethod implements TemplateMethodModelEx {
	/**
	 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
	 */
	@Override
	public Object exec(List args) throws TemplateModelException {
		Object result = null;

		if (args.size() == 1) {
			Object arg0 = args.get(0);
			if (arg0 instanceof TemplateDateModel) {
				result = format(((TemplateDateModel) arg0)
						.getAsDate());
			} else if (arg0 instanceof TemplateScalarModel) {
				result = parse(((TemplateScalarModel) arg0)
						.getAsString());
			}
		}

		return result != null ? result : "";
	}

	private static final ThreadLocal<Map<String, TimeZone>> timezones;
	static {
		timezones = new ThreadLocal<Map<String, TimeZone>>();
	}

	/**
	 * Format date into ISO format
	 * 
	 * @param isoDate
	 *            the date to format
	 * @return the ISO formatted string
	 */
	public static String format(Date isoDate) {
		// Note: always serialise to Gregorian Calendar
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(isoDate);

		StringBuilder formatted = new StringBuilder(28);
		padInt(formatted, calendar.get(Calendar.YEAR), 4);
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.MONTH) + 1, 2);
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), 2);
		formatted.append('T');
		padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), 2);
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.MINUTE), 2);
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.SECOND), 2);
		formatted.append('.');
		padInt(formatted, calendar.get(Calendar.MILLISECOND), 3);

		TimeZone tz = calendar.getTimeZone();
		int offset = tz.getOffset(calendar.getTimeInMillis());
		if (offset != 0) {
			int hours = Math.abs((offset / (60 * 1000)) / 60);
			int minutes = Math.abs((offset / (60 * 1000)) % 60);
			formatted.append(offset < 0 ? '-' : '+');
			padInt(formatted, hours, 2);
			formatted.append(':');
			padInt(formatted, minutes, 2);
		} else {
			formatted.append('Z');
		}

		return formatted.toString();
	}

	/**
	 * Parse date from ISO formatted string
	 * 
	 * @param isoDate
	 *            ISO string to parse
	 * @return the date
	 * @throws PlatformRuntimeException
	 *             if the parse failed
	 */
	public static Date parse(String isoDate) {
		Date parsed = null;

		try {
			int offset = 0;

			// extract year
			int year = Integer.parseInt(isoDate.substring(offset, offset += 4));
			if (isoDate.charAt(offset) != '-') {
				throw new IndexOutOfBoundsException(
						"Expected - character but found "
								+ isoDate.charAt(offset));
			}

			// extract month
			int month = Integer.parseInt(isoDate.substring(offset += 1,
					offset += 2));
			if (isoDate.charAt(offset) != '-') {
				throw new IndexOutOfBoundsException(
						"Expected - character but found "
								+ isoDate.charAt(offset));
			}

			// extract day
			int day = Integer.parseInt(isoDate.substring(offset += 1,
					offset += 2));
			if (isoDate.charAt(offset) != 'T') {
				throw new IndexOutOfBoundsException(
						"Expected T character but found "
								+ isoDate.charAt(offset));
			}

			// extract hours, minutes, seconds and milliseconds
			int hour = Integer.parseInt(isoDate.substring(offset += 1,
					offset += 2));
			if (isoDate.charAt(offset) != ':') {
				throw new IndexOutOfBoundsException(
						"Expected : character but found "
								+ isoDate.charAt(offset));
			}
			int minutes = Integer.parseInt(isoDate.substring(offset += 1,
					offset += 2));
			if (isoDate.charAt(offset) != ':') {
				throw new IndexOutOfBoundsException(
						"Expected : character but found "
								+ isoDate.charAt(offset));
			}
			int seconds = Integer.parseInt(isoDate.substring(offset += 1,
					offset += 2));
			int milliseconds = 0;
			if (isoDate.charAt(offset) == '.') {
				// ALF-3803 bug fix, milliseconds are optional
				milliseconds = Integer.parseInt(isoDate.substring(offset += 1,
						offset += 3));
			}

			// extract timezone
			String timezoneId;
			char timezoneIndicator = isoDate.charAt(offset);
			if (timezoneIndicator == '+' || timezoneIndicator == '-') {
				timezoneId = "GMT" + isoDate.substring(offset);
			} else if (timezoneIndicator == 'Z') {
				timezoneId = "GMT";
			} else {
				throw new IndexOutOfBoundsException(
						"Invalid time zone indicator " + timezoneIndicator);
			}

			// Get the timezone
			Map<String, TimeZone> timezoneMap = timezones.get();
			if (timezoneMap == null) {
				timezoneMap = new HashMap<String, TimeZone>(4);
				timezones.set(timezoneMap);
			}
			TimeZone timezone = timezoneMap.get(timezoneId);
			if (timezone == null) {
				timezone = TimeZone.getTimeZone(timezoneId);
				timezoneMap.put(timezoneId, timezone);
			}
			if (!timezone.getID().equals(timezoneId)) {
				throw new IndexOutOfBoundsException();
			}
			if (!timezone.getID().equals(timezoneId)) {
				throw new IndexOutOfBoundsException();
			}

			// initialize Calendar object#
			// Note: always de-serialise from Gregorian Calendar
			Calendar calendar = new GregorianCalendar(timezone);
			calendar.setLenient(false);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month - 1);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MILLISECOND, milliseconds);

			// extract the date
			parsed = calendar.getTime();
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("Failed to parse date " + isoDate, e);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Failed to parse date " + isoDate, e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to parse date " + isoDate, e);
		}

		return parsed;
	}

	/**
	 * Helper to zero pad a number to specified length
	 */
	private static void padInt(StringBuilder buffer, int value, int length) {
		String strValue = Integer.toString(value);
		for (int i = length - strValue.length(); i > 0; i--) {
			buffer.append('0');
		}
		buffer.append(strValue);
	}

}
