package net.kenevans.gpxinspector.utils;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * TreeWithAddRemoveUpDown manages a tree of file system items, either folders
 * or files. The items can be set from a string separated by the current
 * separator, "," by default.
 * 
 * @author evans
 * 
 */
public class TreeWithAddRemoveUpDown extends Composite
{
    /** Style constant to use the Add Directory button */
    public static final int DIR = 1;
    /** Style constant to use the Add File button */
    public static final int FILE = 2;
    /** Style constant to use the Up / Down buttons */
    public static final int UPDOWN = 4;
    /** Default separator */
    private static final String DEFAULT_SEPARATOR = ",";
    private String separator = DEFAULT_SEPARATOR;
    private static String lastDirectoryDialogPath = null;
    private static String lastFileDialogPath = null;
    private Tree tree;

    /**
     * Constructor.
     * 
     * @param parent The Composite parent.
     * @param style Passed to the Composite parent.
     * @param initialItems The items to be displayed initially given as a single
     *            string separated with the separator.
     */
    public TreeWithAddRemoveUpDown(Composite parent, int style,
        String initialItems) {
        this(parent, style, FILE | UPDOWN, initialItems, DEFAULT_SEPARATOR);
    }

    /**
     * Constructor.
     * 
     * @param parent The Composite parent.
     * @param style Passed to the Composite parent.
     * @param buttonStyle Or'ed combination of DIR, FILE, UPDOWN to indicate
     *            buttons to include.
     * @param initialItems The items to be displayed initially given as a single
     *            string separated with the separator.
     * @param separator The separator to use in String representations of the
     *            items. Use NULL to get the default separator.
     */
    public TreeWithAddRemoveUpDown(Composite parent, int style, int buttonStyle,
        String initialItems, String separator) {
        super(parent, style);
        if(initialItems == null) {
            initialItems = "";
        }
        if(separator != null) {
            this.separator = separator;
        } else {
            this.separator = DEFAULT_SEPARATOR;
        }
        final Shell shell = parent.getShell();
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        this.setLayout(layout);

        tree = new Tree(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);
        resetTreeItems(initialItems);

        // Make a composite for the buttons
        Composite composite = new Composite(this, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
            .applyTo(composite);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button button;

        if((buttonStyle & DIR) > 0) {
            button = new Button(composite, SWT.PUSH);
            button.setText("Add Directory");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(button);
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog dialog = new DirectoryDialog(shell);
                    dialog.setFilterPath(lastDirectoryDialogPath);
                    String filePath = dialog.open();
                    if(filePath != null) {
                        lastDirectoryDialogPath = filePath;
                    }
                    addTreeItem(filePath);
                }
            });
        }

        if((buttonStyle & FILE) > 0) {
            button = new Button(composite, SWT.PUSH);
            button.setText("Add File");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(button);
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    FileDialog dialog = new FileDialog(shell, SWT.MULTI);
                    dialog.setFilterPath(lastFileDialogPath);
                    dialog.open();
                    String[] fileNames = dialog.getFileNames();
                    if(fileNames != null && fileNames.length > 0) {
                        lastFileDialogPath = dialog.getFilterPath();
                        StringBuffer buf;
                        for(String fileName : fileNames) {
                            buf = new StringBuffer(lastFileDialogPath);
                            if(buf.charAt(
                                buf.length() - 1) != File.separatorChar) {
                                buf.append(File.separatorChar);
                            }
                            buf.append(fileName);
                            addTreeItem(buf.toString());
                        }
                    }
                }
            });
        }

        button = new Button(composite, SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, false).applyTo(button);
        button.setText("Remove");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selection = tree.getSelection();
                for(int i = 0; i < selection.length; i++) {
                    selection[i].dispose();
                }
            }
        });

        if((buttonStyle & UPDOWN) > 0) {
            button = new Button(composite, SWT.PUSH);
            button.setText("Up");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(button);
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    moveUp();
                }
            });

            button = new Button(composite, SWT.PUSH);
            button.setText("Down");
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, false).applyTo(button);
            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if(tree.getSelectionCount() == 0) {
                        return;
                    }
                    moveDown();
                }
            });
        }
    }

    /**
     * Creates a String[] by spitting the given items at the separator).
     * 
     * @param items
     * @return
     */
    private String[] stringToStringArray(String items) {
        // Note a blank string will give 1 blank item, not 0.
        if(items.length() == 0) {
            return new String[0];
        }
        return items.split(separator);
    }

    /**
     * Add an item to the tree.
     * 
     * @param string
     */
    private void addTreeItem(String string) {
        if(string != null && string.trim().length() > 0) {
            TreeItem item = new TreeItem(tree, 0);
            item.setText(string);
            File file = new File(string);
            if(file.isDirectory()) {
                item.setImage(PlatformUI.getWorkbench().getSharedImages()
                    .getImage(ISharedImages.IMG_OBJ_FOLDER));
            } else if(file.isFile()) {
                item.setImage(PlatformUI.getWorkbench().getSharedImages()
                    .getImage(ISharedImages.IMG_OBJ_FILE));
            } else {
                item.setImage(PlatformUI.getWorkbench().getSharedImages()
                    .getImage(ISharedImages.IMG_OBJS_WARN_TSK));
            }
        }
    }

    /**
     * Moves the selected items up the tree.
     * 
     */
    private void moveUp() {
        System.out.println("moveUp count=" + tree.getSelectionCount());
        if(tree.getSelectionCount() == 0) {
            return;
        }
        // Get an array of all the items
        String[] items = stringToStringArray(getTreeItemsAsString());
        int nItems = items.length;
        System.out.println(getTreeItemsAsString());
        System.out.println("nItems=" + nItems);
        if(nItems == 0) {
            return;
        }
        // Get the selected items
        TreeItem[] selectedTreeItems = tree.getSelection();
        int nSelected = selectedTreeItems.length;
        // Check if all items are selected
        if(nSelected == nItems) {
            return;
        }
        // Get the start and end index
        String selectedItemsString = getTreeItemsAsString(selectedTreeItems);
        String[] selectedItems = stringToStringArray(selectedItemsString);
        System.out.println("nSelected=" + nSelected);
        int start = tree.indexOf(selectedTreeItems[0]);
        int end = tree.indexOf(selectedTreeItems[nSelected - 1]);
        System.out.printf("nItems=%d nSelected=%d start=%d end=%d\n", nItems,
            nSelected, start, end);
        // Check if the items are at the beginning already
        if(start == 0) {
            return;
        }
        // Rearrange the array
        StringBuffer buf = new StringBuffer(nItems);
        String item;
        String movedItem = items[start - 1];
        for(int i = 0; i < nItems; i++) {
            if(i < start - 1) {
                item = items[i];
            } else if(i >= start - 1 && i < end) {
                item = selectedItems[i - start + 1];
            } else if(i == end) {
                item = movedItem;
            } else {
                item = items[i];
            }
            if(item != null && item.trim().length() > 0) {
                if(buf.length() > 0) {
                    buf.append(separator);
                }
                buf.append(item);
            }
        }
        // Reset the tree with the new array
        String rearrangedString = buf.toString();
        System.out.println(rearrangedString);
        resetTreeItems(rearrangedString);
        tree.redraw();
        // Reselect the item
        for(int i = start - 1; i < end; i++) {
            tree.select(tree.getItem(i));
        }
    }

    /**
     * Moves the selected items down the tree.
     * 
     */
    private void moveDown() {
        System.out.println("moveDown count=" + tree.getSelectionCount());
        if(tree.getSelectionCount() == 0) {
            return;
        }
        // Get an array of all the items
        String[] items = stringToStringArray(getTreeItemsAsString());
        int nItems = items.length;
        System.out.println(getTreeItemsAsString());
        System.out.println("nItems=" + nItems);
        if(nItems == 0) {
            return;
        }
        // Get the selected items
        TreeItem[] selectedTreeItems = tree.getSelection();
        int nSelected = selectedTreeItems.length;
        // Check if all items are selected
        if(nSelected == nItems) {
            return;
        }
        // Get the start and end index
        String selectedItemsString = getTreeItemsAsString(selectedTreeItems);
        String[] selectedItems = stringToStringArray(selectedItemsString);
        System.out.println("nSelected=" + nSelected);
        int start = tree.indexOf(selectedTreeItems[0]);
        int end = tree.indexOf(selectedTreeItems[nSelected - 1]);
        System.out.printf("nItems=%d nSelected=%d start=%d end=%d\n", nItems,
            nSelected, start, end);
        // Check if the items are at the end already
        if(end == nItems - 1) {
            return;
        }
        // Rearrange the array
        StringBuffer buf = new StringBuffer(nItems);
        String item;
        String movedItem = items[end + 1];
        for(int i = 0; i < nItems; i++) {
            if(i < start - 1) {
                item = items[i];
            } else if(i == start) {
                item = movedItem;
            } else if(i > start && i <= end + 1) {
                item = selectedItems[i - start - 1];
            } else {
                item = items[i];
            }
            if(item != null && item.trim().length() > 0) {
                if(buf.length() > 0) {
                    buf.append(separator);
                }
                buf.append(item);
            }
        }
        // Reset the tree with the new array
        String rearrangedString = buf.toString();
        System.out.println(rearrangedString);
        resetTreeItems(rearrangedString);
        tree.redraw();
        // Reselect the item
        for(int i = start + 1; i <= end + 1; i++) {
            tree.select(tree.getItem(i));
        }
    }

    /**
     * Get all the tree items as a single string, separated by the path
     * separator.
     * 
     * @return
     */
    public String getTreeItemsAsString() {
        TreeItem[] items = tree.getItems();
        return getTreeItemsAsString(items);
    }

    /**
     * Get the given tree items as a single string, separated by the path
     * separator.
     * 
     * @param items The array of tree items, possibly tree.getItems().
     * @return
     */
    public String getTreeItemsAsString(TreeItem[] items) {
        StringBuffer ret = new StringBuffer();
        String text;
        for(int i = 0; i < items.length; i++) {
            text = items[i].getText();
            if(text != null && text.trim().length() > 0) {
                if(ret.length() > 0) {
                    ret.append(separator);
                }
                ret.append(text);
            }
        }
        return ret.toString();
    }

    /**
     * Reset the tree items using the given new items.
     * 
     * @param items The items to be displayed given as a single string separated
     *            with the separator.
     */
    public void resetTreeItems(String items) {
        tree.removeAll();
        String[] arrayItems = stringToStringArray(items);
        for(String arrayItem : arrayItems) {
            addTreeItem(arrayItem);
        }
    }

    /**
     * @return The value of separator.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @param separator The new value for separator.
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

}
