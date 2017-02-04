package net.kenevans.gpxinspector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.views.GpxView;

/*
 * Created on Sep 2, 2010
 * By Kenneth Evans, Jr.
 */

public class OpenGpxFileHandler extends AbstractHandler
{
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
     * .ExecutionEvent)
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
            view = (GpxView)window.getActivePage().findView(
                "net.kenevans.gpxinspector.gpxView");
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

        // Run the method
        view.openGpxFile();

        // Must currently be null
        return null;
    }

}
