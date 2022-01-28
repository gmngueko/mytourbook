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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.TourDatabase;

@Entity
public class CustomFieldValue implements Cloneable, Serializable, Comparable<Object>{

   /**
    *
    */
   private static final long          serialVersionUID      = 1664290940005681183L;

   private static final char          NL                    = UI.NEW_LINE;

   public static final int            DB_LENGTH_VALUESTRING = 128;

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
   private Float  valueFloat;

   @ManyToOne(optional = false)
   CustomField      customField;

   @ManyToOne(optional = false)
   private TourData tourData;

   @Transient
   private long     _createId    = 0;

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

   public CustomField getCustomField() {
      return customField;
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

   public Float getValueFloat() {
      return valueFloat;
   }

   public String getValueString() {
      return valueString;
   }

   @Override
   public int hashCode() {
      return Objects.hash(fieldValueId);
   }

   public void setCustomField(final CustomField customField) {
      this.customField = customField;
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

   public void setValueFloat(final Float valueFloat) {
      this.valueFloat = valueFloat;
   }

   public void setValueString(final String valueString) {
      this.valueString = valueString;
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
}
