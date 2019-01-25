package net.kenevans.gpxinspector.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import net.kenevans.gpxinspector.ui.TrkInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxtrackpointextensionv2.TrkType;
import net.kenevans.gpxtrackpointextensionv2.TrksegType;
import net.kenevans.gpxtrackpointextensionv2.parser.GPXClone;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxTrackModel extends GpxModel implements IGpxElementConstants
{
    private TrkType track;
    private LinkedList<GpxTrackSegmentModel> trackSegmentModels;

    /**
     * GpxTrackModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxTrackModel() {
    }

    public GpxTrackModel(GpxModel parent, TrkType track) {
        this.parent = parent;
        if(track == null) {
            this.track = new TrkType();
            this.track.setName("New Track");
        } else {
            this.track = track;
        }
        trackSegmentModels = new LinkedList<GpxTrackSegmentModel>();
        List<TrksegType> trackSegments = this.track.getTrkseg();
        for(TrksegType trackSegment : trackSegments) {
            trackSegmentModels
                .add(new GpxTrackSegmentModel(this, trackSegment));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        TrkInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            // Synchronize first
            GpxFileModel fileModel = getGpxFileModel();
            if(fileModel != null) {
                fileModel.synchronizeGpx();
            }
            dialog = new TrkInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with TrkInfoDialog", ex);
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
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * Removes an element from the GpxTrackSegmentModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxTrackSegmentModel model) {
        boolean retVal = trackSegmentModels.remove(model);
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
    public boolean add(GpxTrackSegmentModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxTrackSegmentModel list at the position
     * specified by the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxTrackSegmentModel oldModel,
        GpxTrackSegmentModel newModel, PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            trackSegmentModels.addFirst(newModel);
            break;
        case BEFORE:
            i = trackSegmentModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                trackSegmentModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = trackSegmentModels.indexOf(oldModel);
            trackSegmentModels.add(i + 1, newModel);
            break;
        case END:
            retVal = trackSegmentModels.add(newModel);
            break;
        }
        if(retVal) {
            newModel.setParent(this);
            fireAddedEvent(newModel);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxTrackModel clone = new GpxTrackModel();
        clone.parent = this.parent;
        clone.track = GPXClone.clone(this.track);
        clone.trackSegmentModels = new LinkedList<GpxTrackSegmentModel>();
        for(GpxTrackSegmentModel model : trackSegmentModels) {
            clone.trackSegmentModels.add((GpxTrackSegmentModel)model.clone());
        }

        return clone;
    }

    /**
     * @return The value of track.
     */
    public TrkType getTrack() {
        return track;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(track != null) {
            return track.getName();
        }
        return "Null Track";
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
        for(GpxTrackSegmentModel model : this.getTrackSegmentModels()) {
            model.setParent(this);
        }
    }

    /**
     * @return The value of trackSegmentModels.
     */
    public LinkedList<GpxTrackSegmentModel> getTrackSegmentModels() {
        return trackSegmentModels;
    }

}
