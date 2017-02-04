package net.kenevans.gpxinspector.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.kenevans.gpxinspector.utils.SWTUtils;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxFileSetModel extends GpxModel
{
    private LinkedList<GpxFileModel> gpxFileModels;
    private String name = "GPX Files";

    /**
     * GpxFileSetModel constructor which is private with no arguments for use in
     * clone.
     */
    private GpxFileSetModel() {
    }

    /**
     * GpxFileSetModel constructor.
     * 
     * @param fileNames
     */
    public GpxFileSetModel(String[] fileNames) {
        disposed = false;
        gpxFileModels = new LinkedList<GpxFileModel>();
        String fileInProgress = null;
        try {
            for(String fileName : fileNames) {
                // Trap any blank items
                if(fileName.length() == 0) {
                    continue;
                }
                fileInProgress = fileName;
                gpxFileModels.add(new GpxFileModel(this, fileName));
            }
        } catch(Throwable t) {
            SWTUtils.excMsgAsync("Error parsing " + fileInProgress, t);
        }
    }

    /**
     * GpxFileSetModel constructor.
     * 
     * @param files
     */
    public GpxFileSetModel(File[] files) {
        disposed = false;
        gpxFileModels = new LinkedList<GpxFileModel>();
        String fileInProgress = null;
        try {
            for(File file : files) {
                gpxFileModels.add(new GpxFileModel(this, file, false));
            }
        } catch(Throwable t) {
            SWTUtils.excMsgAsync("Error parsing " + fileInProgress, t);
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
        for(GpxModel model : gpxFileModels) {
            model.dispose();
        }
        gpxFileModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * Removes an element from the gpxFileModel list.
     * 
     * @param model
     * @return true if this list contained the specified element.
     * @see java.util.List#remove
     */
    public boolean remove(GpxFileModel model) {
        boolean retVal = gpxFileModels.remove(model);
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
    public boolean add(GpxFileModel model) {
        return add(null, model, PasteMode.END);
    }

    /**
     * Adds an element to the GpxFileModel track list at the position specified
     * by the mode relative to the position of the old model.
     * 
     * @param oldModel The old model that specifies the relative location for
     *            the new one. Ignored if the mode is BEGINNING or END.
     * @param newModel The model to be added.
     * @param mode The PasteMode that determines where to place the new model
     *            relative to the old one.
     * @return true if the add appears to be successful.
     */
    public boolean add(GpxFileModel oldModel, GpxFileModel newModel,
        PasteMode mode) {
        boolean retVal = true;
        int i = -1;
        switch(mode) {
        case BEGINNING:
            gpxFileModels.addFirst(newModel);
            break;
        case BEFORE:
            i = gpxFileModels.indexOf(oldModel);
            if(i == -1) {
                retVal = false;
            } else {
                gpxFileModels.add(i, newModel);
            }
            break;
        case REPLACE:
        case AFTER:
            i = gpxFileModels.indexOf(oldModel);
            gpxFileModels.add(i + 1, newModel);
            break;
        case END:
            retVal = gpxFileModels.add(newModel);
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
        GpxFileSetModel clone = new GpxFileSetModel();
        clone.parent = this.parent;
        clone.name = this.name;

        clone.gpxFileModels = new LinkedList<GpxFileModel>();
        for(GpxFileModel model : gpxFileModels) {
            clone.gpxFileModels.add((GpxFileModel)model.clone());
        }

        return clone;
    }

    /**
     * Overrides GpxModel.isDirty() and returns true if any GpxFileModel is
     * dirty.
     * 
     * @return
     */
    @Override
    public boolean isDirty() {
        for(GpxFileModel model : getGpxFileModels()) {
            if(model.isDirty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The value of name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The new value for name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The value of gpxFileModels.
     */
    public List<GpxFileModel> getGpxFileModels() {
        return gpxFileModels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
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
        for(GpxFileModel model : this.getGpxFileModels()) {
            model.setParent(this);
        }
    }

}
