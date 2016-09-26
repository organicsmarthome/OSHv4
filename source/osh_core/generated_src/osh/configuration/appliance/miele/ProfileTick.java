//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.07.17 um 10:39:31 AM CEST 
//


package osh.configuration.appliance.miele;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 * <p>Java-Klasse für ProfileTick complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProfileTick">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="load" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="commodity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="deviceStateName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parameters" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                   &lt;element name="parameterValue" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileTick", propOrder = {
    "load",
    "deviceStateName",
    "parameters"
})
public class ProfileTick implements Cloneable
{

    @XmlElement(required = true)
    protected List<ProfileTick.Load> load;
    @XmlElement(required = true)
    protected String deviceStateName;
    protected List<ProfileTick.Parameters> parameters;

    /**
     * Creates a new {@code ProfileTick} instance.
     * 
     */
    public ProfileTick() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code ProfileTick} instance by deeply copying a given {@code ProfileTick} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public ProfileTick(final ProfileTick o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'ProfileTick' from 'null'.");
        }
        // 'Load' collection.
        if (o.load!= null) {
            copyLoad(o.getLoad(), this.getLoad());
        }
        // CBuiltinLeafInfo: java.lang.String
        this.deviceStateName = ((o.deviceStateName == null)?null:o.getDeviceStateName());
        // 'Parameters' collection.
        if (o.parameters!= null) {
            copyParameters(o.getParameters(), this.getParameters());
        }
    }

    /**
     * Gets the value of the load property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the load property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLoad().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfileTick.Load }
     * 
     * 
     */
    public List<ProfileTick.Load> getLoad() {
        if (load == null) {
            load = new ArrayList<ProfileTick.Load>();
        }
        return this.load;
    }

    /**
     * Ruft den Wert der deviceStateName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceStateName() {
        return deviceStateName;
    }

    /**
     * Legt den Wert der deviceStateName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceStateName(String value) {
        this.deviceStateName = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfileTick.Parameters }
     * 
     * 
     */
    public List<ProfileTick.Parameters> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<ProfileTick.Parameters>();
        }
        return this.parameters;
    }

    /**
     * Copies all values of property {@code Load} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyLoad(final List<ProfileTick.Load> source, final List<ProfileTick.Load> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof ProfileTick.Load) {
                    // CClassInfo: osh.simulation.virtualdevicesdata.ProfileTick$Load
                    target.add(((ProfileTick.Load) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'Load' of class 'osh.simulation.virtualdevicesdata.ProfileTick'."));
            }
        }
    }

    /**
     * Copies all values of property {@code Parameters} deeply.
     * 
     * @param source
     *     The source to copy from.
     * @param target
     *     The target to copy {@code source} to.
     * @throws NullPointerException
     *     if {@code target} is {@code null}.
     */
    private static void copyParameters(final List<ProfileTick.Parameters> source, final List<ProfileTick.Parameters> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source!= null)&&(!source.isEmpty())) {
            for (final Iterator<?> it = source.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                if (next instanceof ProfileTick.Parameters) {
                    // CClassInfo: osh.simulation.virtualdevicesdata.ProfileTick$Parameters
                    target.add(((ProfileTick.Parameters) next).clone());
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '"+ next)+"' for property 'Parameters' of class 'osh.simulation.virtualdevicesdata.ProfileTick'."));
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
    public ProfileTick clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final ProfileTick clone = ((ProfileTick) super.clone());
                // 'Load' collection.
                if (this.load!= null) {
                    clone.load = null;
                    copyLoad(this.getLoad(), clone.getLoad());
                }
                // CBuiltinLeafInfo: java.lang.String
                clone.deviceStateName = ((this.deviceStateName == null)?null:this.getDeviceStateName());
                // 'Parameters' collection.
                if (this.parameters!= null) {
                    clone.parameters = null;
                    copyParameters(this.getParameters(), clone.getParameters());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }


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
     *         &lt;element name="commodity" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
        "commodity",
        "value"
    })
    public static class Load implements Cloneable
    {

        @XmlElement(required = true)
        protected String commodity;
        protected int value;

        /**
         * Creates a new {@code Load} instance.
         * 
         */
        public Load() {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
        }

        /**
         * Creates a new {@code Load} instance by deeply copying a given {@code Load} instance.
         * 
         * 
         * @param o
         *     The instance to copy.
         * @throws NullPointerException
         *     if {@code o} is {@code null}.
         */
        public Load(final ProfileTick.Load o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
            if (o == null) {
                throw new NullPointerException("Cannot create a copy of 'Load' from 'null'.");
            }
            // CBuiltinLeafInfo: java.lang.String
            this.commodity = ((o.commodity == null)?null:o.getCommodity());
            // CBuiltinLeafInfo: java.lang.Integer
            this.value = o.getValue();
        }

        /**
         * Ruft den Wert der commodity-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCommodity() {
            return commodity;
        }

        /**
         * Legt den Wert der commodity-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCommodity(String value) {
            this.commodity = value;
        }

        /**
         * Ruft den Wert der value-Eigenschaft ab.
         * 
         */
        public int getValue() {
            return value;
        }

        /**
         * Legt den Wert der value-Eigenschaft fest.
         * 
         */
        public void setValue(int value) {
            this.value = value;
        }

        /**
         * Creates and returns a deep copy of this object.
         * 
         * 
         * @return
         *     A deep copy of this object.
         */
        @Override
        public ProfileTick.Load clone() {
            try {
                {
                    // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                    final ProfileTick.Load clone = ((ProfileTick.Load) super.clone());
                    // CBuiltinLeafInfo: java.lang.String
                    clone.commodity = ((this.commodity == null)?null:this.getCommodity());
                    // CBuiltinLeafInfo: java.lang.Integer
                    clone.value = this.getValue();
                    return clone;
                }
            } catch (CloneNotSupportedException e) {
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError(e);
            }
        }

    }


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
     *         &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
     *         &lt;element name="parameterValue" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
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
        "parameterName",
        "parameterValue"
    })
    public static class Parameters implements Cloneable
    {

        @XmlElement(required = true)
        protected Object parameterName;
        @XmlElement(required = true)
        protected Object parameterValue;

        /**
         * Creates a new {@code Parameters} instance.
         * 
         */
        public Parameters() {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
        }

        /**
         * Creates a new {@code Parameters} instance by deeply copying a given {@code Parameters} instance.
         * 
         * 
         * @param o
         *     The instance to copy.
         * @throws NullPointerException
         *     if {@code o} is {@code null}.
         */
        public Parameters(final ProfileTick.Parameters o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
            if (o == null) {
                throw new NullPointerException("Cannot create a copy of 'Parameters' from 'null'.");
            }
            // CBuiltinLeafInfo: java.lang.Object
            this.parameterName = ((o.parameterName == null)?null:copyOf(o.getParameterName()));
            // CBuiltinLeafInfo: java.lang.Object
            this.parameterValue = ((o.parameterValue == null)?null:copyOf(o.getParameterValue()));
        }

        /**
         * Ruft den Wert der parameterName-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getParameterName() {
            return parameterName;
        }

        /**
         * Legt den Wert der parameterName-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setParameterName(Object value) {
            this.parameterName = value;
        }

        /**
         * Ruft den Wert der parameterValue-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getParameterValue() {
            return parameterValue;
        }

        /**
         * Legt den Wert der parameterValue-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setParameterValue(Object value) {
            this.parameterValue = value;
        }

        /**
         * Creates and returns a deep copy of a given object.
         * 
         * @param o
         *     The instance to copy or {@code null}.
         * @return
         *     A deep copy of {@code o} or {@code null} if {@code o} is {@code null}.
         */
        @SuppressWarnings("rawtypes")
		private static Object copyOf(final Object o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            try {
                if (o!= null) {
                    if (o.getClass().isPrimitive()) {
                        return o;
                    }
                    if (o.getClass().isArray()) {
                        return copyOfArray(o);
                    }
                    // Immutable types.
                    if (o instanceof Boolean) {
                        return o;
                    }
                    if (o instanceof Byte) {
                        return o;
                    }
                    if (o instanceof Character) {
                        return o;
                    }
                    if (o instanceof Double) {
                        return o;
                    }
                    if (o instanceof Enum) {
                        return o;
                    }
                    if (o instanceof Float) {
                        return o;
                    }
                    if (o instanceof Integer) {
                        return o;
                    }
                    if (o instanceof Long) {
                        return o;
                    }
                    if (o instanceof Short) {
                        return o;
                    }
                    if (o instanceof String) {
                        return o;
                    }
                    if (o instanceof BigDecimal) {
                        return o;
                    }
                    if (o instanceof BigInteger) {
                        return o;
                    }
                    if (o instanceof UUID) {
                        return o;
                    }
                    if (o instanceof QName) {
                        return o;
                    }
                    if (o instanceof Duration) {
                        return o;
                    }
                    if (o instanceof Currency) {
                        return o;
                    }
                    // String based types.
                    if (o instanceof File) {
                        return new File(o.toString());
                    }
                    if (o instanceof URI) {
                        return new URI(o.toString());
                    }
                    if (o instanceof URL) {
                        return new URL(o.toString());
                    }
                    if (o instanceof MimeType) {
                        return new MimeType(o.toString());
                    }
                    // Cloneable types.
                    if (o instanceof XMLGregorianCalendar) {
                        return ((XMLGregorianCalendar) o).clone();
                    }
                    if (o instanceof Date) {
                        return ((Date) o).clone();
                    }
                    if (o instanceof Calendar) {
                        return ((Calendar) o).clone();
                    }
                    if (o instanceof TimeZone) {
                        return ((TimeZone) o).clone();
                    }
                    if (o instanceof Locale) {
                        return ((Locale) o).clone();
                    }
                    if (o instanceof Element) {
                        return ((Element)((Element) o).cloneNode(true));
                    }
                    if (o instanceof JAXBElement) {
                        return copyOf(((JAXBElement) o));
                    }
                    try {
                        return o.getClass().getMethod("clone", ((Class[]) null)).invoke(o, ((Object[]) null));
                    } catch (NoSuchMethodException e) {
                        if (o instanceof Serializable) {
                            return copyOf(((Serializable) o));
                        }
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    } catch (IllegalAccessException e) {
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    } catch (InvocationTargetException e) {
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    } catch (SecurityException e) {
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    } catch (IllegalArgumentException e) {
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    } catch (ExceptionInInitializerError e) {
                        // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                        throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
                    }
                }
                return null;
            } catch (MimeTypeParseException e) {
                throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
            } catch (URISyntaxException e) {
                throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
            } catch (MalformedURLException e) {
                throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ o)+"'.")).initCause(e));
            }
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static Object copyOfArray(final Object array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                if (array.getClass() == boolean[].class) {
                    return copyOf(((boolean[]) array));
                }
                if (array.getClass() == byte[].class) {
                    return copyOf(((byte[]) array));
                }
                if (array.getClass() == char[].class) {
                    return copyOf(((char[]) array));
                }
                if (array.getClass() == double[].class) {
                    return copyOf(((double[]) array));
                }
                if (array.getClass() == float[].class) {
                    return copyOf(((float[]) array));
                }
                if (array.getClass() == int[].class) {
                    return copyOf(((int[]) array));
                }
                if (array.getClass() == long[].class) {
                    return copyOf(((long[]) array));
                }
                if (array.getClass() == short[].class) {
                    return copyOf(((short[]) array));
                }
                final int len = Array.getLength(array);
                final Object copy = Array.newInstance(array.getClass().getComponentType(), len);
                for (int i = (len- 1); (i >= 0); i--) {
                    Array.set(copy, i, copyOf(Array.get(array, i)));
                }
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static boolean[] copyOf(final boolean[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final boolean[] copy = ((boolean[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static byte[] copyOf(final byte[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final byte[] copy = ((byte[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static char[] copyOf(final char[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final char[] copy = ((char[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static double[] copyOf(final double[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final double[] copy = ((double[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static float[] copyOf(final float[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final float[] copy = ((float[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static int[] copyOf(final int[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final int[] copy = ((int[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static long[] copyOf(final long[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final long[] copy = ((long[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given array.
         * 
         * @param array
         *     The array to copy or {@code null}.
         * @return
         *     A deep copy of {@code array} or {@code null} if {@code array} is {@code null}.
         */
        private static short[] copyOf(final short[] array) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (array!= null) {
                final short[] copy = ((short[]) Array.newInstance(array.getClass().getComponentType(), array.length));
                System.arraycopy(array, 0, copy, 0, array.length);
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given {@code JAXBElement} instance.
         * 
         * @param element
         *     The instance to copy or {@code null}.
         * @return
         *     A deep copy of {@code element} or {@code null} if {@code element} is {@code null}.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static JAXBElement copyOf(final JAXBElement element) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (element!= null) {
				final JAXBElement copy = new JAXBElement(element.getName(), element.getDeclaredType(), element.getScope(), element.getValue());
                copy.setNil(element.isNil());
                copy.setValue(copyOf(copy.getValue()));
                return copy;
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of a given {@code Serializable}.
         * 
         * @param serializable
         *     The instance to copy or {@code null}.
         * @return
         *     A deep copy of {@code serializable} or {@code null} if {@code serializable} is {@code null}.
         */
        private static Serializable copyOf(final Serializable serializable) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if (serializable!= null) {
                try {
                    final ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                    final ObjectOutputStream out = new ObjectOutputStream(byteArrayOutput);
                    out.writeObject(serializable);
                    out.close();
                    final ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(byteArrayOutput.toByteArray());
                    final ObjectInputStream in = new ObjectInputStream(byteArrayInput);
                    final Serializable copy = ((Serializable) in.readObject());
                    in.close();
                    return copy;
                } catch (SecurityException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (ClassNotFoundException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (InvalidClassException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (NotSerializableException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (StreamCorruptedException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (OptionalDataException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                } catch (IOException e) {
                    throw((AssertionError) new AssertionError((("Unexpected instance during copying object '"+ serializable)+"'.")).initCause(e));
                }
            }
            return null;
        }

        /**
         * Creates and returns a deep copy of this object.
         * 
         * 
         * @return
         *     A deep copy of this object.
         */
        @Override
        public ProfileTick.Parameters clone() {
            try {
                {
                    // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                    final ProfileTick.Parameters clone = ((ProfileTick.Parameters) super.clone());
                    // CBuiltinLeafInfo: java.lang.Object
                    clone.parameterName = ((this.parameterName == null)?null:copyOf(this.getParameterName()));
                    // CBuiltinLeafInfo: java.lang.Object
                    clone.parameterValue = ((this.parameterValue == null)?null:copyOf(this.getParameterValue()));
                    return clone;
                }
            } catch (CloneNotSupportedException e) {
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError(e);
            }
        }

    }

}
