package net.kenevans.gpxinspector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.find.FindNearDialog;
import net.kenevans.gpxinspector.utils.find.FindNearOptions;
import net.kenevans.gpxinspector.views.GpxView;

public class FindFilesNearHandler extends AbstractHandler
{
    private static FindNearOptions options;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.
     * commands .ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if(window == null) {
            SWTUtils.errMsg("Cannot determine the workbench window");
            return null;
        }

        // Find the GpxView
        GpxView view = null;
        try {
            view = (GpxView)window.getActivePage()
                .findView("net.kenevans.gpxinspector.gpxView");
            if(view == null) {
                SWTUtils.errMsgAsync("Cannot find GpxView");
                return null;
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error finding GpxView", ex);
        }
        if(view == null) {
            SWTUtils.errMsgAsync("GpxView is null");
            return null;
        }

        if(options == null) {
            options = new FindNearOptions();
            options.setLatitude(view.getFindLatitude());
            options.setLongitude(view.getFindLongitude());
            options.setRadius(view.getFindRadius());
            options.setUnits(view.getFindUnits());
            options.setDirName(view.getGpxDirectory());
        }

        FindNearDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new FindNearDialog(Display.getDefault().getActiveShell(),
                options, FindNearDialog.SOURCE);
            success = dialog.open();
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with FindNearDialog", ex);
            return null;
        }
        if(!success) {
            return null;
        }

        // Get the options
        options = dialog.getOptions();
        if(options == null) {
            SWTUtils.errMsgAsync("Find Options is null");
            return null;
        }
        // GpxFileSetModel fileSetModel = view.getGpxFileSetModel();
        // if(fileSetModel == null) {
        // SWTUtils.errMsgAsync("The KmlFileSetModel is null");
        // return null;
        // }

        // Set the check state based on the options
        view.addFromFindFilesNear(options);

        return null;
    }

}
