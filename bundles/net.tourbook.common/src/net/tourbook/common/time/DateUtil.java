/*******************************************************************************
 * Copyright (C) 2022 Gervais-Martial Ngueko
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package net.tourbook.common.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Duration;

public final class DateUtil {
//TODO: convert to java.time and remove Calendar and SimpleDateFormat.


   private static final HashMap<String, String> DATE_FORMAT_REGEXPS = new HashMap<>() {
      /**
       *
       */
      private static final long serialVersionUID = 2121681431495334613L;

      {
         put("^\\d{8}$", "yyyyMMdd");
         put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
         put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
         put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
         put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
         put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
         put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
         put("^\\d{12}$", "yyyyMMddHHmm");
         put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
         put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
         put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
         put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
         put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
         put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
         put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
         put("^\\d{14}$", "yyyyMMddHHmmss");
         put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
         put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
         put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
         put("^\\d{4}-\\d{1,2}-\\d{1,2}t\\d{1,2}:\\d{2}:\\d{2}\\sutc[+-]\\d{2}:\\d{2}$", "yyyy-MM-dd'T'HH:mm:ss 'UTC'XXX");
         put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
         put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
         put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
         put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
      }
   };

   private DateUtil() {
      // Utility class, hide the constructor.
   }

   /**
    * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
    * format is unknown. You can simply extend DateUtil with more formats if needed.
    *
    * @param dateString
    *           The date string to determine the SimpleDateFormat pattern for.
    * @return The matching SimpleDateFormat pattern, or null if format is unknown.
    * @see SimpleDateFormat
    */
   public static String determineDateFormat(final String dateString) {
      for (final String regexp : DATE_FORMAT_REGEXPS.keySet()) {
         if (dateString.toLowerCase().matches(regexp)) {
            return DATE_FORMAT_REGEXPS.get(regexp);
         }
      }
      return null; // Unknown format.
   }

   /**
    * Checks whether the actual date of the given date string is valid. This makes use of the
    * {@link DateUtil#determineDateFormat(String)} to determine the SimpleDateFormat pattern to be
    * used for parsing.
    *
    * @param dateString
    *           The date string.
    * @return True if the actual date of the given date string is valid.
    */
   public static boolean isValidDate(final String dateString) {
      try {
         parse(dateString);
         return true;
      } catch (final ParseException e) {
         return false;
      }
   }

   /**
    * Checks whether the actual date of the given date string is valid based on the given date
    * format pattern.
    *
    * @param dateString
    *           The date string.
    * @param dateFormat
    *           The date format pattern which should respect the SimpleDateFormat rules.
    * @return True if the actual date of the given date string is valid based on the given date
    *         format pattern.
    * @see SimpleDateFormat
    */
   public static boolean isValidDate(final String dateString, final String dateFormat) {
      try {
         parse(dateString, dateFormat);
         return true;
      } catch (final ParseException e) {
         return false;
      }
   }

   /**
    * Parse the given duration string to to check it is a valid HH:mm:ss or mm:ss Strig.
    *
    * @param dateString
    *           The date string to be parsed to date object.
    * @return true or false.
    */
   public static boolean isValideDuration(final String durationString) {
      final Pattern pattern = Pattern.compile("^\\d*:\\d{1,2}:?\\d{1,2}$", Pattern.CASE_INSENSITIVE);
      final Matcher matcher = pattern.matcher(durationString);
      final boolean matchFound = matcher.find();
      if(matchFound) {
        System.out.println("Match found");
        return true;
      } else {
        System.out.println("Match not found");
        return false;
      }
   }

   /**
    * Parse the given date string to date object and return a date instance based on the given
    * date string. This makes use of the {@link DateUtil#determineDateFormat(String)} to determine
    * the SimpleDateFormat pattern to be used for parsing.
    *
    * @param dateString
    *           The date string to be parsed to date object.
    * @return The parsed date object.
    * @throws ParseException
    *            If the date format pattern of the given date string is unknown, or if
    *            the given date string or its actual date is invalid based on the date format
    *            pattern.
    */
   public static Date parse(final String dateString) throws ParseException {
      final String dateFormat = determineDateFormat(dateString);
      if (dateFormat == null) {
         throw new ParseException("Unknown date format.", 0);
      } else {
         System.out.println("Known date format found:" + dateFormat);
      }
      return parse(dateString, dateFormat);
   }

   /**
    * Validate the actual date of the given date string based on the given date format pattern and
    * return a date instance based on the given date string.
    *
    * @param dateString
    *           The date string.
    * @param dateFormat
    *           The date format pattern which should respect the SimpleDateFormat rules.
    * @return The parsed date object.
    * @throws ParseException
    *            If the given date string or its actual date is invalid based on the
    *            given date format pattern.
    * @see SimpleDateFormat
    */
   public static Date parse(final String dateString, final String dateFormat) throws ParseException {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
      simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
      return simpleDateFormat.parse(dateString);
   }


   /**
    * Parse the given duration string .
    *
    * @param dateString
    *           The duration string to be parsed to duration (org.joda.Duration) object.
    * @return The parsed duration object.
    * @throws ParseException
    *            If the duration format pattern of the given duration string is unknown, or if
    *            the given duration string or its actual duration is invalid based on the duration
    *            format
    *            pattern.
    */
   public static Duration parseDuration(final String durationString) throws ParseException{
      //String input = "50:00";  // Or "50:00:00" (fifty hours, either way)

      final String[] parts = durationString.split ( ":" );
      Duration d = new Duration(0);
      try {
         if (parts.length == 3) {
            final int hours = Integer.parseInt(parts[0]);
            final int minutes = Integer.parseInt(parts[1]);
            final int seconds = Integer.parseInt(parts[2]);
            //d = d..plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            d = d.plus(hours * 3600 * 1000).plus(seconds * 1000).plus(minutes * 60 * 1000);
         } else if (parts.length == 2) {
            final int minutes = Integer.parseInt(parts[0]);
            final int seconds = Integer.parseInt(parts[1]);
            //d = d.plusMinutes(minutes).plusSeconds(seconds);
            d = d.plus(seconds * 1000).plus(minutes * 60 * 1000);
         } else {
            System.out.println("ERROR - Unexpected input.");
            throw new ParseException("ERROR - Unexpected input.", 1);
         }
      }catch (final Exception e) {
         System.out.println(e.getMessage());
         throw new ParseException(e.getMessage(), 1);
      }
      return d;
   }

   /**
    * Convert the given date to a Calendar object. The TimeZone will be derived from the local
    * operating system's timezone.
    *
    * @param date
    *           The date to be converted to Calendar.
    * @return The Calendar object set to the given date and using the local timezone.
    */
   public static Calendar toCalendar(final Date date) {
      final Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.setTime(date);
      return calendar;
   }


   /**
    * Convert the given date to a Calendar object with the given timezone.
    *
    * @param date
    *           The date to be converted to Calendar.
    * @param timeZone
    *           The timezone to be set in the Calendar.
    * @return The Calendar object set to the given date and timezone.
    */
   public static Calendar toCalendar(final Date date, final TimeZone timeZone) {
      final Calendar calendar = toCalendar(date);
      calendar.setTimeZone(timeZone);
      return calendar;
   }

}
