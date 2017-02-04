package net.kenevans.gpxinspector.utils;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/*
 * Created on Nov 18, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * LabeledList creates a zero-margin Composite with a Label and a List.
 * 
 * @author Kenneth Evans, Jr.
 */
public class LabeledList
{
    private Composite composite;
    private Label label;
    private List list;

    /**
     * LabeledList constructor.
     * 
     * @param parent The parent of the composite.
     * @param labelText The text for the Label.
     * @param textWidth The suggested width in columns for the Text.
     * @param textHeight The suggested width in rows for the Text.
     */
    public LabeledList(Composite parent, String labelText, int textWidth,
        int textHeight) {
        // Make a zero margin composite
        composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
            .grab(true, false).applyTo(composite);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        GridDataFactory.fillDefaults().applyTo(label);

        list = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridDataFactory.fillDefaults()
            .hint(new Point(SWTUtils.getTextWidth(list, textWidth),
                SWTUtils.getTextHeight(list, textHeight)))
            .align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(list);
    }

    /**
     * @return The value of composite.
     */
    public Composite getComposite() {
        return composite;
    }

    /**
     * @return The value of label.
     */
    public Label getLabel() {
        return label;
    }

    /**
     * @return The value of list.
     */
    public List getList() {
        return list;
    }

}
