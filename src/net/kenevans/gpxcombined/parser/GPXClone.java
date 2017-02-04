package net.kenevans.gpxcombined.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import net.kenevans.gpxcombined.BoundsType;
import net.kenevans.gpxcombined.CopyrightType;
import net.kenevans.gpxcombined.EmailType;
import net.kenevans.gpxcombined.ExtensionsType;
import net.kenevans.gpxcombined.GpxType;
import net.kenevans.gpxcombined.LinkType;
import net.kenevans.gpxcombined.MetadataType;
import net.kenevans.gpxcombined.PersonType;
import net.kenevans.gpxcombined.PtType;
import net.kenevans.gpxcombined.PtsegType;
import net.kenevans.gpxcombined.RteType;
import net.kenevans.gpxcombined.TrkType;
import net.kenevans.gpxcombined.TrksegType;
import net.kenevans.gpxcombined.WptType;

/*
 * Created on Nov 29, 2010
 * By Kenneth Evans, Jr.
 */

/**
 * GPXCloneProvides the methods to clone the JAXB GPX classes. Note that Strings
 * are immutable and are copied directly. BigInteger and BigDecimal are not,
 * even though the documentation says they are. The reason is that they are not
 * final, so a subclass can break immutability. They are converted using<br>
 * <br>
 * new BigDecimal(srcBigDecimal.toString())<br>
 * <br>
 * There is a one-to-one mapping between the distinguishable BigDecimal values
 * and the result of this conversion. That is, every distinguishable BigDecimal
 * value (unscaled value and scale) has a unique string representation as a
 * result of using toString. If that string representation is converted back to
 * a BigDecimal using the BigDecimal(String) constructor, then the original
 * value will be recovered.<br>
 * <br>
 * BigInteger is cloned the same way. XmlGregorianCalander is not immutable but
 * is Cloneable.<br>
 * <br>
 * List<T> is assumed to be ArrayList<T>. Lists needs to be filled element by
 * element.
 * 
 * @author Kenneth Evans, Jr.
 */
public class GPXClone
{
    /**
     * Creates a clone of the given BoundsType.
     * 
     * @param src
     * @return
     */
    public static BoundsType clone(BoundsType src) {
        if(src == null) {
            return null;
        }
        BoundsType dst = new BoundsType();
        dst.setMaxlat(clone(src.getMaxlat()));
        dst.setMaxlon(clone(src.getMaxlon()));
        dst.setMinlat(clone(src.getMinlat()));
        dst.setMinlon(clone(src.getMinlon()));
        return dst;
    }

    /**
     * Creates a clone of the given CopyrightType.
     * 
     * @param src
     * @return
     */
    public static CopyrightType clone(CopyrightType src) {
        if(src == null) {
            return null;
        }
        CopyrightType dst = new CopyrightType();
        dst.setAuthor(clone(src.getAuthor()));
        dst.setLicense(clone(src.getLicense()));
        dst.setYear(clone(clone(src.getYear())));
        return dst;
    }

    /**
     * Creates a clone of the given EmailType.
     * 
     * @param src
     * @return
     */
    public static EmailType clone(EmailType src) {
        if(src == null) {
            return null;
        }
        EmailType dst = new EmailType();
        dst.setDomain(clone(src.getDomain()));
        dst.setId(clone(clone(src.getId())));
        return dst;
    }

    /**
     * Creates a clone of the given ExtensionsType.
     * 
     * @param src
     * @return
     */
    public static ExtensionsType clone(ExtensionsType src) {
        if(src == null) {
            return null;
        }
        ExtensionsType dst = new ExtensionsType();
        // FIXME note that objects will not be cloned, just copied. See
        // clone(Object).
        clone(src.getAny(), dst.getAny());
        return dst;
    }

    /**
     * Creates a clone of the given GpxType.
     * 
     * @param src
     * @return
     */
    public static GpxType clone(GpxType src) {
        if(src == null) {
            return null;
        }
        GpxType dst = new GpxType();
        dst.setCreator(clone(src.getCreator()));
        dst.setExtensions(clone(src.getExtensions()));
        dst.setMetadata(clone(src.getMetadata()));
        dst.setVersion(clone(src.getVersion()));
        clone(src.getRte(), dst.getRte());
        clone(src.getTrk(), dst.getTrk());
        clone(src.getWpt(), dst.getWpt());
        return dst;
    }

    /**
     * Creates a clone of the given LinkType.
     * 
     * @param src
     * @return
     */
    public static LinkType clone(LinkType src) {
        if(src == null) {
            return null;
        }
        LinkType dst = new LinkType();
        dst.setHref(clone(src.getHref()));
        dst.setText(clone(src.getText()));
        dst.setType(clone(src.getType()));
        return dst;
    }

    /**
     * Creates a clone of the given MetadataType.
     * 
     * @param src
     * @return
     */
    public static MetadataType clone(MetadataType src) {
        if(src == null) {
            return null;
        }
        MetadataType dst = new MetadataType();
        dst.setAuthor(clone(src.getAuthor()));
        dst.setBounds(clone(src.getBounds()));
        dst.setCopyright(clone(src.getCopyright()));
        dst.setDesc(clone(src.getDesc()));
        dst.setExtensions(clone(src.getExtensions()));
        dst.setKeywords(clone(src.getKeywords()));
        dst.setName(clone(src.getName()));
        dst.setTime(clone(src.getTime()));
        clone(src.getLink(), dst.getLink());
        return dst;
    }

    /**
     * Creates a clone of the given PersonType.
     * 
     * @param src
     * @return
     */
    public static PersonType clone(PersonType src) {
        if(src == null) {
            return null;
        }
        PersonType dst = new PersonType();
        dst.setEmail(clone(src.getEmail()));
        dst.setLink(clone(src.getLink()));
        dst.setName(clone(src.getName()));
        return dst;
    }

    /**
     * Creates a clone of the given PtsegType.
     * 
     * @param src
     * @return
     */
    public static PtsegType clone(PtsegType src) {
        if(src == null) {
            return null;
        }
        PtsegType dst = new PtsegType();
        clone(src.getPt(), dst.getPt());
        return dst;
    }

    /**
     * Creates a clone of the given PtType.
     * 
     * @param src
     * @return
     */
    public static PtType clone(PtType src) {
        if(src == null) {
            return null;
        }
        PtType dst = new PtType();
        dst.setEle(clone(src.getEle()));
        dst.setLat(clone(src.getLat()));
        dst.setLon(clone(src.getLon()));
        dst.setTime(clone(src.getTime()));
        return dst;
    }

    /**
     * Creates a clone of the given RteType.
     * 
     * @param src
     * @return
     */
    public static RteType clone(RteType src) {
        if(src == null) {
            return null;
        }
        RteType dst = new RteType();
        dst.setCmt(clone(src.getCmt()));
        dst.setDesc(clone(src.getDesc()));
        dst.setExtensions(clone(src.getExtensions()));
        dst.setName(clone(src.getName()));
        dst.setNumber(clone(src.getNumber()));
        dst.setSrc(clone(src.getSrc()));
        dst.setType(clone(src.getType()));
        clone(src.getLink(), dst.getLink());
        clone(src.getRtept(), dst.getRtept());
        return dst;
    }

    /**
     * Creates a clone of the given TrksegType.
     * 
     * @param src
     * @return
     */
    public static TrksegType clone(TrksegType src) {
        if(src == null) {
            return null;
        }
        TrksegType dst = new TrksegType();
        dst.setExtensions(clone(src.getExtensions()));
        clone(src.getTrkpt(), dst.getTrkpt());
        return dst;
    }

    /**
     * Creates a clone of the given TrkType.
     * 
     * @param src
     * @return
     */
    public static TrkType clone(TrkType src) {
        if(src == null) {
            return null;
        }
        TrkType dst = new TrkType();
        dst.setCmt(clone(src.getCmt()));
        dst.setDesc(clone(src.getDesc()));
        dst.setExtensions(clone(src.getExtensions()));
        dst.setName(clone(src.getName()));
        dst.setNumber(clone(src.getNumber()));
        dst.setSrc(clone(src.getSrc()));
        dst.setType(clone(src.getType()));
        clone(src.getLink(), dst.getLink());
        clone(src.getTrkseg(), dst.getTrkseg());
        return dst;
    }

    /**
     * Creates a clone of the given WptType.
     * 
     * @param src
     * @return
     */
    public static WptType clone(WptType src) {
        if(src == null) {
            return null;
        }
        WptType dst = new WptType();
        dst.setAgeofdgpsdata(clone(src.getAgeofdgpsdata()));
        dst.setCmt(clone(src.getCmt()));
        dst.setDesc(clone(src.getDesc()));
        dst.setDgpsid(clone(src.getDgpsid()));
        dst.setEle(clone(src.getEle()));
        dst.setExtensions(clone(src.getExtensions()));
        dst.setFix(clone(src.getFix()));
        dst.setGeoidheight(clone(src.getGeoidheight()));
        dst.setHdop(clone(src.getHdop()));
        dst.setLat(clone(src.getLat()));
        dst.setLon(clone(src.getLon()));
        dst.setMagvar(clone(src.getMagvar()));
        dst.setName(clone(src.getName()));
        dst.setPdop(clone(src.getPdop()));
        dst.setSat(clone(src.getSat()));
        dst.setSrc(clone(src.getSrc()));
        dst.setSym(clone(src.getSym()));
        dst.setTime(clone(src.getTime()));
        dst.setType(clone(src.getType()));
        dst.setVdop(clone(src.getVdop()));
        clone(src.getLink(), dst.getLink());
        return dst;
    }

    // Other types

    /**
     * Creates a clone of the given Object.
     * 
     * @param src
     * @return
     */
    public static Object clone(Object src) {
        if(src == null) {
            return null;
        }
        // Just return it as we don't know how to do anything else
        Object dst = src;
        return dst;
    }

    /**
     * Creates a clone of the given Integer.
     * 
     * @param src
     * @return
     */
    public static Integer clone(Integer src) {
        if(src == null) {
            return null;
        }
        // Integer is immutable, just return it
        Integer dst = src;
        return dst;
    }

    /**
     * Creates a clone of the given String.
     * 
     * @param src
     * @return
     */
    public static String clone(String src) {
        if(src == null) {
            return null;
        }
        // String is immutable, just return it
        String dst = src;
        return dst;
    }

    /**
     * Creates a clone of the given BigDecimal.
     * 
     * @param src
     * @return
     */
    public static BigDecimal clone(BigDecimal src) {
        if(src == null) {
            return null;
        }
        // There is a one-to-one mapping between the distinguishable BigDecimal
        // values and the result of this conversion (toString()). That is, every
        // distinguishable BigDecimal value (unscaled value and scale) has a
        // unique string representation as a result of using toString. If that
        // string representation is converted back to a BigDecimal using the
        // BigDecimal. We could always just assume it is immutable as the
        // documenation says, and just copy it.
        BigDecimal dst = new BigDecimal(src.toString());
        return dst;
    }

    /**
     * Creates a clone of the given BigInteger.
     * 
     * @param src
     * @return
     */
    public static BigInteger clone(BigInteger src) {
        if(src == null) {
            return null;
        }
        // The Javadoc does not say if this is unique as for BigDecimal. We
        // could always just assume it is immutable as the documenation says,
        // and just copy it.
        BigInteger dst = new BigInteger(src.toString());
        return dst;
    }

    /**
     * Creates a clone of the given XMLGregorianCalendar.
     * 
     * @param src
     * @return
     */
    public static XMLGregorianCalendar clone(XMLGregorianCalendar src) {
        if(src == null) {
            return null;
        }
        // Use clone.
        XMLGregorianCalendar dst = (XMLGregorianCalendar)src.clone();
        return dst;
    }

    /**
     * Creates a clone of the given List<T>. Note that these lists should not be
     * null since the get method creates a new ArrayList if the field was
     * previously null. The method, however, checks for null and does nothing if
     * either is null.<br>
     * <br>
     * This clone method is different from the others in that it takes two
     * arguments. This is related to the fact that there is no set method for
     * List<T> fields.
     * 
     * @param src The source List<T>.
     * @param dst The clone's List<T>.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> void clone(List<T> src, List<T> dst) {
        // Note that these should not be null, since the get method creates a
        // new ArrayList if it was previously null.
        if(dst == null || src == null || src.size() == 0) {
            return;
        }
        for(T item : src) {
            // This is an unchecked cast and cannot be checked since the type
            // information is lost at runtime.
            // Thus use @SuppressWarnings("unchecked")
            dst.add((T)clone(item));
        }
    }

}
