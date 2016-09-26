//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.08 um 02:06:24 PM CET 
//


package osh.configuration.appliance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für XsdLoadProfiles complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="XsdLoadProfiles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LoadProfile" type="{http://www.aifb.kit.edu/osh}XsdLoadProfile" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XsdLoadProfiles", propOrder = {
    "loadProfile"
})
public class XsdLoadProfiles implements Cloneable
{

    @XmlElement(name = "LoadProfile", required = true)
    protected List<XsdLoadProfile> loadProfile;

    /**
     * Creates a new {@code XsdLoadProfiles} instance.
     * 
     */
    public XsdLoadProfiles() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code XsdLoadProfiles} instance by deeply copying a given {@code XsdLoadProfiles} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public XsdLoadProfiles(final XsdLoadProfiles o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'XsdLoadProfiles' from 'null'.");
        }
        // 'LoadProfile' collection.
        if (o.loadProfile!= null) {
            copyLoadProfile(o.getLoadProfile(), this.getLoadProfile());
        }
    }

    /**
     * Gets the value of the loadProfile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the loadProfile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLoadProfile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XsdLoadProfile }
     * 
     * 
     */
    public List<XsdLoadProfile> getLoadProfile() {
        if (loadProfile == null) {
            loadProfile = new ArrayList<XsdLoadProfile>();
        }
        return this.loadProfile;
    }

    /**
     * Copies all values of property {@code LoadProfile} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyLoadProfile(final List<XsdLoadProfile> source, final List<XsdLoadProfile> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof XsdLoadProfile) {
                    // CClassInfo: edu.kit.aifb.osh.XsdLoadProfile
                    target.add(((XsdLoadProfile) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'LoadProfile' of class 'edu.kit.aifb.osh.XsdLoadProfiles'."));
            }
        }
    }

    /**
     * Creates and returns a deep copy of this object.
     * 
     * 
     * @return
     *     A deep copy of this object.
     */
    @Override
    public XsdLoadProfiles clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final XsdLoadProfiles clone = ((XsdLoadProfiles) super.clone());
                // 'LoadProfile' collection.
                if (this.loadProfile!= null) {
                    clone.loadProfile = null;
                    copyLoadProfile(this.getLoadProfile(), clone.getLoadProfile());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
