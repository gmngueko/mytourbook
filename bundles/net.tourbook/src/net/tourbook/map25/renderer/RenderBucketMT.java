/*
 * Copyright 2012, 2013 Hannes Janetzek
 * Copyright 2016 Stephan Leuschner
 * Copyright 2017 Luca Osten
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.tourbook.map25.renderer;

import gnu.trove.list.array.TShortArrayList;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.bucket.RenderBuckets;
import org.oscim.renderer.bucket.VertexData;
import org.oscim.utils.pool.Inlist;

public abstract class RenderBucketMT extends Inlist<RenderBucketMT> {

   public static final byte LINE = 0;
//   public static final byte   TEXLINE   = 1;
//   public static final byte   POLYGON   = 2;
//   public static final byte   MESH      = 3;
//   public static final byte   EXTRUSION = 4;
//   public static final byte   HAIRLINE  = 5;
//   public static final byte   SYMBOL    = 6;
//   public static final byte   BITMAP    = 7;
//   public static final byte   CIRCLE    = 8;

   static final VertexData      EMPTY = new VertexData();

   public final byte            type;

   /**
    * Drawing order from bottom to top.
    */
   int                          level;

   /**
    * Number of vertices for this layer.
    */
   protected int                numVertices;
   protected int                numIndices;

   /**
    * Temporary list of vertex data.
    */
   protected final VertexDataMT vertexItems;
   protected final VertexData   indiceItems;

   /**
    * Contains the vertices for direction arrows multiplied with {@link MapRenderer#COORD_SCALE}:
    * <p>
    *
    * <pre>
    * x1
    * y2
    * z1
    *
    * x2
    * y2
    * z2
    *
    * ...
    * </pre>
    */
   protected TShortArrayList    directionArrow_XYZPositions;

   /**
    * Barycentric coordinates are simply (1, 0, 0), (0, 1, 0) and (0, 0, 1) for the three
    * triangle vertices (the order does not really matter, which makes packing into triangle strips
    * potentially easier).
    * 
    * Source: https://stackoverflow.com/questions/137629/how-do-you-render-primitives-as-wireframes-in-opengl#answer-33004265
    */
   protected TShortArrayList    colorCoords;

   final boolean                quads;

   protected int                vertexOffset;               // in bytes
   protected int                indiceOffset;               // in bytes

   protected RenderBucketMT(final byte type, final boolean indexed, final boolean quads) {

      this.type = type;

      vertexItems = new VertexDataMT();

      if (indexed) {
         indiceItems = new VertexData();
      } else {
         indiceItems = EMPTY;
      }

      this.quads = quads;

      directionArrow_XYZPositions = new TShortArrayList();
      colorCoords = new TShortArrayList();
   }

   /**
    * Clear all resources.
    */
   protected void clear() {

      vertexItems.dispose();
      indiceItems.dispose();

      numVertices = 0;
      numIndices = 0;
   }

   protected void compile(final ShortBuffer vboData, final ShortBuffer iboData, final ByteBuffer colorBuffer) {

      compileVertexItems(vboData, colorBuffer);

      if (iboData != null) {
         compileIndicesItems(iboData);
      }
   }

   protected void compileIndicesItems(final ShortBuffer iboData) {

      /* keep offset of layer data in ibo */
      if (indiceItems == null || indiceItems.empty()) {
         return;
      }

      indiceOffset = iboData.position() * RenderBuckets.SHORT_BYTES;
      indiceItems.compile(iboData);
   }

   protected void compileVertexItems(final ShortBuffer vboBuffer, final ByteBuffer colorBuffer) {

      // Keep offset of layer data in vbo
      vertexOffset = vboBuffer.position() * RenderBuckets.SHORT_BYTES;

      vertexItems.compile(vboBuffer, colorBuffer);
   }

   /**
    * Start position in ibo for this bucket (in bytes)
    */
   public int getIndiceOffset() {
      return indiceOffset;
   }

   /**
    * For line- and polygon-buckets this is the offset
    * of VERTICES in its bucket.vbo.
    * For all other types it is the byte offset in vbo.
    * FIXME - always use byte offset?
    */
   public int getVertexOffset() {
      return vertexOffset;
   }

   /**
    * Final preparation of content before compilation
    * for stuff that should not be done on render-thread.
    */
   protected void prepare() {

   }

   public void setLevel(final int level) {
      this.level = level;
   }

   public void setVertexOffset(final int offset) {
      this.vertexOffset = offset;
   }
}
