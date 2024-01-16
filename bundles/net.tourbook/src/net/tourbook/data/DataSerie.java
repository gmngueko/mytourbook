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

import static javax.persistence.CascadeType.ALL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.FIELD_VALIDATION;
import net.tourbook.database.TourDatabase;

@Entity
public class DataSerie implements Cloneable, Serializable, Comparable<Object> {
   /**
    *
    */
   private static final long          serialVersionUID          = 7682285057192056157L;

   public static final String         DEFAULT_CUSTOM_TRACK_NAME = "default";          //$NON-NLS-1$

   private static final char          NL                        = UI.NEW_LINE;

   public static final int            DB_LENGTH_NAME            = 128;
   public static final int            DB_LENGTH_REFID           = 128;
   public static final int            DB_LENGTH_UNIT            = 40;

   public static final String         UIFIELD_NAME              = "DataSerie Name";
   public static final String         UIFIELD_REFID             = "DataSerie ReferenceId";
   public static final String         UIFIELD_UNIT              = "DataSerie Unit";

   /**
    * Manually created marker or imported marker create a unique id to identify them, saved marker
    * are compared with the marker id
    */
   private static final AtomicInteger _createCounter            = new AtomicInteger();

   /*
    * DON'T USE THE FINAL KEYWORD FOR THE ID otherwise the Id cannot be set.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long   serieId   = TourDatabase.ENTITY_IS_NOT_SAVED;

   /**
    * Display name of the DataSerie
    */
   @Basic(optional = false)
   private String name;

   /**
    * Reference Id for this DataSerie
    * used mainly for ST3 UUID, but can alos be used by
    * other external entity.
    * used to map the CustomTrack data series too!!
    */
   @Column(unique = true, nullable = false)
   private String refId;

   /**
    * Unit for this DataSerie
    */
   private String unit;

   /**
    * Contains all tours which are associated with this tag
    */
   @ManyToMany(mappedBy = "dataSeries", cascade = ALL, fetch = FetchType.LAZY)
   private final Set<TourData> tourData  = new HashSet<>();

   /**
    * Unique id for manually created tour tags because the {@link #tagId} is -1 when it's not
    * persisted
    */
   @Transient
   private long   _createId = 0;

   /**
    * default constructor used in ejb
    */
   public DataSerie() {}

   public DataSerie(final String name, final String refId, final String unit) {
      _createId = _createCounter.incrementAndGet();

      this.name = name.trim();
      this.refId = refId;
      this.unit = unit;
   }

   @Override
   public DataSerie clone() {
      final DataSerie newDataSerie = new DataSerie();
      newDataSerie.refId = refId;
      newDataSerie.name = name;
      newDataSerie.unit = unit;
      newDataSerie.serieId = serieId;
      newDataSerie._createId = _createId;
      return newDataSerie;
   }

   public DataSerie cloneNoId() {
      final DataSerie newDataSerie = new DataSerie();
      newDataSerie.refId = refId;
      newDataSerie.name = name;
      newDataSerie.unit = unit;
      return newDataSerie;
   }

   public DataSerie cloneNoSerieId() {
      final DataSerie newDataSerie = new DataSerie();
      newDataSerie.refId = refId;
      newDataSerie.name = name;
      newDataSerie.unit = unit;
      newDataSerie._createId = _createId;
      return newDataSerie;
   }

   public int compareTo(final DataSerie dataSerieToCompare) {
      if (this.getName() == null) {
         this.setName(UI.EMPTY_STRING);
      }
      if (dataSerieToCompare.getName() == null) {
         dataSerieToCompare.setName(UI.EMPTY_STRING);
      }
      return this.getName().compareTo(dataSerieToCompare.getName());
   }

   @Override
   public int compareTo(final Object o) {
      if (o instanceof DataSerie) {
         final DataSerie dataSerieToCompare = (DataSerie) o;
         if (this.getName() == null) {
            this.setName(UI.EMPTY_STRING);
         }
         if (dataSerieToCompare.getName() == null) {
            dataSerieToCompare.setName(UI.EMPTY_STRING);
         }
         return this.getName().compareTo(dataSerieToCompare.getName());
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
      if (!(obj instanceof DataSerie)) {
         return false;
      }

      final DataSerie other = (DataSerie) obj;

      if (_createId == 0) {

         // DataSerie is from the database
         if (serieId != other.serieId) {
            return false;
         }
      } else {

         // DataSerie was create
         if (_createId != other._createId) {
            return false;
         }
      }

      return true;
   }

   public String getName() {
      if (name == null) {
         return UI.EMPTY_STRING;
      }
      return name;
   }

   public String getRefId() {
      return refId;
   }

   public long getSerieId() {
      return serieId;
   }

   public Set<TourData> getTourData() {
      return tourData;
   }

   public String getUnit() {
      if (unit == null) {
         return UI.EMPTY_STRING;
      }
      return unit;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (_createId ^ (_createId >>> 32));
      result = prime * result + (int) (serieId ^ (serieId >>> 32));
      return result;
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
            name,
            DB_LENGTH_NAME,
            UIFIELD_NAME);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         name = name.substring(0, DB_LENGTH_NAME);
      }

      /*
       * Check: refId
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            refId,
            DB_LENGTH_REFID,
            UIFIELD_REFID);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         refId = refId.substring(0, DB_LENGTH_REFID);
      }

      /*
       * Check: unit
       */
      fieldValidation = TourDatabase.isFieldValidForSave(
            unit,
            DB_LENGTH_UNIT,
            UIFIELD_UNIT);

      if (fieldValidation == FIELD_VALIDATION.IS_INVALID) {
         return false;
      } else if (fieldValidation == FIELD_VALIDATION.TRUNCATE) {
         unit = unit.substring(0, DB_LENGTH_UNIT);
      }
      return true;
   }

   public void setName(final String newname) {
      this.name = newname;
   }

   public void setRefId(final String id) {
      this.refId = id;
   }

   public void setSerieId(final long serieId) {
      this.serieId = serieId;
   }

   public void setUnit(final String newunit) {
      this.unit = newunit;

   }

   public void setupDeepClone(final TourData tourDataFromClone) {

      _createId = _createCounter.incrementAndGet();

      serieId = TourDatabase.ENTITY_IS_NOT_SAVED;
      //TODO: not sure what to do below
      //tourData = tourDataFromClone;
   }

   @Override
   public String toString() {
      return "DataSerie [name=" + name + ", unit=" + unit + ",dbId=" + serieId + ", refId=" + refId + ", _createId=" + _createId //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
            + "]";//$NON-NLS-1$
   }

   public String toStringShort() {
      return "[name=" + name + "; unit=" + unit + "; refId=" + refId //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            + "]";//$NON-NLS-1$
   }

   public String toStringShortRefId() {
      return "[name=" + name + "; refId=" + refId //$NON-NLS-1$//$NON-NLS-2$
            + "]";//$NON-NLS-1$
   }

}
