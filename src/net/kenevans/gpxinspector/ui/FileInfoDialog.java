package net.kenevans.gpxinspector.ui;

import java.io.File;
import java.util.Date;

import net.kenevans.gpxtrackpointextensionv2.BoundsType;
import net.kenevans.gpxtrackpointextensionv2.CopyrightType;
import net.kenevans.gpxtrackpointextensionv2.EmailType;
import net.kenevans.gpxtrackpointextensionv2.ExtensionsType;
import net.kenevans.gpxtrackpointextensionv2.GpxType;
import net.kenevans.gpxtrackpointextensionv2.LinkType;
import net.kenevans.gpxtrackpointextensionv2.MetadataType;
import net.kenevans.gpxtrackpointextensionv2.PersonType;
import net.kenevans.gpxinspector.model.GpxFileModel;
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

public class FileInfoDialog extends InfoDialog {
	private GpxFileModel model;
	private Text nameText;
	private Text dateText;
	private Text sizeText;
	private Text creatorText;
	private Text versionText;
	private Text authorText;
	private Text descText;
	private Text keywordsText;
	private Text metadataNameText;
	private Text timeText;
	private Text licenseText;
	private Text yearText;
	private Text minLatText;
	private Text maxLatText;
	private Text minLonText;
	private Text maxLonText;
	private Text authorNameText;
	private Text domainText;
	private Text idText;
	private Text linkHrefText;
	private Text linkTextText;
	private Text linkTypeText;
	private List extensionsList;
	private List metadataExtensionsList;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public FileInfoDialog(Shell parent, GpxFileModel model) {
		// We want this to be modeless
		this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent The parent of this dialog.
	 * @param style  Style passed to the parent.
	 */
	public FileInfoDialog(Shell parent, int style, GpxFileModel model) {
		super(parent, style);
		this.model = model;
		if (model != null && model.getFile() != null && model.getFile().getName() != null) {
			setTitle(model.getFile().getName());
		} else {
			setTitle("File Info");
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
		createFileGroup(parent);
		createGpxGroup(parent);
		createMetadataGroup(parent);
	}

	/**
	 * Creates the file group.
	 * 
	 * @param parent
	 */
	private void createFileGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("File");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Name
		LabeledText labeledText = new LabeledText(box, "Name:", TEXT_WIDTH_LARGE);
		labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).grab(true, false).applyTo(labeledText.getComposite());
		nameText = labeledText.getText();
		nameText.setToolTipText("Name.");

		// Size
		labeledText = new LabeledText(box, "Size (bytes):", TEXT_WIDTH_LARGE);
		labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).grab(true, false).applyTo(labeledText.getComposite());
		sizeText = labeledText.getText();
		sizeText.setToolTipText("Size.");

		// Date
		labeledText = new LabeledText(box, "Last Modified:", TEXT_WIDTH_LARGE);
		labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).grab(true, false).applyTo(labeledText.getComposite());
		dateText = labeledText.getText();
		dateText.setToolTipText("Last modified date.");

		// Dirty
		if (model.isDirty()) {
			// Really only need a label here
			labeledText = new LabeledText(box, "* File has been modified but not saved", TEXT_WIDTH_LARGE);
			labeledText.getText().setEditable(false);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
			labeledText.getText().setToolTipText("Indicates if the file has been modified.");
		}
	}

	/**
	 * Creates the GPX group.
	 * 
	 * @param parent
	 */
	private void createGpxGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("GPX");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Creator
		LabeledText labeledText = new LabeledText(box, "Creator:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		creatorText = labeledText.getText();
		creatorText.setToolTipText("All GPX files must include the name of the application or\n"
				+ "web service which created the file. This is to assist\n"
				+ "developers in tracking down the source of a mal-formed GPX\n"
				+ "document, and to provide a hint to users as to how they can\n" + "open the file.");

		// Version
		labeledText = new LabeledText(box, "Version:", TEXT_WIDTH_LARGE);
		labeledText.getText().setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		versionText = labeledText.getText();
		versionText.setToolTipText(
				"All GPX files must include the version of the " + "GPX schema\n" + "which the file references.");

		// Extensions
		LabeledList labeledList = new LabeledList(box, "Extensions:", TEXT_WIDTH_LARGE, LIST_ROWS);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledList.getComposite());
		extensionsList = labeledList.getList();
		extensionsList.setToolTipText("Extensions (Read only).");
	}

	/**
	 * Creates the metadata group.
	 * 
	 * @param parent
	 */
	private void createMetadataGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Metadata");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Author
		createAuthorGroup(box);

		// Bounds
		createBoundsGroup(box);

		// Copyright
		createCopyrightGroup(box);

		// Desc
		LabeledText labeledText = new LabeledText(box, "Desc:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		descText = labeledText.getText();
		descText.setToolTipText(
				"Holds additional information about the " + "element intended for the user, not the GPS.");

		// Keywords
		labeledText = new LabeledText(box, "Keywords:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		keywordsText = labeledText.getText();
		keywordsText.setToolTipText("Keywords for indexing the GPX file with " + "search engines. Comma separated.");

		// Name
		labeledText = new LabeledText(box, "Name:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		metadataNameText = labeledText.getText();
		metadataNameText.setToolTipText("Metadata name");

		// Time
		labeledText = new LabeledText(box, "Time:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		timeText = labeledText.getText();
		timeText.setToolTipText("Metadata time.");

		// Extensions
		LabeledList labeledList = new LabeledList(box, "Extensions:", TEXT_WIDTH_LARGE, LIST_ROWS);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledList.getComposite());
		metadataExtensionsList = labeledList.getList();
		metadataExtensionsList.setToolTipText("Extensions (Read only).");
	}

	/**
	 * Creates the author group.
	 * 
	 * @param parent
	 */
	private void createAuthorGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Author");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Email
		createEmailGroup(box);

		// Link
		createLinkGroup(box);

		// Name
		LabeledText labeledText = new LabeledText(box, "Name:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		authorNameText = labeledText.getText();
		authorNameText.setToolTipText("Author's name.");
	}

	/**
	 * Creates the email group.
	 * 
	 * @param parent
	 */
	private void createEmailGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Email");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Id
		LabeledText labeledText = new LabeledText(box, "Id:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		idText = labeledText.getText();
		idText.setToolTipText("Id part of author's email (id@domain).");

		// Domain
		labeledText = new LabeledText(box, "Domain:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		domainText = labeledText.getText();
		domainText.setToolTipText("Domain part of author's email (id@domain).");
	}

	/**
	 * Creates the link group.
	 * 
	 * @param parent
	 */
	private void createLinkGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Link");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Link
		LabeledText labeledText = new LabeledText(box, "Href:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		linkHrefText = labeledText.getText();
		linkHrefText.setToolTipText("Href.");

		// Text
		labeledText = new LabeledText(box, "Text:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		linkTextText = labeledText.getText();
		linkTextText.setToolTipText("Author's name.");

		// Text
		labeledText = new LabeledText(box, "Type:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		linkTypeText = labeledText.getText();
		linkTypeText.setToolTipText("Type ?");
	}

	/**
	 * Creates the bounds group.
	 * 
	 * @param parent
	 */
	private void createBoundsGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Bounds");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// MinLat
		LabeledText labeledText = new LabeledText(box, "MinLat:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		minLatText = labeledText.getText();
		minLatText.setToolTipText("Minimum latiture.");

		// MaxLat
		labeledText = new LabeledText(box, "MaxLat:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		maxLatText = labeledText.getText();
		maxLatText.setToolTipText("Minimum latiture.");

		// MinLon
		labeledText = new LabeledText(box, "MinLon:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		minLonText = labeledText.getText();
		minLonText.setToolTipText("Minimum latiture.");

		// MaxLon
		labeledText = new LabeledText(box, "MaxLon:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		maxLonText = labeledText.getText();
		maxLonText.setToolTipText("Minimum latiture.");
	}

	/**
	 * Creates the copyright group.
	 * 
	 * @param parent
	 */
	private void createCopyrightGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Copyright");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Author
		LabeledText labeledText = new LabeledText(box, "Author:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		authorText = labeledText.getText();
		authorText.setToolTipText("The author of the GPX file. " + "The GPX file's owner/creator.");

		// License
		labeledText = new LabeledText(box, "License:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		licenseText = labeledText.getText();
		licenseText.setToolTipText("License.");

		// Year
		labeledText = new LabeledText(box, "Year:", TEXT_WIDTH_LARGE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labeledText.getComposite());
		yearText = labeledText.getText();
		yearText.setToolTipText("Year.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.ui.InfoDialog#setModelFromWidgets()
	 */
	@Override
	protected void setModelFromWidgets() {
		GpxType gpx = model.getGpx();

		// GPX
		Text text = creatorText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			gpx.setCreator(LabeledText.toString(text));
		}
		text = versionText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			gpx.setVersion(LabeledText.toString(text));
		}

		// Metadata
		MetadataType metadataType = null;
		PersonType personType = null;
		BoundsType boundsType = null;
		EmailType emailType = null;
		LinkType linkType = null;
		CopyrightType copyrightType = null;

		// Author
		text = authorNameText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			personType.setName(LabeledText.toString(text));
		}

		text = idText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			if (gpx.getMetadata().getAuthor().getEmail() == null) {
				emailType = new EmailType();
			}
			emailType.setId(LabeledText.toString(text));
		}
		text = domainText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			if (gpx.getMetadata().getAuthor().getEmail() == null) {
				emailType = new EmailType();
			}
			emailType.setDomain(LabeledText.toString(text));
		}
		text = linkHrefText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			if (gpx.getMetadata().getAuthor().getEmail() == null) {
				linkType = new LinkType();
			}
			linkType.setHref(LabeledText.toString(text));
		}
		text = linkTextText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			if (gpx.getMetadata().getAuthor().getEmail() == null) {
				linkType = new LinkType();
			}
			linkType.setText(LabeledText.toString(text));
		}
		text = linkTypeText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getAuthor() == null) {
				personType = new PersonType();
			}
			if (gpx.getMetadata().getAuthor().getEmail() == null) {
				linkType = new LinkType();
			}
			linkType.setType(LabeledText.toString(text));
		}

		// Bounds
		text = minLatText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getBounds() == null) {
				boundsType = new BoundsType();
			}
			boundsType.setMinlat(LabeledText.toBigDecimal(text));
		}
		text = maxLatText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getBounds() == null) {
				boundsType = new BoundsType();
			}
			boundsType.setMaxlat(LabeledText.toBigDecimal(text));
		}
		text = minLonText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getBounds() == null) {
				boundsType = new BoundsType();
			}
			boundsType.setMinlon(LabeledText.toBigDecimal(text));
		}
		text = maxLonText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getBounds() == null) {
				boundsType = new BoundsType();
			}
			boundsType.setMaxlon(LabeledText.toBigDecimal(text));
		}

		// Copyright
		text = authorText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getCopyright() == null) {
				copyrightType = new CopyrightType();
			}
			copyrightType.setAuthor(LabeledText.toString(text));
		}
		text = licenseText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getCopyright() == null) {
				copyrightType = new CopyrightType();
			}
			copyrightType.setLicense(LabeledText.toString(text));
		}
		text = yearText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			if (gpx.getMetadata().getCopyright() == null) {
				copyrightType = new CopyrightType();
			}
			copyrightType.setYear(LabeledText.toXMLGregorianCalendar(text));
		}

		text = descText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			metadataType.setDesc(LabeledText.toString(text));
		}
		text = keywordsText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			metadataType.setKeywords(LabeledText.toString(text));
		}
		text = metadataNameText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			metadataType.setName(LabeledText.toString(text));
		}
		text = timeText;
		if (text != null && !text.isDisposed() && text.getEditable()) {
			if (gpx.getMetadata() == null) {
				metadataType = new MetadataType();
				gpx.setMetadata(metadataType);
			}
			metadataType.setTime(LabeledText.toXMLGregorianCalendar(text));
		}

		// Write the metadata
		gpx.setMetadata(metadataType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.ui.InfoDialog#setWidgetsFromModel()
	 */
	@Override
	protected void setWidgetsFromModel() {
		File file = model.getFile();
		nameText.setText(file.getPath());
		long longVal = file.length();
		sizeText.setText(String.format("%d", longVal));
		longVal = file.lastModified();
		Date date = new Date(longVal);
		dateText.setText(date.toString());

		GpxType gpx = model.getGpx();
		LabeledText.read(creatorText, gpx.getCreator());
		LabeledText.read(versionText, gpx.getVersion());
		ExtensionsType extType = gpx.getExtensions();
		extensionsList.removeAll();
		if (extType == null) {
			extensionsList.add("null");
		} else {
			java.util.List<Object> objs = extType.getAny();
			for (Object obj : objs) {
				extensionsList.add(obj.getClass().getName() + " " + obj.toString());
			}
		}

		// Metadata
		MetadataType metadataType = gpx.getMetadata();
		PersonType personType = null;
		BoundsType boundsType = null;
		EmailType emailType = null;
		LinkType linkType = null;
		CopyrightType copyrightType = null;
		if (metadataType != null) {
			personType = metadataType.getAuthor();
			if (personType != null) {
				emailType = personType.getEmail();
				linkType = personType.getLink();
			}
			boundsType = metadataType.getBounds();
			copyrightType = metadataType.getCopyright();
			LabeledText.read(descText, metadataType != null ? metadataType.getDesc() : null);
			LabeledText.read(keywordsText, metadataType != null ? metadataType.getKeywords() : null);
			LabeledText.read(metadataNameText, metadataType != null ? metadataType.getName() : null);
			LabeledText.read(timeText, metadataType != null ? metadataType.getTime() : null);
			extType = metadataType.getExtensions();
			metadataExtensionsList.removeAll();
			if (extType == null) {
				metadataExtensionsList.add("null");
			} else {
				java.util.List<Object> objs = extType.getAny();
				for (Object obj : objs) {
					metadataExtensionsList.add(obj.getClass().getName() + " " + obj.toString());
				}
			}
		}

		// Author
		LabeledText.read(idText, emailType != null ? emailType.getId() : null);
		LabeledText.read(domainText, emailType != null ? emailType.getDomain() : null);
		LabeledText.read(linkHrefText, linkType != null ? linkType.getHref() : null);
		LabeledText.read(linkTextText, linkType != null ? linkType.getText() : null);
		LabeledText.read(linkTypeText, linkType != null ? linkType.getType() : null);
		LabeledText.read(authorNameText, personType != null ? personType.getName() : null);

		// Bounds
		LabeledText.read(minLatText, boundsType != null ? boundsType.getMinlat() : null);
		LabeledText.read(maxLatText, boundsType != null ? boundsType.getMaxlat() : null);
		LabeledText.read(minLonText, boundsType != null ? boundsType.getMinlon() : null);
		LabeledText.read(maxLonText, boundsType != null ? boundsType.getMaxlon() : null);

		// Copyright
		LabeledText.read(authorText, copyrightType != null ? copyrightType.getAuthor() : null);
		LabeledText.read(licenseText, copyrightType != null ? copyrightType.getLicense() : null);
		LabeledText.read(yearText, copyrightType != null ? copyrightType.getYear() : null);
	}

	/**
	 * @return The value of model.
	 */
	public GpxFileModel getModel() {
		return model;
	}

}
