package net.kenevans.gpxinspector.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import net.kenevans.gpxinspector.ui.RteInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxtrackpointextensionsv1.RteType;
import net.kenevans.gpxtrackpointextensionsv1.WptType;
import net.kenevans.gpxtrackpointextensionsv1.parser.GPXClone;

/*
 * Created on Dec 06, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxRouteModel extends GpxModel implements IGpxElementConstants
{
    private RteType route;
    private LinkedList<GpxWaypointModel> waypointModels;

    /**
     * GpxRouteModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxRouteModel() {
    }

    public GpxRouteModel(GpxModel parent, RteType route) {
        this.parent = parent;
        if(route == null) {
            this.route = new RteType();
            this.route.setName("New Route");
        } else {
            this.route = route;
        }
        waypointModels = new LinkedList<GpxWaypointModel>();
        List<WptType> waypoints = this.route.getRtept();
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
        RteInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            // Synchronize first
            GpxFileModel fileModel = getGpxFileModel();
            if(fileModel != null) {
                fileModel.synchronizeGpx();
            }
            dialog = new RteInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with RteInfoDialog", ex);
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

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxRouteModel clone = new GpxRouteModel();
        clone.parent = this.parent;
        clone.route = GPXClone.clone(this.route);
        clone.waypointModels = new LinkedList<GpxWaypointModel>();
        for(GpxWaypointModel model : waypointModels) {
            clone.waypointModels.add((GpxWaypointModel)model.clone());
        }

        return clone;
    }

    /**
     * @return The value of route.
     */
    public RteType getRoute() {
        return route;
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
        if(route != null) {
            return route.getName();
        }
        return "Null Route";
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
        for(GpxWaypointModel model : this.getWaypointModels()) {
            model.setParent(this);
        }
    }

}
