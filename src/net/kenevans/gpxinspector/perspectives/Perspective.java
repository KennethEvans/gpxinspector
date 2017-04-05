package net.kenevans.gpxinspector.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class Perspective implements IPerspectiveFactory
{
    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        layout.setFixed(true);
        layout.addStandaloneView("net.kenevans.gpxinspector.gpxView", true,
            IPageLayout.TOP, 1.0f, IPageLayout.ID_EDITOR_AREA);
        IViewLayout view = layout
            .getViewLayout("net.kenevans.gpxinspector.gpxView");
        view.setCloseable(false);
        view.setMoveable(false);
    }

}
