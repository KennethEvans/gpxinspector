package net.kenevans.gpxinspector.utils.find;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.kenevans.gpxinspector.kml.KmlUtils;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.find.FindNearOptions.Units;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class FindNearDialog extends Dialog
{
    private static final int TEXT_COLS_LONG = 50;
    private static final int TEXT_COLS_SHORT = 15;
    /**
     * The name of the document into which to place placemarks and circles. Use
     * null. It will get placed inside the currently selected element in Google
     * Earth, and giving it a name just gets confusing.
     */
    private static final String FIND_DOCUMENT_NAME = null;
    /** Flag indicating to create Source group. */
    public static int SOURCE = 1;
    /** Flag indicating to create GPX and GPSL buttons. */
    public static int FILETYPES = 1 << 1;
    /** The flags that are passed in the constructor. */
    private int flags = 0;
    /** The return value. Set to be true on OK and false on Cancel. */
    private boolean success = false;
    private FindNearOptions options;

    private Shell shell;
    private Text directoryText;
    private Text latText;
    private Text lonText;
    private Text radiusText;
    private Text filterText;
    private Combo unitsCombo;
    private Button doGpslButton;
    private Button doGpxButton;
    private Button doWptButton;
    private Button doTrkButton;
    private Button doTrimButton;
    private Button pastePlacemarkButton;
    private Button copyPlacemarkButton;
    private Button copyCircleButton;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public FindNearDialog(Shell parent, FindNearOptions options) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, options, SOURCE | FILETYPES);
    }

    /**
     * Constructor.
     * 
     * @param parent
     */
    public FindNearDialog(Shell parent, FindNearOptions options, int flags) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, options, flags);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public FindNearDialog(Shell parent, int style, FindNearOptions options,
        int flags) {
        super(parent, style);
        this.options = options;
        this.flags = flags;
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText("Find Near");
        // It can take a long time to do this so use a wait cursor
        // Probably not, though
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents();
        setWidgetsFromFindOptions();
        getParent().setCursor(null);
        waitCursor.dispose();
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return success;
    }

    /**
     * Creates the contents of the dialog.
     * 
     * @param shell
     */
    private void createContents() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        shell.setLayout(gridLayout);

        // Source group
        if((flags & SOURCE) != 0) {
            createSourceGroup(shell);
        }

        // Parameters group
        createParametersGroup(shell);

        // Dialog buttons
        createDialogButtons(shell);
    }

    private void createSourceGroup(Composite parent) {
        Group box = new Group(parent, SWT.BORDER);
        box.setText("Source");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Make a zero margin composite
        Composite composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Directory");
        GridDataFactory.fillDefaults().applyTo(label);

        directoryText = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults()
            .hint(
                new Point(SWTUtils.getTextWidth(directoryText, TEXT_COLS_LONG),
                    SWT.DEFAULT))
            .align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(directoryText);
        directoryText.setToolTipText("Full path for search directory.");
        directoryText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                    // String filename = directoryText.getText();
                    // File file = new File(filename);
                }
            }
        });
        directoryText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent ev) {
                // TODO Auto-generated method stub
            }
        });
        // Create a drop target
        DropTarget dropTarget = new DropTarget(directoryText,
            DND.DROP_COPY | DND.DROP_DEFAULT);
        dropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance(),
            FileTransfer.getInstance()});
        dropTarget.addDropListener(new DropTargetAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.dnd.DropTargetAdapter#dragEnter(org.eclipse.swt
             * .dnd.DropTargetEvent)
             */
            public void dragEnter(DropTargetEvent event) {
                if(event.detail == DND.DROP_DEFAULT) {
                    if((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd
             * .DropTargetEvent)
             */
            public void drop(DropTargetEvent event) {
                // See if it is selectionText
                if(TextTransfer.getInstance()
                    .isSupportedType(event.currentDataType)) {
                    String fileName = (String)event.data;
                    directoryText.setText(fileName);
                } else if(FileTransfer.getInstance()
                    .isSupportedType(event.currentDataType)) {
                    String[] fileNames = (String[])event.data;
                    directoryText.setText(fileNames[0]);
                }
            }
        });

        Button button = new Button(composite, SWT.PUSH);
        button.setText("Browse");
        button.setToolTipText("Select or enter a search directory.");
        GridDataFactory.fillDefaults().applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog fileDlg = new DirectoryDialog(shell, SWT.NONE);
                // fileDlg.setFilterPath(initialDirGve);
                fileDlg.setText("Select a GPX directory for Searching");
                String file = fileDlg.open();
                if(file != null) {
                    directoryText.setText(file);
                }
            }
        });
    }

    private void createParametersGroup(Composite parent) {
        Group box = new Group(parent, SWT.BORDER);
        box.setText("Parameters");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // // Make a zero margin composite
        // Composite box = new Composite(box, SWT.NONE);
        // GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
        // .grab(true, false).applyTo(box);
        // gridLayout = new GridLayout();
        // gridLayout.marginHeight = 0;
        // gridLayout.marginWidth = 0;
        // gridLayout.numColumns = 2;
        // // Note
        // gridLayout.makeColumnsEqualWidth = true;
        // box.setLayout(gridLayout);

        // Make a zero margin composite for latitude
        Composite composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Latitude");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .applyTo(label);

        latText = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, true)
            .hint(new Point(SWTUtils.getTextWidth(latText, TEXT_COLS_SHORT),
                SWT.DEFAULT))
            .applyTo(latText);
        latText.setToolTipText("Center latitude for search.");
        latText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                }
            }
        });

        // Make a zero margin composite for longitude
        composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 4;
        composite.setLayout(gridLayout);

        label = new Label(composite, SWT.NONE);
        label.setText("Longitude");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .applyTo(label);

        lonText = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, true)
            .hint(new Point(SWTUtils.getTextWidth(lonText, TEXT_COLS_SHORT),
                SWT.DEFAULT))
            .applyTo(lonText);
        lonText.setToolTipText("Center latitude for search.");
        lonText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                }
            }
        });

        // Make a zero margin composite for radius
        composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 4;
        composite.setLayout(gridLayout);

        label = new Label(composite, SWT.NONE);
        label.setText("Radius");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .applyTo(label);

        radiusText = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, true)
            .hint(new Point(SWTUtils.getTextWidth(radiusText, TEXT_COLS_SHORT),
                SWT.DEFAULT))
            .applyTo(radiusText);
        radiusText.setToolTipText("Radius of the search.");
        radiusText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                }
            }
        });

        // Make a combo for the units
        unitsCombo = new Combo(box, SWT.NULL);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(true, false).applyTo(unitsCombo);
        Units[] units = FindNearOptions.getUnitTypes();
        int len = units.length;
        String[] items = new String[len];
        for(int i = 0; i < len; i++) {
            items[i] = units[i].getName();
        }
        unitsCombo.setItems(items);
        unitsCombo.setToolTipText("Set the units for the radius");
        // unitsCombo.addSelectionListener(new SelectionAdapter() {
        // public void widgetSelected(SelectionEvent e) {
        // int paletteIndex = unitsCombo.getSelectionIndex();
        // iv.setPalette(paletteIndex);
        // }
        // });

        // Make a zero margin composite for the file filter
        composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).span(2, 1).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        label = new Label(composite, SWT.NONE);
        label.setText("File filter");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .applyTo(label);

        filterText = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, true)
            .hint(new Point(SWTUtils.getTextWidth(filterText, TEXT_COLS_LONG),
                SWT.DEFAULT))
            .applyTo(filterText);
        filterText.setToolTipText(
            "Filter the files for the search using '?' and '*'.\n"
                + "Note that it only searches files with extensions it\n"
                + "knows how to convert in any case.  Blank is no filter.");
        filterText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ex) {
                if(ex.keyCode == SWT.CR || ex.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                }
            }
        });

        // File types
        if((flags & FILETYPES) != 0) {
            doGpxButton = new Button(box, SWT.CHECK);
            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
                .grab(true, false).applyTo(doGpxButton);
            doGpxButton.setText("GPX Files");
            doGpxButton.setToolTipText("Set whether to look for GPX files.");

            doGpslButton = new Button(box, SWT.CHECK);
            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
                .grab(true, false).applyTo(doGpslButton);
            doGpslButton.setText("GPSL files");
            doGpslButton.setToolTipText("Set whether to look for GPSL files.");
        }

        doWptButton = new Button(box, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
            .grab(true, false).applyTo(doWptButton);
        doWptButton.setText("Waypoints");
        doWptButton.setToolTipText("Set whether to do waypoints.");

        doTrkButton = new Button(box, SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
            .grab(true, false).applyTo(doTrkButton);
        doTrkButton.setText("Tracks");
        doTrkButton.setToolTipText("Set whether to do tracks.");

        // Trim button
        if((flags & SOURCE) != 0) {
            // Only do the trim button if files are involved
            doTrimButton = new Button(box, SWT.CHECK);
            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
                .grab(true, false).span(2, 1).applyTo(doTrimButton);
            doTrimButton.setText("Remove Non-Matching Items");
            doTrimButton
                .setToolTipText("Remove items that do not match the find"
                    + " criteria.  Marks the file as modified.");
        }

        // Make a zero margin composite for the copy and paste buttons
        composite = new Composite(box, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);

        // Paste Placemark
        pastePlacemarkButton = new Button(composite, SWT.PUSH);
        pastePlacemarkButton.setText("Paste Placemark");
        pastePlacemarkButton.setToolTipText("Get the latitude and longitude "
            + "from a Google Earth\nPlacemark that was copied "
            + "to the clipboard.");
        GridDataFactory.fillDefaults().applyTo(pastePlacemarkButton);
        pastePlacemarkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                double[] coords = KmlUtils.coordinatesFromClipboardPlacemark();
                if(coords == null) {
                    return;
                }
                if(Double.isNaN(coords[0])) {
                    lonText.setText("");
                } else {
                    lonText.setText(String.format("%.6f", coords[0]));
                }
                if(Double.isNaN(coords[1])) {
                    latText.setText("");
                } else {
                    latText.setText(String.format("%.6f", coords[1]));
                }
            }
        });

        // Copy Placemark
        copyPlacemarkButton = new Button(composite, SWT.PUSH);
        copyPlacemarkButton.setText("Copy Placemark");
        String msg = "Copy a Google Earth Placemark to the system clipboard "
            + "with the\n"
            + "latitude and longitude of the center of the search.";
        copyPlacemarkButton.setToolTipText(msg);
        GridDataFactory.fillDefaults().applyTo(copyPlacemarkButton);
        copyPlacemarkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String name = "Find Center";
                String lat = latText.getText();
                String lon = lonText.getText();
                String ele = "0";
                KmlUtils.copyPlacemarkToClipboard(FIND_DOCUMENT_NAME, name, lat,
                    lon, ele);
            }
        });

        // Copy Circle
        copyCircleButton = new Button(composite, SWT.PUSH);
        copyCircleButton.setText("Copy Circle");
        msg = "Copy a Google Earth Placemark and Line to the system clipboard\n"
            + "marking the center and radius of the search.";
        copyCircleButton.setToolTipText(msg);
        GridDataFactory.fillDefaults().applyTo(copyCircleButton);
        copyCircleButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String name = "Find Radius";
                String lat = latText.getText();
                String lon = lonText.getText();
                String ele = "0";
                double radius = 0;
                Units units = Units.UNSPECIFIED;
                try {
                    radius = Double.parseDouble(radiusText.getText());
                } catch(NumberFormatException ex) {
                    radius = Double.NaN;
                }
                int index = unitsCombo.getSelectionIndex();
                if(index >= 0
                    && index < FindNearOptions.getUnitTypes().length) {
                    units = FindNearOptions.getUnitTypes()[index];
                }
                // Convert to meters
                radius = units.radiusInMeters(radius);
                KmlUtils.copyPlacemarkCircleToClipboard(FIND_DOCUMENT_NAME,
                    name, lat, lon, ele, radius);
            }
        });
    }

    private void createDialogButtons(Composite parent) {
        // Make a zero margin composite for the OK and Cancel buttons
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        // Change END to FILL to center the buttons
        GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL)
            .grab(true, false).applyTo(buttonComposite);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        buttonComposite.setLayout(gridLayout);

        Button button = new Button(buttonComposite, SWT.PUSH);
        button.setText("OK");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setFindOptionsFromWidgets();
                success = true;
                shell.close();
            }
        });
        shell.setDefaultButton(button);

        button = new Button(buttonComposite, SWT.PUSH);
        button.setText("Cancel");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                success = false;
                shell.close();
            }
        });
    }

    /**
     * Sets options value from the widgets
     */
    private void setFindOptionsFromWidgets() {
        if(directoryText != null) {
            options.setDirName(directoryText.getText());
        }
        try {
            options.setLatitude(Double.parseDouble(latText.getText()));
        } catch(NumberFormatException ex) {
            options.setLatitude(Double.NaN);
        }
        try {
            options.setLongitude(Double.parseDouble(lonText.getText()));
        } catch(NumberFormatException ex) {
            options.setLongitude(Double.NaN);
        }
        try {
            options.setRadius(Double.parseDouble(radiusText.getText()));
        } catch(NumberFormatException ex) {
            options.setRadius(Double.NaN);
        }

        options.setFilter(filterText.getText());

        int index = unitsCombo.getSelectionIndex();
        if(index >= 0 && index < FindNearOptions.getUnitTypes().length) {
            options.setUnits(FindNearOptions.getUnitTypes()[index]);
        } else {
            options.setUnits(Units.UNSPECIFIED);
        }

        if(doGpxButton != null) {
            options.setDoGpx(doGpxButton.getSelection());
        }
        if(doGpslButton != null) {
            options.setDoGpsl(doGpslButton.getSelection());
        }
        if(doWptButton != null) {
            options.setDoWpt(doWptButton.getSelection());
        }
        if(doTrkButton != null) {
            options.setDoTrk(doTrkButton.getSelection());
        }
        if(doTrimButton != null) {
            options.setTrim(doTrimButton.getSelection());
        }
    }

    /**
     * Sets widget values from the options.
     */
    private void setWidgetsFromFindOptions() {
        String stringVal;
        if(directoryText != null) {
            stringVal = options.getDirName();
            if(stringVal == null) {
                directoryText.setText("");
            } else {
                directoryText.setText(options.getDirName());
            }
        }
        double doubleVal = options.getLatitude();
        if(Double.isNaN(doubleVal)) {
            latText.setText("");
        } else {
            latText.setText(String.format("%.6f", doubleVal));
        }
        doubleVal = options.getLongitude();
        if(Double.isNaN(doubleVal)) {
            lonText.setText("");
        } else {
            lonText.setText(String.format("%.6f", doubleVal));
        }
        doubleVal = options.getRadius();
        if(Double.isNaN(doubleVal)) {
            radiusText.setText("");
        } else {
            radiusText.setText(String.format("%.6f", doubleVal));
        }
        filterText.setText(options.getFilter());
        Units[] units = FindNearOptions.getUnitTypes();
        int len = units.length;
        Units unit = options.getUnits();
        boolean found = false;
        for(int i = 0; i < len; i++) {
            // TODO Check if == would work here
            if(unit.getName().equals(units[i].getName())) {
                found = true;
                unitsCombo.select(i);
                break;
            }
            if(!found) {
                unitsCombo.select(-1);
            }
        }
        if(doGpxButton != null) {
            doGpxButton.setSelection(options.getDoGpx());
        }
        if(doGpslButton != null) {
            doGpslButton.setSelection(options.getDoGpsl());
        }
        doWptButton.setSelection(options.getDoWpt());
        doTrkButton.setSelection(options.getDoTrk());
        if(doTrimButton != null) {
            doTrimButton.setSelection(options.getTrim());
        }
    }

    /**
     * @return The value of options.
     */
    public FindNearOptions getOptions() {
        return options;
    }

}
