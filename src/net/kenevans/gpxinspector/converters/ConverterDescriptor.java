package net.kenevans.gpxinspector.converters;

import java.io.File;
import java.util.List;

import net.kenevans.gpxinspector.plugin.Activator;
import net.kenevans.gpxtrackpointextensionv2.GpxType;

import org.eclipse.core.runtime.IConfigurationElement;

/*
 * Created on May 11, 2011
 * By Kenneth Evans, Jr.
 */

/**
 * ConverterDescriptor is a proxy for a converter. It stores the information
 * from the plug-in registry, but will not load the defining plug-in until it is
 * necessary to load its required class.
 * 
 * @author Kenneth Evans, Jr.
 */
public class ConverterDescriptor implements IGpxConverter {
	private IConfigurationElement ce;

	/**
	 * ConverterDescriptor constructor.
	 * 
	 * @param ce The IConfigurationElement used to determine the parameters.
	 */
	public ConverterDescriptor(IConfigurationElement ce) {
		this.ce = ce;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.converters.IGpxConverter#getFilterExtensions()
	 */
	@Override
	public String getFilterExtensions() {
		if (ce == null) {
			return null;
		}
		try {
			IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
			return converter.getFilterExtensions();
		} catch (Throwable t) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.kenevans.gpxinspector.converters.IGpxConverter#getPreferredExtension()
	 */
	@Override
	public String getPreferredExtension() {
		if (ce == null) {
			return null;
		}
		try {
			IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
			return converter.getPreferredExtension();
		} catch (Throwable t) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.converters.IGpxConverter#isReadSupported(java
	 * .io.File)
	 */
	@Override
	public boolean isParseSupported(File file) {
		if (ce == null || file == null) {
			return false;
		}
		try {
			IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
			return converter.isParseSupported(file);
		} catch (Throwable t) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.converters.IGpxConverter#isWriteSupported(java
	 * .io.File)
	 */
	@Override
	public boolean isSaveSupported(File file) {
		if (ce == null || file == null) {
			return false;
		}
		try {
			IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
			return converter.isSaveSupported(file);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Gets the id of the converter.
	 * 
	 * @return
	 */
	public String getId() {
		if (ce == null) {
			return null;
		}
		return ce.getAttribute("id");
	}

	/**
	 * Gets the name of the converter.
	 * 
	 * @return
	 */
	public String getName() {
		if (ce == null) {
			return null;
		}
		return ce.getAttribute("name");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.kenevans.gpxinspector.converters.IGpxConverter#save(net.kenevans.
	 * gpx.GpxType, java.io.File)
	 */
	@Override
	public void save(String creator, GpxType gpxType, File file) throws Throwable {
		if (ce == null || file == null) {
			return;
		}
		IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
		converter.save(creator, gpxType, file);
	}

	@Override
	public GpxType parse(File file) throws Throwable {
		if (ce == null || file == null) {
			return null;
		}
		IGpxConverter converter = (IGpxConverter) ce.createExecutableExtension("class");
		return converter.parse(file);
	}

	/**
	 * Static method to check if parse is supported by any converter for the given
	 * File. Use to avoid trying to read irrelevant files.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isAnyParseSupported(File file) {
		List<ConverterDescriptor> converters = Activator.getDefault().getConverterDescriptors();
		for (ConverterDescriptor converter : converters) {
			if (converter.isParseSupported(file)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Static method to check if save is supported by any converter for the given
	 * File. Use to avoid trying to save irrelevant files.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isAnySaveSupported(File file) {
		List<ConverterDescriptor> converters = Activator.getDefault().getConverterDescriptors();
		for (ConverterDescriptor converter : converters) {
			if (converter.isSaveSupported(file)) {
				return true;
			}
		}
		return false;
	}

}
