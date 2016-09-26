package osh.mgmt.globalcontroller.jmetal.esc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.PowerInterval;

public class CostChecker extends Thread {
	
	private static boolean running = true;
	private static PrintWriter pw;
	private static final double EPSILON = 0.000001;
	private static final double WsTOkWHDivisor = 3600000.0;
	private static int counter = 0;
	private static int epsObjective;
	private static int plsObjective;
	private static int varObjective;
	private static double upperOverlimitFactor;
	private static double lowerOverlimitFactor;
	
	
	public static void shutDown() {
		running = false;
	}
	
	private CostChecker() {
	}
	
	public static void init(int epsObjective, int plsObjective, int varObjective, double upperOverlimitFactor, double lowerOverlimitFactor) {
		try {
			pw = new PrintWriter("CostLog_" + (System.currentTimeMillis() / 1000) + ".txt");
			CostChecker.epsObjective = epsObjective;
			CostChecker.plsObjective = plsObjective;
			CostChecker.varObjective = varObjective;
			CostChecker.upperOverlimitFactor = upperOverlimitFactor;
			CostChecker.lowerOverlimitFactor = lowerOverlimitFactor;
			CostChecker.logQueue = new LinkedBlockingQueue<CheckObject>();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		new CostChecker().start();
	}
	
	private static Queue<CheckObject> logQueue;	
	
	@Override
	public void run() {
		while (running || !logQueue.isEmpty()) {
            try {
            	CheckObject work = null;

                synchronized ( logQueue ) {
                    while ( logQueue.isEmpty() )
                    	logQueue.wait();
                    
                    // Get the next work item off of the queue
                    work = logQueue.remove();
                }
                System.out.println("Done: " + counter + ", Todo: " + logQueue.size());
                checkCosts(work);
                counter++;
            }
            catch ( InterruptedException ie ) {
                break;  // Terminate
            }
        }
		pw.flush();
		pw.close();
	}
	
	
	
	
	public static void checkCosts(CheckObject cq) {
		double finalEpsCosts = 0, finalPlsCosts = 0, finalFeedInCosts = 0, finalGasCosts = 0, finalAutoConsumptionCosts = 0;
		
		for (long time = cq.from; time < cq.to; time++) {
			double epsCosts = 0, plsCosts = 0, feedInCosts = 0, gasCosts = 0, autoConsumptionCosts = 0;
			//ACTIVEPOWER Costs
			double activePower = cq.meter.getLoadAt(AncillaryCommodity.ACTIVEPOWEREXTERNAL, time);
			double activePrice = cq.eps.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPrice(time);
			PowerInterval activePI = null;

			if (activePower > 0)
				epsCosts += activePower * activePrice;

			if (plsObjective > 0) {
				activePI = cq.pls.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPowerLimitInterval(time);

				double upperLimit = activePI.getPowerUpperLimit();			

				if (activePower > upperLimit)
					plsCosts += upperOverlimitFactor * Math.abs(activePower - upperLimit) * activePrice;

				//If Feed-In costs, lowerLimit Violations will be calculated there
				if (epsObjective < 1) {
					double lowerLimit = activePI.getPowerLowerLimit();

					if (activePower < lowerLimit)
						plsCosts += lowerOverlimitFactor * Math.abs(activePower - lowerLimit) * activePrice;
				}
			}

			//GASPOWER Costs
			double power = 0.0, price = 0.0;
				power = cq.meter.getLoadAt(AncillaryCommodity.NATURALGASPOWEREXTERNAL, time);
			if (cq.eps.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL) != null) 
				price = cq.eps.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(time);

			gasCosts += power * price;

			//FeedIn and/or AutoConsumption costs
			if (epsObjective > 0) {

				//FEEDIN costs
				double feedInPower = 0;

				//FeedIn costs for PV
				double pvFeedIn = cq.meter.getLoadAt(AncillaryCommodity.PVACTIVEPOWERFEEDIN, time);				

				if (pvFeedIn < 0) {
					feedInPower += pvFeedIn;
					feedInCosts += pvFeedIn * cq.eps.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPrice(time);
				}				

				//FeedIn costs for CHP if eps == 3 || eps == 4
				if (epsObjective > 2) {
					double chpFeedIn = cq.meter.getLoadAt(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, time);


					if (chpFeedIn < 0) {
						feedInPower += chpFeedIn;
						feedInCosts += chpFeedIn * cq.eps.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPrice(time);
					}					
				}

				epsCosts += feedInCosts;

				//PLS costs for active Power --> negative FeedInCosts over the limit * lowerOverLimitFactor
				if (plsObjective > 0) {
					double lowerLimit = activePI.getPowerLowerLimit();

					if (activePower < lowerLimit) {
						plsCosts -= lowerOverlimitFactor * Math.abs(Math.abs(activePower - lowerLimit) / feedInPower) * feedInCosts;
					}				
				}


				//AUTOCONSUMPTION costs
				if (epsObjective == 2 || epsObjective == 4) {				

					//AUTOCONSUMPTION costs for PV
					double pvAutoConsumption = cq.meter.getLoadAt(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, time);	
					if (pvAutoConsumption < 0)
						autoConsumptionCosts += pvAutoConsumption * cq.eps.get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION).getPrice(time);


					//AUTOCONSUMPTION costs for CHP if eps == 4
					if (epsObjective > 2) {
						double chpAutoConsumption = cq.meter.getLoadAt(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, time);	
						if (chpAutoConsumption < 0)
							autoConsumptionCosts += chpAutoConsumption * cq.eps.get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION).getPrice(time);

					}

					epsCosts += autoConsumptionCosts;
				}
			}

			//var == 1 --> calculate ReactivePower Costs
			if (varObjective > 0) {

				double varPower = cq.meter.getLoadAt(AncillaryCommodity.REACTIVEPOWEREXTERNAL, time);
				PriceSignal varPrice = cq.eps.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

				//mosts EPS dont have a varPriceSignal, so we check to be on the safe side
				if (varPrice != null) {

					epsCosts += Math.abs(varPower) * varPrice.getPrice(time);

					//pls == 2 --> we need to respect PLS for ReactivePower
					if (plsObjective == 2) {
						PowerInterval varPI = cq.pls.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPowerLimitInterval(time); 
						double lowerLimit = varPI.getPowerLowerLimit();
						double upperLimit = varPI.getPowerUpperLimit();

						if (varPower > upperLimit)
							plsCosts += upperOverlimitFactor * Math.abs(upperLimit - varPower) * varPrice.getPrice(time); 
						else if (varPower < lowerLimit)
							plsCosts += lowerOverlimitFactor * Math.abs(lowerLimit - varPower) * varPrice.getPrice(time); 
					}							

				}		
			}
			
			finalEpsCosts += epsCosts;
			finalPlsCosts += plsCosts;
			finalFeedInCosts += feedInCosts; 
			finalGasCosts += gasCosts; 
			finalAutoConsumptionCosts+= autoConsumptionCosts;
		}
		finalEpsCosts /= WsTOkWHDivisor;
		finalPlsCosts /= WsTOkWHDivisor;
		finalFeedInCosts /= WsTOkWHDivisor;
		finalGasCosts /= WsTOkWHDivisor;
		finalAutoConsumptionCosts /= WsTOkWHDivisor;
		
		
		if (Math.abs((finalEpsCosts + finalPlsCosts + finalGasCosts) - cq.shouldCosts) > EPSILON) {
			System.out.println("COST MISMATCH, LOGGING");
			
			pw.println("-----------------------------------------------------------------------------------------------------------------");
			if (cq.last)
				pw.println("was last eval!!!");
			pw.print("Shouldcosts: " + cq.shouldCosts + ", is: eps: " + finalEpsCosts + ", pls: " + finalPlsCosts + ", feedIn: " + finalFeedInCosts + ", autoCon: " + finalAutoConsumptionCosts + ", gas: " + finalGasCosts);
			pw.println();
			pw.println("From: " + cq.from + ", to: " + cq.to);
			pw.println(cq.meter.toString());
			pw.println();
			for (Entry<AncillaryCommodity, PriceSignal> en : cq.eps.entrySet()) {
				pw.println(en.getKey() + ": " + en.getValue().getPrices());
			}
			pw.println();
			for (Entry<AncillaryCommodity, PowerLimitSignal> en : cq.pls.entrySet()) {
				pw.println(en.getKey() + ": " + en.getValue().getLimits());
			}
			
		}		
	}
	
	public synchronized static void enqueueCheck(AncillaryCommodityLoadProfile meter, EnumMap<AncillaryCommodity, PriceSignal> eps, 
			EnumMap<AncillaryCommodity, PowerLimitSignal> pls, double shouldCosts, long from, long to) {
		enqueueCheck(meter, eps, pls, shouldCosts, from, to, false);	
	}

	public synchronized static void enqueueCheck(AncillaryCommodityLoadProfile meter, EnumMap<AncillaryCommodity, PriceSignal> eps, 
			EnumMap<AncillaryCommodity, PowerLimitSignal> pls, double shouldCosts, long from, long to, boolean last) {
		synchronized(logQueue) {
			CheckObject logInf = new CheckObject(meter, eps, pls, shouldCosts, from, to, last);		
			logQueue.add(logInf);
			logQueue.notify();
		}
		
		while (logQueue.size() > 500) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static class CheckObject {
		public AncillaryCommodityLoadProfile meter;
		public EnumMap<AncillaryCommodity, PriceSignal> eps;
		public EnumMap<AncillaryCommodity, PowerLimitSignal> pls;
		public double shouldCosts;
		public long from;
		public long to;
		public boolean last = false;
		
		public CheckObject(AncillaryCommodityLoadProfile meter, EnumMap<AncillaryCommodity, PriceSignal> eps,
				EnumMap<AncillaryCommodity, PowerLimitSignal> pls, double shouldCosts, long from, long to, boolean last) {
			super();
			this.meter = meter;
			this.eps = eps;
			this.pls = pls;
			this.shouldCosts = shouldCosts;
			this.from = from;
			this.to = to;
			this.last = last;
		}
		
	
	}
}
