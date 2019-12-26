package net.kenevans.gpxinspector.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import net.kenevans.gpxinspector.converters.ConverterDescriptor;
import net.kenevans.gpxinspector.utils.SWTUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

    // The plug-in ID
    public static final String PLUGIN_ID = "net.kenevans.gpxinspector";
    public static final String CONVERTERS_EXTENSION_POINT_ID = "converters";
    /** A list of converter proxies. */
    private List<ConverterDescriptor> converterDescriptors;

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        processExtensions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static Optional<ImageDescriptor> getImageDescriptor(String path) {
        return ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, path);
    }

    /**
     * Looks at the extension registry for converters. (There should at least be
     * the one for GPX files.)
     */
    private void processExtensions() {
        converterDescriptors = new ArrayList<ConverterDescriptor>();
        IExtensionPoint point = Platform.getExtensionRegistry()
            .getExtensionPoint(PLUGIN_ID, CONVERTERS_EXTENSION_POINT_ID);

        if(point != null) {
            IExtension[] extensions = point.getExtensions();

            for(int i = 0; i < extensions.length; i++) {
                IConfigurationElement[] ces = extensions[i]
                    .getConfigurationElements();
                for(int j = 0; j < ces.length; j++) {
                    converterDescriptors.add(new ConverterDescriptor(ces[j]));
                }
            }
        }

        // Check if no extensions or empty extensions
        if(point == null || converterDescriptors.size() == 0) {
            // Write this fact to the log
            IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, 0,
                "Failed to find any Converters.", null);
            getDefault().getLog().log(status);
            SWTUtils.errMsgAsync("Failed to find any Converters");
        }
    }

    /**
     * @return The list of converterDescriptors.
     */
    public List<ConverterDescriptor> getConverterDescriptors() {
        return converterDescriptors;
    }

}
