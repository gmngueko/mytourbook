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
package net.tourbook.data;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.FIELD_VALIDATION;
import net.tourbook.database.TourDatabase;

@Entity
public class CustomFieldValue implements Cloneable, Serializable, Comparable<Object>{

   /**
    *
    */
   private static final long          serialVersionUID      = 1664290940005681183L;

   private static final char          NL                    = UI.NEW_LINE;

   public static final int            DB_LENGTH_VALUESTRING = 128;

   public static final String             UIFIELD_VALUESTRING   = "CustomFieldValue ValueString";

   private static final String        NOT_APPLICABLE        = "N/A";                    //$NON-NLS-1$
   private static final String        ERROR                 = "Error";                  //$NON-NLS-1$
   private static final String        DATE_FORMATTING       = "dd/MM/yyyy HH:mm:ss XXX";//$NON-NLS-1$
   private static final String        TIME_FORMATTING       = "%02d:%02d:%02d";         //$NON-NLS-1$
   private static final DateTimeFormatter dateTimeFormatterEdit = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

   /**
    * Manually created CustomField or imported CustomField create a unique id to identify them,
    * saved CustomField
    * are compared with the CustomField id
    */
   private static final AtomicInteger _createCounter = new AtomicInteger();

   /*
    * DON'T USE THE FINAL KEYWORD FOR THE ID otherwise the Id cannot be set.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long fieldValueId = TourDatabase.ENTITY_IS_NOT_SAVED;

   /**
    * Tour start time
    */
   private long     tourStartTime;

   private long     tourEndTime;

   /**
    * Contains a CustomField String value
    */
   private String valueString;
   /**
    * Contains a CustomField Float value
    */
   private Float    valueFloat;

   @ManyToOne(optional = false)
   CustomField      customField;

   @ManyToOne(optional = false)
   private TourData tourData;

   @Transient
   private long     _createId    = 0;

   @Transient
   private String   valueStringTemp;

   @Transient
   private Float    valueFloatTemp;

   //below variable are only used for a merged tourData
   @Transient
   private Float   minimum      = null;

   @Transient
   private Float   maximum      = null;

   @Transient
   private Float   sum          = null;

   @Transient
   private Long    minimumDate  = null;

   @Transient
   private Long    maximumDate  = null;

   @Transient
   private Long    sumDate      = null;

   @Transient
   private Integer countNotNull = null;

   @Transient
   private Integer countNull    = null;

   /**
    * Default constructor used in EJB
    */
   public CustomFieldValue() {}

   public CustomFieldValue(final CustomField customField) {

      _createId = _createCounter.incrementAndGet();

      this.customField = customField;
   }

   public CustomFieldValue(final CustomField customField, final String valueString, final Float valueFloat) {
      _createId = _createCounter.incrementAndGet();

      this.customField = customField;
      this.valueString = valueString;
      this.valueFloat = valueFloat;
   }

   @Override
   public int compareTo(final Object o) {
      if (o instanceof CustomFieldValue) {
         final CustomFieldValue fieldToCompare = (CustomFieldValue) o;
         if (this.valueString == null) {
            if (fieldToCompare.valueString != null) {
               return -1;
            }
            if (this.valueFloat == null && fieldToCompare.valueFloat == null) {
               if (fieldToCompare.valueString == null) {
                  return 0;
               } else {
                  return -1;
               }
            }
            if (this.valueFloat != null && fieldToCompare.valueFloat == null) {
               return 1;
            }
            if (this.valueFloat == null && fieldToCompare.valueFloat != null) {
               return -1;
            }
            return this.valueFloat.compareTo(fieldToCompare.valueFloat);
         }
         if (this.valueString != null && fieldToCompare.valueString == null) {
            return 1;
         }
         return this.valueString.compareTo(fieldToCompare.valueString);
      }
      return 1;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof CustomFieldValue)) {
         return false;
      }

      final CustomFieldValue other = (CustomFieldValue) obj;

      if (_createId == 0) {

         // sensor is from the database
         if (fieldValueId != other.fieldValueId) {
            return false;
         }

      } else {

         // CustomFieldValue is create
         if (_createId != other._createId) {
            return false;
         }
      }

      return true;
   }

   public String getAverageAsString() {
      String value = UI.EMPTY_STRING;
      if (this.sum == null) {
         return NOT_APPLICABLE;
      }
      if (getSum() != null && getCountNotNull() != null && getCountNotNull() != 0) {
         final float val = getSum() / getCountNotNull();
         value = getValueAsString(val, null);
      }
      return value;
   }

   public String getAverageDateAsString() {
      String value = UI.EMPTY_STRING;
      if (this.sumDate == null) {
         return NOT_APPLICABLE;
      }
      if (getSumDate() != null && getCountNotNull() != null && getCountNotNull() != 0) {
         final long val = getSumDate() / getCountNotNull();
         value = getDateValue(val);
      } else {
         value = NOT_APPLICABLE;
      }
      return value;
   }

   public Integer getCountNotNull() {
      return countNotNull;
   }

   public Integer getCountNull() {
      return countNull;
   }

   public CustomField getCustomField() {
      return customField;
   }

   public String getDateValue( final Long inputValue) {
      String value = UI.EMPTY_STRING;

      if (this.customField.getFieldType().compareTo(CustomFieldType.FIELD_DATE) == 0) {
         try {
            final Long epochMilli = Long.valueOf(inputValue) * 1000;
            final ZoneId zoneId = ZoneId.systemDefault();

            ZonedDateTime zonedDateTime;
            final Instant startInstant = Instant.ofEpochMilli(epochMilli);
            zonedDateTime = ZonedDateTime.ofInstant(startInstant, zoneId);

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTING);
            value = zonedDateTime.format(formatter);
         } catch (final Exception e) {
            //fallback
            System.out.println(e.getMessage());
            value = ERROR;
         }

      } else {
         value = NOT_APPLICABLE;
      }
      return value;
   }

   public long getFieldValueId() {
      return fieldValueId;
   }

   public Float getMaximum() {
      return maximum;
   }

   public String getMaximumAsString() {
      if (this.maximum == null) {
         return NOT_APPLICABLE;
      }
      return getValueAsString(maximum, null);
   }

   public Long getMaximumDate() {
      return maximumDate;
   }

   public String getMaximumDateAsString() {
      return getDateValue(this.maximumDate);
   }

   public Float getMinimum() {
      return minimum;
   }

   public String getMinimumAsString() {
      if (this.minimum == null) {
         return NOT_APPLICABLE;
      }
      return getValueAsString(minimum, null);
   }

   public Long getMinimumDate() {
      return minimumDate;
   }

   public String getMinimumDateAsString() {
      return getDateValue(this.minimumDate);
   }

   public Float getSum() {
      return sum;
   }

   public String getSumAsString() {
      if (this.sum == null) {
         return NOT_APPLICABLE;
      }
      return getValueAsString(sum, null);
   }

   public Long getSumDate() {
      return sumDate;
   }

   public TourData getTourData() {
      return tourData;
   }

   public long getTourEndTime() {
      return tourEndTime;
   }

   public long getTourStartTime() {
      return tourStartTime;
   }

   public String getValue() {
      return getValueAsString(valueFloat, valueString);
   }

   private String getValueAsString(final Float inputValue, final String inputString) {
      String value = UI.EMPTY_STRING;
      if (inputString != null) {
         if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_DATE) == 0) {
            try {
               final Long epochMilli = Long.valueOf(inputString) * 1000;
               final ZoneId zoneId = ZoneId.systemDefault();

               ZonedDateTime zonedDateTime;
               final Instant startInstant = Instant.ofEpochMilli(epochMilli);
               zonedDateTime = ZonedDateTime.ofInstant(startInstant, zoneId);

               final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTING);
               value = zonedDateTime.format(formatter);
            } catch (final Exception e) {
               //fallback
               System.out.println(e.getMessage());
               value = inputString;
            }

         } else {
            value = inputString;
         }

      }
      if (inputValue != null) {
         if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_DURATION) == 0) {
            String value2 = UI.EMPTY_STRING;
            final Long sec = inputValue.longValue();
            final long second = sec % 60;
            long minute = sec / 60;
            if (minute >= 60) {
               final long hour = minute / 60;
               minute %= 60;
               value2 = String.format(TIME_FORMATTING, hour, minute, second);
            } else {
               final long hour = 0;
               value2 = String.format(TIME_FORMATTING, hour, minute, second);
            }
            value = !value.isEmpty() ? value + UI.SLASH + value2 : value2;
         } else {
            value = !value.isEmpty() ? value + UI.SLASH + String.valueOf(inputValue) : String.valueOf(inputValue);
         }
      }
      return value;
   }

   public Float getValueFloat() {
      return valueFloat;
   }

   public Float getValueFloatTemp() {
      return valueFloatTemp;
   }

   public String getValueString() {
      return valueString;
   }

   public String getValueStringTemp() {
      return valueStringTemp;
   }

   public String getValueValidated() {
      if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_STRING) == 0) {
         if(this.valueString == null) {
            return UI.EMPTY_STRING;
         }else {
            return this.valueString;
         }
      }else if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_NUMBER) == 0) {
         if(this.valueFloat == null) {
            return String.valueOf(0.0f);
         }else {
            return String.valueOf(this.valueFloat);
         }
      }else if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_DURATION) == 0) {
         long finalValue = 0;
         if(this.valueFloat != null) {
            finalValue = this.valueFloat.longValue();
         }
         String value2 = UI.EMPTY_STRING;
         final Long sec = finalValue;
         final long second = sec % 60;
         long minute = sec / 60;
         if (minute >= 60) {
            final long hour = minute / 60;
            minute %= 60;
            value2 = String.format(TIME_FORMATTING, hour, minute, second);
         } else {
            final long hour = 0;
            value2 = String.format(TIME_FORMATTING, hour, minute, second);
         }
         return value2;
      }else if (getCustomField().getFieldType().compareTo(CustomFieldType.FIELD_DATE) == 0) {
         if(this.valueString == null) {
            return LocalDateTime.now().format(dateTimeFormatterEdit);
         } else {
            try {
               final long epochMilli = Long.parseLong(this.valueString);
               final Instant tourStartInstant = Instant.ofEpochMilli(epochMilli);
               final ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
               final LocalDateTime dateTime = LocalDateTime.ofInstant(tourStartInstant, zoneId);
               return dateTime.format(dateTimeFormatterEdit);
            } catch (final Exception e) {
               System.out.println(e.toString());
               return LocalDateTime.of(1970, 1, 1, 0, 0).format(dateTimeFormatterEdit);
            }
         }
      }
      else {
         return UI.EMPTY_STRING;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(fieldValueId);
   }

   /**
    * Checks if VARCHAR fields have the correct length
    *
    * @return Returns <code>true</code> when the data are valid and can be saved
    */
   public boolean isValidForSave() {

      FIELD_VALIDATION fieldValidation;

      /*
       * Check: name
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            valueString,
            DB_LENGTH_VALUESTRING,
            UIFIELD_VALUESTRING);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         valueString = valueString.substring(0, DB_LENGTH_VALUESTRING);
      }

      return true;
   }

   public void setCountNotNull(final Integer countNotNull) {
      this.countNotNull = countNotNull;
   }

   public void setCountNotNullPlusIndex(final int index) {
      if (this.countNotNull != null) {
         this.countNotNull += index;
      } else {
         this.countNotNull = index;
      }
   }

   public void setCountNull(final Integer countNull) {
      this.countNull = countNull;
   }

   public void setCountNullPlusIndex(final int index) {
      if (this.countNull != null) {
         this.countNull += index;
      } else {
         this.countNull = index;
      }
   }

   public void setCustomField(final CustomField customField) {
      this.customField = customField;
   }

   public void setFieldValueId(final long fieldValueId) {
      this.fieldValueId = fieldValueId;
   }

   public void setMaximum(final Float maximum) {
      this.maximum = maximum;
   }

   public void setMaximumDate(final Long maximumDate) {
      this.maximumDate = maximumDate;
   }

   public void setMinimum(final Float minimum) {
      this.minimum = minimum;
   }

   public void setMinimumDate(final Long minimumDate) {
      this.minimumDate = minimumDate;
   }

   public void setSum(final Float sum) {
      this.sum = sum;
   }

   public void setSumDate(final Long sumDate) {
      this.sumDate = sumDate;
   }

   public void setTourData(final TourData tourData) {
      this.tourData = tourData;
   }

   public void setTourEndTime(final long tourEndTime) {
      this.tourEndTime = tourEndTime;
   }

   public void setTourStartTime(final long tourStartTime) {
      this.tourStartTime = tourStartTime;
   }

   public void setupDeepClone(final TourData tourDataFromClone) {

      _createId = _createCounter.incrementAndGet();

      fieldValueId = TourDatabase.ENTITY_IS_NOT_SAVED;

      tourData = tourDataFromClone;
   }

   public boolean setValue(final String value) {
      if (customField == null) {
         return false;
      }else {
         if(customField.getFieldType() == CustomFieldType.NONE) {
            return false;
         }else if(customField.getFieldType() == CustomFieldType.FIELD_STRING) {
            this.valueString = value;
            this.valueFloat = null;
            return true;
         }else if(customField.getFieldType() == CustomFieldType.FIELD_NUMBER) {
            this.valueString = null;
            try {
               this.valueFloat = Float.parseFloat(value);
            }catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_DATE) {
            this.valueFloat = null;
            try {
               final LocalDateTime dateTime = LocalDateTime.parse(value, dateTimeFormatterEdit);
               final ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
               final long epoch = dateTime.atZone(zoneId).toEpochSecond();
               this.valueString = String.valueOf(epoch);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_DURATION) {
            this.valueString = null;
            try {
               final String[] tokens = value.split(":");
               final int hours = Integer.parseInt(tokens[0]);
               final int minutes = Integer.parseInt(tokens[1]);
               final int seconds = Integer.parseInt(tokens[2]);
               this.valueFloat = (float) (3600 * hours + 60 * minutes + seconds);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         }
      }
      return false;
   }

   public void setValueFloat(final Float valueFloat) {
      this.valueFloat = valueFloat;
   }

   public void setValueFloatTemp(final Float valueFloatTemp) {
      this.valueFloatTemp = valueFloatTemp;
   }

   public void setValueString(final String valueString) {
      this.valueString = valueString;
   }

   public void setValueStringTemp(final String valueStringTemp) {
      this.valueStringTemp = valueStringTemp;
   }

   public boolean setValueTemp(final String value) {
      if (customField == null) {
         return false;
      } else {
         if (customField.getFieldType() == CustomFieldType.NONE) {
            return false;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_STRING) {
            this.valueStringTemp = value;
            this.valueFloatTemp = null;
            return true;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_NUMBER) {
            this.valueStringTemp = null;
            try {
               this.valueFloatTemp = Float.parseFloat(value);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_DATE) {
            this.valueFloatTemp = null;
            try {
               final LocalDateTime dateTime = LocalDateTime.parse(value);
               final ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
               final long epoch = dateTime.atZone(zoneId).toEpochSecond();
               this.valueStringTemp = String.valueOf(epoch);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         } else if (customField.getFieldType() == CustomFieldType.FIELD_DURATION) {
            this.valueStringTemp = null;
            try {
               final String[] tokens = value.split(UI.SYMBOL_COLON);
               final int hours = Integer.parseInt(tokens[0]);
               final int minutes = Integer.parseInt(tokens[1]);
               final int seconds = Integer.parseInt(tokens[2]);
               this.valueFloatTemp = (float) (3600 * hours + 60 * minutes + seconds);
            } catch (final Exception e) {
               System.out.println(e.getMessage());
               return false;
            }
            return true;
         }
      }
      return false;
   }

   /**
    * This method is called in the MT UI in the "Tour Data" view
    */
   @Override
   public String toString() {

      return "CustomFieldValue" + NL //                                            //$NON-NLS-1$

            + "   fieldValueId      = " + fieldValueId + NL //                  //$NON-NLS-1$
            + "   valueString       = " + valueString + NL //                  //$NON-NLS-1$
            + "   valueFloat        = " + valueFloat + NL //                  //$NON-NLS-1$
            + "   customField      = " + customField + NL //                   //$NON-NLS-1$
//          + "   tourData          = " + tourData + NL //                       //$NON-NLS-1$

      ;
   }

   public void updateStatistic(final Float newFloat, final String newString) {
      Float newValue = null;
      if (this.customField.getFieldType().compareTo(CustomFieldType.NONE) == 0) {
         if (this.countNotNull == null) {
            this.countNotNull = 0;
         }
         if (this.countNull == null) {
            this.countNull = 0;
         }
      } else if (this.customField.getFieldType().compareTo(CustomFieldType.FIELD_STRING) == 0) {
         if (newString != null) {
            newValue = (float) newString.length();
         }
      } else if (this.customField.getFieldType().compareTo(CustomFieldType.FIELD_DURATION) == 0) {
         if (newFloat != null) {
            newValue = newFloat;
         }
      } else if (this.customField.getFieldType().compareTo(CustomFieldType.FIELD_NUMBER) == 0) {
         if (newFloat != null) {
            newValue = newFloat;
         }
      } else if (this.customField.getFieldType().compareTo(CustomFieldType.FIELD_DATE) == 0) {
         if (newString != null) {
            try {
               final long newDate = Long.parseLong(newString);
               if (this.countNotNull == null) {
                  this.countNotNull = 0;
               }
               if (this.countNull == null) {
                  this.countNull = 0;
               }
               this.countNotNull += 1;
               this.countNull -= 1;

               if (this.maximumDate == null) {
                  this.maximumDate = newDate;
               }
               if (this.minimumDate == null) {
                  this.minimumDate = newDate;
               }
               if (newDate > this.maximumDate) {
                  this.maximumDate = newDate;
               }
               if (newDate < this.minimumDate) {
                  this.minimumDate = newDate;
               }

               if (this.sumDate == null) {
                  this.sumDate = newDate;
               } else {
                  this.sumDate += newDate;
               }
            } catch (final Exception e) {
               System.out.println(e.getMessage());
            }
         }
         return;
      } else {
         //FIELD_NONE
         if (this.countNotNull == null) {
            this.countNotNull = 0;
         }
         if (this.countNull == null) {
            this.countNull = 0;
         }
      }

//      if(newFloat == null && newString == null) {
//         return;
//      } else
      if (newValue != null) {
         if (this.countNotNull == null) {
            this.countNotNull = 0;
         }
         if (this.countNull == null) {
            this.countNull = 0;
         }
         this.countNotNull += 1;
         this.countNull -= 1;

         if (this.maximum == null) {
            this.maximum = newValue;
         }
         if (this.minimum == null) {
            this.minimum = newValue;
         }
         if (newValue > this.maximum) {
            this.maximum = newValue;
         }
         if (newValue < this.minimum) {
            this.minimum = newValue;
         }

         if (this.sum == null) {
            this.sum = newValue;
         } else {
            this.sum += newValue;
         }
      }
   }

}
