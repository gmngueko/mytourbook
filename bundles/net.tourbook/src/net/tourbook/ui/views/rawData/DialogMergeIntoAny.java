package net.tourbook.ui.views.rawData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DialogMergeIntoAny extends TitleAreaDialog {

   private Text     txtDateTarget;

   private String   dateTargetString;
   private LocalDateTime dateTarget;
   private Long     targetTourId;

   private List          tourList = null;
   private ArrayList<Long> tourIdsForDate = null;
   private HashMap<Long, TourData> tourDatasForDate = new HashMap<>();
   Text                    textidSelected = null;

   public DialogMergeIntoAny(final Shell parentShell) {
      super(parentShell);
   }

   @Override
   public void create() {
      super.create();
      setTitle("Select a Tour to Merge into");
      setMessage("List of Tours for the date", IMessageProvider.INFORMATION);
   }

   private void createDateTarget(final Composite container) {
      final GridLayout gridLayout = new GridLayout();
      final Composite containerSub = new Composite(container, SWT.NONE);
      containerSub.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      containerSub.setLayout(gridLayout);

      final Label lbtDateTarget = new Label(containerSub, SWT.NONE);
      lbtDateTarget.setText("Date of Target Tour(dd/MM/YYYY)");

      final GridData dataDateTarget = new GridData();
      dataDateTarget.grabExcessHorizontalSpace = true;
      dataDateTarget.horizontalAlignment = GridData.FILL;

      txtDateTarget = new Text(containerSub, SWT.BORDER);
      txtDateTarget.setLayoutData(dataDateTarget);

      final DateTime calendar = new DateTime(containerSub, SWT.CALENDAR | SWT.BORDER);
      calendar.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final String dateString = (calendar.getDay()) + "/" + calendar.getMonth() + 1 + "/" + calendar.getYear();
            System.out.println("Calendar date selected (MM/DD/YYYY) = " + (calendar.getMonth() + 1) + "/" + calendar.getDay() + "/" + calendar
                  .getYear());
            txtDateTarget.setText(dateString);
            dateTarget = LocalDateTime.of(calendar.getYear(), calendar.getMonth() + 1, calendar.getDay(), 0, 0);
            final LocalDate dateStart = LocalDate.of(calendar.getYear(), calendar.getMonth() + 1, calendar.getDay());
            final LocalDate dateEnd = dateStart.plusDays(1);
            tourIdsForDate = TourDatabase.getAllTourIds_BetweenTwoDates(dateStart, dateEnd);
            tourDatasForDate.clear();
            if (tourList != null) {
               tourList.removeAll();
               if (tourIdsForDate != null) {
                  for (final Long element : tourIdsForDate) {
                     final TourData tempTour = TourDatabase.getTourFromDb(element);
                     String showString = "";
                     showString += tempTour.getTourStartTime();
                     showString += ";type:" + tempTour.getTourType();
                     showString += ";id:" + element;
                     tourList.add(showString);
                     tourDatasForDate.put(element, tempTour);
                  }
               } else {
                  System.out.println("tourIdsForDate (DB) is null !!!!");
               }
            } else {
               System.out.println("tourList List box is null !!!!");
            }

            if (textidSelected != null) {
               textidSelected.setText("");
            }

            targetTourId = null;
         }
      });
   }

   @Override
   protected Control createDialogArea(final Composite parent) {
      final Composite area = (Composite) super.createDialogArea(parent);
      final Composite container = new Composite(area, SWT.NONE);
      container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      final GridLayout layout = new GridLayout(1, false);
      container.setLayout(layout);

      createDateTarget(container);
      createTourList(container);

      return area;
   }

   private void createTourList(final Composite container) {
      final GridLayout gridLayout = new GridLayout(1, false);
      final Composite containerSub = new Composite(container, SWT.NONE);
      containerSub.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      containerSub.setLayout(gridLayout);

      final Label lbtTourList = new Label(containerSub, SWT.NONE);
      lbtTourList.setText("List of Tours for the date");

      final GridData dataTourList = new GridData();
      dataTourList.grabExcessHorizontalSpace = true;
      dataTourList.horizontalAlignment = GridData.FILL;

      tourList = new List(containerSub, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
      for (int loopIndex = 0; loopIndex < 9; loopIndex++) {
         tourList.add("....");
      }
      tourList.setLayoutData(dataTourList);

      final GridData dataText = new GridData();
      dataText.horizontalAlignment = SWT.FILL;
      dataText.grabExcessHorizontalSpace = true;

      textidSelected = new Text(containerSub, SWT.BORDER);
      textidSelected.setLayoutData(dataText);

      tourList.addSelectionListener(new SelectionListener() {
         @Override
         public void widgetDefaultSelected(final SelectionEvent event) {
            final int[] selectedItems = tourList.getSelectionIndices();
            String outString = "";
            for (final int selectedItem : selectedItems) {
               outString += selectedItem + " ";
            }
            System.out.println("Selected Items from default: " + outString);
         }

         @Override
         public void widgetSelected(final SelectionEvent event) {
            final int[] selectedItems = tourList.getSelectionIndices();
            String outString = "";
            String showString = "";
            for (final int selectedItem : selectedItems) {
               outString += selectedItem + " ";
               targetTourId = tourIdsForDate.get(selectedItem);
               showString += ",id:" + targetTourId;
            }
            textidSelected.setText("Selected Items: " + outString + showString);
         }
      });
   }


   public LocalDateTime getDateTarget() {
      return dateTarget;
   }

   public String getDateTargetString() {
      return dateTargetString;
   }

   public Long getTargetTourId() {
      return targetTourId;
   }

   @Override
   protected boolean isResizable() {
      return true;
   }

   @Override
   protected void okPressed() {
      saveInput();
      super.okPressed();
   }

   // save content of the Text fields because they get disposed
   // as soon as the Dialog closes
   private void saveInput() {
      dateTargetString = txtDateTarget.getText();
   }
}
