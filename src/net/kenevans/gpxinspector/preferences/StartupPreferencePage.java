package net.kenevans.gpxinspector.preferences;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.TreeWithAddRemoveUpDown;

/**
 * This class is a Python preferences pages. At this time, the only preference
 * available is dedicated to a prefered directory where user can save python
 * script recorded when using python commands via FableJep.
 * 
 * @author SUCHET
 * 
 */
public class StartupPreferencePage extends PreferencePage
    implements IWorkbenchPreferencePage, IPreferenceConstants
{
    private TreeWithAddRemoveUpDown tree;
    private BooleanFieldEditor useStartupFilesEditor;
    private IPreferenceStore prefs;

    /**
     * StartupPreferencePage constructor.
     */
    public StartupPreferencePage() {
        // TODO Auto-generated constructor stub
    }

    /**
     * StartupPreferencePage constructor.
     * 
     * @param title
     * @see org.eclipse.jface.preference.PreferencePage#PreferencePage(String
     *      title)
     */
    public StartupPreferencePage(String title) {
        super(title);
        // TODO Auto-generated constructor stub
    }

    /**
     * StartupPreferencePage constructor
     * 
     * @param title
     * @param image
     * @see org.eclipse.jface.preference.PreferencePage#PreferencePage(String
     *      title, ImageDescriptor image)
     */
    public StartupPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Control createContents(Composite parent) {
        // Rely on the field editor parent being a Composite with a GridData
        // layout. Set the span to be 2 columns. Will have to be modified if
        // there are field editors with more than 2 columns.
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        Text text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setText("Choose the GPX files to load on startup:");
        GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
            .hint(TEXT_WIDTH_MED, SWT.DEFAULT).applyTo(text);

        // Use SWT.NONE here. SWT.DEFAULT results in a scrolled window without
        // the contents on some platforms
        tree = new TreeWithAddRemoveUpDown(composite, SWT.NONE,
            prefs.getString(P_STARTUP_FILES));
        GridDataFactory.fillDefaults().grab(true, true).span(3, 1)
            .applyTo(tree);

        useStartupFilesEditor = new BooleanFieldEditor(P_USE_STARTUP_FILES,
            "Use specified startup files", composite);
        useStartupFilesEditor.setPreferenceStore(prefs);
        useStartupFilesEditor.load();

        text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setText(
            "Note: These settings will be applied at the next " + "startup.");
        GridDataFactory.fillDefaults().grab(false, false).span(3, 1)
            .hint(TEXT_WIDTH_MED, SWT.DEFAULT).applyTo(text);

        return composite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        prefs = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(prefs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        tree.resetTreeItems(prefs.getDefaultString(P_STARTUP_FILES));
        useStartupFilesEditor.loadDefault();
        super.performDefaults();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        try {
            String itemsString = tree.getTreeItemsAsString();
            // Note a blank string will give 1 blank item, not 0.
            String[] items = itemsString.split(tree.getSeparator());
            // Check extensions are .gpx
            File file;
            String ext;
            for(String item : items) {
                if(item == null || item.length() == 0) {
                    continue;
                }
                file = new File(item);
                ext = SWTUtils.getExtension(file);
                if(ext == null | !ext.toLowerCase().equals("gpx")) {
                    boolean res = SWTUtils.confirmMsg(
                        file.getPath() + "\ndoes not have a .gpx extension!\n"
                            + "OK to continue?");
                    if(!res) {
                        return false;
                    }
                }
            }
            prefs.setValue(P_STARTUP_FILES, itemsString);
            System.out.println("performOk: itemsString=|" + itemsString + "|"
                + "\nitems=" + items.length);

            prefs.setValue(P_USE_STARTUP_FILES,
                useStartupFilesEditor.getBooleanValue());
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error setting preferences", ex);
            return false;
        }
        return super.performOk();
    }
}
