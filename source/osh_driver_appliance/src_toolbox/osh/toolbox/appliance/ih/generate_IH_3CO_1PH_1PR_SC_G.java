package osh.toolbox.appliance.ih;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import osh.configuration.appliance.XsdApplianceProgramConfiguration;
import osh.configuration.appliance.XsdApplianceProgramConfigurations;
import osh.configuration.appliance.XsdDescription;
import osh.configuration.appliance.XsdDescriptions;
import osh.configuration.appliance.XsdLoad;
import osh.configuration.appliance.XsdLoadProfile;
import osh.configuration.appliance.XsdLoadProfiles;
import osh.configuration.appliance.XsdPhase;
import osh.configuration.appliance.XsdPhases;
import osh.configuration.appliance.XsdProgram;
import osh.configuration.appliance.XsdTick;
import osh.datatypes.commodity.Commodity;
import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.utils.csv.CSVImporter;

/**
 * 
 * @author Ingo Mauser
 * 
 */
public class generate_IH_3CO_1PH_1PR_SC_G {

	// for JAXBContext
	static String contextPath = "osh.configuration.appliance";
	
	// device data
	static ToolApplianceConfiguration[] wpConfigs = IH_3CO_1PH_1PR_SC_G.configurations;
	
	static String outputFile = "data/profiles/out/ih/IH_3CO_1PH_1PR_SC_G.xml";
	
	
	public static void main(String[] args) {

		XsdApplianceProgramConfigurations xsdAPCs = new XsdApplianceProgramConfigurations(); 
		
		List<XsdApplianceProgramConfiguration> listCtWashingParameterConfiguration = xsdAPCs.getApplianceProgramConfiguration();

		for (ToolApplianceConfiguration wpConfig : wpConfigs) {
			
			XsdApplianceProgramConfiguration apc = new XsdApplianceProgramConfiguration();
			listCtWashingParameterConfiguration.add(apc);
			
			// attribute: washingParameterConfigurationID
			apc.setId(wpConfig.configurationID);
			
			// attribute: washingParameterConfigurationName (optional)
			
			// element: Program (mandatory)
			XsdProgram program = new XsdProgram();
			program.setId(wpConfig.program.programID);
			program.setName(wpConfig.program.programName);
			XsdDescriptions desc = new XsdDescriptions();
			program.setDescriptions(desc);
			List<XsdDescription> desclist = desc.getDescription();
			{
				XsdDescription descEN = new XsdDescription();
				descEN.setLanguage("EN");
				descEN.setValue(wpConfig.program.descriptionEN);
				desclist.add(descEN);
			}
			{
				XsdDescription descDE = new XsdDescription();
				descDE.setLanguage("DE");
				descDE.setValue(wpConfig.program.descriptionDE);
				desclist.add(descDE);
			}
			apc.setProgram(program);
			
			// element: Extras (optional)
			
			// element: Parameters (optional)
			
			// element: LoadProfiles (mandatory)
			XsdLoadProfiles loadProfiles = new XsdLoadProfiles();
			apc.setLoadProfiles(loadProfiles);
			List<XsdLoadProfile> listCtLoadProfile = loadProfiles.getLoadProfile();
			for (ToolApplianceConfigurationProfile profile : wpConfig.profiles) {
				XsdLoadProfile loadProfile = new XsdLoadProfile();
				listCtLoadProfile.add(loadProfile);

				loadProfile.setId(profile.profileID);
				loadProfile.setName(profile.profileName);
				
				// Segments
				XsdPhases phases = new XsdPhases();
				loadProfile.setPhases(phases);
				List<XsdPhase> listPhases = phases.getPhase();
				int numberOfPhases = profile.phaseInputFiles.length;
				for (int i = 0; i < numberOfPhases; i++) {
					XsdPhase phase = new XsdPhase();
					phase.setId(i);
					phase.setName(profile.phaseNames[i]);
					
					listPhases.add(phase);
					
					List<XsdTick> listPhase = phase.getTick();
					int[][] phaseValues = CSVImporter.readInteger2DimArrayFromFile(profile.phaseInputFiles[i], ";", null);
					
					for (int j = 0; j < phaseValues.length; j++) {
						XsdTick tick = new XsdTick();
						listPhase.add(tick);
						List<XsdLoad> listLoad = tick.getLoad();
						
						if (profile.activePowerColumn >= 0) {
							XsdLoad ap = new XsdLoad();
							listLoad.add(ap);
							ap.setCommodity(Commodity.ACTIVEPOWER.toString());
							ap.setValue(phaseValues[j][profile.activePowerColumn]);
						}
						if (profile.reactivePowerColumn >= 0) {
							XsdLoad ap = new XsdLoad();
							listLoad.add(ap);
							ap.setCommodity(Commodity.REACTIVEPOWER.toString());
							ap.setValue(phaseValues[j][profile.reactivePowerColumn]);
						}
						if (profile.naturalGasPowerColumn >= 0) {
							XsdLoad ap = new XsdLoad();
							listLoad.add(ap);
							ap.setCommodity(Commodity.NATURALGASPOWER.toString());
							ap.setValue(phaseValues[j][profile.naturalGasPowerColumn]);
						}
					}
					
					// set length to min max lengths
					phase.setMinLength(phaseValues.length);
					phase.setMaxLength(phaseValues.length);
					if (phaseValues.length == 1) {
						phase.setMaxLength(86400);
					}
					
				}
				
			}
			
		}
		
		// marshall to file
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(xsdAPCs, new File(outputFile));

		} catch (JAXBException e3) {
			e3.printStackTrace();
		}

		System.out.println("DONE");

	}

}
