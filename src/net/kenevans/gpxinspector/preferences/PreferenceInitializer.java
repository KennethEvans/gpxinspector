package net.kenevans.gpxinspector.preferences;

import java.util.List;

import net.kenevans.gpxinspector.converters.ConverterDescriptor;
import net.kenevans.gpxinspector.plugin.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
    implements IPreferenceConstants
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        prefs.setDefault(P_GPX_DIR, D_GPX_DIR);
        prefs.setDefault(P_NO_MOVE_SPEED, D_NO_MOVE_SPEED);
        prefs.setDefault(P_STARTUP_FILES, D_STARTUP_FILES);
        prefs.setDefault(P_USE_STARTUP_FILES, D_USE_STARTUP_FILES);

        // We have to determine which index corresponds to the default.
        boolean useConverters = true;
        int defaultIndex = 0;
        List<ConverterDescriptor> converters = null;
        try {
            converters = Activator.getDefault().getConverterDescriptors();
            if(converters == null || converters.size() == 0) {
                useConverters = false;
            }
        } catch(Throwable t) {
            useConverters = false;
        }
        if(useConverters) {
            for(int i = 0; i < converters.size(); i++) {
                String filterExtensions = converters.get(i)
                    .getFilterExtensions();
                if(filterExtensions != null
                    && filterExtensions.equals(D_PREFERRED_FILE_EXTENSION)) {
                    defaultIndex = i;
                }
            }
        }
        prefs.setDefault(P_PREFERRED_FILE_EXTENSION,
            Integer.toString(defaultIndex));

        prefs.setDefault(P_KML_FILENAME, D_KML_FILENAME);
        prefs.setDefault(P_ICON_SCALE, D_ICON_SCALE);

        prefs.setDefault(P_USE_TRK_ICON, D_USE_TRK_ICON);
        prefs.setDefault(P_USE_TRK_TRACK, D_USE_TRK_TRACK);
        prefs.setDefault(P_TRK_COLOR, D_TRK_COLOR);
        prefs.setDefault(P_TRK_ALPHA, D_TRK_ALPHA);
        prefs.setDefault(P_TRK_LINEWIDTH, D_TRK_LINEWIDTH);
        prefs.setDefault(P_TRK_COLOR_MODE, D_TRK_COLOR_MODE);
        prefs.setDefault(P_TRK_ICON_URL, D_TRK_ICON_URL);

        prefs.setDefault(P_USE_RTE_ICON, D_USE_RTE_ICON);
        prefs.setDefault(P_RTE_ICON_URL, D_RTE_ICON_URL);
        prefs.setDefault(P_RTE_LINEWIDTH, D_RTE_LINEWIDTH);
        prefs.setDefault(P_RTE_COLOR_MODE, D_RTE_COLOR_MODE);
        prefs.setDefault(P_RTE_COLOR, D_RTE_COLOR);
        prefs.setDefault(P_RTE_ALPHA, D_RTE_ALPHA);

        prefs.setDefault(P_WPT_ICON_URL, D_WPT_ICON_URL);
        prefs.setDefault(P_WPT_COLOR, D_WPT_COLOR);
        prefs.setDefault(P_WPT_ALPHA, D_WPT_ALPHA);
        prefs.setDefault(P_TRK_COLOR_MODE, D_TRK_COLOR_MODE);

        prefs.setDefault(P_KML_PROMPT_TO_OVERWRITE, D_KML_PROMPT_TO_OVERWRITE);
        prefs.setDefault(P_KML_SEND_TO_GOOGLE_EARTH,
            D_KML_SEND_TO_GOOGLE_EARTH);
    }

}
