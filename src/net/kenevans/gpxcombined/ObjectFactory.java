//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.22 at 02:36:55 PM CDT 
//


package net.kenevans.gpxcombined;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.kenevans.gpxcombined package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Gpx_QNAME = new QName("http://www.topografix.com/GPX/1/1", "gpx");
    private final static QName _TrackPointExtension_QNAME = new QName("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", "TrackPointExtension");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.kenevans.gpxcombined
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EmailType }
     * 
     */
    public EmailType createEmailType() {
        return new EmailType();
    }

    /**
     * Create an instance of {@link ExtensionsT }
     * 
     */
    public ExtensionsT createExtensionsT() {
        return new ExtensionsT();
    }

    /**
     * Create an instance of {@link LinkType }
     * 
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link TrackPointExtensionT }
     * 
     */
    public TrackPointExtensionT createTrackPointExtensionT() {
        return new TrackPointExtensionT();
    }

    /**
     * Create an instance of {@link PtType }
     * 
     */
    public PtType createPtType() {
        return new PtType();
    }

    /**
     * Create an instance of {@link PersonType }
     * 
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link WptType }
     * 
     */
    public WptType createWptType() {
        return new WptType();
    }

    /**
     * Create an instance of {@link RteType }
     * 
     */
    public RteType createRteType() {
        return new RteType();
    }

    /**
     * Create an instance of {@link Oruxmapsextensions }
     * 
     */
    public Oruxmapsextensions createOruxmapsextensions() {
        return new Oruxmapsextensions();
    }

    /**
     * Create an instance of {@link BoundsType }
     * 
     */
    public BoundsType createBoundsType() {
        return new BoundsType();
    }

    /**
     * Create an instance of {@link Oruxmapsextensions.Ext }
     * 
     */
    public Oruxmapsextensions.Ext createOruxmapsextensionsExt() {
        return new Oruxmapsextensions.Ext();
    }

    /**
     * Create an instance of {@link MetadataType }
     * 
     */
    public MetadataType createMetadataType() {
        return new MetadataType();
    }

    /**
     * Create an instance of {@link PtsegType }
     * 
     */
    public PtsegType createPtsegType() {
        return new PtsegType();
    }

    /**
     * Create an instance of {@link CopyrightType }
     * 
     */
    public CopyrightType createCopyrightType() {
        return new CopyrightType();
    }

    /**
     * Create an instance of {@link ExtensionsType }
     * 
     */
    public ExtensionsType createExtensionsType() {
        return new ExtensionsType();
    }

    /**
     * Create an instance of {@link TrkType }
     * 
     */
    public TrkType createTrkType() {
        return new TrkType();
    }

    /**
     * Create an instance of {@link GpxType }
     * 
     */
    public GpxType createGpxType() {
        return new GpxType();
    }

    /**
     * Create an instance of {@link TrksegType }
     * 
     */
    public TrksegType createTrksegType() {
        return new TrksegType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpxType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.topografix.com/GPX/1/1", name = "gpx")
    public JAXBElement<GpxType> createGpx(GpxType value) {
        return new JAXBElement<GpxType>(_Gpx_QNAME, GpxType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrackPointExtensionT }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.garmin.com/xmlschemas/TrackPointExtension/v1", name = "TrackPointExtension")
    public JAXBElement<TrackPointExtensionT> createTrackPointExtension(TrackPointExtensionT value) {
        return new JAXBElement<TrackPointExtensionT>(_TrackPointExtension_QNAME, TrackPointExtensionT.class, null, value);
    }

}
