//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.07.17 um 09:45:20 AM CEST 
//


package osh.simulation.screenplay;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the osh.simulation.screenplay package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: osh.simulation.screenplay
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Screenplay }
     * 
     */
    public Screenplay createScreenplay() {
        return new Screenplay();
    }

    /**
     * Create an instance of {@link SubjectAction }
     * 
     */
    public SubjectAction createSubjectAction() {
        return new SubjectAction();
    }

    /**
     * Create an instance of {@link ActionParameters }
     * 
     */
    public ActionParameters createActionParameters() {
        return new ActionParameters();
    }

    /**
     * Create an instance of {@link ActionParameter }
     * 
     */
    public ActionParameter createActionParameter() {
        return new ActionParameter();
    }

    /**
     * Create an instance of {@link PerformAction }
     * 
     */
    public PerformAction createPerformAction() {
        return new PerformAction();
    }

}
