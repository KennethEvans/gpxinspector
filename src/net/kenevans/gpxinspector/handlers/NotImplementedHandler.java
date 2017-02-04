package net.kenevans.gpxinspector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import net.kenevans.gpxinspector.utils.SWTUtils;

/**
 * Generic handler that display a Not Implemented dialog.
 * 
 * @author evans
 * 
 */
public class NotImplementedHandler extends AbstractHandler
{
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
        SWTUtils.infoMsg(window.getShell(),
            "Sorry, this feature is not implemented yet");

        // Must currently be null
        return null;
    }
}
