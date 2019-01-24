package net.kenevans.gpxinspector.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import net.kenevans.gpxinspector.converters.ConverterDescriptor;
import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.utils.SWTUtils;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class SaveFilesDialog extends Dialog implements IPreferenceConstants {
	private boolean success = false;
	private boolean doSaveAs = true;

	private GpxFileSetModel fileSetModel;
	private Table table;
	private Button saveAsButton;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public SaveFilesDialog(Shell parent, GpxFileSetModel fileSetModel) {
		// We want this to be modal
		this(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL, fileSetModel);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent The parent of this dialog.
	 * @param style  Style passed to the parent.
	 */
	public SaveFilesDialog(Shell parent, int style, GpxFileSetModel fileSetModel) {
		super(parent, style);
		this.fileSetModel = fileSetModel;
	}

	/**
	 * Convenience method to open the dialog.
	 * 
	 * @return Whether OK was selected or not.
	 */
	public boolean open() {
		Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText("Files Modified and Not Saved");
		// It can take a long time to do this so use a wait cursor
		// Probably not, though
		Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		if (waitCursor != null)
			getParent().setCursor(waitCursor);
		createContents(shell);
		setTableFromModel();
		getParent().setCursor(null);
		waitCursor.dispose();
		shell.pack();
		// Resize it to fit the display
		int width = shell.getSize().x;
		int height = shell.getSize().y;
		int displayHeight = shell.getDisplay().getBounds().height;
		int displayWidth = shell.getDisplay().getBounds().width;
		if (displayHeight < height) {
			// Set the height to 2/3 the display height
			height = (20 * height / 30);
		}
		if (displayWidth < width) {
			// Set the width to 2/3 the display height
			width = (20 * width / 30);
		}
		shell.setSize(width, height);
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
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
	private void createContents(final Shell shell) {
		shell.setLayout(new FillLayout());

		// Note we do not need a ScrolledComposite here as the table will scroll
		Composite parent = new Composite(shell, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);

		// Create the groups
		createTableGroup(parent);

		// Create the buttons
		// Make a zero margin composite for the OK and Cancel buttons
		Composite composite = new Composite(parent, SWT.NONE);
		// Change END to FILL to center the buttons
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true, false).applyTo(composite);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 5;
		composite.setLayout(gridLayout);

		// Save as button to specify Save or Save As
		saveAsButton = new Button(composite, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true, false).span(5, 1).applyTo(saveAsButton);
		saveAsButton.setText("Use Save As");
		saveAsButton.setToolTipText("Whether to use Save or Save As with a " + "FileSelectionDialog.");
		saveAsButton.setSelection(false);

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Check All");
		button.setToolTipText("Check all boxes, whether modified or not.");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, true).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				checkAll(true);
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setText("Uncheck All");
		button.setToolTipText("Ucheck all boxes, whether modified or not.");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, true).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				checkAll(false);
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setText("Reset");
		button.setToolTipText("Reset all boxes to whether modified or not.");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, true).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				reset();
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setText("Save Checked");
		button.setToolTipText("Save the checked files.");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, true).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				saveChecked();
				success = true;
				shell.close();
			}
		});

		button = new Button(composite, SWT.PUSH);
		button.setToolTipText("Do nothing about saving.");
		button.setText("Do Nothing");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, true).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				success = false;
				shell.close();
			}
		});
		shell.setDefaultButton(button);
	}

	/**
	 * Creates the icons group.
	 * 
	 * @param parent
	 */
	private void createTableGroup(Composite parent) {
		Group box = new Group(parent, SWT.BORDER);
		box.setText("Files to Save");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		box.setLayout(gridLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

		// Name
		table = new Table(box, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		table.setToolTipText("Files to save.");
		table.setHeaderVisible(false);
		new TableColumn(table, SWT.NULL);

	}

	/**
	 * Checks or unchecks all rows.
	 * 
	 * @param checked
	 */
	private void checkAll(boolean checked) {
		if (table == null) {
			return;
		}
		TableItem[] items = table.getItems();
		if (items == null) {
			return;
		}
		for (TableItem item : items) {
			item.setChecked(checked);
		}
	}

	/**
	 * Resets the check state to whether the file is dirty or not.
	 * 
	 * @param checked
	 */
	private void reset() {
		if (table == null) {
			return;
		}
		TableItem[] items = table.getItems();
		if (items == null) {
			return;
		}
		Object data = null;
		GpxFileModel fileModel = null;
		for (TableItem item : items) {
			data = item.getData();
			if (data != null && (data instanceof GpxFileModel)) {
				fileModel = (GpxFileModel) item.getData();
				item.setChecked(fileModel.isDirty());
			}
		}
	}

	/**
	 * Saves the selected files.
	 */
	public void saveChecked() {
		if (table == null || table.isDisposed()) {
			return;
		}
		TableItem[] items = table.getItems();
		if (items == null) {
			return;
		}
		Object data = null;
		GpxFileModel fileModel = null;
		for (TableItem item : items) {
			if (!item.getChecked()) {
				continue;
			}
			data = item.getData();
			if (data != null && (data instanceof GpxFileModel)) {
				fileModel = (GpxFileModel) item.getData();
				if (!saveAsButton.getSelection() && !fileModel.isNewFile()) {
					fileModel.save();
				} else {
					saveAs(fileModel);
				}
			}
		}
	}

	/**
     * Brings up a FileDialog to save the given GpxFileModel
     * 
     * @param fileModel
     */
    public static void saveAs(GpxFileModel fileModel) {
        // Find the converters
        boolean useConverters = true;
        List<ConverterDescriptor> converters = null;
        try {
            converters = Activator.getDefault().getConverterDescriptors();
            if(converters == null || converters.size() == 0) {
                useConverters = false;
            }
        } catch(Throwable t) {
            useConverters = false;
        }
        // Open a FileDialog
        FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell(),
            SWT.SAVE);

        File modelFile = null;
        if(fileModel != null) {
            modelFile = fileModel.getFile();
            dlg.setFilterPath(modelFile.getPath());
            dlg.setFileName(modelFile.getName());
        }
        String string;
        int index = 0;
        int filterIndex = 0;
        if(useConverters) {
            // Use the index of the preferred extension as the default filter
            IPreferenceStore prefs = Activator.getDefault()
                .getPreferenceStore();
            filterIndex = prefs.getInt(P_PREFERRED_FILE_EXTENSION);
            ArrayList<String> extList = new ArrayList<String>();
            for(ConverterDescriptor converter : converters) {
                string = converter.getFilterExtensions();
                if(string != null && string.length() > 0) {
                    extList.add(string);
                    if(converter.isSaveSupported(modelFile)) {
                        filterIndex = index;
                    }
                    index++;
                }
            }
            // Reset the extension of the model file if necessary
            if(modelFile != null) {
            	String modelExt = SWTUtils.getExtension(modelFile);
            	String filterExt = converters.get(filterIndex).getPreferredExtension();
            	if(modelExt != null && !modelExt.equals(filterExt)) {
            		String newModelName = modelFile.getName();
                    int i = newModelName.lastIndexOf('.');
                    if(i > 0 && i < newModelName.length() - 1) {
                        newModelName = newModelName.substring(0, i+1) + filterExt;
                        dlg.setFileName(newModelName);
                   }
            	}
            }
            String[] ext = new String[extList.size()];
            ext = extList.toArray(ext);
            dlg.setFilterExtensions(ext);
            dlg.setFilterIndex(filterIndex);
        }
        String selectedPath = dlg.open();
        if(selectedPath != null) {
            File file = new File(selectedPath);
            boolean doIt = true;
            if(file.exists()) {
                Boolean res = SWTUtils.confirmMsg(
                    "File exists: " + file.getPath() + "\nOK to overwrite?");
                if(!res) {
                    doIt = false;
                }
            }
            if(doIt) {
                fileModel.saveAs(file);
            }
        }
    }

	/**
	 * Sets the values from the fileSetModel to the Text's.
	 */
	private void setTableFromModel() {
		if (table == null || fileSetModel == null) {
			return;
		}
		for (GpxFileModel fileModel : fileSetModel.getGpxFileModels()) {
			TableItem item = new TableItem(table, SWT.NULL);
			item.setText(fileModel.getFile().getPath());
			item.setText(0, fileModel.getFile().getPath());
			item.setChecked(fileModel.isDirty());
			item.setData(fileModel);
		}
		table.getColumn(0).pack();
	}

	/**
	 * @return The value of doSaveAs.
	 */
	public boolean isDoSaveAs() {
		return doSaveAs;
	}

	/**
	 * @param doSaveAs The new value for doSaveAs.
	 */
	public void setDoSaveAs(boolean doSaveAs) {
		this.doSaveAs = doSaveAs;
	}

}
