/*
 * Original: org.oscim.layers.marker.MarkerItem
 */
package net.tourbook.map25.layer.marker;

import net.tourbook.common.UI;
import net.tourbook.map25.layer.marker.algorithm.distance.ClusterItem;

import org.oscim.core.GeoPoint;

/**
 * Immutable class describing a GeoPoint with a Title and a Description.
 */
public class MapMarker implements ClusterItem {

   public String       title;
   public String       description;

   public GeoPoint     geoPoint;
   public MarkerSymbol markerSymbol;

   /**
    * @param title
    *           this should be <b>singleLine</b> (no <code>'\n'</code> )
    * @param description
    *           a <b>multiLine</b> description ( <code>'\n'</code> possible)
    */
   public MapMarker(final String title,
                    final String description,
                    final GeoPoint geoPoint) {

      this.title = title;
      this.description = description;

      this.geoPoint = geoPoint;
   }

   @Override
   public GeoPoint getPosition() {
      return geoPoint;
   }

   @Override
   public String toString() {

      return UI.EMPTY_STRING

            + "MapMarker" //$NON-NLS-1$

            + " geoPoint =" + geoPoint + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + " title = " + title //$NON-NLS-1$

            //				+ "description=" + description + ", "
            //				+ "markerSymbol=" + markerSymbol

            + UI.NEW_LINE;
   }

}
