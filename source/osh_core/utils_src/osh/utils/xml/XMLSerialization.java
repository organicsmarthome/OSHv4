package osh.utils.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Simplifies the XML-serialization using the java built-in marshaller
 * 
 * @author Till Schuberth
 */
public class XMLSerialization {
	
    /**
     * marshal (serialize) an object (JaXB!!!) to an outputstream
     * @param os
     * @param obj
     * @throws JAXBException
     */
    public static void marshal(OutputStream os, Object obj) throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(obj.getClass().getPackage().getName());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(obj, os);
    }
 
    
    /**
     * marshal (serialize) an object (JaXB!!!) to file with the given name
     * @param fileName
     * @param obj
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    public static void marshal2File(String fileName, Object obj) throws FileNotFoundException, JAXBException {
    	FileOutputStream fileStream = new FileOutputStream(fileName);
    	marshal(fileStream, obj);
    }
     
    /**
     * unmarshal (deserialize) an inputstream (JaXB!!!) to an object based on the the given targetClass
     * Note: you have to cast from object to the specified object before use ;-)
     * @param is
     * @param targetClass
     * @return
     * @throws JAXBException
     */
    @SuppressWarnings({ "rawtypes" })
	public static Object unmarshal(InputStream is, Class targetClass) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(targetClass.getPackage().getName());
		Unmarshaller um = jc.createUnmarshaller();
		return um.unmarshal(is);
    }
    
    /**
     * unmarshal (deserialize) from a file (JaXB!!!) to an object based on the the given targetClass
     * Note: you have to cast from object to the specified object before use ;-)
     * @param fileName
     * @param targetClass
     * @return
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    @SuppressWarnings({ "rawtypes" })
	public static Object file2Unmarshal(String fileName, Class targetClass) throws FileNotFoundException, JAXBException {
    	FileInputStream fileStream = new FileInputStream(fileName);
    	return unmarshal(fileStream,targetClass);
    }
    
}
