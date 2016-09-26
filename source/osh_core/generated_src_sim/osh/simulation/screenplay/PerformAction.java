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
 * <p>Java-Klasse für PerformAction complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PerformAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="actionParameterCollection" type="{http://osh/Simulation/Screenplay}ActionParameters" maxOccurs="unbounded"/>
 *         &lt;element name="appendAction" type="{http://osh/Simulation/Screenplay}SubjectAction" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformAction", propOrder = {
    "actionParameterCollection",
    "appendAction"
})
public class PerformAction implements Cloneable
{

    @XmlElement(required = true)
    protected List<ActionParameters> actionParameterCollection;
    protected List<SubjectAction> appendAction;

    /**
     * Creates a new {@code PerformAction} instance.
     * 
     */
    public PerformAction() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code PerformAction} instance by deeply copying a given {@code PerformAction} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public PerformAction(final PerformAction o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'PerformAction' from 'null'.");
        }
        // 'ActionParameterCollection' collection.
        if (o.actionParameterCollection!= null) {
            copyActionParameterCollection(o.getActionParameterCollection(), this.getActionParameterCollection());
        }
        // 'AppendAction' collection.
        if (o.appendAction!= null) {
            copyAppendAction(o.getAppendAction(), this.getAppendAction());
        }
    }

    /**
     * Gets the value of the actionParameterCollection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionParameterCollection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionParameterCollection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionParameters }
     * 
     * 
     */
    public List<ActionParameters> getActionParameterCollection() {
        if (actionParameterCollection == null) {
            actionParameterCollection = new ArrayList<ActionParameters>();
        }
        return this.actionParameterCollection;
    }

    /**
     * Gets the value of the appendAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appendAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppendAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubjectAction }
     * 
     * 
     */
    public List<SubjectAction> getAppendAction() {
        if (appendAction == null) {
            appendAction = new ArrayList<SubjectAction>();
        }
        return this.appendAction;
    }

    /**
     * Copies all values of property {@code ActionParameterCollection} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyActionParameterCollection(final List<ActionParameters> source, final List<ActionParameters> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof ActionParameters) {
                    // CClassInfo: osh.simulation.screenplay.ActionParameters
                    target.add(((ActionParameters) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'ActionParameterCollection' of class 'osh.simulation.screenplay.PerformAction'."));
            }
        }
    }

    /**
     * Copies all values of property {@code AppendAction} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyAppendAction(final List<SubjectAction> source, final List<SubjectAction> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof SubjectAction) {
                    // CClassInfo: osh.simulation.screenplay.SubjectAction
                    target.add(((SubjectAction) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'AppendAction' of class 'osh.simulation.screenplay.PerformAction'."));
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
    public PerformAction clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final PerformAction clone = ((PerformAction) super.clone());
                // 'ActionParameterCollection' collection.
                if (this.actionParameterCollection!= null) {
                    clone.actionParameterCollection = null;
                    copyActionParameterCollection(this.getActionParameterCollection(), clone.getActionParameterCollection());
                }
                // 'AppendAction' collection.
                if (this.appendAction!= null) {
                    clone.appendAction = null;
                    copyAppendAction(this.getAppendAction(), clone.getAppendAction());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
