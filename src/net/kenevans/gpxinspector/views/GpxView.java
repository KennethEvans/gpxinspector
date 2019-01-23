package net.kenevans.gpxinspector.views;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import net.kenevans.gpxinspector.converters.ConverterDescriptor;
import net.kenevans.gpxinspector.model.GpxFileModel;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.model.GpxModel;
import net.kenevans.gpxinspector.model.GpxModel.PasteMode;
import net.kenevans.gpxinspector.model.GpxRouteModel;
import net.kenevans.gpxinspector.model.GpxTrackModel;
import net.kenevans.gpxinspector.model.GpxTrackSegmentModel;
import net.kenevans.gpxinspector.model.GpxWaypointModel;
import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.preferences.IPreferenceConstants;
import net.kenevans.gpxinspector.ui.GpxCheckStateProvider;
import net.kenevans.gpxinspector.ui.GpxContentProvider;
import net.kenevans.gpxinspector.ui.GpxLabelProvider;
import net.kenevans.gpxinspector.ui.LocalSelection;
import net.kenevans.gpxinspector.ui.SaveFilesDialog;
import net.kenevans.gpxinspector.utils.GpxUtils;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.ScrolledTextDialog;
import net.kenevans.gpxinspector.utils.find.FindNear;
import net.kenevans.gpxinspector.utils.find.FindNear.Mode;
import net.kenevans.gpxinspector.utils.find.FindNearOptions;
import net.kenevans.gpxinspector.utils.find.FindNearOptions.Units;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 */
public class GpxView extends ViewPart implements IPreferenceConstants
{
    /**
     * Currently the debug handler is implemented in plugin.xml and in this
     * view. Setting this to false will insure it is not enabled even though it
     * is implemented. If the menu that uses it checks for enabled (in
     * plugin.xml), then it should not appear.
     */
    private static boolean ACTIVATE_DEBUG_HANDLER = false;
    /** The name for a newly created GPX file. */
    private static final String NEW_FILE_NAME = "New.gpx";
    /**
     * Whether to implement a Text with the contents of the current selection or
     * not. Used for learning.
     */
    private boolean showSelectionText = false;

    /** Used to specify the task to do for a selection location */
    public static enum Task {
        SORT, REVERSE, NEWFILE, NEWTRK, NEWSEG, NEWRTE, NEWWPT
    };

    /** The separator used for the initial files preference */
    public static final String STARTUP_FILE_SEPARATOR = ",";
    /** Maximum number of messages printed for possible error storms */
    private static final int MAX_MESSAGES = 5;
    /** A listener on preferences property change. */
    private IPropertyChangeListener preferencesListener;
    /** A listener on part changes. */
    private IPartListener2 partListener;

    protected CheckboxTreeViewer treeViewer;
    protected Text selectionText;
    protected GpxLabelProvider labelProvider;
    protected GpxCheckStateProvider checkStateProvider;

    protected GpxFileSetModel gpxFileSetModel;

    protected String initialPath;
    protected String initialFileGroupPath;

    private double findLatitude = 46.068393;
    private double findLongitude = -89.596687;
    private double findRadius = FindNearOptions.DEFAULT_RADIUS;
    private Units findUnits = FindNearOptions.DEFAULT_UNITS;
    protected String gpxDirectory = D_GPX_DIR;

    /**
     * The maximum level to which the buttons can expand the tree. This should
     * be the maximum level it is possible to expand it. treeLevel is restricted
     * to be less than or equal to this value.
     */
    public static final int MAX_TREE_LEVEL = 5;
    /** The initial tree level */
    public static final int INITIAL_TREE_LEVEL = 1;
    /**
     * The current level to which the tree is expanded. Should be non-negative.
     */
    private int treeLevel = INITIAL_TREE_LEVEL;

    /** The local clipboard */
    private Clipboard clipboard = new Clipboard("local");

    /**
     * The constructor.
     */
    public GpxView() {
    }

    @Override
    public void init(IViewSite site, IMemento memento)
        throws PartInitException {
        super.init(site, memento);

        // Create and add a listener for Workspace events
        partListener = new IPartListener2() {
            public void partActivated(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partDeactivated(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partBroughtToTop(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partClosed(IWorkbenchPartReference partRef) {
                if(partRef.getPart(false) == getSite().getPart()) {
                    // Prompt to save dirty files
                    if(gpxFileSetModel == null || !gpxFileSetModel.isDirty()) {
                        return;
                    }
                    try {
                        SaveFilesDialog dialog = new SaveFilesDialog(
                            Display.getDefault().getActiveShell(),
                            gpxFileSetModel);
                        // Can either do SaveAs or Save without prompt
                        dialog.setDoSaveAs(true);
                        Boolean success = dialog.open();
                        if(success) {
                            dialog.saveChecked();
                        }
                    } catch(Exception ex) {
                        SWTUtils.excMsgAsync("Error with SaveFilesDialog", ex);
                        ex.printStackTrace();
                    }
                }
            }

            public void partHidden(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partInputChanged(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partOpened(IWorkbenchPartReference partRef) {
                // Ignored
            }

            public void partVisible(IWorkbenchPartReference partRef) {
                // Ignored
            }
        };
        // Add the listener
        getSite().getPage().addPartListener(partListener);
    }

    /*
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {
        // Get preferences
        final IPreferenceStore prefs = Activator.getDefault()
            .getPreferenceStore();
        gpxDirectory = prefs.getString(P_GPX_DIR);
        // Add a property change listener for preferences
        preferencesListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getProperty().equals(P_GPX_DIR)) {
                    gpxDirectory = prefs.getString(P_GPX_DIR);
                } else if(event.getProperty().equals(P_NO_MOVE_SPEED)) {
                    String stringVal = prefs.getString(P_NO_MOVE_SPEED);
                    try {
                        double val = Double.parseDouble(stringVal);
                        GpxUtils.NO_MOVE_SPEED = val;
                    } catch(NumberFormatException ex) {
                        SWTUtils.excMsg(
                            "Got invalid value for \"No Move\" " + "speed", ex);
                    }
                }
            }
        };
        prefs.addPropertyChangeListener(preferencesListener);

        // DEBUG
        if(false) {
            try {
                Bundle bundle = Platform.getBundle("net.kenevans.jaxb");
                System.out.println("bundle: " + bundle);
                String symbolicName = bundle.getSymbolicName();
                System.out.println(symbolicName);
                Path path = new Path("/");
                URL url = FileLocator
                    .resolve(FileLocator.find(bundle, path, null));
                String pluginPath = url.getPath();
                System.out.println(pluginPath);
                if(pluginPath.startsWith("/")) {
                    pluginPath = pluginPath.substring(1);
                }
                File file = new File(pluginPath);
                System.setProperty("java.endorsed.dirs", file.getPath());
                System.out.println(System.getProperty("java.endorsed.dirs"));
                file = new File(
                    file.getPath() + File.separator + "jaxb-impl.jar");
                System.out.println(file.getPath() + ": " + file.exists());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);

        // Text to show the current selection
        if(showSelectionText) {
            selectionText = new Text(parent,
                SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false)
                .align(SWT.FILL, SWT.FILL).applyTo(selectionText);
        }

        // CheckboxTreeViewer (SWT.MULTI is needed to get multiple selection)
        treeViewer = new CheckboxTreeViewer(parent, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true)
            .align(SWT.FILL, SWT.FILL).applyTo(treeViewer.getControl());
        treeViewer.setContentProvider(new GpxContentProvider());
        labelProvider = new GpxLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
        checkStateProvider = new GpxCheckStateProvider();
        treeViewer.setCheckStateProvider(checkStateProvider);
        treeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent ev) {
                GpxModel model = (GpxModel)ev.getElement();
                if(model != null) {
                    model.setChecked(ev.getChecked());
                }
            }
        });
        treeViewer.setUseHashlookup(true);

        // Create a drop target
        DropTarget dropTarget = new DropTarget(treeViewer.getControl(),
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
                // See if it is Text
                if(TextTransfer.getInstance()
                    .isSupportedType(event.currentDataType)) {
                    String fileName = (String)event.data;
                    GpxFileModel model;
                    try {
                        model = new GpxFileModel(gpxFileSetModel, fileName);
                        gpxFileSetModel.add(model);
                    } catch(Throwable t) {
                        SWTUtils.excMsgAsync("Error parsing " + fileName, t);
                    }
                } else if(FileTransfer.getInstance()
                    .isSupportedType(event.currentDataType)) {
                    String[] fileNames = (String[])event.data;
                    for(String fileName : fileNames) {
                        GpxFileModel model;
                        try {
                            model = new GpxFileModel(gpxFileSetModel, fileName);
                            gpxFileSetModel.add(model);
                        } catch(Throwable t) {
                            SWTUtils.excMsgAsync("Error parsing " + fileName,
                                t);
                        }
                    }
                }
            }
        });

        // Create menu, toolbars, filters, sorters.
        createFiltersAndSorters();
        createHandlers();
        // Debug
        createDebugHandler();
        hookListeners();
        hookContextMenu(treeViewer.getControl());

        // Initialize the CheckboxTreeViewer
        treeViewer.setInput(getInitalInput());
        treeViewer.expandToLevel(treeLevel);
    }

    protected void createFiltersAndSorters() {
        // atLeastThreeFilter = new ThreeItemFilter();
        // onlyBoardGamesFilter = new BoardgameFilter();
        // booksBoxesGamesSorter = new BookBoxBoardSorter();
        // noArticleSorter = new NoArticleSorter();
    }

    /**
     * Adds a menu listener to hook the context menu when it is invoked.
     * 
     * @param control
     */
    private void hookContextMenu(Control control) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
        getSite().registerContextMenu(menuMgr, treeViewer);
    }

    /**
     * Adds a ISelectionChangedListener to the tree. It is used to display the
     * selection in a Text. It only does something if showSelectionText is true,
     * and is only intended for debugging.
     */
    protected void hookListeners() {
        // DEBUG
        if(!showSelectionText) {
            return;
        }
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                // if the selection is empty clear the label
                if(event.getSelection().isEmpty()) {
                    selectionText.setText("");
                    return;
                }
                if(event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)event
                        .getSelection();
                    StringBuffer toShow = new StringBuffer();
                    for(Iterator<?> iterator = selection.iterator(); iterator
                        .hasNext();) {
                        Object domain = (GpxModel)iterator.next();
                        String value = labelProvider.getText(domain);
                        toShow.append(value);
                        toShow.append(", ");
                    }
                    // Remove the trailing comma space pair
                    if(toShow.length() > 0) {
                        toShow.setLength(toShow.length() - 2);
                    }
                    selectionText.setText(toShow.toString());
                }
            }
        });
    }

    /**
     * Creates handlers.
     */
    protected void createHandlers() {
        // IHandlerService from the workbench is the global handler service. it
        // provides no special activation scoping or lifecycle.

        // IHandlerService from the workbench window is the window handler
        // service. Any handlers activated through the window handler service
        // will be active when that window is active. Any listeners added to the
        // window handler service will be removed when the window is disposed,
        // and any active handlers will be deactivated (but not disposed).

        // IHandlerService from the workbench part site is the part handler
        // service. Any handlers activated through the part handlers service
        // will only be active when that part is active. Any listeners added to
        // the part handler service will be removed when the part is disposed,
        // and any active handlers will be deactivated (but not disposed).

        // Get the handler service from the view site
        IHandlerService handlerService = (IHandlerService)getSite()
            .getService(IHandlerService.class);

        // New
        AbstractHandler handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                newGpxFile();
                return null;
            }
        };
        String id = "net.kenevans.gpxinspector.newGpxFile";
        handlerService.activateHandler(id, handler);

        // Save
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveGpxFiles();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.save";
        handlerService.activateHandler(id, handler);

        // Open Gpx file group
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                openGpxFileGroup();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.openGpxFileGroup";
        handlerService.activateHandler(id, handler);

        // Save Gpx file group
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveGpxFileGroup();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.saveAsFileGroup";
        handlerService.activateHandler(id, handler);

        // Remove
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                removeSelected();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.remove";
        handlerService.activateHandler(id, handler);

        // Collapse
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                collapseTreeToNextLevel();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.collapse";
        handlerService.activateHandler(id, handler);

        // Collapse all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                collapseAll();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.collapseAll";
        handlerService.activateHandler(id, handler);

        // Expand
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                expandTreeToNextLevel();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.expand";
        handlerService.activateHandler(id, handler);

        // Check selected
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                checkSelected(true);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.checkSelected";
        handlerService.activateHandler(id, handler);

        // Uncheck selected
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                checkSelected(false);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.uncheckSelected";
        handlerService.activateHandler(id, handler);

        // Select All
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                selectAll(true);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.selectAll";
        handlerService.activateHandler(id, handler);

        // Select None
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                selectAll(false);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.selectNone";
        handlerService.activateHandler(id, handler);

        // Add startup files from preferences
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                addStartupFilesFromPreferences();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.addStartupFilesFromPreferences";
        handlerService.activateHandler(id, handler);

        // Save Checked models as Startup Preference
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                saveCheckedAsStartupPreference();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.saveAsStartupPreference";
        handlerService.activateHandler(id, handler);

        // Remove all
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                removeAll();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.removeAll";
        handlerService.activateHandler(id, handler);

        // Show info
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                showInfo();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.showInfo";
        handlerService.activateHandler(id, handler);

        // Refresh
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                if(treeViewer != null) {
                    treeViewer.setSelection(null);
                    treeViewer.refresh(true);
                }
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.refreshTree";
        handlerService.activateHandler(id, handler);

        // Cut
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                cut();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.cut";
        handlerService.activateHandler(id, handler);

        // Copy
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                copy();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.copy";
        handlerService.activateHandler(id, handler);

        // Paste
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.END);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.paste";
        handlerService.activateHandler(id, handler);

        // Paste First
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.BEGINNING);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.pasteFirst";
        handlerService.activateHandler(id, handler);

        // Paste Before
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.BEFORE);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.pasteBefore";
        handlerService.activateHandler(id, handler);

        // Paste Replace
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.REPLACE);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.pasteReplace";
        handlerService.activateHandler(id, handler);

        // Paste After
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.AFTER);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.pasteAfter";
        handlerService.activateHandler(id, handler);

        // Paste Last
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                paste(PasteMode.END);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.pasteLast";
        handlerService.activateHandler(id, handler);

        // Sort
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.SORT);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.sort";
        handlerService.activateHandler(id, handler);

        // Reverse
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.REVERSE);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.reverse";
        handlerService.activateHandler(id, handler);

        // New file
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.NEWFILE);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.newFile";
        handlerService.activateHandler(id, handler);

        // New track
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.NEWTRK);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.newTrack";
        handlerService.activateHandler(id, handler);

        // New track segment
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.NEWSEG);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.newTrackSegment";
        handlerService.activateHandler(id, handler);

        // New route
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.NEWRTE);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.newRoute";
        handlerService.activateHandler(id, handler);

        // New waypoint
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                doTask(Task.NEWWPT);
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.newWaypoint";
        handlerService.activateHandler(id, handler);

        // Merge all segments
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                mergeTracks();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.mergeAllSegments";
        handlerService.activateHandler(id, handler);

        // Merge segments
        handler = new AbstractHandler() {
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                mergeTracks();
                return null;
            }
        };
        id = "net.kenevans.gpxinspector.mergeSegments";
        handlerService.activateHandler(id, handler);

    }

    /**
     * Sets up the debug handler. This is a separate method since it has messy,
     * experimental code.
     */
    void createDebugHandler() {
        // Set this to get printout
        final boolean verbose = false;
        // Set this to use a selection listener to do setBaseEnabled
        final boolean useSelectionListener = true;

        // Get the handler service from the view site
        IHandlerService handlerService = (IHandlerService)getSite()
            .getService(IHandlerService.class);

        AbstractHandler handler = new AbstractHandler() {
            ISelectionChangedListener selectionChangedListener = null;

            @Override
            public Object execute(ExecutionEvent event)
                throws ExecutionException {
                if(false) {
                    ISelection sel = HandlerUtil.getCurrentSelection(event);
                    System.out.println("getCurrentSelection: " + sel);
                    sel = HandlerUtil.getActiveMenuSelection(event);
                    System.out.println("getActiveMenuSelection: " + sel);
                    Collection<?> collection = HandlerUtil
                        .getActiveMenus(event);
                    System.out.println("getActiveMenus: " + collection);
                }
                if(true) {
                    debug();
                }
                return null;
            }

            @Override
            public void dispose() {
                if(selectionChangedListener != null && treeViewer != null) {
                    treeViewer.removeSelectionChangedListener(
                        selectionChangedListener);
                    selectionChangedListener = null;
                }

            }

            @Override
            public void setBaseEnabled(boolean enabled) {
                // Override is to make it public
                super.setBaseEnabled(enabled);
            }

            @Override
            public boolean isEnabled() {
                boolean enabled = super.isEnabled();
                boolean handled = super.isHandled();
                if(verbose) {
                    System.out.println("isEnabled(debug):super.enabled="
                        + enabled + " super.handled=" + handled);
                }
                // This will insure it is not enabled. The menu can do
                // setVisible with checked set to make it not appear.
                if(!ACTIVATE_DEBUG_HANDLER) {
                    enabled = false;
                } else if(useSelectionListener) {
                    // This doesn't work. isEnabled is not called enough.
                    enabled = true;
                    IStructuredSelection selection = (IStructuredSelection)treeViewer
                        .getSelection();
                    // if the selection is empty clear the label
                    if(selection.size() > 1) {
                        enabled = false;
                    }
                }
                if(verbose) {
                    System.out.println("  return " + enabled);
                }
                return enabled;
            }

            @Override
            public void setEnabled(Object obj) {
                if(!(obj instanceof IEvaluationContext)) {
                    return;
                }
                // IEvaluationContext context = (IEvaluationContext)obj;
                // ISources sources;
                if(verbose) {
                    System.out.println("setEnabled(debug): enabled="
                        + isEnabled() + " handled=" + isHandled());
                }
                if(!ACTIVATE_DEBUG_HANDLER) {
                    return;
                }
                if(true && selectionChangedListener == null) {
                    selectionChangedListener = new ISelectionChangedListener() {
                        public void selectionChanged(
                            SelectionChangedEvent event) {
                            boolean enabled = true;
                            IStructuredSelection selection = (IStructuredSelection)event
                                .getSelection();
                            // if the selection is empty clear the label
                            if(selection.size() > 1) {
                                enabled = false;
                            }
                            if(verbose) {
                                System.out.println(
                                    "  Selection about to change" + enabled);
                            }
                            setBaseEnabled(enabled);
                            if(verbose) {
                                System.out.println(
                                    "  Selection changed: enabled=" + enabled);
                            }
                        }
                    };
                    treeViewer
                        .addSelectionChangedListener(selectionChangedListener);
                    if(verbose) {
                        System.out.println("  SelectionChangedListener added");
                    }
                }
            }
        };
        String id = "net.kenevans.gpxinspector.debug";
        handlerService.activateHandler(id, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        clearClipboard();
        if(partListener != null) {
            getSite().getPage().removePartListener(partListener);
            partListener = null;
        }
        if(preferencesListener != null) {
            Activator.getDefault().getPreferenceStore()
                .removePropertyChangeListener(preferencesListener);
            preferencesListener = null;
        }
        super.dispose();
    }

    /**
     * Show information for the selected item. Only uses the first element in
     * the selection. If nothing is selected do nothing.
     */
    protected void showInfo() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        if(selection.isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        // Only do the first one
        GpxModel model = (GpxModel)selection.getFirstElement();
        model.showInfo();
    }

    /**
     * Remove the selected domain object(s). If multiple objects are selected
     * remove all of them.
     * 
     * If nothing is selected do nothing.
     */
    protected void removeSelected() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        if(selection.isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        // Tell the tree to not redraw
        treeViewer.getTree().setRedraw(false);
        int count = 0;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            GpxModel model = (GpxModel)iterator.next();
            GpxModel parent = model.getParent();
            if(parent instanceof GpxFileSetModel) {
                if(model instanceof GpxFileModel) {
                    ((GpxFileSetModel)parent).remove((GpxFileModel)model);
                }
            } else if(parent instanceof GpxFileModel) {
                if(model instanceof GpxTrackModel) {
                    ((GpxFileModel)parent).remove((GpxTrackModel)model);
                } else if(model instanceof GpxRouteModel) {
                    ((GpxFileModel)parent).remove((GpxRouteModel)model);
                } else if(model instanceof GpxWaypointModel) {
                    ((GpxFileModel)parent).remove((GpxWaypointModel)model);
                }
            } else if(parent instanceof GpxTrackModel) {
                if(model instanceof GpxTrackSegmentModel) {
                    ((GpxTrackModel)parent).remove((GpxTrackSegmentModel)model);
                } else if(model instanceof GpxTrackSegmentModel) {
                    ((GpxTrackModel)parent).remove((GpxTrackSegmentModel)model);
                }
            } else if(parent instanceof GpxTrackSegmentModel) {
                if(model instanceof GpxWaypointModel) {
                    ((GpxTrackSegmentModel)parent)
                        .remove((GpxWaypointModel)model);
                }
            } else if(parent instanceof GpxRouteModel) {
                if(model instanceof GpxWaypointModel) {
                    ((GpxRouteModel)parent).remove((GpxWaypointModel)model);
                }
            } else {
                if(count < MAX_MESSAGES) {
                    String parentClass = (parent == null) ? null
                        : parent.getClass().getName();
                    SWTUtils.errMsg("Delete not implemented for "
                        + model.getClass().getName() + " with parent "
                        + parentClass);
                    count++;
                } else if(count == MAX_MESSAGES) {
                    // Avoid error storms
                    SWTUtils.errMsg(
                        "Will not show any more not-implemented messages");
                    count++;
                }
            }
            treeViewer.getTree().setRedraw(true);
        }
    }

    /**
     * Removes all items from the tree
     */
    private void removeAll() {
        // Tell the tree to not redraw until we finish removing everything
        treeViewer.getTree().setRedraw(false);
        List<GpxFileModel> fileSetModels = gpxFileSetModel.getGpxFileModels();
        fileSetModels.clear();
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh(true);
        treeLevel = 1;
    }

    /**
     * Check all selected elements and their children to the specified state. If
     * no selection, check everything.
     * 
     * @param checked
     */
    protected void checkSelected(boolean checked) {
        // Note that we cannot use treeviewer.setSubtreeChecked, etc. as they
        // do not notify listeners so the underlying model is not changed. And
        // they tend to only apply to visible children.
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        treeViewer.getTree().setRedraw(false);
        if(selection.isEmpty()) {
            check(gpxFileSetModel, checked);
        } else {
            for(Iterator<?> iterator = selection.iterator(); iterator
                .hasNext();) {
                GpxModel model = (GpxModel)iterator.next();
                check(model, checked);
            }
        }
        treeViewer.getTree().setRedraw(true);
        // TODO Is this necessary?
        treeViewer.refresh(true);
    }

    /**
     * Select all items in the tree if checked is true. Clear the selection if
     * checked if false.
     * 
     * @param checked
     */
    protected void selectAll(boolean checked) {
        if(checked) {
            // This seems to be what Ctrl-A does. It selects the visible
            // elements.
            treeViewer.getTree().selectAll();
        } else {
            treeViewer.setSelection(null);
        }
    }

    /**
     * Recursively sets the check state to the specified value for the model and
     * its children.
     * 
     * @param model
     * @param checked
     */
    protected void check(GpxModel model, boolean checked) {
        if(model instanceof GpxFileSetModel) {
            GpxFileSetModel fileSetModel = (GpxFileSetModel)model;
            model.setChecked(checked);
            for(GpxFileModel fileModel : fileSetModel.getGpxFileModels()) {
                check(fileModel, checked);
            }
        } else if(model instanceof GpxFileModel) {
            GpxFileModel fileModel = (GpxFileModel)model;
            model.setChecked(checked);
            for(GpxTrackModel trkModel : fileModel.getTrackModels()) {
                check(trkModel, checked);
            }
            for(GpxRouteModel rteModel : fileModel.getRouteModels()) {
                check(rteModel, checked);
            }
            for(GpxWaypointModel wptModel : fileModel.getWaypointModels()) {
                check(wptModel, checked);
            }
        } else if(model instanceof GpxTrackModel) {
            model.setChecked(checked);
        } else if(model instanceof GpxTrackSegmentModel) {
            model.setChecked(checked);
        } else if(model instanceof GpxRouteModel) {
            model.setChecked(checked);
        } else if(model instanceof GpxWaypointModel) {
            model.setChecked(checked);
        }
    }

    /**
     * Brings up a FileDialog to prompt for a GPX file and then adds it to the
     * GpxFileSetModel at the end.
     */
    public void openGpxFile() {
        openGpxFile(null, PasteMode.END);
    }

    /**
     * Brings up a FileDialog to prompt for a GPX file and then adds it to the
     * GpxFileSetModel using the given mode at the location specified by
     * oldModel.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     */
    public void openGpxFile(GpxFileModel oldModel, PasteMode mode) {
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
            SWT.MULTI);

        File modelFile = null;
        if(oldModel != null) {
            modelFile = oldModel.getFile();
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
                    // Override the preference if there is an old model
                    if(converter.isParseSupported(modelFile)) {
                        filterIndex = index;
                    }
                }
                index++;
            }
            String[] ext = new String[extList.size()];
            ext = extList.toArray(ext);
            dlg.setFilterExtensions(ext);
            dlg.setFilterIndex(filterIndex);
        }
        String selectedPath = dlg.open();
        if(selectedPath != null) {
            initialPath = selectedPath;
            // Extract the directory part of the selectedPath
            String initialDirectory = initialPath;
            index = selectedPath.lastIndexOf(File.separator);
            if(index > 0) {
                initialDirectory = selectedPath.substring(0, index);
            }
            // Loop over the selected files
            String fileNames[] = dlg.getFileNames();
            GpxFileModel newModel = null;
            String filePath = null;
            for(String fileName : fileNames) {
                filePath = initialDirectory + File.separator + fileName;
                try {
                    newModel = new GpxFileModel(gpxFileSetModel, filePath);
                    gpxFileSetModel.add(oldModel, newModel, mode);
                } catch(Throwable t) {
                    SWTUtils.excMsgAsync("Error parsing " + fileName, t);
                }
            }
        }
    }

    /**
     * Creates a new GPX file.
     */
    public void newGpxFile() {
        try {
            // String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = NEW_FILE_NAME;
            // if(tempDir != null) {
            // fileName = tempDir + File.separator + fileName;
            // }
            File file = new File(fileName);
            GpxFileModel model = new GpxFileModel(gpxFileSetModel, file, true);
            gpxFileSetModel.add(null, model, PasteMode.END);
            // The saving is done by the dialog
            // Reset the input to cause the listeners to change
            Object oldInput = treeViewer.getInput();
            treeViewer.setInput(oldInput);
            treeViewer.expandToLevel(treeLevel);
        } catch(Throwable t) {
            SWTUtils.excMsgAsync("Error with SaveFilesDialog", t);
            t.printStackTrace();
        }
    }

    /**
     * Saves the selected GPX files.
     */
    public void saveGpxFiles() {
        try {
            SaveFilesDialog dialog = new SaveFilesDialog(
                Display.getDefault().getActiveShell(), gpxFileSetModel);
            Boolean success = dialog.open();
            if(success) {
                // The saving is done by the dialog
                // Reset the input to cause the listeners to change
                Object oldInput = treeViewer.getInput();
                treeViewer.setInput(oldInput);
                treeViewer.expandToLevel(treeLevel);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with SaveFilesDialog", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Brings up a FileDialog to prompt for a GPX file group.
     */
    public void openGpxFileGroup() {
        // Open a FileDialog
        FileDialog dlg = new FileDialog(Display.getDefault().getActiveShell(),
            SWT.OPEN);
        String[] ext = {"*.gpxfg"};
        int filterIndex = 0;
        dlg.setFilterExtensions(ext);
        dlg.setFilterIndex(filterIndex);
        String selectedPath = dlg.open();
        if(selectedPath != null) {
            initialFileGroupPath = selectedPath;
            loadGpxFileGroup(selectedPath);
        }
    }

    /**
     * Load the files in a file group.
     */
    public void loadGpxFileGroup(String path) {
        if(gpxFileSetModel == null) {
            return;
        }
        if(path == null || path.isEmpty()) {
            return;
        }
        File file = new File(path);
        if(!file.exists()) {
            SWTUtils.errMsgAsync("File does not exist: " + path);
            return;
        }
        // Parse the file for file names
        BufferedReader in = null;
        long lineNum = 0;
        String line = null;
        GpxFileModel newModel = null;
        File file1 = null;
        String errors = "";
        String LS = SWTUtils.LS;
        int nFiles = 0;
        try {
            in = new BufferedReader(new FileReader(file));
            while((line = in.readLine()) != null) {
                lineNum++;
                file1 = new File(line);
                if(!file1.exists()) {
                    errors += "Skipping nonexistent file: " + path
                        + SWTUtils.LS;
                    continue;
                }
                try {
                    newModel = new GpxFileModel(gpxFileSetModel, line);
                    gpxFileSetModel.add(newModel);
                    nFiles++;
                } catch(Throwable t) {
                    errors += "Error parsing " + line + LS + t.getMessage()
                        + SWTUtils.LS;
                }
            }
        } catch(Exception ex) {
            SWTUtils.errMsg("Error reading " + file.getName() + "\nat line "
                + lineNum + "\n" + ex + "\n" + ex.getMessage());
        } finally {
            try {
                if(in != null) in.close();
            } catch(Exception ex1) {
                // Do nothing
            }
        }
        if(nFiles == 0) {
            SWTUtils.errMsgAsync(
                errors.isEmpty() ? "" : LS + "No valid files found");
        }
        if(!errors.isEmpty()) {
            SWTUtils.errMsgAsync(errors);
        }
    }

    /**
     * Saves a file group.
     */
    public void saveGpxFileGroup() {
        final StringBuffer buf;
        List<GpxFileModel> fileSetModels = gpxFileSetModel.getGpxFileModels();
        buf = new StringBuffer(fileSetModels.size());
        for(GpxFileModel model : fileSetModels) {
            if(!model.getChecked()) {
                continue;
            }
            buf.append(model.getFile().getPath() + SWTUtils.LS);
        }
        if(buf.length() > 0) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    FileDialog dlg = new FileDialog(
                        Display.getDefault().getActiveShell(), SWT.SAVE);
                    String[] ext = {"*.gpxfg"};
                    int filterIndex = 0;
                    dlg.setFilterExtensions(ext);
                    dlg.setFilterIndex(filterIndex);
                    String selectedPath = dlg.open();
                    if(selectedPath != null) {
                        File file = new File(selectedPath);
                        boolean doIt = true;
                        if(file.exists()) {
                            Boolean res = SWTUtils.confirmMsg("File exists: "
                                + file.getPath() + "\nOK to overwrite?");
                            if(!res) {
                                doIt = false;
                            }
                        }
                        if(doIt) {
                            PrintWriter out = null;
                            try {
                                out = new PrintWriter(new FileWriter(file));
                                out.print(buf);
                            } catch(Exception ex) {
                                SWTUtils.excMsgAsync(
                                    "Error writing file group file", ex);
                            } finally {
                                if(out != null) out.close();
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Sets the check state based on the given FindNearOptions.
     * 
     * @param options
     */
    public void setCheckedFromFindNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        // TODO Check this is fixed
        Cursor waitCursor = null;
        Shell shell = null;
        Display display = Display.getCurrent();
        if(display == null) {
            display = Display.getDefault();
        }
        if(display != null) {
            shell = display.getActiveShell();
        }
        if(shell != null) {
            waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            if(waitCursor != null) {
                shell.setCursor(waitCursor);
            }
        }
        treeViewer.getTree().setRedraw(false);
        for(GpxFileModel model : gpxFileSetModel.getGpxFileModels()) {
            fn.processGpxFileModel(model, Mode.CHECK, options);
        }
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh(true);
        if(shell != null) {
            shell.setCursor(null);
            if(waitCursor != null) {
                waitCursor.dispose();
            }
        }
    }

    /**
     * Sets the check state based on the given FindFilesNearOptions. The
     * implementation is inefficient and based on parsing the output from the
     * command-line find. Use addFromFindFilesNear(FindNearOptions) instead.
     * 
     * 
     * @param options
     * @deprecated
     * @see #addFromFindFilesNear(FindNearOptions)
     */
    public void setCheckedFromFindFilesNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        // TODO Check this is fixed
        Cursor waitCursor = null;
        Shell shell = null;
        Display display = Display.getCurrent();
        if(display == null) {
            display = Display.getDefault();
        }
        if(display != null) {
            shell = display.getActiveShell();
        }
        if(shell != null) {
            waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            if(waitCursor != null) {
                shell.setCursor(waitCursor);
            }
        }
        treeViewer.getTree().setRedraw(false);
        String errors = fn.findAndAddToFileSetModel(gpxFileSetModel, options);
        // All, done, restore things
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh(true);
        if(shell != null) {
            shell.setCursor(null);
            if(waitCursor != null) {
                waitCursor.dispose();
            }
        }

        // Display errors
        if(errors != null && errors.length() > 0) {
            boolean res = SWTUtils.confirmMsg(shell,
                "Errors occurred. Want to see them?");
            if(res) {
                ScrolledTextDialog dialog = new ScrolledTextDialog(shell);
                dialog.setDialogTitle("Find Files Near Errors");
                dialog.setGroupLabel("Errors");
                dialog.setMessage(errors);
                dialog.setWidth(600);
                dialog.setHeight(400);
                dialog.open();
            }
        }
    }

    /**
     * Adds trimmed GpxFileModels to the gpxFileSetModel based on the given
     * FindFilesNearOptions.
     * 
     * @param options
     */
    public void addFromFindFilesNear(FindNearOptions options) {
        if(options == null) {
            SWTUtils.errMsgAsync("FindNearOptions is null");
            return;
        }
        FindNear fn = new FindNear(options.getLatitude(),
            options.getLongitude(), options.getRadius(), options.getUnits());
        if(fn == null) {
            SWTUtils.errMsgAsync("Could not create FindNear instance");
            return;
        }
        // TODO Check this is fixed
        Cursor waitCursor = null;
        Shell shell = null;
        Display display = Display.getCurrent();
        if(display == null) {
            display = Display.getDefault();
        }
        if(display != null) {
            shell = display.getActiveShell();
        }
        if(shell != null) {
            waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            if(waitCursor != null) {
                shell.setCursor(waitCursor);
            }
        }
        treeViewer.getTree().setRedraw(false);

        // Null the out PrintStream
        // DEBUG Comment out nulling the out PrintStream
        fn.setOutStream(null);
        // Capture the err PrintStream
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(baosErr);
        fn.setErrStream(err);

        // Don't do GPSL
        options.setDoGpsl(false);
        // Do the search
        fn.find(gpxFileSetModel, options, Mode.ADD);
        err.close();

        // Restore things
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh(true);
        treeViewer.expandToLevel(treeLevel);
        if(shell != null) {
            shell.setCursor(null);
            if(waitCursor != null) {
                waitCursor.dispose();
            }
        }

        // Display errors
        String errors = baosErr.toString();
        if(errors != null && errors.length() > 0) {
            boolean res = SWTUtils.confirmMsg(shell,
                "Errors occurred. Want to see them?");
            if(res) {
                ScrolledTextDialog dialog = new ScrolledTextDialog(shell);
                dialog.setDialogTitle("Find Files Near Errors");
                dialog.setGroupLabel("Errors");
                dialog.setMessage(errors);
                dialog.setWidth(600);
                dialog.setHeight(400);
                dialog.open();
            }
        }
    }

    protected void updateSorter(Action action) {
        // if(action == booksBoxesGamesAction) {
        // noArticleAction.setChecked(!booksBoxesGamesAction.isChecked());
        // if(action.isChecked()) {
        // treeViewer.setSorter(booksBoxesGamesSorter);
        // } else {
        // treeViewer.setSorter(null);
        // }
        // } else if(action == noArticleAction) {
        // booksBoxesGamesAction.setChecked(!noArticleAction.isChecked());
        // if(action.isChecked()) {
        // treeViewer.setSorter(noArticleSorter);
        // } else {
        // treeViewer.setSorter(null);
        // }
        // }
    }

    /* Multiple filters can be enabled at a time. */
    protected void updateFilter(Action action) {
        // if(action == atLeatThreeItems) {
        // if(action.isChecked()) {
        // treeViewer.addFilter(atLeastThreeFilter);
        // } else {
        // treeViewer.removeFilter(atLeastThreeFilter);
        // }
        // } else if(action == onlyBoardGamesAction) {
        // if(action.isChecked()) {
        // treeViewer.addFilter(onlyBoardGamesFilter);
        // } else {
        // treeViewer.removeFilter(onlyBoardGamesFilter);
        // }
        // }
    }

    public GpxFileSetModel getInitalInput() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        boolean useStartupFiles = prefs.getBoolean(P_USE_STARTUP_FILES);
        String startupFilesString = prefs.getString(P_STARTUP_FILES);
        String[] initialFiles;
        ArrayList<String> initialFilesList = new ArrayList<String>();
        try {
            // Check command-line arguments for files
            String[] args = Platform.getCommandLineArgs();
            if(args != null && args.length > 0) {
                File file = null;
                for(String arg : args) {
                    if(arg.startsWith("-")) {
                        continue;
                    }
                    if(arg.equals("net.kenevans.gpxinspector.product")) {
                        continue;
                    }
                    file = new File(arg);
                    if(file.exists()) {
                        initialFilesList.add(arg);
                    }
                }
            }
            // Get initial files from preferences
            // Note a blank string will give 1 blank item, not 0.
            if(!useStartupFiles || startupFilesString == null
                || startupFilesString.length() == 0) {
                initialFiles = new String[0];
            } else {
                initialFiles = startupFilesString.split(STARTUP_FILE_SEPARATOR);
            }
            for(String initialFile : initialFiles) {
                initialFilesList.add(initialFile);
            }
            String[] startupFiles = initialFilesList
                .toArray(new String[initialFilesList.size()]);

            gpxFileSetModel = new GpxFileSetModel(startupFiles);
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error parsing initial input", ex);
        }
        return gpxFileSetModel;
    }

    /*
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
    }

    /**
     * Collapses the tree one level if not already fully collapsed.
     */
    private void collapseTreeToNextLevel() {
        if(treeLevel > 1) {
            // KE: Can't use viewer.collapseToLevel(elementOrTreePath, level)
            // It appears to collapse the levels up to level, rather than above
            // level. There is no viewer.collapseToLevel(level). If this _were_
            // used, the right thing to use for elementOrTreePath is
            // viewer.getInput(), which is the same as the protected
            // viewer.getRoot(). We need to do use this kludge.
            treeLevel--;
            treeViewer.collapseAll();
            treeViewer.expandToLevel(treeLevel);
        }
    }

    /**
     * Expands the tree one level.
     */
    private void expandTreeToNextLevel() {
        if(treeLevel <= MAX_TREE_LEVEL) {
            treeLevel++;
            treeViewer.expandToLevel(treeLevel);
        }
    }

    /**
     * Expands the tree one level.
     */
    private void collapseAll() {
        treeViewer.collapseAll();
        treeLevel = 1;
    }

    /**
     * Save the file in the current GpxFileSetModel as the startup preferences,
     */
    private void saveCheckedAsStartupPreference() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        StringBuffer buf;
        List<GpxFileModel> fileSetModels = gpxFileSetModel.getGpxFileModels();
        buf = new StringBuffer(fileSetModels.size());
        for(GpxFileModel model : fileSetModels) {
            if(!model.getChecked()) {
                continue;
            }
            if(buf.length() > 0) {
                buf.append(STARTUP_FILE_SEPARATOR);
            }
            buf.append(model.getFile().getPath());
        }
        prefs.setValue(P_STARTUP_FILES, buf.toString());
    }

    /**
     * Save the file in the cuurent GpxFileSetModel as the startup preferences,
     */
    private void addStartupFilesFromPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String initalFilesString = prefs.getString(P_STARTUP_FILES);
        String[] initialFiles = initalFilesString.split(STARTUP_FILE_SEPARATOR);
        GpxFileModel newModel = null;
        for(String fileName : initialFiles) {
            try {
                newModel = new GpxFileModel(gpxFileSetModel, fileName);
                gpxFileSetModel.add(newModel);
            } catch(Throwable t) {
                SWTUtils.excMsgAsync("Error parsing " + fileName, t);
            }
        }
    }

    /**
     * Implements cut. Copies items from the selection to the local clipboard
     * then re moves the selected items.
     */
    private void cut() {
        // First do copy
        boolean res = copy();
        if(res) {
            removeSelected();
        }
    }

    /**
     * Copies items from the selection to the local clipboard.
     * 
     * @return If the operation was apparently successful or not.
     */
    private boolean copy() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        if(selection.isEmpty()) {
            SWTUtils.errMsg("Nothing selected to copy");
            return false;
        }
        List<GpxModel> list = new ArrayList<GpxModel>();
        GpxModel model = null;
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            model = (GpxModel)iterator.next();
            try {
                list.add((GpxModel)model.clone());
            } catch(Exception ex) {
                SWTUtils.excMsg("Problem adding to the clipboard", ex);
                ex.printStackTrace();
                return false;
            }
        }
        if(list != null && list.size() > 0) {
            LocalSelection sel = new LocalSelection(list);
            // Clear it to avoid problems disposing GpxFileModels that are not
            // saved
            clearClipboard();
            clipboard.setContents(sel, null);
        }
        return true;
    }

    /**
     * Gets the clipboard contents as a List<GpxModel> or returns null if that
     * is not possible. The list may be empty.
     * 
     * @return The list or null if there is a problem.
     */
    private List<GpxModel> getClipboardList() {
        DataFlavor[] flavors = clipboard.getAvailableDataFlavors();
        if(flavors.length == 0) {
            return null;
        }
        Object data = null;
        try {
            DataFlavor flavor = new DataFlavor(
                "application/x-java-jvm-local-objectref;class=java.util.List");
            if(!clipboard.isDataFlavorAvailable(flavor)) {
                return null;
            }
            data = clipboard.getData(flavor);
        } catch(Exception ex) {
            return null;
        }
        if(data == null) {
            return null;
        }
        // Avoid unchecked cast warning in the smallest possible scope. To check
        // here you would have to use List<?> since you cannot perform
        // instanceof check against parameterized type List<GpxModel>.
        // That generic type information is erased at runtime. Using List<?>
        // doesn't eliminate the warning. Use tempList in the try block as the
        // @SuppressWarnings("unchecked") has to be where the value is declared
        // _and_ set.
        List<GpxModel> clipboardList = null;
        try {
            @SuppressWarnings("unchecked")
            List<GpxModel> tempList = (List<GpxModel>)data;
            clipboardList = tempList;
        } catch(Exception ex) {
            return null;
        }
        if(clipboardList == null || clipboardList.size() == 0) {
            return null;
        }
        return clipboardList;
    }

    /**
     * Clears all items from the clipboard, sets any GpxFileModels to not dirty,
     * and disposes them.
     */
    private void clearClipboard() {
        List<GpxModel> clipboardList = getClipboardList();
        if(clipboardList == null || clipboardList.size() == 0) {
            return;
        }
        for(GpxModel model : clipboardList) {
            if(model instanceof GpxFileModel) {
                ((GpxFileModel)model).setDirty(false);
            }
            model.dispose();
        }
        clipboardList.clear();
        LocalSelection sel = new LocalSelection(clipboardList);
        clipboard.setContents(sel, null);
    }

    /**
     * Pastes the clipboard models into the appropriate target model or its
     * parent. Only uses the first element in the selection. The target model is
     * the first item. Others are ignored except for replace. If the target
     * model is same class as the clipboard model, then the clipboard model is
     * added to its list. If the targetModel is a possible parent of the
     * clipboard model, then the the clipboard model is added to the appropriate
     * list in the parent. Otherwise an error is indicated and the user is
     * prompted to continue or not.<br>
     * <br>
     * BEGINNING: Items will be inserted at the beginning of the list.<br>
     * BEFORE: The target model and the clipboard model must be of the same
     * class. Items will be inserted before the target model in its list.<br>
     * AFTER: The target model and the clipboard model must be of the same
     * class. Items will be inserted after the target model in its list.<br>
     * REPLACE: Same as AFTER except all the selected items are removed after
     * all adding is done.<br>
     * END: Items will be inserted at the end of the list.<br>
     * 
     * @param mode
     */
    private void paste(PasteMode mode) {
        boolean replaceFlag = (mode == PasteMode.REPLACE);
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        if(selection.isEmpty()) {
            // Do nothing
            // Can paste a GpxFileModel at the end of the GPxFileSet
            // If this is not desired, implement the following:
            // SWTUtils.errMsg("Nothing selected for the target");
            // return;
        }
        List<GpxModel> clipboardList = getClipboardList();
        if(clipboardList == null || clipboardList.size() == 0) {
            return;
        }
        // Reverse the list for BEGINNING, AFTER, and REPLACE
        if(mode == PasteMode.BEGINNING || mode == PasteMode.AFTER
            || mode == PasteMode.REPLACE) {
            Collections.reverse(clipboardList);
        }
        // Determine the targetModel to be the first item in the selection list
        GpxModel targetModel = (GpxModel)selection.getFirstElement();
        GpxModel targetParent = null;
        if(targetModel != null) {
            targetParent = targetModel.getParent();
        }
        // Set the treeviewer to not redraw for now
        treeViewer.getTree().setRedraw(false);
        // Loop over the clipboard items
        boolean added = false;
        int iClipboardItem = 0;
        for(GpxModel clipboardModel : clipboardList) {
            iClipboardItem++;
            added = false;
            if(targetModel == null) {
                if(clipboardModel instanceof GpxFileModel) {
                    added = gpxFileSetModel.add((GpxFileModel)targetModel,
                        (GpxFileModel)clipboardModel.clone(), mode);
                }
            } else if(targetModel instanceof GpxFileModel) {
                if(clipboardModel instanceof GpxFileModel) {
                    added = ((GpxFileSetModel)targetParent).add(
                        (GpxFileModel)targetModel,
                        (GpxFileModel)clipboardModel.clone(), mode);
                } else if(mode == PasteMode.BEGINNING
                    || mode == PasteMode.END) {
                    if(clipboardModel instanceof GpxTrackModel) {
                        GpxFileModel fileModel = (GpxFileModel)targetModel;
                        added = fileModel.add(null,
                            (GpxTrackModel)clipboardModel.clone(), mode);
                    } else if(clipboardModel instanceof GpxRouteModel) {
                        GpxFileModel fileModel = (GpxFileModel)targetModel;
                        added = fileModel.add(null,
                            (GpxRouteModel)clipboardModel.clone(), mode);
                    } else if(clipboardModel instanceof GpxWaypointModel) {
                        GpxFileModel fileModel = (GpxFileModel)targetModel;
                        added = fileModel.add(null,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    }
                }
            } else if(targetModel instanceof GpxTrackModel) {
                if(clipboardModel instanceof GpxTrackModel) {
                    added = ((GpxFileModel)targetParent).add(
                        (GpxTrackModel)targetModel,
                        (GpxTrackModel)clipboardModel.clone(), mode);
                } else if(mode == PasteMode.BEGINNING
                    || mode == PasteMode.END) {
                    if(clipboardModel instanceof GpxTrackSegmentModel) {
                        GpxTrackModel trackModel = (GpxTrackModel)targetModel;
                        added = trackModel.add(null,
                            (GpxTrackSegmentModel)clipboardModel.clone(), mode);
                    }
                }
            } else if(targetModel instanceof GpxTrackSegmentModel) {
                if(clipboardModel instanceof GpxTrackSegmentModel) {
                    added = ((GpxTrackModel)targetParent).add(
                        (GpxTrackSegmentModel)targetModel,
                        (GpxTrackSegmentModel)clipboardModel.clone(), mode);
                } else if(mode == PasteMode.BEGINNING
                    || mode == PasteMode.END) {
                    if(clipboardModel instanceof GpxWaypointModel) {
                        GpxTrackSegmentModel trackSegmentModel = (GpxTrackSegmentModel)targetModel;
                        added = trackSegmentModel.add(null,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    }
                }
            } else if(targetModel instanceof GpxRouteModel) {
                if(clipboardModel instanceof GpxRouteModel) {
                    added = ((GpxFileModel)targetParent).add(
                        (GpxRouteModel)targetModel,
                        (GpxRouteModel)clipboardModel.clone(), mode);
                } else if(mode == PasteMode.BEGINNING
                    || mode == PasteMode.END) {
                    if(clipboardModel instanceof GpxWaypointModel) {
                        GpxRouteModel routeModel = (GpxRouteModel)targetModel;
                        added = routeModel.add(null,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    }
                }
            } else if(targetModel instanceof GpxWaypointModel) {
                if(clipboardModel instanceof GpxWaypointModel) {
                    if(targetParent instanceof GpxFileModel) {
                        added = ((GpxFileModel)targetParent).add(
                            (GpxWaypointModel)targetModel,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    } else if(targetParent instanceof GpxTrackSegmentModel) {
                        added = ((GpxTrackSegmentModel)targetParent).add(
                            (GpxWaypointModel)targetModel,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    } else if(targetParent instanceof GpxRouteModel) {
                        added = ((GpxRouteModel)targetParent).add(
                            (GpxWaypointModel)targetModel,
                            (GpxWaypointModel)clipboardModel.clone(), mode);
                    }
                }
            }
            if(!added) {
                // Set it to not replace since there were problems
                replaceFlag = false;
                String from = clipboardModel.getClass().toString();
                int index = from.lastIndexOf("Gpx");
                if(index != -1) {
                    from = from.substring(index);
                }
                String to = targetModel.getClass().toString();
                index = to.lastIndexOf("Gpx");
                if(index != -1) {
                    to = to.substring(index);
                }
                if(iClipboardItem < clipboardList.size()) {
                    boolean res = SWTUtils.confirmMsg(
                        "Failed to add " + from + " to " + to + " for add at "
                            + mode + ".\nPress OK to continue with remaining "
                            + "Clipboard items or Cancel to abort.");
                    if(!res) {
                        break;
                    }

                } else {
                    SWTUtils.errMsg("Failed to add " + from + " to " + to
                        + " for add at " + mode);
                }
            }
        }
        // If the mode is REPLACE, then delete all the items in the selection
        if(mode == PasteMode.REPLACE) {
            if(!replaceFlag) {
                SWTUtils
                    .infoMsg("Asking for REPLACE.  However, since there were\n"
                        + "problems, nothing will be removed.");

            } else {
                removeSelected();
            }
        }
        // Let the treeviewer redraw again
        treeViewer.getTree().setRedraw(true);
    }

    /**
     * Pastes the clipboard models into the appropriate target model or its
     * parent. Only uses the first element in the selection. The target model is
     * the first item. Others are ignored except for replace. If the target
     * model is same class as the clipboard model, then the clipboard model is
     * added to its list. If the targetModel is a possible parent of the
     * clipboard model, then the the clipboard model is added to the appropriate
     * list in the parent. Otherwise an error is indicated and the user is
     * prompted to continue or not.<br>
     * <br>
     * BEGINNING: Items will be inserted at the beginning of the list.<br>
     * BEFORE: The target model and the clipboard model must be of the same
     * class. Items will be inserted before the target model in its list.<br>
     * AFTER: The target model and the clipboard model must be of the same
     * class. Items will be inserted after the target model in its list.<br>
     * REPLACE: Same as AFTER except all the selected items are removed after
     * all adding is done.<br>
     * END: Items will be inserted at the end of the list.<br>
     * 
     * @param mode
     */
    private void mergeTracks() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        if(selection.isEmpty()) {
            return;
        }
        // Determine the targetModel to be the first item in the selection list
        GpxModel targetModel = (GpxModel)selection.getFirstElement();
        GpxModel targetParent = null;
        if(targetModel != null) {
            targetParent = targetModel.getParent();
        }
        // Set the treeviewer to not redraw for now
        treeViewer.getTree().setRedraw(false);
        if(targetModel instanceof GpxTrackModel) {
            // Merge all the segments in all the selected tracks
            for(Iterator<?> iterator = selection.iterator(); iterator
                .hasNext();) {
                GpxTrackModel trackModel = (GpxTrackModel)iterator.next();
                LinkedList<GpxTrackSegmentModel> trackSegmentModels = trackModel
                    .getTrackSegmentModels();
                if(trackSegmentModels.size() < 2) {
                    // Either no models or 1, which doesn't need anything done
                    continue;
                }
                // Get the first segment
                GpxTrackSegmentModel firstSegment = (GpxTrackSegmentModel)trackSegmentModels
                    .getFirst();
                // Keep a list to use for removing segments
                List<GpxTrackSegmentModel> removedList = new ArrayList<GpxTrackSegmentModel>();
                // Loop over segments
                for(GpxTrackSegmentModel segment : trackSegmentModels) {
                    if(segment == firstSegment) {
                        continue;
                    }
                    // Loop over wayPoints in the segment
                    List<GpxWaypointModel> wayPoints = segment
                        .getWaypointModels();
                    removedList.add(segment);
                    for(GpxWaypointModel waypointModel : wayPoints) {
                        firstSegment.add(null,
                            (GpxWaypointModel)waypointModel.clone(),
                            PasteMode.END);
                    }
                }
                // Remove the segments in the removeList. Has to be done outside
                // the loop.
                for(GpxTrackSegmentModel model : removedList) {
                    trackModel.remove(model);
                }
            }
        } else if(targetModel instanceof GpxTrackSegmentModel) {
            // Is a track segment, merge all selected
            // Check they all have the same parent
            for(Iterator<?> iterator = selection.iterator(); iterator
                .hasNext();) {
                GpxModel model = (GpxModel)iterator.next();
                GpxModel parent = model.getParent();
                if(parent != targetParent) {
                    SWTUtils
                        .errMsg("Selected tracks for merge must have the same "
                            + "parent");
                    treeViewer.getTree().setRedraw(true);
                    treeViewer.refresh(true);
                    return;
                }
            }
            // Check they are contiguous
            GpxTrackModel parent = (GpxTrackModel)targetParent;
            List<GpxTrackSegmentModel> segments = parent
                .getTrackSegmentModels();
            int firstIndex = segments.indexOf(targetModel);
            if(firstIndex == -1) {
                SWTUtils.errMsg("Unexpected error: Could not find first "
                    + "selected segment");
                treeViewer.getTree().setRedraw(true);
                treeViewer.refresh(true);
                return;
            }
            ListIterator<GpxTrackSegmentModel> segIterator = segments
                .listIterator(firstIndex);
            for(Iterator<?> iterator = selection.iterator(); iterator
                .hasNext();) {
                GpxTrackSegmentModel selModel = (GpxTrackSegmentModel)iterator
                    .next();
                if(!segIterator.hasNext()) {
                    SWTUtils.errMsg("Unexpected error: Could not find all the "
                        + "selected segments");
                    treeViewer.getTree().setRedraw(true);
                    treeViewer.refresh(true);
                    return;
                }
                GpxTrackSegmentModel segModel = (GpxTrackSegmentModel)segIterator
                    .next();
                if(segModel != selModel) {
                    SWTUtils.errMsg("Selected segments must be contiguous");
                    treeViewer.getTree().setRedraw(true);
                    treeViewer.refresh(true);
                    return;
                }
            }
            // Keep a list to use for removing segments
            List<GpxTrackSegmentModel> removedList = new ArrayList<GpxTrackSegmentModel>();
            // Define the first segment that will hold all the others
            GpxTrackSegmentModel firstSegment = (GpxTrackSegmentModel)targetModel;
            // Merge the selected segments
            for(Iterator<?> iterator = selection.iterator(); iterator
                .hasNext();) {
                GpxTrackSegmentModel trackSegmentModel = (GpxTrackSegmentModel)iterator
                    .next();
                if(trackSegmentModel == firstSegment) {
                    continue;
                }
                // Loop over wayPoints in the segment
                List<GpxWaypointModel> wayPoints = trackSegmentModel
                    .getWaypointModels();
                removedList.add(trackSegmentModel);
                for(GpxWaypointModel waypointModel : wayPoints) {
                    firstSegment.add(null,
                        (GpxWaypointModel)waypointModel.clone(), PasteMode.END);
                }
            }
            // Remove the segments in the removeList. Has to be done outside
            // the loop.
            for(GpxTrackSegmentModel model : removedList) {
                parent.remove(model);
            }
        } else {
            // Should not happen if visibleWhen is implemented correctly
            SWTUtils.errMsg("Unexpected error: Selection contains a "
                + targetModel.getClass().getName());
            return;
        }

        // Let the treeviewer redraw again
        treeViewer.getTree().setRedraw(true);
        treeViewer.refresh(true);
    }

    /**
     * Does the specified task for the currently selected element. Only uses the
     * first element in the selection.
     * 
     * @param task
     */
    public void doTask(Task task) {
        IStructuredSelection selection = (IStructuredSelection)treeViewer
            .getSelection();
        // We can open a new file even if there is no selection
        if(task != Task.NEWFILE && selection.isEmpty()) {
            SWTUtils.errMsg("Nothing selected");
            return;
        }
        // Determine the targetModel to be the first item in the selection list
        GpxModel model = (GpxModel)selection.getFirstElement();
        GpxModel parent = null;
        if(model != null) {
            parent = model.getParent();
        }
        // Set the treeviewer to not redraw for now
        treeViewer.getTree().setRedraw(false);
        // Switch depending on the task
        boolean implemented = false;
        switch(task) {
        case SORT:
            if(parent instanceof GpxFileSetModel) {
                Collections.sort(((GpxFileSetModel)parent).getGpxFileModels());
                parent.fireChangedEvent(parent);
                implemented = true;
            } else if(parent instanceof GpxFileModel) {
                if(model instanceof GpxTrackModel) {
                    Collections.sort(((GpxFileModel)parent).getTrackModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                } else if(model instanceof GpxRouteModel) {
                    Collections.sort(((GpxFileModel)parent).getRouteModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                } else if(model instanceof GpxWaypointModel) {
                    Collections
                        .sort(((GpxFileModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxTrackModel) {
                if(model instanceof GpxTrackSegmentModel) {
                    Collections
                        .sort(((GpxTrackModel)parent).getTrackSegmentModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxTrackSegmentModel) {
                if(model instanceof GpxWaypointModel) {
                    Collections
                        .sort(((GpxRouteModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxRouteModel) {
                if(model instanceof GpxWaypointModel) {
                    Collections
                        .sort(((GpxRouteModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            }
            break;
        case REVERSE:
            if(parent instanceof GpxFileSetModel) {
                Collections
                    .reverse(((GpxFileSetModel)parent).getGpxFileModels());
                parent.fireChangedEvent(parent);
                implemented = true;
            } else if(parent instanceof GpxFileModel) {
                if(model instanceof GpxTrackModel) {
                    Collections
                        .reverse(((GpxFileModel)parent).getTrackModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                } else if(model instanceof GpxRouteModel) {
                    Collections
                        .reverse(((GpxFileModel)parent).getRouteModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                } else if(model instanceof GpxWaypointModel) {
                    Collections
                        .reverse(((GpxFileModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxTrackModel) {
                if(model instanceof GpxTrackSegmentModel) {
                    Collections.reverse(
                        ((GpxTrackModel)parent).getTrackSegmentModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxTrackSegmentModel) {
                if(model instanceof GpxWaypointModel) {
                    Collections
                        .reverse(((GpxRouteModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            } else if(parent instanceof GpxRouteModel) {
                if(model instanceof GpxWaypointModel) {
                    Collections
                        .reverse(((GpxRouteModel)parent).getWaypointModels());
                    parent.fireChangedEvent(parent);
                    implemented = true;
                }
            }
            break;
        case NEWFILE:
            if((model == null || model instanceof GpxFileSetModel)) {
                openGpxFile();
                implemented = true;
            } else if((model instanceof GpxFileModel)) {
                openGpxFile((GpxFileModel)model, PasteMode.AFTER);
                implemented = true;
            }
            break;
        case NEWTRK:
            if((model instanceof GpxFileModel)) {
                GpxTrackModel newModel = new GpxTrackModel(model, null);
                ((GpxFileModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxTrackModel)) {
                GpxTrackModel newModel = new GpxTrackModel(parent, null);
                ((GpxFileModel)parent).add((GpxTrackModel)model, newModel,
                    PasteMode.AFTER);
                implemented = true;
            }
            break;
        case NEWSEG:
            if((model instanceof GpxTrackModel)) {
                GpxTrackSegmentModel newModel = new GpxTrackSegmentModel(model,
                    null);
                ((GpxTrackModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxTrackSegmentModel)) {
                GpxTrackSegmentModel newModel = new GpxTrackSegmentModel(parent,
                    null);
                ((GpxTrackModel)parent).add((GpxTrackSegmentModel)model,
                    newModel, PasteMode.AFTER);
                implemented = true;
            }
            break;
        case NEWRTE:
            if((model instanceof GpxFileModel)) {
                GpxRouteModel newModel = new GpxRouteModel(model, null);
                ((GpxFileModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxRouteModel)) {
                GpxRouteModel newModel = new GpxRouteModel(parent, null);
                ((GpxFileModel)parent).add((GpxRouteModel)model, newModel,
                    PasteMode.AFTER);
                implemented = true;
            }
            break;
        case NEWWPT:
            if((model instanceof GpxFileModel)) {
                GpxWaypointModel newModel = new GpxWaypointModel(model, null);
                ((GpxFileModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxRouteModel)) {
                GpxWaypointModel newModel = new GpxWaypointModel(model, null);
                ((GpxRouteModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxTrackSegmentModel)) {
                GpxWaypointModel newModel = new GpxWaypointModel(model, null);
                ((GpxTrackSegmentModel)model).add(newModel);
                implemented = true;
            } else if((model instanceof GpxWaypointModel)) {
                GpxWaypointModel newModel = new GpxWaypointModel(parent, null);
                if(parent instanceof GpxFileModel) {
                    ((GpxFileModel)parent).add((GpxWaypointModel)model,
                        newModel, PasteMode.AFTER);
                    implemented = true;
                } else if(parent instanceof GpxTrackSegmentModel) {
                    ((GpxTrackSegmentModel)parent).add((GpxWaypointModel)model,
                        newModel, PasteMode.AFTER);
                    implemented = true;
                } else if(parent instanceof GpxRouteModel) {
                    ((GpxRouteModel)parent).add((GpxWaypointModel)model,
                        newModel, PasteMode.AFTER);
                    implemented = true;
                }
            }
            break;
        }
        // Let the treeviewer redraw again
        treeViewer.getTree().setRedraw(true);
        // treeViewer.refresh(true);
        if(!implemented) {
            String modelName = (model == null) ? "Unknown"
                : model.getClass().getSimpleName();
            String parentName = (parent == null) ? "Unknown"
                : parent.getClass().getSimpleName();
            SWTUtils.errMsg("Failed to implement " + task + " for " + modelName
                + " with parent " + parentName);
        }
    }

    /**
     * @return The value of gpxFileSetModel.
     */
    public GpxFileSetModel getGpxFileSetModel() {
        return gpxFileSetModel;
    }

    /**
     * @return The value of findLatitude.
     */
    public double getFindLatitude() {
        return findLatitude;
    }

    /**
     * @param findLatitude The new value for findLatitude.
     */
    public void setFindLatitude(double findLatitude) {
        this.findLatitude = findLatitude;
    }

    /**
     * @return The value of findLongitude.
     */
    public double getFindLongitude() {
        return findLongitude;
    }

    /**
     * @param findLongitude The new value for findLongitude.
     */
    public void setFindLongitude(double findLongitude) {
        this.findLongitude = findLongitude;
    }

    /**
     * @return The value of findRadius.
     */
    public double getFindRadius() {
        return findRadius;
    }

    /**
     * @param findRadius The new value for findRadius.
     */
    public void setFindRadius(double findRadius) {
        this.findRadius = findRadius;
    }

    /**
     * @return The value of findUnits.
     */
    public Units getFindUnits() {
        return findUnits;
    }

    /**
     * @param findUnits The new value for findUnits.
     */
    public void setFindUnits(Units findUnits) {
        this.findUnits = findUnits;
    }

    /**
     * @return The value of gpxDirectory.
     */
    public String getGpxDirectory() {
        return gpxDirectory;
    }

    /**
     * @param gpxDirectory The new value for gpxDirectory.
     */
    public void setGpxDirectory(String gpxDirectory) {
        this.gpxDirectory = gpxDirectory;
    }

    /**
     * Generic debug routine. The implementation may change as needed.
     */
    private void debug() {
        if(true) {
            for(GpxFileModel fileModel : gpxFileSetModel.getGpxFileModels()) {
                System.out.println(GpxFileModel.hierarchyInfo(fileModel));
            }
        }
        if(false) {
            System.out.println("GpxFileSetModel");
            for(GpxFileModel fileModel : gpxFileSetModel.getGpxFileModels()) {
                boolean dirty = fileModel.isDirty();
                System.out.println(dirty + " " + fileModel.getFile().getName());
            }
            System.out.println("ClipboardList");
            List<GpxModel> clipboardList = getClipboardList();
            if(clipboardList == null || clipboardList.size() == 0) {
                return;
            }
            for(GpxModel model : clipboardList) {
                if(model instanceof GpxFileModel) {
                    GpxFileModel fileModel = (GpxFileModel)model;
                    boolean dirty = fileModel.isDirty();
                    System.out
                        .println(dirty + " " + fileModel.getFile().getName());
                }
            }
        }
    }

}
