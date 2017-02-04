/*
 * Program to implement the actions for the ImageEditorPlugin
 * Created on Nov 8, 2006
 * By Kenneth Evans, Jr.
 */

package net.kenevans.gpxinspector.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Class to implement a modeless, scrolled selectionText dialog. The caller
 * should set the dialogTitle, groupTitle, and message to be displayed.
 */
public class ScrolledTextDialog extends Dialog
{
    /**
     * The width of the dialog, 400 by default.
     */
    private int width = 400;
    /**
     * The width of the dialog, 300 by default.
     */
    private int height = 300;
    /**
     * The dialog title.
     */
    private String dialogTitle = "Information";
    /**
     * The label on the group.
     */
    private String groupLabel = "Information";
    /**
     * The message to be displayed.
     */
    private String message = "No message specified";

    /**
     * Constructor that uses the default style which is SWT.DIALOG_TRIM |
     * SWT.NONE, resulting in a modeless dialog.
     * 
     * @param parent Parent of the dialog.
     */
    public ScrolledTextDialog(Shell parent) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE);
    }

    /**
     * Constructor that sets a style. Use SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL to
     * get a modal dialog, for example.
     * 
     * @param parent Parent of the dialog.
     * @param style Style of the dialog.SWT.APPLICATION_MODAL
     */
    public ScrolledTextDialog(Shell parent, int style) {
        super(parent, style);
    }

    public String open() {
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText(getDialogTitle());
        Image image = null;
        try {
            image = PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJS_INFO_TSK);
            // Might be better to get the image from the main window, but
            // haven't figured out how
            // This doesn't seem to work:
            // image = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            // .getShell().getImage();
        } catch(Exception ex) {
        }
        if(image != null) shell.setImage(image);
        // It can take a long time to do this so use a wait cursor
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents(shell);
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
        return null;
    }

    private void createContents(final Shell shell) {
        GridLayout grid = new GridLayout();
        grid.numColumns = 1;
        shell.setLayout(grid);

        Group box = new Group(shell, SWT.BORDER);
        box.setText(getGroupLabel());
        grid = new GridLayout();
        grid.numColumns = 1;
        box.setLayout(grid);
        GridData gridData = new GridData(GridData.FILL_BOTH
            | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        box.setLayoutData(gridData);

        Text text = new Text(box, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setEditable(false);
        text.setText(getMessage());
        gridData = new GridData(
            GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = width;
        gridData.heightHint = height;
        text.setLayoutData(gridData);

        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        close.setLayoutData(gridData);

        close.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });

        shell.setDefaultButton(close);
    }

    /**
     * @return the dialogTitle
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    /**
     * @param dialogTitle the dialogTitle to set
     */
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    /**
     * @return the groupLabel
     */
    public String getGroupLabel() {
        return groupLabel;
    }

    /**
     * @param groupLabel the groupLabel to set
     */
    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

}
