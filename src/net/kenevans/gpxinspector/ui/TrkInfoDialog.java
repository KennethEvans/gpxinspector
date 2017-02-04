package net.kenevans.gpxinspector.ui;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.utils.GpxUtils;
import net.kenevans.gpxinspector.utils.LabeledList;
import net.kenevans.gpxinspector.utils.LabeledText;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.TrackStat;
import net.kenevans.gpxtrackpointextensionsv1.ExtensionsType;
import net.kenevans.gpxtrackpointextensionsv1.TrkType;
import net.kenevans.gpxtrackpointextensionsv1.TrksegType;
import net.kenevans.gpxtrackpointextensionsv1.WptType;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class TrkInfoDialog extends InfoDialog
{
    /** String used for non-available data */
    private static final String NOT_AVAILABLE = "NA";
    private GpxTrackModel model;
    private Text nameText;
    private Text descText;
    private Text numberText;
    private Text srcText;
    private Text typeText;
    private Text segText;
    private Text trkPointsText;
    private List extensionsList;
    private Label timeLabel;
    private Label distanceLabel;
    private Label speedLabel;
    private Label elevationLabel;
    private Combo distanceCombo;
    private Combo speedCombo;
    private Combo elevationCombo;

    /**
     * Constructor. This constructor seems to be necessary so the subclass can
     * call super(Shell, int).
     * 
     * @param parent
     */
    public TrkInfoDialog(Shell parent, int style) {
        super(parent, style);
    }

    /**
     * Constructor.
     * 
     * @param parent
     */
    public TrkInfoDialog(Shell parent, GpxTrackModel model) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public TrkInfoDialog(Shell parent, int style, GpxTrackModel model) {
        super(parent, style);
        this.model = model;
        if(model != null && model.getLabel() != null) {
            setTitle(model.getLabel());
        } else {
            setTitle("Track Info");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.ui.InfoDialog#createControls(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected void createControls(Composite parent) {
        // Create the groups
        createInfoGroup(parent);

    }

    /**
     * Creates the info group.
     * 
     * @param parent
     */
    private void createInfoGroup(Composite parent) {
        Group box = new Group(parent, SWT.BORDER);
        box.setText("Track");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Name
        LabeledText labeledText = new LabeledText(box, "Name:",
            TEXT_WIDTH_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        nameText = labeledText.getText();
        nameText.setToolTipText("The GPS name of the element.");

        // Desc
        labeledText = new LabeledText(box, "Desc:", TEXT_WIDTH_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        descText = labeledText.getText();
        descText.setToolTipText(
            "A text description of the element.Holds " + "additional\n"
                + "information about the element intended for the\n"
                + "user, not the GPS.");

        // Number
        labeledText = new LabeledText(box, "Number:", TEXT_WIDTH_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        numberText = labeledText.getText();
        numberText.setToolTipText("GPS slot number for element.");

        // Source
        labeledText = new LabeledText(box, "Source:", TEXT_WIDTH_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        srcText = labeledText.getText();
        srcText.setToolTipText("Source of data. Included to give user some\n"
            + "idea of reliability and accuracy of data.");

        // Type
        labeledText = new LabeledText(box, "Type:", TEXT_WIDTH_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        typeText = labeledText.getText();
        typeText.setToolTipText("Type (classification) of element.");

        // Extensions
        LabeledList labeledList = new LabeledList(box, "Extensions:",
            TEXT_WIDTH_LARGE, LIST_ROWS);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledList.getComposite());
        extensionsList = labeledList.getList();
        extensionsList.setToolTipText("Extensions (Read only).");

        // Segments
        labeledText = new LabeledText(box, "Segments:", TEXT_WIDTH_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        segText = labeledText.getText();
        segText.setToolTipText("Number of segments.");

        // Trackpoints
        labeledText = new LabeledText(box, "Trackpoints:", TEXT_WIDTH_LARGE);
        labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        trkPointsText = labeledText.getText();
        trkPointsText.setToolTipText("Number of trackpoints by segment.");

        // Create the statistics
        createStatisticsControls(box);
    }

    /**
     * Creates the statistics group.
     * 
     * @param parent
     */
    protected void createStatisticsControls(Composite parent) {
        // Time statistics
        timeLabel = new Label(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(true, false).applyTo(timeLabel);
        timeLabel.setToolTipText("Time statistics.");

        // Distance composite
        String toolTipText = "Distance statistics.";
        Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        composite.setToolTipText(toolTipText);

        // Distance label
        Label label = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(label);
        label.setText("Distance:");
        label.setToolTipText(toolTipText);

        // Distance combo
        distanceCombo = new Combo(composite, SWT.NULL);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(distanceCombo);
        DistanceUnits[] distanceUnits = distanceUnitTypes;
        int len = distanceUnits.length;
        String[] items = new String[len];
        for(int i = 0; i < len; i++) {
            items[i] = distanceUnits[i].getName();
        }
        distanceCombo.setItems(items);
        distanceCombo.setToolTipText("Set the units for distance.");
        distanceCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                distanceUnitsIndex = distanceCombo.getSelectionIndex();
                setWidgetsFromModel();
            }
        });

        // Distance info
        distanceLabel = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(true, false).applyTo(distanceLabel);
        distanceLabel.setToolTipText(toolTipText);

        // Elevation composite
        toolTipText = "Elevation statistics.  Average is over track points.";
        composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        composite.setToolTipText(toolTipText);

        // Elevation label
        label = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(label);
        label.setText("Elevation:");
        label.setToolTipText(toolTipText);

        // Elevation combo
        elevationCombo = new Combo(composite, SWT.NULL);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(elevationCombo);
        DistanceUnits[] elevationUnits = distanceUnitTypes;
        len = elevationUnits.length;
        items = new String[len];
        for(int i = 0; i < len; i++) {
            items[i] = elevationUnits[i].getName();
        }
        elevationCombo.setItems(items);
        elevationCombo.setToolTipText("Set the units for elevation.");
        elevationCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                elevationUnitsIndex = elevationCombo.getSelectionIndex();
                setWidgetsFromModel();
            }
        });

        // Elevation info
        elevationLabel = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(true, false).applyTo(elevationLabel);
        elevationLabel.setToolTipText(toolTipText);

        // Speed composite
        toolTipText = "Speed statistics.  Average is over time.";
        composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        composite.setToolTipText(toolTipText);

        // Distance label
        label = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(label);
        label.setText("Speed:");
        label.setToolTipText(toolTipText);

        // Speed combo
        speedCombo = new Combo(composite, SWT.NULL);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(false, false).applyTo(speedCombo);
        VelocityUnits[] speedUnits = velocityUnitTypes;
        len = speedUnits.length;
        items = new String[len];
        for(int i = 0; i < len; i++) {
            items[i] = speedUnits[i].getName();
        }
        speedCombo.setItems(items);
        speedCombo.setToolTipText("Set the units for speed.");
        speedCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                speedUnitsIndex = speedCombo.getSelectionIndex();
                setWidgetsFromModel();
            }
        });

        // Speed label
        speedLabel = new Label(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
            .grab(true, false).applyTo(speedLabel);
        speedLabel.setToolTipText(toolTipText);

        // Copy statistics button
        Button button = new Button(composite, SWT.PUSH);
        button.setText("Copy");
        button.setToolTipText("Copy a summary consisting of comma-separated "
            + "values\nto the clipboard, potentially useful for spreadsheets.");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                copySummary();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setModelFromWidgets()
     */
    @Override
    protected void setModelFromWidgets() {
        TrkType trk = model.getTrack();
        Text text = descText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setDesc(LabeledText.toString(text));
        }
        text = nameText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setName(LabeledText.toString(text));
        }
        text = numberText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setNumber(LabeledText.toBigInteger(text));
        }
        text = srcText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setSrc(LabeledText.toString(text));
        }
        text = typeText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            trk.setType(LabeledText.toString(text));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.ui.InfoDialog#setWidgetsFromModel()
     */
    @Override
    protected void setWidgetsFromModel() {
        TrkType trk = model.getTrack();
        LabeledText.read(descText, trk.getDesc());
        LabeledText.read(nameText, trk.getName());
        LabeledText.read(numberText, trk.getNumber());
        LabeledText.read(srcText, trk.getSrc());
        LabeledText.read(typeText, trk.getType());
        ExtensionsType extType = trk.getExtensions();
        extensionsList.removeAll();
        if(extType == null) {
            extensionsList.add("null");
        } else {
            java.util.List<Object> objs = extType.getAny();
            for(Object obj : objs) {
                extensionsList
                    .add(obj.getClass().getName() + " " + obj.toString());
            }
        }

        // Calculated
        int intVal = trk.getTrkseg().size();
        segText.setText(String.format("%d", intVal));
        String nPointsString = "";
        int nPointsTotal = 0;
        int nPoints = 0;
        for(TrksegType seg : trk.getTrkseg()) {
            nPoints = seg.getTrkpt().size();
            nPointsString += "+" + nPoints;
            nPointsTotal += nPoints;
        }
        // Lose the first +
        if(nPointsString.length() > 0) {
            nPointsString = nPointsString.substring(1);
        }
        if(nPointsString.length() == 0 || intVal < 2) {
            nPointsString = Integer.toString(nPointsTotal);
        } else {
            nPointsString = nPointsTotal + "=" + nPointsString;
        }
        trkPointsText.setText(nPointsString);

        // Get the statistics
        TrackStat stat = GpxUtils.trkStatistics(trk);
        setStatistics(stat);
    }

    /**
     * Sets the statistics controls from the given statistics.
     */
    protected void setStatistics(TrackStat stat) {
        String statString = "";
        // Time
        String gmtString = GpxUtils.getGmtTimeString(stat.getStartTime());
        String localString = GpxUtils.getLocalTimeString(stat.getStartTime());
        statString += "Time: \tLocal: " + localString + ", GMT: " + gmtString
            + SWTUtils.LS;
        statString += "\tElapsed: ";
        double val = stat.getElapsedTime();
        statString += GpxUtils.timeString(val);
        statString += ", Moving: ";
        val = stat.getMovingTime();
        statString += GpxUtils.timeString(val);
        timeLabel.setText(statString);
        // Should not be necessary ?
        timeLabel.pack();

        // Distance
        DistanceUnits distanceUnits = distanceUnitTypes[distanceUnitsIndex];
        distanceCombo.select(distanceUnitsIndex);
        statString = "Length: ";
        val = stat.getLength();
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        distanceLabel.setText(statString);
        // Should not be necessary ?
        distanceLabel.pack();

        // Elevation
        distanceUnits = distanceUnitTypes[elevationUnitsIndex];
        elevationCombo.select(elevationUnitsIndex);
        val = stat.getAvgEle();
        statString = "Avg: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        double delEle = Double.NaN;
        val = stat.getMinEle();
        statString += ", Min: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            delEle = -val;
            statString += String.format("%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        val = stat.getMaxEle();
        statString += ", Max: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            delEle += val;
            statString += String.format("%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        val = delEle;
        statString += ", Delta: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        elevationLabel.setText(statString);
        // Should not be necessary ?
        elevationLabel.pack();

        // Speed
        VelocityUnits velocityUnits = velocityUnitTypes[speedUnitsIndex];
        speedCombo.select(speedUnitsIndex);
        val = stat.getAvgSpeed();
        statString = "Avg: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }
        val = stat.getMaxSpeed();
        statString += ", Max: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }
        val = stat.getAvgMovingSpeed();
        statString += ", Avg Moving: ";
        if(Double.isNaN(val)) {
            statString += NOT_AVAILABLE;
        } else {
            statString += String.format("%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }
        speedLabel.setText(statString);
        // Should not be necessary ?
        speedLabel.pack();
    }

    /**
     * Copies a comma-separated track summary to the clipboard. This routine
     * should be overwritten in inheriting classes to handle different models.
     */
    protected void copySummary() {
        // Get the statistics
        TrkType trk = model.getTrack();
        TrackStat stat = GpxUtils.trkStatistics(trk);
        // Tracks do not have a time, use the timestamp of the first trackpoint
        XMLGregorianCalendar xgcal = null;
        String time;
        for(TrksegType seg : trk.getTrkseg()) {
            for(WptType wpt : seg.getTrkpt()) {
                xgcal = wpt.getTime();
                if(xgcal == null) {
                    continue;
                }
            }
        }
        time = GpxUtils.getSpreadsheetTimeFromXMLGregorianCalendar(xgcal);

        copySummary(trk.getName(), time,
            trk.getDesc() == null ? "" : trk.getDesc(), stat);
    }

    /**
     * Copies a comma-separated track summary to the clipboard. This routine
     * should not have to overwritten in inheriting classes.
     * 
     * @param name The name.
     * @param desc The description.
     * @param stat The statistics.
     */
    protected void copySummary(String name, String date, String desc,
        TrackStat stat) {
        // Fill in data from arguments
        String string = name + "," + date + "," + desc;

        // Distance
        DistanceUnits distanceUnits = distanceUnitTypes[distanceUnitsIndex];
        double val = stat.getLength();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        // Time
        val = stat.getElapsedTime();
        string += "," + GpxUtils.timeString(val);
        val = stat.getMovingTime();
        string += "," + GpxUtils.timeString(val);
        // Elevation
        distanceUnits = distanceUnitTypes[elevationUnitsIndex];
        val = stat.getAvgEle();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        double delEle = Double.NaN;
        val = stat.getMinEle();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            delEle = -val;
            string += String.format(",%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        val = stat.getMaxEle();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            delEle += val;
            string += String.format(",%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }
        val = delEle;
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                distanceUnits.convertMeters(val), distanceUnits.getName());
        }

        // Speed
        VelocityUnits velocityUnits = velocityUnitTypes[speedUnitsIndex];
        val = stat.getAvgSpeed();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }
        val = stat.getMaxSpeed();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }
        val = stat.getAvgMovingSpeed();
        if(Double.isNaN(val)) {
            string += "," + NOT_AVAILABLE;
        } else {
            string += String.format(",%.2f %s",
                velocityUnits.convertMetersPerSec(val),
                velocityUnits.getName());
        }

        // Copy it to the clipboard
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] {string},
            new Transfer[] {TextTransfer.getInstance()});
        clipboard.dispose();

        // DEBUG
        // System.out.println(string);
    }

}
