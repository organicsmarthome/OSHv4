package osh.mgmt.globalcontroller.jmetal;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.StoppingRule;
import osh.configuration.system.ConfigurationParameter;

/**
	 * INNER CLASS
	 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
	 *
	 */
@SuppressWarnings({"rawtypes", "unchecked"})
	public class GAParameters implements Cloneable {
		private int numEvaluations;
		
		private int popSize;
		
		private String crossoverOperator;
		private String mutationOperator;
		private String selectionOperator;
		
		private HashMap crossoverParameters;
		
		private HashMap mutationParameters;
		
		private HashMap selectionParameters;
		
		private HashMap<String, HashMap> stoppingRules;
		
		public GAParameters() {
			crossoverOperator = "SingleBinaryNPointsCrossover";
			mutationOperator = "BitFlipMutation";
			selectionOperator = "BinaryTournament";
			numEvaluations = 1000;			
			popSize = 50;
			
			//crossOverProbability
			crossoverParameters = new HashMap();			
			crossoverParameters.put("probability", String.valueOf(0.7));
			crossoverParameters.put("points", String.valueOf(2));
			//mutationProbability
			mutationParameters = new HashMap();
			mutationParameters.put("probability", String.valueOf(0.1));
			
			selectionParameters = new HashMap();
			
			stoppingRules = new HashMap<String, HashMap>();
			HashMap ruleParams = new HashMap();
			ruleParams.put("populationSize", popSize);
			ruleParams.put("maxEvaluations", numEvaluations);
			stoppingRules.put("EvaluationsStoppingRule", ruleParams);
		}
		
		
		public GAParameters(int numEvaluations, int popSize, String crossoverOperator, String mutationOperator,
		String selectionOperator, HashMap crossoverParameters, HashMap mutationParameters,
		HashMap selectionParameters, HashMap<String, HashMap> stoppingRules) {
			super();
			this.numEvaluations = numEvaluations;
			this.popSize = popSize;
			this.crossoverOperator = crossoverOperator;
			this.mutationOperator = mutationOperator;
			this.selectionOperator = selectionOperator;
			this.crossoverParameters = crossoverParameters;
			this.mutationParameters = mutationParameters;
			this.selectionParameters = selectionParameters;
			this.stoppingRules = stoppingRules;
		}
		
		public GAParameters(GAConfiguration gaConfig) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			super();
			this.numEvaluations = gaConfig.getNumEvaluations();
			this.popSize = gaConfig.getPopSize();
			this.crossoverOperator = gaConfig.getCrossoverOperator();
			this.mutationOperator = gaConfig.getMutationOperator();
			this.selectionOperator = gaConfig.getSelectionOperator();
			this.crossoverParameters = new HashMap();
			for (ConfigurationParameter cp : gaConfig.getCrossoverParameters()) {
				Class cl = Class.forName(cp.getParameterType());
				this.crossoverParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
			}
			this.mutationParameters = new HashMap();
			for (ConfigurationParameter cp : gaConfig.getMutationParameters()) {
				Class cl = Class.forName(cp.getParameterType());
				this.mutationParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
			}
			this.selectionParameters = new HashMap();
			for (ConfigurationParameter cp : gaConfig.getSelectionParameters()) {
				Class cl = Class.forName(cp.getParameterType());
				this.selectionParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
			}
			this.stoppingRules = new HashMap<String, HashMap>();
			for (StoppingRule sr : gaConfig.getStoppingRules()) {
				HashMap params = new HashMap();
				for (ConfigurationParameter cp : sr.getRuleParameters()) {
					Class cl = Class.forName(cp.getParameterType());
					params.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
				}
				stoppingRules.put(sr.getStoppingRuleName(), params);
			}
		}

		public int getNumEvaluations() {
			return numEvaluations;
		}

		public int getPopSize() {
			return popSize;
		}

		public String getCrossoverOperator() {
			return crossoverOperator;
		}

		public String getMutationOperator() {
			return mutationOperator;
		}

		public String getSelectionOperator() {
			return selectionOperator;
		}

		public HashMap getCrossoverParameters() {
			return crossoverParameters;
		}

		public HashMap getMutationParameters() {
			return mutationParameters;
		}

		public HashMap getSelectionParameters() {
			return selectionParameters;
		}

		public HashMap<String, HashMap> getStoppingRules() {
			return stoppingRules;
		}


		@Override
		public GAParameters clone() {
			GAParameters other = new GAParameters();
			
			other.crossoverOperator = this.crossoverOperator;
			other.mutationOperator = this.mutationOperator;
			other.selectionOperator = this.selectionOperator;
			other.numEvaluations = this.numEvaluations;
			other.popSize = this.popSize;
			
			other.crossoverParameters = new HashMap(this.crossoverParameters);
			
			other.mutationParameters = new HashMap(this.mutationParameters);
			
			other.selectionParameters = new HashMap(this.selectionParameters);
			
			other.stoppingRules = new HashMap(this.stoppingRules);
			
			return other;
		}
	}