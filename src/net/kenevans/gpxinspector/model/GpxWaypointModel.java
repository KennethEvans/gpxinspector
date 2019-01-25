package net.kenevans.gpxinspector.model;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.widgets.Display;

import net.kenevans.gpxinspector.ui.WptInfoDialog;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxtrackpointextensionv2.WptType;
import net.kenevans.gpxtrackpointextensionv2.parser.GPXClone;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxWaypointModel extends GpxModel implements IGpxElementConstants
{
    private WptType waypoint;

    /**
     * GpxWaypointModel constructor which is private with no arguments for use
     * in clone.
     */
    private GpxWaypointModel() {
    }

    public GpxWaypointModel(GpxModel parent, WptType waypoint) {
        this.parent = parent;
        if(waypoint == null) {
            this.waypoint = new WptType();
            this.waypoint.setName("New Waypoint");
            // Required fields
            this.waypoint.setLat(new BigDecimal(0.0));
            this.waypoint.setLon(new BigDecimal(0.0));
        } else {
            this.waypoint = waypoint;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#showInfo()
     */
    @Override
    public void showInfo() {
        WptInfoDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            // Synchronize first
            GpxFileModel fileModel = getGpxFileModel();
            if(fileModel != null) {
                fileModel.synchronizeGpx();
            }
            dialog = new WptInfoDialog(Display.getDefault().getActiveShell(),
                this);
            success = dialog.open();
            if(success) {
                // This also sets dirty
                fireChangedEvent(this);
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with WptInfoDialog", ex);
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

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#clone()
     */
    @Override
    public Object clone() {
        GpxWaypointModel clone = new GpxWaypointModel();
        clone.parent = this.parent;
        clone.waypoint = GPXClone.clone(this.waypoint);

        return clone;
    }

    /**
     * @return The value of waypoint.
     */
    public WptType getWaypoint() {
        return waypoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(waypoint == null) {
            return "Null Waypoint";
        }
        String name = waypoint.getName();
        // Make a label for trackpoints without a name
        if((parent instanceof GpxTrackSegmentModel) && name == null) {
            String info = "";
            try {
                info = String.format(": %.6f, %6f", waypoint.getLat(),
                    waypoint.getLon());
            } catch(Exception ex) {
                // Do nothing
            }
            try {
            	XMLGregorianCalendar time = waypoint.getTime();
            	if(info.length() != 0) {
            		info += ", ";
            	}
            	info += time;
            } catch(Exception ex) {
                // Do nothing
            }
            List<GpxWaypointModel> waypointModels = ((GpxTrackSegmentModel)parent)
                .getWaypointModels();
            if(waypointModels != null) {
                int index = waypointModels.indexOf(this);
                if(index == -1) {
                    return "[Point ?" + info + "]";
                } else {
                    return "[Point " + (index + 1) + info + "]";
                }
            } else {
                return "[Point ??" + info + "]";
            }
        }
        return name;
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
    }

}
