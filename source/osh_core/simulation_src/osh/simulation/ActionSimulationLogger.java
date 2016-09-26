package osh.simulation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import osh.core.logging.IGlobalLogger;
import osh.simulation.screenplay.Screenplay;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.xml.XMLSerialization;

public class ActionSimulationLogger implements ISimulationActionLogger {

	private IGlobalLogger logger;
	private OutputStream stream;
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
	    "element"
	})
	@XmlRootElement(name = "root")
	public static class RootElement {
		
		
		@XmlElement(name = "element")
		private SubjectAction element;

		public RootElement() {}
		
		public RootElement(SubjectAction element) {
			super();
			this.element = element;
		}

		public SubjectAction getElement() {
			return element;
		}

		public void setElement(SubjectAction element) {
			this.element = element;
		}
		
	}
	
	public ActionSimulationLogger(IGlobalLogger logger, String filename) throws FileNotFoundException {
		this.logger = logger;
		stream = new FileOutputStream(filename);
	}
	@Override
	public void logAction(SubjectAction action) {
		if (stream == null) {
			logger.logError("logger stream was closed", new Exception());
			return;
		}
		
		try {
			Screenplay sp = new Screenplay();
			sp.getSIMActions().add(action);
			XMLSerialization.marshal(stream, sp);
		} catch (JAXBException e) {
			logger.logError("could not log action", e);
		}
	}
	
	public void closeStream() {
		try {
			if (stream != null) stream.close();
		} catch (IOException e) {}
		stream = null;
	}

}
