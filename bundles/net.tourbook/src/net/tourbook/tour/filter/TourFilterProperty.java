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
package net.tourbook.tour.filter;

import java.time.LocalDateTime;
import java.time.MonthDay;

import net.tourbook.common.UI;
import net.tourbook.common.util.StatusUtil;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * UI controls for all tour filter properties
 */
public class TourFilterProperty implements Cloneable {

   private static final char  NL               = UI.NEW_LINE;

   static final LocalDateTime DEFAULT_DATE_1   = LocalDateTime.now().withDayOfMonth(1).withMonth(1);
   static final LocalDateTime DEFAULT_DATE_2   = LocalDateTime.now();
   static final MonthDay      DEFAULT_SEASON_1 = MonthDay.of(1, 1);
   static final MonthDay      DEFAULT_SEASON_2 = MonthDay.now();

   /**
    * Is <code>true</code> when this property is enabled
    */
   boolean                    isEnabled        = true;

   TourFilterFieldConfig      fieldConfig      = TourFilterManager.getFieldConfig(TourFilterFieldId.TIME_TOUR_DATE);

   /**
    * Selected operator for the property field
    */
   TourFilterFieldOperator    fieldOperator    = TourFilterFieldOperator.GREATER_THAN;

   /*
    * Field data for the different operators and field types
    */
   LocalDateTime dateTime1  = DEFAULT_DATE_1;
   LocalDateTime dateTime2  = DEFAULT_DATE_2;

   MonthDay      monthDay1  = DEFAULT_SEASON_1;
   MonthDay      monthDay2  = DEFAULT_SEASON_2;

   int           intValue1;
   int           intValue2;

   double        doubleValue1;
   double        doubleValue2;

   String        textValue1 = UI.EMPTY_STRING;
   String        textValue2 = UI.EMPTY_STRING;

   /*
    * UI controls, not all of them are used, it depends on the selected field type and operator
    */

   Button       checkboxIsPropertyEnabled;

   Combo        comboFieldName;
   Combo        comboFieldOperator;

   Composite    fieldDetailContainer;

   DateTime     uiDateTime1;
   DateTime     uiDateTime2;
   TimeDuration uiDuration1;
   TimeDuration uiDuration2;

   Combo        uiCombo_MostRecent;
   Combo        uiCombo_SeasonMonth1;
   Combo        uiCombo_SeasonMonth2;

   Spinner      uiSpinner_Number1;
   Spinner      uiSpinner_Number2;
   Spinner      uiSpinner_SeasonDay1;
   Spinner      uiSpinner_SeasonDay2;

   Text         uiText1;
   Text         uiText2;

   public TourFilterProperty() {}

   @Override
   protected TourFilterProperty clone() throws CloneNotSupportedException {

      TourFilterProperty clonedObject = null;

      try {

         clonedObject = (TourFilterProperty) super.clone();

         /*
          * Reset UI controls
          */
         disposeFieldInnerContainer();

         clonedObject.checkboxIsPropertyEnabled = null;
         clonedObject.comboFieldName = null;
         clonedObject.comboFieldOperator = null;

      } catch (final CloneNotSupportedException e) {
         StatusUtil.log(e);
      }

      return clonedObject;
   }

   void disposeFieldInnerContainer() {

      if (fieldDetailContainer != null) {

         for (final Control control : fieldDetailContainer.getChildren()) {
            control.dispose();
         }
      }

      uiDateTime1 = null;
      uiDateTime2 = null;
      uiDuration1 = null;
      uiDuration2 = null;

      uiCombo_MostRecent = null;
      uiCombo_SeasonMonth1 = null;
      uiCombo_SeasonMonth2 = null;
      uiSpinner_SeasonDay1 = null;
      uiSpinner_SeasonDay2 = null;

      uiSpinner_Number1 = null;
      uiSpinner_Number2 = null;

      uiText1 = null;
      uiText2 = null;
   }

   @Override
   public String toString() {

      return UI.EMPTY_STRING

            + "TourFilterProperty" + NL //                     //$NON-NLS-1$

            + "[" + NL //                                      //$NON-NLS-1$

            + "  fieldConfig   =" + fieldConfig + NL //        //$NON-NLS-1$
            + "  fieldOperator =" + fieldOperator + NL //      //$NON-NLS-1$

            + "]" + NL; //                                     //$NON-NLS-1$
   }

}
