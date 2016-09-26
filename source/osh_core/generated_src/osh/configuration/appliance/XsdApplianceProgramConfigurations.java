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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ApplianceProgramConfiguration" type="{http://www.aifb.kit.edu/osh}XsdApplianceProgramConfiguration" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "applianceProgramConfiguration"
})
@XmlRootElement(name = "XsdApplianceProgramConfigurations")
public class XsdApplianceProgramConfigurations implements Cloneable
{

    @XmlElement(name = "ApplianceProgramConfiguration", required = true)
    protected List<XsdApplianceProgramConfiguration> applianceProgramConfiguration;

    /**
     * Creates a new {@code XsdApplianceProgramConfigurations} instance.
     * 
     */
    public XsdApplianceProgramConfigurations() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code XsdApplianceProgramConfigurations} instance by deeply copying a given {@code XsdApplianceProgramConfigurations} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public XsdApplianceProgramConfigurations(final XsdApplianceProgramConfigurations o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'XsdApplianceProgramConfigurations' from 'null'.");
        }
        // 'ApplianceProgramConfiguration' collection.
        if (o.applianceProgramConfiguration!= null) {
            copyApplianceProgramConfiguration(o.getApplianceProgramConfiguration(), this.getApplianceProgramConfiguration());
        }
    }

    /**
     * Gets the value of the applianceProgramConfiguration property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applianceProgramConfiguration property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplianceProgramConfiguration().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XsdApplianceProgramConfiguration }
     * 
     * 
     */
    public List<XsdApplianceProgramConfiguration> getApplianceProgramConfiguration() {
        if (applianceProgramConfiguration == null) {
            applianceProgramConfiguration = new ArrayList<XsdApplianceProgramConfiguration>();
        }
        return this.applianceProgramConfiguration;
    }

    /**
     * Copies all values of property {@code ApplianceProgramConfiguration} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyApplianceProgramConfiguration(final List<XsdApplianceProgramConfiguration> source, final List<XsdApplianceProgramConfiguration> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof XsdApplianceProgramConfiguration) {
                    // CClassInfo: edu.kit.aifb.osh.XsdApplianceProgramConfiguration
                    target.add(((XsdApplianceProgramConfiguration) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'ApplianceProgramConfiguration' of class 'edu.kit.aifb.osh.XsdApplianceProgramConfigurations'."));
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
    public XsdApplianceProgramConfigurations clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final XsdApplianceProgramConfigurations clone = ((XsdApplianceProgramConfigurations) super.clone());
                // 'ApplianceProgramConfiguration' collection.
                if (this.applianceProgramConfiguration!= null) {
                    clone.applianceProgramConfiguration = null;
                    copyApplianceProgramConfiguration(this.getApplianceProgramConfiguration(), clone.getApplianceProgramConfiguration());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
