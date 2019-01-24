package net.kenevans.gpxinspector.converters;

import java.io.File;

import net.kenevans.gpxtrackpointextensionsv1.GpxType;

/*
 * Created on May 11, 2011
 * By Kenneth Evans, Jr.
 */

public interface IGpxConverter
{
    /**
     * Returns the filter extensions supported by this converter. These are the
     * filter extensions used by FileDialog. They are separated with a
     * semi-colon if there are more than one and are typically of the form
     * "*.xxx;*.yyy".
     * 
     * @return
     * 
     * @see org.eclipse.swt.widgets.FileDialog#setFilterExtensions (String []
     *      extensions)
     */
    public String getFilterExtensions();
    
    /**
     * Returns the preferred extension (without the dot) for this converter.
     * 
     * @return
     */
    public String getPreferredExtension();

    /**
     * Returns whether this converter supports reading for the given file name.
     * 
     * @param file A File representing the file to be read. Typically only the
     *            extension is used.
     * @return
     */
    public boolean isParseSupported(File file);

    /**
     * Returns whether this converter supports writing for the given file name.
     * 
     * @param file A File representing the file to be written. Typically only
     *            the extension is used.
     * @return
     */
    public boolean isSaveSupported(File file);

    /**
     * Attempts to parse the file represented by the specified File. Returns
     * null on failure. Otherwise returns a GpxType.
     * 
     * @param file A File representing the file to be read.
     * @return A GpxType representing the file or null on failure.
     * @throws Throwable
     */
    public GpxType parse(File file) throws Throwable;

    /**
     * Attempts to write the GpxType to the specified File.
     * 
     * @param creator Creator information, e.g. "GPX Inspector " +
     *            SWTUtils.getPluginVersion("net.kenevans.gpxinspector").
     * @param gpxType The GpxType to write.
     * @param file A File representing the file to be written.
     * @throws Throwable
     */
    public void save(String creator, GpxType gpxType, File file)
        throws Throwable;

}
