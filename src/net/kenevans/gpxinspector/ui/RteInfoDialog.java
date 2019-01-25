package net.kenevans.gpxinspector.ui;

import net.kenevans.gpxtrackpointextensionv2.ExtensionsType;
import net.kenevans.gpxtrackpointextensionv2.RteType;
import net.kenevans.gpxinspector.model.GpxRouteModel;
import net.kenevans.gpxinspector.utils.LabeledList;
import net.kenevans.gpxinspector.utils.LabeledText;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class RteInfoDialog extends InfoDialog {
	private GpxRouteModel model;
	private Text nameText;
	private Text descText;
	private Text numberText;
	private Text srcText;
	private Text typeText;
	private Text wptText;
	private List extensionsList;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public RteInfoDialog(Shell parent, GpxRouteModel model) {
		// We want this to be modeless
		this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent The parent of this dialog.
	 * @param style  Style passed to the parent.
	 */
	public RteInfoDialog(Shell parent, int style, GpxRouteModel model) {
		super(parent, style);
		this.model = model;
		if (model != null && model.getLabel() != null) {
			setTitle(model.getLabel());
		} else {
			setTitle("Route Info");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.ui.InfoDialog#createControls(org.eclipse.swt
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
		LabeledText labeledText = new LabeledText(box, "Name:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		nameText = labeledText.getText();
		nameText.setToolTipText("The GPS name of the element.");

		// Desc
		labeledText = new LabeledText(box, "Desc:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		descText = labeledText.getText();
		descText.setToolTipText("A text description of the element.Holds " + "additional\n"
				+ "information about the element intended for the\n" + "user, not the GPS.");

		// Number
		labeledText = new LabeledText(box, "Number:", TEXT_WIDTH_LARGE);
		// labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		numberText = labeledText.getText();
		numberText.setToolTipText("GPS slot number for element.");

		// Source
		labeledText = new LabeledText(box, "Source:", TEXT_WIDTH_LARGE);
		// labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		srcText = labeledText.getText();
		srcText.setToolTipText(
				"Source of data. Included to give user some\n" + "idea of reliability and accuracy of data.");

		// Type
		labeledText = new LabeledText(box, "Type:", TEXT_WIDTH_LARGE);
		// labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		typeText = labeledText.getText();
		typeText.setToolTipText("Type (classification) of element.");

		// Waypoints
		labeledText = new LabeledText(box, "Waypoints:", TEXT_WIDTH_LARGE);
		labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		wptText = labeledText.getText();
		wptText.setToolTipText("Number of waypoints.");

		// Extensions
		LabeledList labeledList = new LabeledList(box, "Extensions:", TEXT_WIDTH_LARGE, LIST_ROWS);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledList.getComposite());
		extensionsList = labeledList.getList();
		extensionsList.setToolTipText("Extensions (Read only).");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.ui.InfoDialog#setModelFromWidgets()
	 */
	@Override
	protected void setModelFromWidgets() {
		RteType rte = model.getRoute();
		Text text = descText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			rte.setDesc(LabeledText.toString(text));
		}
		text = nameText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			rte.setName(LabeledText.toString(text));
		}
		text = numberText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			rte.setNumber(LabeledText.toBigInteger(text));
		}
		text = srcText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			rte.setSrc(LabeledText.toString(text));
		}
		text = typeText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			rte.setType(LabeledText.toString(text));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.ui.InfoDialog#setWidgetsFromModel()
	 */
	@Override
	protected void setWidgetsFromModel() {
		RteType rte = model.getRoute();
		LabeledText.read(descText, rte.getDesc());
		LabeledText.read(nameText, rte.getName());
		LabeledText.read(numberText, rte.getNumber());
		LabeledText.read(srcText, rte.getSrc());
		LabeledText.read(typeText, rte.getType());
		ExtensionsType extType = rte.getExtensions();
		extensionsList.removeAll();
		if (extType == null) {
			extensionsList.add("null");
		} else {
			java.util.List<Object> objs = extType.getAny();
			for (Object obj : objs) {
				extensionsList.add(obj.getClass().getName() + " " + obj.toString());
			}
		}

		// Calculated
		int intVal = rte.getRtept().size();
		wptText.setText(String.format("%d", intVal));
	}

	/**
	 * @return The value of model.
	 */
	public GpxRouteModel getModel() {
		return model;
	}

}
