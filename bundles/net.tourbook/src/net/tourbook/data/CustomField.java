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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.FIELD_VALIDATION;
import net.tourbook.database.TourDatabase;

@Entity
public class CustomField implements Cloneable, Serializable, Comparable<Object> {

   /**
    *
    */
   private static final long          serialVersionUID      = -8797779764544385961L;

   private static final char          NL                    = UI.NEW_LINE;

   private static final int           LENGTH_SHORT_DESCRIPTION = 20;

   public static final int            DB_LENGTH_FIELDNAME   = 80;

   public static final int            DB_LENGTH_UNIT        = 10;
   public static final int            DB_LENGTH_REFID       = 128;
   public static final int            DB_LENGTH_DESCRIPTION = 32000;
   //
   public static final String         UIFIELD_NAME          = "CustomField Name";
   public static final String         UIFIELD_DESCRIPTION   = "CustomField Description";
   public static final String         UIFIELD_REFID         = "CustomField Reference Id";
   public static final String         UIFIELD_UNIT          = "CustomField Unit";
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
   private long fieldId = TourDatabase.ENTITY_IS_NOT_SAVED;

   /**
    * Contains a CustomField name
    */
   @Basic(optional = false)
   private String fieldName;

   /**
    * Contains a CustomField unit
    */
   private String          unit;

   /**
    * Contains a CustomField reference Id (coming from ST3)
    */
   @Column(unique = true, nullable = false)
   private String          refId;

   /**
    * The type is displayed in the
    */
   @Enumerated(EnumType.STRING)
   private CustomFieldType fieldType = CustomFieldType.NONE;

   /**
    * Description for the CustomField
    */
   private String description;

   @Transient
   private long            _createId = 0;

   /**
    * Default constructor used in EJB
    */
   public CustomField() {
   }

   public CustomField(final String fieldName, final String unit, final String refId, final CustomFieldType fieldType, final String description) {
      _createId = _createCounter.incrementAndGet();

      this.fieldName = fieldName;
      this.unit = unit;
      this.refId = refId;
      this.fieldType = fieldType;
      this.description = description;
   }

   @Override
   public CustomField clone() {

      CustomField newCustomField = null;

      try {
         newCustomField = (CustomField) super.clone();
      } catch (final CloneNotSupportedException e) {
         e.printStackTrace();
      }

      return newCustomField;
   }

   @Override
   public int compareTo(final Object o) {
      if (o instanceof CustomField) {
         final CustomField fieldToCompare = (CustomField) o;
         if (this.getFieldName() == null) {
            this.setFieldName(UI.EMPTY_STRING);
         }
         if (fieldToCompare.getFieldName() == null) {
            fieldToCompare.setFieldName(UI.EMPTY_STRING);
         }
         return this.getFieldName().compareTo(fieldToCompare.getFieldName());
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
      if (!(obj instanceof CustomField)) {
         return false;
      }

      final CustomField other = (CustomField) obj;

      if (_createId == 0) {

         // sensor is from the database
         if (fieldId != other.fieldId) {
            return false;
         }

      } else {

         // CustomField is create
         if (_createId != other._createId) {
            return false;
         }
      }

      return true;
   }

   public boolean equals1(final Object obj) {

      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final CustomField other = (CustomField) obj;

      return fieldId == other.fieldId;
   }

   public boolean equals2(final Object obj) {

      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final CustomField other = (CustomField) obj;

      if (refId.compareTo(other.refId) != 0) {
         return false;
      } else if (fieldName == null && other.fieldName != null) {
         return false;
      } else if (fieldName != null && other.fieldName == null) {
         return false;
      } else if (fieldName.compareTo(other.fieldName) != 0) {
         return false;
      } else if (fieldType.compareTo(other.fieldType) != 0) {
         return false;
      } else {
         return true;
      }
   }

   public String getDescription() {
      if (description == null) {
         return UI.EMPTY_STRING;
      }
      return description;
   }

   public String getDescriptionShort() {
      if (description == null) {
         return UI.EMPTY_STRING;
      }
      if (description.length() < LENGTH_SHORT_DESCRIPTION) {
         return description;
      }
      return description.substring(0, LENGTH_SHORT_DESCRIPTION);
   }

   public long getFieldId() {
      return fieldId;
   }

   public String getFieldName() {
      if (fieldName == null) {
         return UI.EMPTY_STRING;
      }

      return fieldName;
   }

   public CustomFieldType getFieldType() {
      return fieldType;
   }

   public String getRefId() {
      return refId;
   }

   public String getUnit() {
      if (unit == null) {
         return UI.EMPTY_STRING;
      }
      return unit;
   }

   @Override
   public int hashCode() {

      return Objects.hash(fieldId, _createId);
   }

   /**
    * Checks if VARCHAR fields have the correct length
    *
    * @return Returns <code>true</code> when the data are valid and can be saved
    */
   public boolean isValidForSave() {

      FIELD_VALIDATION fieldValidation;

      /*
       * Check: Name
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            fieldName,
            DB_LENGTH_FIELDNAME,
            UIFIELD_NAME);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         fieldName = fieldName.substring(0, DB_LENGTH_FIELDNAME);
      }

      /*
       * Check: Description
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            description,
            DB_LENGTH_DESCRIPTION,
            UIFIELD_DESCRIPTION);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         description = description.substring(0, DB_LENGTH_DESCRIPTION);
      }

      /*
       * Check: Unit
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            description,
            DB_LENGTH_UNIT,
            UIFIELD_UNIT);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         unit = unit.substring(0, DB_LENGTH_UNIT);
      }

      /*
       * Check: refId
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            description,
            DB_LENGTH_REFID,
            UIFIELD_REFID);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         refId = unit.substring(0, DB_LENGTH_REFID);
      }

      return true;
   }

   public void setDescription(final String description) {
      this.description = description;
   }

   public void setFieldName(final String fieldName) {
      this.fieldName = fieldName;
   }

   public void setFieldType(final CustomFieldType fieldType) {
      this.fieldType = fieldType;
   }

   public void setRefId(final String refId) {
      this.refId = refId;
   }

   public void setUnit(final String unit) {
      this.unit = unit;
   }

   /**
    * This method is called in the MT UI in the "Tour Data" view
    */
   @Override
   public String toString() {

      return "CustomField" + NL //                                     //$NON-NLS-1$

//            + "[" + NL //                                               //$NON-NLS-1$

            + "      fieldName           = " + fieldName + NL //            //$NON-NLS-1$
            + "      fieldId             = " + fieldId + NL //              //$NON-NLS-1$
            + "      refId               = " + refId + NL //           //$NON-NLS-1$
            + "      unit                = " + unit + NL //          //$NON-NLS-1$
            + "      description         = " + getDescriptionShort() + NL //          //$NON-NLS-1$

//            + "]" + NL //                                              //$NON-NLS-1$
      ;
   }

   /**
    * Updates values from a modified {@link CustomField}
    *
    * @param modifiedField
    */
   public void updateFromModified(final CustomField modifiedField) {

      fieldName = modifiedField.fieldName;
      description = modifiedField.description;
      unit = modifiedField.unit;
      fieldType = modifiedField.fieldType;
   }

}
