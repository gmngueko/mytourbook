/*******************************************************************************
 * Copyright (C) 2005, 2023 Wolfgang Schramm and Contributors
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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import net.tourbook.database.TourDatabase;

@Entity
public class TourBike implements Serializable {

   private static final long serialVersionUID    = 1L;

   public static final int   DB_LENGTH_NAME      = 255;

   public static final int   BIKE_ID_NOT_DEFINED = -1;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long              bikeId              = BIKE_ID_NOT_DEFINED;

   @Basic(optional = false)
   private String            name;

   /**
    * weight in kg
    */
   private float             weight;

   /**
    * type of the bike: MTB, Hollandbike, Rennvelo
    */
   private int               typeId;

   private int               frontTyreId;

   private int               rearTyreId;

   /**
    * default constructor used in ejb
    */
   public TourBike() {}

   public long getBikeId() {
      return bikeId;
   }

   public int getFrontTyreId() {
      return frontTyreId;
   }

   public String getName() {
      return name;
   }

   public int getRearTyreId() {
      return rearTyreId;
   }

   public int getTypeId() {
      return typeId;
   }

   public float getWeight() {
      return weight;
   }

   public boolean persist() {

      boolean isSaved = false;

      final EntityManager em = TourDatabase.getInstance().getEntityManager();
      final EntityTransaction ts = em.getTransaction();

      try {

         if (getBikeId() == BIKE_ID_NOT_DEFINED) {
            // entity is new
            ts.begin();
            em.persist(this);
            ts.commit();
         } else {
            // update entity
            ts.begin();
            em.merge(this);
            ts.commit();
         }

      } catch (final Exception e) {
         e.printStackTrace();
      } finally {
         if (ts.isActive()) {
            ts.rollback();
         } else {
            isSaved = true;
         }
         em.close();
      }
      return isSaved;
   }

   public void setFrontTyreId(final int frontTyre) {
      this.frontTyreId = frontTyre;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public void setRearTyreId(final int rearTyre) {
      this.rearTyreId = rearTyre;
   }

   public void setTypeId(final int typeId) {
      this.typeId = typeId;
   }

   public void setWeight(final float weight) {
      this.weight = weight;
   }

}
