package net.kenevans.gpxinspector.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/*
 * Created on Nov 18, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * LabeledText creates a zero-margin Composite with a Label and a Text.
 * 
 * @author Kenneth Evans, Jr.
 */
public class LabeledText
{
    private Composite composite;
    private Label label;
    private Text text;

    /**
     * LabeledText constructor.
     * 
     * @param parent The parent of the composite.
     * @param labelText The text for the Label.
     * @param textWidth The suggested width in columns for the Text.
     */
    public LabeledText(Composite parent, String labelText, int textWidth) {
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

        text = new Text(composite, SWT.NONE);
        GridDataFactory.fillDefaults()
            .hint(
                new Point(SWTUtils.getTextWidth(text, textWidth), SWT.DEFAULT))
            .align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(text);
    }

    /**
     * Converts the value of text.getText() to a String. Returns null if the
     * text is not editable.
     * 
     * @param text
     * @return The converted value or null on error.
     */
    public static String toString(Text text) {
        if(text.isDisposed() || !text.getEditable()) {
            return null;
        }
        String val = text.getText();
        if(val == null || val.equals("null")) {
            return null;
        } else {
            return val;
        }
    }

    /**
     * Converts the value of text.getText() to an Integer destination. Returns
     * null if the text is not editable.
     * 
     * @param text
     * @return The converted value or null on error.
     */
    public static Integer toInteger(Text text) {
        if(text.isDisposed() || !text.getEditable()) {
            return null;
        }
        String val = text.getText();
        if(val == null || val.equals("null")) {
            return null;
        } else {
            try {
                return Integer.parseInt(val);
            } catch(Exception ex) {
                String msg = "Failed to convert \"" + val + "\" to Integer";
                SWTUtils.excMsg(msg, ex);
                return null;
            }
        }
    }

    /**
     * Converts the value of text.getText() to a BigInteger destination. Returns
     * null if the text is not editable.
     * 
     * @param text
     * @return The converted value or null on error.
     */
    public static BigInteger toBigInteger(Text text) {
        if(text.isDisposed() || !text.getEditable()) {
            return null;
        }
        String val = text.getText();
        if(val == null || val.equals("null")) {
            return null;
        } else {
            try {
                return new BigInteger(val);
            } catch(Exception ex) {
                String msg = "Failed to convert \"" + val + "\" to BigInteger";
                SWTUtils.excMsg(msg, ex);
                return null;
            }
        }
    }

    /**
     * Converts the value of text.getText() to a BigDecimal destination. Returns
     * null if the text is not editable.
     * 
     * @param text
     * @return The converted value or null on error.
     */
    public static BigDecimal toBigDecimal(Text text) {
        if(text.isDisposed() || !text.getEditable()) {
            return null;
        }
        String val = text.getText();
        if(val == null || val.equals("null")) {
            return null;
        } else {
            try {
                return new BigDecimal(val);
            } catch(Exception ex) {
                String msg = "Failed to convert \"" + val + "\" to BigDecimal";
                SWTUtils.excMsg(msg, ex);
                return null;
            }
        }
    }

    /**
     * Converts the value of text.getText() to a XMLGregorianCalendar
     * destination. Returns null if the text is not editable.
     * 
     * @param text
     * @return The converted value or null on error.
     */
    public static XMLGregorianCalendar toXMLGregorianCalendar(Text text) {
        if(text.isDisposed() || !text.getEditable()) {
            return null;
        }
        String val = text.getText();
        if(val == null || val.equals("null")) {
            return null;
        } else {
            DatatypeFactory dtf = null;
            try {
                dtf = DatatypeFactory.newInstance();
                return dtf.newXMLGregorianCalendar(val);
            } catch(Exception ex) {
                String msg = "Failed to convert \"" + val
                    + "\" to XMLGregorianCalendar";
                SWTUtils.excMsg(msg, ex);
                return null;
            }
        }
    }

    /**
     * Sets text.setText() from a String source.
     * 
     * @param text
     * @param src
     */
    public static void read(Text text, String src) {
        if(text.isDisposed()) {
            return;
        }
        if(src == null) {
            text.setText("null");
        } else {
            text.setText(src);
        }
    }

    /**
     * Sets text.setText() from an Integer source.
     * 
     * @param text
     * @param src
     */
    public static void read(Text text, Integer src) {
        if(text.isDisposed()) {
            return;
        }
        if(src == null) {
            text.setText("null");
        } else {
            text.setText(src.toString());
        }
    }

    /**
     * Sets text.setText() from a BigInteger source.
     * 
     * @param text
     * @param src
     */
    public static void read(Text text, BigInteger src) {
        if(text.isDisposed()) {
            return;
        }
        if(src == null) {
            text.setText("null");
        } else {
            text.setText(src.toString());
        }
    }

    /**
     * Sets text.setText() from a BigDecimal source.
     * 
     * @param text
     * @param src
     */
    public static void read(Text text, BigDecimal src) {
        if(text.isDisposed()) {
            return;
        }
        if(src == null) {
            text.setText("null");
        } else {
            text.setText(src.toString());
        }
    }

    /**
     * Sets text.setText() from a XMLGregorianCalendar source.
     * 
     * @param text
     * @param src
     */
    public static void read(Text text, XMLGregorianCalendar src) {
        if(text.isDisposed()) {
            return;
        }
        if(src == null) {
            text.setText("null");
        } else {
            text.setText(src.toString());
        }
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
     * @return The value of text.
     */
    public Text getText() {
        return text;
    }

}
