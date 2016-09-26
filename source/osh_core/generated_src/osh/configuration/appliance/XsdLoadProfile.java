//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.08 um 02:06:24 PM CET 
//


package osh.configuration.appliance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für XsdLoadProfile complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="XsdLoadProfile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Phases" type="{http://www.aifb.kit.edu/osh}XsdPhases"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.aifb.kit.edu/osh}nonNegativeInt" />
 *       &lt;attribute name="name" type="{http://www.aifb.kit.edu/osh}name" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XsdLoadProfile", propOrder = {
    "phases"
})
public class XsdLoadProfile implements Cloneable
{

    @XmlElement(name = "Phases", required = true)
    protected XsdPhases phases;
    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String name;

    /**
     * Creates a new {@code XsdLoadProfile} instance.
     * 
     */
    public XsdLoadProfile() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code XsdLoadProfile} instance by deeply copying a given {@code XsdLoadProfile} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public XsdLoadProfile(final XsdLoadProfile o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'XsdLoadProfile' from 'null'.");
        }
        // CClassInfo: edu.kit.aifb.osh.XsdPhases
        this.phases = ((o.phases == null)?null:((o.getPhases() == null)?null:o.getPhases().clone()));
        // CBuiltinLeafInfo: java.lang.Integer
        this.id = o.getId();
        // CBuiltinLeafInfo: java.lang.String
        this.name = ((o.name == null)?null:o.getName());
    }

    /**
     * Ruft den Wert der phases-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XsdPhases }
     *     
     */
    public XsdPhases getPhases() {
        return phases;
    }

    /**
     * Legt den Wert der phases-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdPhases }
     *     
     */
    public void setPhases(XsdPhases value) {
        this.phases = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Creates and returns a deep copy of this object.
     * 
     * 
     * @return
     *     A deep copy of this object.
     */
    @Override
    public XsdLoadProfile clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final XsdLoadProfile clone = ((XsdLoadProfile) super.clone());
                // CClassInfo: edu.kit.aifb.osh.XsdPhases
                clone.phases = ((this.phases == null)?null:((this.getPhases() == null)?null:this.getPhases().clone()));
                // CBuiltinLeafInfo: java.lang.Integer
                clone.id = this.getId();
                // CBuiltinLeafInfo: java.lang.String
                clone.name = ((this.name == null)?null:this.getName());
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
