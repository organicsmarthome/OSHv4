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
 * <p>Java-Klasse für XsdApplianceProgramConfiguration complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="XsdApplianceProgramConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Program" type="{http://www.aifb.kit.edu/osh}XsdProgram"/>
 *         &lt;element name="Parameters" type="{http://www.aifb.kit.edu/osh}XsdConfigurationParameters" minOccurs="0"/>
 *         &lt;element name="LoadProfiles" type="{http://www.aifb.kit.edu/osh}XsdLoadProfiles"/>
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
@XmlType(name = "XsdApplianceProgramConfiguration", propOrder = {
    "program",
    "parameters",
    "loadProfiles"
})
public class XsdApplianceProgramConfiguration implements Cloneable
{

    @XmlElement(name = "Program", required = true)
    protected XsdProgram program;
    @XmlElement(name = "Parameters")
    protected XsdConfigurationParameters parameters;
    @XmlElement(name = "LoadProfiles", required = true)
    protected XsdLoadProfiles loadProfiles;
    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String name;

    /**
     * Creates a new {@code XsdApplianceProgramConfiguration} instance.
     * 
     */
    public XsdApplianceProgramConfiguration() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code XsdApplianceProgramConfiguration} instance by deeply copying a given {@code XsdApplianceProgramConfiguration} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public XsdApplianceProgramConfiguration(final XsdApplianceProgramConfiguration o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'XsdApplianceProgramConfiguration' from 'null'.");
        }
        // CClassInfo: edu.kit.aifb.osh.XsdProgram
        this.program = ((o.program == null)?null:((o.getProgram() == null)?null:o.getProgram().clone()));
        // CClassInfo: edu.kit.aifb.osh.XsdConfigurationParameters
        this.parameters = ((o.parameters == null)?null:((o.getParameters() == null)?null:o.getParameters().clone()));
        // CClassInfo: edu.kit.aifb.osh.XsdLoadProfiles
        this.loadProfiles = ((o.loadProfiles == null)?null:((o.getLoadProfiles() == null)?null:o.getLoadProfiles().clone()));
        // CBuiltinLeafInfo: java.lang.Integer
        this.id = o.getId();
        // CBuiltinLeafInfo: java.lang.String
        this.name = ((o.name == null)?null:o.getName());
    }

    /**
     * Ruft den Wert der program-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XsdProgram }
     *     
     */
    public XsdProgram getProgram() {
        return program;
    }

    /**
     * Legt den Wert der program-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdProgram }
     *     
     */
    public void setProgram(XsdProgram value) {
        this.program = value;
    }

    /**
     * Ruft den Wert der parameters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XsdConfigurationParameters }
     *     
     */
    public XsdConfigurationParameters getParameters() {
        return parameters;
    }

    /**
     * Legt den Wert der parameters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdConfigurationParameters }
     *     
     */
    public void setParameters(XsdConfigurationParameters value) {
        this.parameters = value;
    }

    /**
     * Ruft den Wert der loadProfiles-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XsdLoadProfiles }
     *     
     */
    public XsdLoadProfiles getLoadProfiles() {
        return loadProfiles;
    }

    /**
     * Legt den Wert der loadProfiles-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XsdLoadProfiles }
     *     
     */
    public void setLoadProfiles(XsdLoadProfiles value) {
        this.loadProfiles = value;
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
    public XsdApplianceProgramConfiguration clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final XsdApplianceProgramConfiguration clone = ((XsdApplianceProgramConfiguration) super.clone());
                // CClassInfo: edu.kit.aifb.osh.XsdProgram
                clone.program = ((this.program == null)?null:((this.getProgram() == null)?null:this.getProgram().clone()));
                // CClassInfo: edu.kit.aifb.osh.XsdConfigurationParameters
                clone.parameters = ((this.parameters == null)?null:((this.getParameters() == null)?null:this.getParameters().clone()));
                // CClassInfo: edu.kit.aifb.osh.XsdLoadProfiles
                clone.loadProfiles = ((this.loadProfiles == null)?null:((this.getLoadProfiles() == null)?null:this.getLoadProfiles().clone()));
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
