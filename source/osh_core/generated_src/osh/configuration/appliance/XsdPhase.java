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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für XsdPhase complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="XsdPhase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Tick" type="{http://www.aifb.kit.edu/osh}XsdTick" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.aifb.kit.edu/osh}nonNegativeInt" />
 *       &lt;attribute name="name" type="{http://www.aifb.kit.edu/osh}name" />
 *       &lt;attribute name="minLength" use="required" type="{http://www.aifb.kit.edu/osh}nonNegativeInt" />
 *       &lt;attribute name="maxLength" use="required" type="{http://www.aifb.kit.edu/osh}nonNegativeInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XsdPhase", propOrder = {
    "tick"
})
public class XsdPhase implements Cloneable
{

    @XmlElement(name = "Tick", required = true)
    protected List<XsdTick> tick;
    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String name;
    @XmlAttribute(name = "minLength", required = true)
    protected int minLength;
    @XmlAttribute(name = "maxLength", required = true)
    protected int maxLength;

    /**
     * Creates a new {@code XsdPhase} instance.
     * 
     */
    public XsdPhase() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code XsdPhase} instance by deeply copying a given {@code XsdPhase} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public XsdPhase(final XsdPhase o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'XsdPhase' from 'null'.");
        }
        // 'Tick' collection.
        if (o.tick!= null) {
            copyTick(o.getTick(), this.getTick());
        }
        // CBuiltinLeafInfo: java.lang.Integer
        this.id = o.getId();
        // CBuiltinLeafInfo: java.lang.String
        this.name = ((o.name == null)?null:o.getName());
        // CBuiltinLeafInfo: java.lang.Integer
        this.minLength = o.getMinLength();
        // CBuiltinLeafInfo: java.lang.Integer
        this.maxLength = o.getMaxLength();
    }

    /**
     * Gets the value of the tick property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tick property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTick().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XsdTick }
     * 
     * 
     */
    public List<XsdTick> getTick() {
        if (tick == null) {
            tick = new ArrayList<XsdTick>();
        }
        return this.tick;
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
     * Ruft den Wert der minLength-Eigenschaft ab.
     * 
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Legt den Wert der minLength-Eigenschaft fest.
     * 
     */
    public void setMinLength(int value) {
        this.minLength = value;
    }

    /**
     * Ruft den Wert der maxLength-Eigenschaft ab.
     * 
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Legt den Wert der maxLength-Eigenschaft fest.
     * 
     */
    public void setMaxLength(int value) {
        this.maxLength = value;
    }

    /**
     * Copies all values of property {@code Tick} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyTick(final List<XsdTick> source, final List<XsdTick> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof XsdTick) {
                    // CClassInfo: edu.kit.aifb.osh.XsdTick
                    target.add(((XsdTick) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'Tick' of class 'edu.kit.aifb.osh.XsdPhase'."));
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
    public XsdPhase clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final XsdPhase clone = ((XsdPhase) super.clone());
                // 'Tick' collection.
                if (this.tick!= null) {
                    clone.tick = null;
                    copyTick(this.getTick(), clone.getTick());
                }
                // CBuiltinLeafInfo: java.lang.Integer
                clone.id = this.getId();
                // CBuiltinLeafInfo: java.lang.String
                clone.name = ((this.name == null)?null:this.getName());
                // CBuiltinLeafInfo: java.lang.Integer
                clone.minLength = this.getMinLength();
                // CBuiltinLeafInfo: java.lang.Integer
                clone.maxLength = this.getMaxLength();
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
