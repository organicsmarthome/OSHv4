//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.07.17 um 09:45:20 AM CEST 
//


package osh.simulation.screenplay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für SubjectAction complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="SubjectAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tick" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="periodicAction" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="periodicRefresh" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="actionType" type="{http://osh/Simulation/Screenplay}ActionType"/>
 *         &lt;element name="deviceID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nextState" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="performAction" type="{http://osh/Simulation/Screenplay}PerformAction" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubjectAction", propOrder = {
    "tick",
    "periodicAction",
    "periodicRefresh",
    "actionType",
    "deviceID",
    "nextState",
    "performAction"
})
public class SubjectAction implements Cloneable
{

    protected long tick;
    @XmlElement(defaultValue = "false")
    protected boolean periodicAction;
    @XmlElement(defaultValue = "0")
    protected int periodicRefresh;
    @XmlElement(required = true)
    protected ActionType actionType;
    @XmlElement(required = true)
    protected String deviceID;
    protected boolean nextState;
    protected List<PerformAction> performAction;

    /**
     * Creates a new {@code SubjectAction} instance.
     * 
     */
    public SubjectAction() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code SubjectAction} instance by deeply copying a given {@code SubjectAction} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public SubjectAction(final SubjectAction o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'SubjectAction' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.Long
        this.tick = o.getTick();
        // CBuiltinLeafInfo: java.lang.Boolean
        this.periodicAction = o.isPeriodicAction();
        // CBuiltinLeafInfo: java.lang.Integer
        this.periodicRefresh = o.getPeriodicRefresh();
        // CEnumLeafInfo: osh.simulation.screenplay.ActionType
        this.actionType = ((o.actionType == null)?null:o.getActionType());
        // CBuiltinLeafInfo: java.lang.String
        this.deviceID = ((o.deviceID == null)?null:o.getDeviceID());
        // CBuiltinLeafInfo: java.lang.Boolean
        this.nextState = o.isNextState();
        // 'PerformAction' collection.
        if (o.performAction!= null) {
            copyPerformAction(o.getPerformAction(), this.getPerformAction());
        }
    }

    /**
     * Ruft den Wert der tick-Eigenschaft ab.
     * 
     */
    public long getTick() {
        return tick;
    }

    /**
     * Legt den Wert der tick-Eigenschaft fest.
     * 
     */
    public void setTick(long value) {
        this.tick = value;
    }

    /**
     * Ruft den Wert der periodicAction-Eigenschaft ab.
     * 
     */
    public boolean isPeriodicAction() {
        return periodicAction;
    }

    /**
     * Legt den Wert der periodicAction-Eigenschaft fest.
     * 
     */
    public void setPeriodicAction(boolean value) {
        this.periodicAction = value;
    }

    /**
     * Ruft den Wert der periodicRefresh-Eigenschaft ab.
     * 
     */
    public int getPeriodicRefresh() {
        return periodicRefresh;
    }

    /**
     * Legt den Wert der periodicRefresh-Eigenschaft fest.
     * 
     */
    public void setPeriodicRefresh(int value) {
        this.periodicRefresh = value;
    }

    /**
     * Ruft den Wert der actionType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActionType }
     *     
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Legt den Wert der actionType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActionType }
     *     
     */
    public void setActionType(ActionType value) {
        this.actionType = value;
    }

    /**
     * Ruft den Wert der deviceID-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Legt den Wert der deviceID-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceID(String value) {
        this.deviceID = value;
    }

    /**
     * Ruft den Wert der nextState-Eigenschaft ab.
     * 
     */
    public boolean isNextState() {
        return nextState;
    }

    /**
     * Legt den Wert der nextState-Eigenschaft fest.
     * 
     */
    public void setNextState(boolean value) {
        this.nextState = value;
    }

    /**
     * Gets the value of the performAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the performAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPerformAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PerformAction }
     * 
     * 
     */
    public List<PerformAction> getPerformAction() {
        if (performAction == null) {
            performAction = new ArrayList<PerformAction>();
        }
        return this.performAction;
    }

    /**
     * Copies all values of property {@code PerformAction} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyPerformAction(final List<PerformAction> source, final List<PerformAction> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof PerformAction) {
                    // CClassInfo: osh.simulation.screenplay.PerformAction
                    target.add(((PerformAction) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'PerformAction' of class 'osh.simulation.screenplay.SubjectAction'."));
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
    public SubjectAction clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final SubjectAction clone = ((SubjectAction) super.clone());
                // CBuiltinLeafInfo: java.lang.Long
                clone.tick = this.getTick();
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.periodicAction = this.isPeriodicAction();
                // CBuiltinLeafInfo: java.lang.Integer
                clone.periodicRefresh = this.getPeriodicRefresh();
                // CEnumLeafInfo: osh.simulation.screenplay.ActionType
                clone.actionType = ((this.actionType == null)?null:this.getActionType());
                // CBuiltinLeafInfo: java.lang.String
                clone.deviceID = ((this.deviceID == null)?null:this.getDeviceID());
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.nextState = this.isNextState();
                // 'PerformAction' collection.
                if (this.performAction!= null) {
                    clone.performAction = null;
                    copyPerformAction(this.getPerformAction(), clone.getPerformAction());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
