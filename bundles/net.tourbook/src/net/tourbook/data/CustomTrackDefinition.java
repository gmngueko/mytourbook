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
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import net.tourbook.common.UI;
import net.tourbook.database.TourDatabase;

@Entity
public class CustomTrackDefinition implements Cloneable, Serializable, Comparable<Object> {

   /**
    *
    */
   private static final long  serialVersionUID          = -4880414084528375769L;

   public static final String DEFAULT_CUSTOM_TRACK_NAME = "default";            //$NON-NLS-1$

   private static final char  NL                        = UI.NEW_LINE;

   public static final int    DB_LENGTH_NAME            = 128;
   public static final int    DB_LENGTH_REFID           = 40;
   public static final int    DB_LENGTH_UNIT            = 40;

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
   private long   custTrackDefId = TourDatabase.ENTITY_IS_NOT_SAVED;

   /**
    * Display name of the CustomTrackDefinition
    */
   @Basic(optional = false)
   private String             name;

   /**
    * Reference Id for this CustomTrackDefinition
    * used mainly for ST3 UUID, but can alos be used by
    * other external entity.
    * used to map the CustomTrack data series too!!
    */
   @Column(unique = true, nullable = false)
   private String             refId;

   /**
    * Unit for this CustomTrackDefinition
    */
   private String             unit;

   /**
    * Unique id for manually created tour tags because the {@link #tagId} is -1 when it's not
    * persisted
    */
   @Transient
   private long   _createId = 0;

   /**
    * default constructor used in ejb
    */
   public CustomTrackDefinition() {}

   public CustomTrackDefinition(final String name, final String refId, final String unit) {
      _createId = _createCounter.incrementAndGet();

      this.name = name.trim();
      this.refId = refId;
      this.unit = unit;
   }

   @Override
   public CustomTrackDefinition clone() {
      final CustomTrackDefinition newCustomTrackDefinition = new CustomTrackDefinition();
      newCustomTrackDefinition.refId = refId;
      newCustomTrackDefinition.name = name;
      newCustomTrackDefinition.unit = unit;
      newCustomTrackDefinition.custTrackDefId = custTrackDefId;
      newCustomTrackDefinition._createId = _createId;
      return newCustomTrackDefinition;
   }

   public int compareTo(final CustomTrackDefinition customTrackDefinitionToCompare) {
      return this.getName().compareTo(customTrackDefinitionToCompare.getName());
   }

   @Override
   public int compareTo(final Object o) {
      if (o instanceof CustomTrackDefinition) {
         final CustomTrackDefinition customTrackDefinitionToCompare = (CustomTrackDefinition) o;
         return this.getName().compareTo(customTrackDefinitionToCompare.getName());
      }
      return 0;
   }

   @Override
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof CustomTrackDefinition)) {
         return false;
      }

      final CustomTrackDefinition other = (CustomTrackDefinition) obj;

      if (_createId == 0) {

         // CustomTrackDefinition is from the database
         if (custTrackDefId != other.custTrackDefId) {
            return false;
         }
      } else {

         // CustomTrackDefinition was create
         if (_createId != other._createId) {
            return false;
         }
      }

      return true;
   }

   public long getCustTrackDefId() {
      return custTrackDefId;
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
      result = prime * result + (int) (custTrackDefId ^ (custTrackDefId >>> 32));
      return result;
   }

   public void setCustTrackDefId(final long custTrackDefId) {
      this.custTrackDefId = custTrackDefId;
   }

   public void setName(final String newname) {
      name = newname;
   }

   public void setRefId(final String id) {
      refId = id;
   }

   public void setUnit(final String newunit) {
      unit = newunit;

   }

   @Override
   public String toString() {
      return "CustomTrackDefinition [dbId=" + custTrackDefId + ", name=" + name + ", refId=" + refId + ", unit=" + unit + ", _createId=" + _createId //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
            + "]";//$NON-NLS-1$
   }

}
