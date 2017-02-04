package net.kenevans.gpxinspector.converters;

import java.io.File;

import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxtrackpointextensionsv1.GpxType;
import net.kenevans.gpxtrackpointextensionsv1.parser.GPXParser;

/*
 * Created on May 11, 2011
 * By Kenneth Evans, Jr.
 */

/**
 * Converts GPX files. GpxConverter
 * 
 * @author Kenneth Evans, Jr.
 */
public class GpxConverter implements IGpxConverter
{
    private static final String[] extensions = {".gpx"};

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.converters.IGpxConverter#getFilterExtensions()
     */
    @Override
    public String getFilterExtensions() {
        String retVal = "";
        for(String ext : extensions) {
            if(retVal.length() > 0) {
                retVal += ";";
            }
            retVal += "*" + ext;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.converters.IGpxConverter#isReadSupported(java
     * .lang.String)
     */
    @Override
    public boolean isParseSupported(File file) {
        String fileExt = "." + SWTUtils.getExtension(file);
        if(fileExt != null) {
            for(String ext : extensions) {
                if(fileExt.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.converters.IGpxConverter#isWriteSupported(java
     * .lang.String)
     */
    @Override
    public boolean isSaveSupported(File file) {
        String fileExt = "." + SWTUtils.getExtension(file);
        if(fileExt != null) {
            for(String ext : extensions) {
                if(fileExt.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.converters.IGpxConverter#parse(java.io.File)
     */
    @Override
    public GpxType parse(File file) throws Throwable {
        return GPXParser.parse(file);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.kenevans.gpxinspector.converters.IGpxConverter#save(net.kenevans.
     * gpx.GpxType, java.io.File)
     */
    @Override
    public void save(String creator, GpxType gpxType, File file)
        throws Throwable {
        // Put the current time in the MetaData
        GPXParser.setMetaDataTime(gpxType);

        GPXParser.save(
            "GPX Inspector "
                + SWTUtils.getPluginVersion("net.kenevans.gpxinspector"),
            gpxType, file);
    }

}
