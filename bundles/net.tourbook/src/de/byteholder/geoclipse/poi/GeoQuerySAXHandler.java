/*******************************************************************************
 * Copyright (C) 2005, 2024 Wolfgang Schramm and Contributors
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
package de.byteholder.geoclipse.poi;

import de.byteholder.gpx.PointOfInterest;

import java.util.ArrayList;
import java.util.List;

import net.tourbook.common.UI;
import net.tourbook.common.map.GeoPosition;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GeoQuerySAXHandler extends DefaultHandler {

   private static final String   TAG_PLACE          = "place";         //$NON-NLS-1$
   private static final String   TAG_NEAREST_PLACES = "nearestplaces"; //$NON-NLS-1$

   private static final String   ATTR_LAT           = "lat";           //$NON-NLS-1$
   private static final String   ATTR_LON           = "lon";           //$NON-NLS-1$
//	private static final String			ATTR_NAME			= "name";			//$NON-NLS-1$
   private static final String   ATTR_NAME          = "display_name";  //$NON-NLS-1$
   private static final String   ATTR_CATEGORY      = "class";         //$NON-NLS-1$
   private static final String   ATTR_INFO          = "info";          //$NON-NLS-1$
   private static final String   ATTR_TYPE          = "type";          //$NON-NLS-1$
//	private static final String			ATTR_ZOOM			= "zoom";			//$NON-NLS-1$
   private static final String   ATTR_BOUNDINGBOX   = "boundingbox";   //$NON-NLS-1$

   private List<PointOfInterest> _searchResult;

   /**
    * The named tag can be recursive, this counts the level of the named hierarchy
    */
   private int                   _poiLevel          = 0;
   private PointOfInterest       _poiRoot           = null;
   private List<PointOfInterest> _nearestPlaces     = null;

   public GeoQuerySAXHandler(final List<PointOfInterest> searchResult) {
      _searchResult = searchResult;
   }

   private static double parseDouble(final String textValue, final double defaultValue) {

      try {
         if (textValue != null) {
            return Double.parseDouble(textValue);
         } else {
            return defaultValue;
         }

      } catch (final NumberFormatException e) {
         return defaultValue;
      }
   }

//	private static int parseInt(final String textValue, final int defaultValue) {
//		try {
//			if (textValue != null) {
//				return Integer.parseInt(textValue);
//			} else {
//				return defaultValue;
//			}
//
//		} catch (final NumberFormatException e) {
//			return defaultValue;
//		}
//	}

   private PointOfInterest createPOI(final Attributes attributes) {

      final PointOfInterest poi = new PointOfInterest();

      final String attrLatitude = attributes.getValue(ATTR_LAT);
      final String attrLongitude = attributes.getValue(ATTR_LON);
      final String attrName = attributes.getValue(ATTR_NAME);
      final String attrCategory = attributes.getValue(ATTR_CATEGORY);
      final String attrInfo = attributes.getValue(ATTR_INFO);
      final String attrType = attributes.getValue(ATTR_TYPE);
//		final String attrZoom = attributes.getValue(ATTR_ZOOM);
      final String attrBoundingBox = attributes.getValue(ATTR_BOUNDINGBOX);

      poi.setPosition(new GeoPosition(parseDouble(attrLatitude, 0), parseDouble(attrLongitude, 0)));
//		poi.setRecommendedZoom(parseInt(attrZoom, 8));
      poi.setBoundingBox(attrBoundingBox);

      poi.setName(attrName == null ? UI.EMPTY_STRING : attrName);
      poi.setCategory(attrCategory == null ? UI.EMPTY_STRING : attrCategory);
      poi.setInfo(attrInfo == null ? UI.EMPTY_STRING : attrInfo);
      poi.setType(attrType == null ? UI.EMPTY_STRING : attrType);

      return poi;
   }

   @Override
   public void endElement(final String uri, final String localName, final String name) throws SAXException {

      if (name.equals(TAG_PLACE)) {

         _poiLevel--;

         if (_poiLevel == 0) {

            _poiRoot.setNearestPlaces(_nearestPlaces);

            // initialize nearest places for the next root poi
            _nearestPlaces = null;
         }
      }

   }

   @Override
   public void startElement(final String uri, final String localName, final String name, final Attributes attributes)
         throws SAXException {

      if (name.equals(TAG_PLACE)) {

         final PointOfInterest poi = createPOI(attributes);

         if (_poiLevel == 0) {
            _poiRoot = poi;
            _searchResult.add(poi);
         } else {
            _nearestPlaces.add(poi);
         }

         _poiLevel++;

      } else if (name.equals(TAG_NEAREST_PLACES)) {

         if (_nearestPlaces == null) {
            _nearestPlaces = new ArrayList<>();
         }
      }
   }
}
