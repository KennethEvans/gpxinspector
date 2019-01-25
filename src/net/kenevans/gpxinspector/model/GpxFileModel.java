package net.kenevans.gpxinspector.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.widgets.Display;

import net.kenevans.gpxinspector.converters.ConverterDescriptor;
import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxinspector.ui.FileInfoDialog;
import net.kenevans.gpxinspector.ui.SaveFilesDialog;
import net.kenevans.gpxinspector.utils.GpxException;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxtrackpointextensionv2.GpxType;
import net.kenevans.gpxtrackpointextensionv2.RteType;
import net.kenevans.gpxtrackpointextensionv2.TrkType;
import net.kenevans.gpxtrackpointextensionv2.TrksegType;
import net.kenevans.gpxtrackpointextensionv2.WptType;
import net.kenevans.gpxtrackpointextensionv2.parser.GPXClone;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxFileModel extends GpxModel implements IGpxElementConstants
{
    private File file;
    private GpxType gpx;
    private LinkedList<GpxTrackModel> trackModels;
    private LinkedList<GpxRouteModel> routeModels;
    private LinkedList<GpxWaypointModel> waypointModels;
    /** Indicates whether the file has changed or not. */
    private boolean dirty = false;
    /** Indicates whether the file was a new file or not. */
    private boolean newFile = false;
    /**
     * Indicates whether the GpxType is unsynchronized or not or not. Note that
     * synchronized is a keyword so unsynchronized is used.
     */
    private boolean unsynchronized = false;

    /**
     * GpxFileModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxFileModel() {
    }

    public GpxFileModel(GpxModel parent, String fileName) throws Throwable {
        this(parent, new File(fileName), false);
    }

    /**
     * GpxFileModel constructor.
     * 
     * @param parent The parent model.
     * @param file The File that contains the file name.
     * @param newFile True if this is a new file that needs to be created rather
     *            than parsed.
     * @throws Throwable
     */
    public GpxFileModel(GpxModel parent, File file, boolean newFile)
        throws Throwable {
        this.parent = parent;
        this.newFile = newFile;
        // DEBUG
        // if(false) {
        // List<ConverterDescriptor> converters = Activator.getDefault()
        // .getConverterDescriptors();
        // for(ConverterDescriptor converter : converters) {
        // System.out.println(file.getName());
        // System.out.println(" " + converter.getName());
        // System.out.println(" " + converter.getId());
        // System.out.println(" Is parse supported: "
        // + converter.isParseSupported(file));
        // System.out.println(" Is save supported: "
        // + converter.isSaveSupported(file));
        // }
        // }
        reset(file, newFile);
    }

    /**
     * Attempts to parse the file represented by the specified File using one of
     * the converters. Returns null on failure. Otherwise returns a GpxType.
     * 
     * @param file A File representing the file to be read.
     * @return A GpxType representing the file or null on failure.
     * @throws Throwable
     */
    public GpxType parse(File file) throws Throwable {
        if(file == null) {
            throw new GpxException("Cannot parse a null file");
        }
        if(!file.exists()) {
            throw new GpxException(
                "File to parse does not exist:\n" + file.getPath());
        }
        // Find the converters
        List<ConverterDescriptor> converters = null;
        try {
            converters = Activator.getDefault().getConverterDescriptors();
            if(converters == null) {
                throw new GpxException("Error locating converters");
            }
            if(converters.size() == 0) {
                throw new GpxException("No converters found");
            }
        } catch(Throwable t) {
            throw new GpxException("Error locating converters", t);
        }
        // Find a converter that will parse the file, then parse it
        for(ConverterDescriptor converter : converters) {
            if(converter.isParseSupported(file)) {
                return converter.parse(file);
            }
        }
        throw new GpxException(
            "No converters found to parse file:\n" + file.getPath());
    }

    /**
     * Attempts to write the GpxType to the specified File using one of the
     * converters.
     * 
     * @param creator Creator information, e.g. "GPX Inspector " +
     *            SWTUtils.getPluginVersion("net.kenevans.gpxinspector").
     * @param gpxType The GpxType to write.
     * @param file A File representing the file to be written.
     * @throws Throwable
     */
    public void save(String creator, GpxType gpxType, File file)
        throws Throwable {
        if(file == null) {
            throw new GpxException("Cannot write a null file");
        }
        // Find the converters
        List<ConverterDescriptor> converters = null;
        try {
            converters = Activator.getDefault().getConverterDescriptors();
            if(converters == null) {
                throw new GpxException("Error locating converters");
            }
            if(converters.size() == 0) {
                throw new GpxException("No converters found");
            }
        } catch(Throwable t) {
            throw new GpxException("Error locating converters", t);
        }
        // Find a converter that will write the file, then save it
        for(ConverterDescriptor converter : converters) {
            if(converter.isSaveSupported(file)) {
                converter.save(creator, gpxType, file);
                return;
            }
        }
        throw new GpxException(
            "No converters found to write file:\n" + file.getPath());
    }

    /**
     * Resets the contents of this model from the given file.
     * 
     * @param file
     * @throws JAXBException
     */
    public void reset(File file, boolean newFile) throws Throwable {
        dirty = false;
        if(newFile) {
            gpx = new GpxType();
            dirty = true;
        } else {
            // try {
            gpx = parse(file);
            // } catch(JAXBException ex) {
            // SWTUtils.excMsg("Error parsing " + file.getPath(), ex);
            // }
        }
        this.file = file;
        trackModels = new LinkedList<GpxTrackModel>();
        List<TrkType> tracks = gpx.getTrk();
        for(TrkType track : tracks) {
            trackModels.add(new GpxTrackModel(this, track));
        }
        routeModels = new LinkedList<GpxRouteModel>();
        List<RteType> routes = gpx.getRte();
        for(RteType route : routes) {
            routeModels.add(new GpxRouteModel(this, route));
        }
        waypointModels = new LinkedList<GpxWaypointModel>();
        List<WptType> waypoints = gpx.getWpt();
        for(WptType waypoint : waypoints) {
            waypointModels.add(new GpxWaypointModel(this, waypoint));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        FileInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            // Synchronize first
            GpxFileModel fileModel = getGpxFileModel();
            if(fileModel != null) {
                fileModel.synchronizeGpx();
            }
            dialog = new FileInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with FileInfoDialog", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#dispose()
     */
    public void dispose() {
        if(disposed) {
            return;
        }
        if(isDirty()) {
            // FIXME
            String msg;
            if(newFile) {
                msg = this.getLabel() + " is new and not saved yet.\n"
                    + "Select OK to do Save As or Cancel to delete without saving.";
                boolean res = SWTUtils.confirmMsg(msg);
                if(res) {
                    SaveFilesDialog.saveAs(this);
                    newFile = false;
                }
            } else {
                msg = this.getLabel() + "\nhas been modified and not saved.\n"
                    + "Select OK to save it or Cancel to delete without saving.";
                boolean res = SWTUtils.confirmMsg(msg);
                if(res) {
                    save();
                }
            }
        }
        for(GpxModel model : trackModels) {
            model.dispose();
        }
        trackModels.clear();
        for(GpxModel model : routeModels) {
            model.dispose();
        }
        routeModels.clear();
        for(GpxModel model : waypointModels) {
            model.dispose();
        }
        waypointModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * Removes an element from the GpxRouteModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxRouteModel model) {
        boolean retVal = routeModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxRouteModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxRouteModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxRouteModel oldModel, GpxRouteModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            routeModels.addFirst(newModel);
            break;
        case BEFORE:
            i = routeModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                routeModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = routeModels.indexOf(oldModel);
            routeModels.add(i + 1, newModel);
            break;
        case END:
            retVal = routeModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Removes an element from the GpxTrackModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxTrackModel model) {
        boolean retVal = trackModels.remove(model);
        // DEBUG
        // System.out.println("remove " + model + " " + model.getParent());
        // System.out.println(" from " + this + " " + this.getParent());
        int n = 0;
        for(GpxTrackModel item : trackModels) {
            System.out.printf("%d %s %s\n", n++, item.toString(),
                item.getParent().toString());
        }
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxTrackModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxTrackModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxTrackModel oldModel, GpxTrackModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            trackModels.addFirst(newModel);
            break;
        case BEFORE:
            i = trackModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                trackModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = trackModels.indexOf(oldModel);
            trackModels.add(i + 1, newModel);
            break;
        case END:
            retVal = trackModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Removes an element from the GpxWaypointModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxWaypointModel model) {
        boolean retVal = waypointModels.remove(model);
        if(retVal) {
            model.dispose();
            fireRemovedEvent(model);
        }
        return retVal;
    }

    /**
     * Adds an element to the GpxFileModel list at the end.
     * 
     * @param newModel The model to be added.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxWaypointModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxWaypointModel list at the position specified by
     * the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxWaypointModel oldModel, GpxWaypointModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            waypointModels.addFirst(newModel);
            break;
        case BEFORE:
            i = waypointModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                waypointModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = waypointModels.indexOf(oldModel);
            waypointModels.add(i + 1, newModel);
            break;
        case END:
            retVal = waypointModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /**
     * Saves the GpxType in the original file.
     * 
     * @return If the save was successful or not.
     */
    public boolean save() {
        return saveAs(this.file);
    }

    /**
     * Saves the GpxType in a new file.
     * 
     * @return If the save was successful or not.
     */
    public boolean saveAs(File file) {
        try {
            synchronizeGpx();
            save(
                "GPX Inspector "
                    + SWTUtils.getPluginVersion("net.kenevans.gpxinspector"),
                gpx, file);
            reset(file, false);
            fireChangedEvent(this);
            // Reset dirty, which was set by fireChangedEvent to true
            setDirty(false);
            return true;
        } catch(Throwable t) {
            SWTUtils.excMsg("Error saving " + file.getPath(), t);
            return false;
        }
    }

    /**
     * Prints the hierarchy of the given GpxFileModel.
     * 
     * @param fileModel
     * @return
     */
    public static String hierarchyInfo(GpxFileModel fileModel) {
        if(fileModel == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("Hierarchy for %s %x\n",
            fileModel.getFile().getName(), fileModel.hashCode()));
        buf.append(String.format("Parent %s %x\n",
            fileModel.getParent().getClass().getName(),
            fileModel.getParent().hashCode()));
        buf.append(String.format("Tracks:\n"));
        for(GpxTrackModel model : fileModel.getTrackModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
            if(true) {
                buf.append(String.format("  Track Segments:\n"));
                for(GpxTrackSegmentModel trackSegmentModel : model
                    .getTrackSegmentModels()) {
                    buf.append(String.format("    %s %x parent %x\n",
                        trackSegmentModel.getLabel(),
                        trackSegmentModel.hashCode(),
                        trackSegmentModel.getParent().hashCode()));
                    if(true) {
                        buf.append(String.format("    Track Points:\n"));
                        for(GpxWaypointModel waypointModel : trackSegmentModel
                            .getWaypointModels()) {
                            buf.append(String.format("      %s %x parent %x\n",
                                waypointModel.getLabel(),
                                trackSegmentModel.hashCode(),
                                waypointModel.getParent().hashCode()));
                        }
                    }
                }
            }
        }
        buf.append(String.format("Routes:\n"));
        for(GpxRouteModel model : fileModel.getRouteModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
        }
        buf.append(String.format("Waypoints:\n"));
        for(GpxWaypointModel model : fileModel.getWaypointModels()) {
            buf.append(String.format("  %s %x parent %x\n", model.getLabel(),
                model.hashCode(), model.getParent().hashCode()));
        }

        return buf.toString();
    }

    /**
     * Synchronizes the GpxType to the current model. Clears any lists and
     * re-adds them from the model.
     */
    public void synchronizeGpx() {
        if(!unsynchronized) {
            return;
        }
        List<GpxTrackModel> tracks = getTrackModels();
        List<TrkType> trkTypes = gpx.getTrk();
        List<GpxTrackSegmentModel> segments = null;
        List<TrksegType> tracksegTypes = null;
        List<GpxWaypointModel> waypoints = null;
        List<WptType> wptTypes = null;
        trkTypes.clear();
        for(GpxTrackModel model : tracks) {
            segments = model.getTrackSegmentModels();
            tracksegTypes = model.getTrack().getTrkseg();
            tracksegTypes.clear();
            for(GpxTrackSegmentModel trackSegmentModel : segments) {
                waypoints = trackSegmentModel.getWaypointModels();
                wptTypes = trackSegmentModel.getTrackseg().getTrkpt();
                wptTypes.clear();
                for(GpxWaypointModel waypointModel : waypoints) {
                    wptTypes.add(waypointModel.getWaypoint());
                }
                tracksegTypes.add(trackSegmentModel.getTrackseg());
            }
            trkTypes.add(model.getTrack());
        }

        List<GpxRouteModel> routes = getRouteModels();
        List<RteType> rteTypes = gpx.getRte();
        waypoints = null;
        wptTypes = null;
        rteTypes.clear();
        for(GpxRouteModel model : routes) {
            waypoints = model.getWaypointModels();
            wptTypes = model.getRoute().getRtept();
            wptTypes.clear();
            for(GpxWaypointModel waypointModel : waypoints) {
                wptTypes.add(waypointModel.getWaypoint());
            }
            rteTypes.add(model.getRoute());
        }

        waypoints = getWaypointModels();
        wptTypes = gpx.getWpt();
        wptTypes.clear();
        for(GpxWaypointModel model : waypoints) {
            wptTypes.add(model.getWaypoint());
        }
        unsynchronized = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxFileModel clone = new GpxFileModel();
        clone.parent = this.parent;
        clone.dirty = this.dirty;
        clone.unsynchronized = this.unsynchronized;
        clone.file = new File(this.file.getPath());
        clone.gpx = GPXClone.clone(this.gpx);

        clone.trackModels = new LinkedList<GpxTrackModel>();
        for(GpxTrackModel model : trackModels) {
            clone.trackModels.add((GpxTrackModel)model.clone());
        }
        clone.routeModels = new LinkedList<GpxRouteModel>();
        for(GpxRouteModel model : routeModels) {
            clone.routeModels.add((GpxRouteModel)model.clone());
        }
        clone.waypointModels = new LinkedList<GpxWaypointModel>();
        for(GpxWaypointModel model : waypointModels) {
            clone.waypointModels.add((GpxWaypointModel)model.clone());
        }

        return clone;
    }

    /**
     * Overrides GpxModel.isDirty() and does not search for a parent
     * GpxFileModel.
     * 
     * @return The value of dirty for this GpxFileModel.
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overrides GpxModel.setDirty() and does not search for a parent
     * GpxFileModel.
     * 
     * @param dirty The new value for dirty for this GpxFileModel.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return The value of file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return The value of gpx.
     */
    public GpxType getGpx() {
        return gpx;
    }

    /**
     * @return The value of trackModels.
     */
    public List<GpxTrackModel> getTrackModels() {
        return trackModels;
    }

    /**
     * @return The value of routeModels.
     */
    public LinkedList<GpxRouteModel> getRouteModels() {
        return routeModels;
    }

    /**
     * @return The value of waypointModels.
     */
    public List<GpxWaypointModel> getWaypointModels() {
        return waypointModels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(file != null) {
            return file.getPath();
        }
        return "Null File";
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#setParent(net.kenevans.
     * gpxinspector .model.GpxModel)
     */
    @Override
    public void setParent(GpxModel parent) {
        this.parent = parent;
        for(GpxTrackModel model : this.getTrackModels()) {
            model.setParent(this);
        }
        for(GpxRouteModel model : this.getRouteModels()) {
            model.setParent(this);
        }
        for(GpxWaypointModel model : this.getWaypointModels()) {
            model.setParent(this);
        }
    }

    /**
     * @return The value of unsynchronized.
     */
    public boolean isUnsynchronized() {
        return unsynchronized;
    }

    /**
     * @param unsynchronized The new value for unsynchronized.
     */
    public void setUnsynchronized(boolean unsynchronized) {
        this.unsynchronized = unsynchronized;
    }

    /**
     * @return The value of newFile.
     */
    public boolean isNewFile() {
        return newFile;
    }

    /**
     * @param newFile The new value for newFile.
     */
    public void setNewFile(boolean newFile) {
        this.newFile = newFile;
    }

}
