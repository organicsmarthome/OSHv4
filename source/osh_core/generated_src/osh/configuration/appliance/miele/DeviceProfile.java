//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.07.17 um 10:39:31 AM CEST 
//


package osh.configuration.appliance.miele;

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
 *         &lt;element name="deviceUUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deviceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hasProfile" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Intelligent" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="profileTicks" type="{http://osh/Simulation/VirtualDevicesData}ProfileTicks"/>
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
    "deviceUUID",
    "deviceType",
    "deviceDescription",
    "hasProfile",
    "intelligent",
    "profileTicks"
})
@XmlRootElement(name = "DeviceProfile")
public class DeviceProfile implements Cloneable
{

    @XmlElement(required = true)
    protected String deviceUUID;
    @XmlElement(required = true)
    protected String deviceType;
    @XmlElement(required = true)
    protected String deviceDescription;
    protected boolean hasProfile;
    @XmlElement(name = "Intelligent")
    protected boolean intelligent;
    @XmlElement(required = true)
    protected ProfileTicks profileTicks;

    /**
     * Creates a new {@code DeviceProfile} instance.
     * 
     */
    public DeviceProfile() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code DeviceProfile} instance by deeply copying a given {@code DeviceProfile} instance.
     * 
     * 
     * @param o
     *     The instance to copy.
     * @throws NullPointerException
     *     if {@code o} is {@code null}.
     */
    public DeviceProfile(final DeviceProfile o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'DeviceProfile' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.String
        this.deviceUUID = ((o.deviceUUID == null)?null:o.getDeviceUUID());
        // CBuiltinLeafInfo: java.lang.String
        this.deviceType = ((o.deviceType == null)?null:o.getDeviceType());
        // CBuiltinLeafInfo: java.lang.String
        this.deviceDescription = ((o.deviceDescription == null)?null:o.getDeviceDescription());
        // CBuiltinLeafInfo: java.lang.Boolean
        this.hasProfile = o.isHasProfile();
        // CBuiltinLeafInfo: java.lang.Boolean
        this.intelligent = o.isIntelligent();
        // CClassInfo: osh.simulation.virtualdevicesdata.ProfileTicks
        this.profileTicks = ((o.profileTicks == null)?null:((o.getProfileTicks() == null)?null:o.getProfileTicks().clone()));
    }

    /**
     * Ruft den Wert der deviceUUID-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceUUID() {
        return deviceUUID;
    }

    /**
     * Legt den Wert der deviceUUID-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceUUID(String value) {
        this.deviceUUID = value;
    }

    /**
     * Ruft den Wert der deviceType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Legt den Wert der deviceType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceType(String value) {
        this.deviceType = value;
    }

    /**
     * Ruft den Wert der deviceDescription-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceDescription() {
        return deviceDescription;
    }

    /**
     * Legt den Wert der deviceDescription-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceDescription(String value) {
        this.deviceDescription = value;
    }

    /**
     * Ruft den Wert der hasProfile-Eigenschaft ab.
     * 
     */
    public boolean isHasProfile() {
        return hasProfile;
    }

    /**
     * Legt den Wert der hasProfile-Eigenschaft fest.
     * 
     */
    public void setHasProfile(boolean value) {
        this.hasProfile = value;
    }

    /**
     * Ruft den Wert der intelligent-Eigenschaft ab.
     * 
     */
    public boolean isIntelligent() {
        return intelligent;
    }

    /**
     * Legt den Wert der intelligent-Eigenschaft fest.
     * 
     */
    public void setIntelligent(boolean value) {
        this.intelligent = value;
    }

    /**
     * Ruft den Wert der profileTicks-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProfileTicks }
     *     
     */
    public ProfileTicks getProfileTicks() {
        return profileTicks;
    }

    /**
     * Legt den Wert der profileTicks-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileTicks }
     *     
     */
    public void setProfileTicks(ProfileTicks value) {
        this.profileTicks = value;
    }

    /**
     * Creates and returns a deep copy of this object.
     * 
     * 
     * @return
     *     A deep copy of this object.
     */
    @Override
    public DeviceProfile clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final DeviceProfile clone = ((DeviceProfile) super.clone());
                // CBuiltinLeafInfo: java.lang.String
                clone.deviceUUID = ((this.deviceUUID == null)?null:this.getDeviceUUID());
                // CBuiltinLeafInfo: java.lang.String
                clone.deviceType = ((this.deviceType == null)?null:this.getDeviceType());
                // CBuiltinLeafInfo: java.lang.String
                clone.deviceDescription = ((this.deviceDescription == null)?null:this.getDeviceDescription());
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.hasProfile = this.isHasProfile();
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.intelligent = this.isIntelligent();
                // CClassInfo: osh.simulation.virtualdevicesdata.ProfileTicks
                clone.profileTicks = ((this.profileTicks == null)?null:((this.getProfileTicks() == null)?null:this.getProfileTicks().clone()));
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }

}
