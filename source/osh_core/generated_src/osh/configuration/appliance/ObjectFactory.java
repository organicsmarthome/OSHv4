//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.08 um 02:06:24 PM CET 
//


package osh.configuration.appliance;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.kit.aifb.osh package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GenericApplianceProfile_QNAME = new QName("http://www.aifb.kit.edu/osh", "GenericApplianceProfile");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.kit.aifb.osh
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XsdApplianceProgramConfigurations }
     * 
     */
    public XsdApplianceProgramConfigurations createXsdApplianceProgramConfigurations() {
        return new XsdApplianceProgramConfigurations();
    }

    /**
     * Create an instance of {@link XsdApplianceProgramConfiguration }
     * 
     */
    public XsdApplianceProgramConfiguration createXsdApplianceProgramConfiguration() {
        return new XsdApplianceProgramConfiguration();
    }

    /**
     * Create an instance of {@link XsdPhase }
     * 
     */
    public XsdPhase createXsdPhase() {
        return new XsdPhase();
    }

    /**
     * Create an instance of {@link XsdLoadProfiles }
     * 
     */
    public XsdLoadProfiles createXsdLoadProfiles() {
        return new XsdLoadProfiles();
    }

    /**
     * Create an instance of {@link XsdPhases }
     * 
     */
    public XsdPhases createXsdPhases() {
        return new XsdPhases();
    }

    /**
     * Create an instance of {@link XsdDescription }
     * 
     */
    public XsdDescription createXsdDescription() {
        return new XsdDescription();
    }

    /**
     * Create an instance of {@link XsdTick }
     * 
     */
    public XsdTick createXsdTick() {
        return new XsdTick();
    }

    /**
     * Create an instance of {@link XsdConfigurationParameters }
     * 
     */
    public XsdConfigurationParameters createXsdConfigurationParameters() {
        return new XsdConfigurationParameters();
    }

    /**
     * Create an instance of {@link XsdLoadProfile }
     * 
     */
    public XsdLoadProfile createXsdLoadProfile() {
        return new XsdLoadProfile();
    }

    /**
     * Create an instance of {@link XsdProgram }
     * 
     */
    public XsdProgram createXsdProgram() {
        return new XsdProgram();
    }

    /**
     * Create an instance of {@link XsdLoad }
     * 
     */
    public XsdLoad createXsdLoad() {
        return new XsdLoad();
    }

    /**
     * Create an instance of {@link XsdDescriptions }
     * 
     */
    public XsdDescriptions createXsdDescriptions() {
        return new XsdDescriptions();
    }

    /**
     * Create an instance of {@link XsdConfigurationParameter }
     * 
     */
    public XsdConfigurationParameter createXsdConfigurationParameter() {
        return new XsdConfigurationParameter();
    }

}
