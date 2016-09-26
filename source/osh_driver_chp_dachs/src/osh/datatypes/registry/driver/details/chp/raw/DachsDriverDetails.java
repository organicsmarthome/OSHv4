package osh.datatypes.registry.driver.details.chp.raw;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import osh.datatypes.registry.StateExchange;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement(name="DachsDriverDetails")
@XmlType
@SuppressWarnings("unused")
public class DachsDriverDetails extends StateExchange {
	
	private static final long serialVersionUID = 1449180454003778385L;
	
	// Dachs
	private String hkaBdAnforderungModulAnzahl;
	private String hkaBdAnforderungUStromFAnfbFlagSF;
	private String hkaBdUStromFFreibFreigabe;
	private String hkaBdbStoerung;
	private String hkaBdbWarnung;
	private String hkaBdUHkaAnfAnforderungfStrom;
	private String hkaBdUHkaAnfusAnforderung;
	private String hkaBdUHkaFreiusFreigabe;
	private String hkaBdulArbeitElektr;
	private String hkaBdulArbeitThermHka;
	private String hkaBdulBetriebssekunden;
	private String hkaBdulAnzahlStarts;
	
	// "Betriebsdaten 31.12."
	private String bD3112HkaBdulBetriebssekunden;
	private String bD3112HkaBdulAnzahlStarts;
	private String bD3112HkaBdulArbeitElektr;
	private String bD3112HkaBdulArbeitThermHka;
//	private String bD3112HkaBdulArbeitThermKon;
	private String bD3112WwBdulWwMengepA;
	
	// "Daten 2. Waermeerzeuger (SEplus)"
	private String brennerBdbIstStatus;
	 // ... (KIT)
	
	// "Hydraulik Schema"
		 //none
	
	// Temperaturen
	 // ...
	
	
	private String hkaMw1TempsbAussen;
	private String hkaMw1TempsbFuehler1;
	private String hkaMw1TempsbFuehler2;
	private String hkaMw1TempsbGen;
	private String hkaMw1TempsbMotor;
	private String hkaMw1TempsbRuecklauf;
	private String hkaMw1TempsbVorlauf;

	 // ...
	
	private String hkaMw1TempsbZSVorlauf1;
	
	 // ...

	private String hkaMw1TempsbZSWarmwasser;
	
	 // ...
	
	// "Aktoren"
	private String hkaMw1sWirkleistung;
	private String hkaMw1ulMotorlaufsekunden;
	private String hkaMw1usDrehzahl;
	
	// Tageslauf
	 //none
	
	// Informationen über Wartung

	private String wartungCachefStehtAn;

	@XmlTransient
	private HashMap<String, String> values;
	
	
	/** for JAXB only */
	@Deprecated
	private DachsDriverDetails() {
		super(null, 0);
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public DachsDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	
//	@Transient
	public void setValues(HashMap<String, String> values) {
		this.values = values;
		for (Entry<String,String> e : values.entrySet()) {
			// Betriebsdaten Dachs
			if (e.getKey().equals("Hka_Bd.Anforderung.ModulAnzahl")) {
				hkaBdAnforderungModulAnzahl = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.Anforderung.UStromF_Anf.bFlagSF")) {
				hkaBdAnforderungUStromFAnfbFlagSF = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.UStromF_Frei.bFreigabe")) {
				hkaBdUStromFFreibFreigabe = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.bStoerung")) {
				hkaBdbStoerung = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.bWarnung")) {
				hkaBdbWarnung = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.UHka_Anf.Anforderung.fStrom")) {
				hkaBdUHkaAnfAnforderungfStrom = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.UHka_Anf.usAnforderung")) {
				hkaBdUHkaAnfusAnforderung = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.UHka_Frei.usFreigabe")) {
				hkaBdUHkaFreiusFreigabe = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.ulArbeitElektr")) {
				hkaBdulArbeitElektr = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.ulArbeitThermHka")) {
				hkaBdulArbeitThermHka = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.ulBetriebssekunden")) {
				hkaBdulBetriebssekunden = e.getValue();
			}
			else if (e.getKey().equals("Hka_Bd.ulAnzahlStarts")) {
				hkaBdulAnzahlStarts = e.getValue();
			}
			
			// Betriebsdaten 31.12.
			else if (e.getKey().equals("BD3112.Hka_Bd.ulBetriebssekunden")) {
				bD3112HkaBdulBetriebssekunden = e.getValue();
			}
			else if (e.getKey().equals("BD3112.Hka_Bd.ulAnzahlStarts")) {
				bD3112HkaBdulAnzahlStarts = e.getValue();
			}
			else if (e.getKey().equals("BD3112.Hka_Bd.ulArbeitElektr")) {
				bD3112HkaBdulArbeitElektr = e.getValue();
			}
			else if (e.getKey().equals("BD3112.Hka_Bd.ulArbeitThermHka")) {
				bD3112HkaBdulArbeitThermHka = e.getValue();
			}
//			else if (e.getKey().equals("BD3112.Hka_Bd.ulArbeitThermKon")) {
//				bD3112HkaBdulArbeitThermKon = e.getValue();
//			}
			else if (e.getKey().equals("BD3112.Ww_Bd.ulWwMengepA")) {
				bD3112WwBdulWwMengepA = e.getValue();
			}
			// Daten 2. Wärmeerzeuger (SEplus)
			else if (e.getKey().equals("Brenner_Bd.bIstStatus")) {
				brennerBdbIstStatus = e.getValue();
			}
			// Temperaturen
			else if (e.getKey().equals("Hka_Mw1.Temp.sbAussen")) {
				hkaMw1TempsbAussen = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbFuehler1")) {
				hkaMw1TempsbFuehler1 = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbFuehler2")) {
				hkaMw1TempsbFuehler2 = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbGen")) {
				hkaMw1TempsbGen = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbMotor")) {
				hkaMw1TempsbMotor = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbVorlauf")) {
				hkaMw1TempsbVorlauf = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbRuecklauf")) {
				hkaMw1TempsbRuecklauf = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbZS_Vorlauf1")) {
				hkaMw1TempsbZSVorlauf1 = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.Temp.sbZS_Warmwasser")) {
				hkaMw1TempsbZSWarmwasser = e.getValue();
			}
			
			// Hydraulik Schema
			 //none
			// Aktoren
			else if (e.getKey().equals("Hka_Mw1.sWirkleistung")) {
				hkaMw1sWirkleistung = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.ulMotorlaufsekunden")) {
				hkaMw1ulMotorlaufsekunden = e.getValue();
			}
			else if (e.getKey().equals("Hka_Mw1.usDrehzahl")) {
				hkaMw1usDrehzahl = e.getValue();
			}
			// Tageslauf
			 //none
			// Informationen über Wartung
			else if (e.getKey().equals("Wartung_Cache.fStehtAn")) {
				this.wartungCachefStehtAn = e.getValue();
			}
		}
	}
	
	/**
	 * use with CARE! (is not cloned)
	 */
	public HashMap<String, String> getValues() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		
		boolean first = true;
		
		for (Entry<String,String> e : values.entrySet()) {
			if (!first) {
				builder.append(",");
			}
			else {
				first = false;
			}
			builder.append(e.getKey() + "=" + e.getValue());
		}
		
		builder.append("]");
		
		return builder.toString();
	}
	
	public boolean isEmpty(){
		if (this.values == null){
			return true;
		}
		else {
			return false;
		}
	}
	
}
