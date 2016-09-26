package osh.driver.chiller;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class AdsorptionChillerModel {
	//TODO chilled water in
	//TODO chilled water out
			
	/**
	 * Chilled Water Power [kW] (positive value)
	 * 
	 * @param hotWater 			Hot Water Temperature [°C]
	 * -param chilledWaterIn 	Chilled Water Temperature in [°C]
	 * -param chilledWaterOut 	Chilled Water Temperature out [°C]
	 * @param recooling 		Recooling Temperature [°C]
	 * 
	 * @return Cold Water Power [kW] (negative value)
	 */
	private static double powerHelp(double hotWater, double recooling) {
	      
		double y1 = 0;
		double y2 = 0;
		double x1 = 0;
		double x2 = 0;

		double y;

		// 22 degree Celsius recooling temperature
		if (recooling == 22) {
			// check hot water temperature
			if (hotWater >= 45 && hotWater < 50) {
				y1 = 2.75;
				y2 = 4;
				x1 = 45;
				x2 = 50;
			} else if (hotWater >= 50 && hotWater < 55) {
				y1 = 4;
				y2 = 6;
				x1 = 50;
				x2 = 55;
			} else if (hotWater >= 55 && hotWater < 60) {
				y1 = 6;
				y2 = 9;
				x1 = 55;
				x2 = 60;
			} else if (hotWater >= 60 && hotWater < 65) {
				y1 = 9;
				y2 = 10.375;
				x1 = 60;
				x2 = 65;
			} else if (hotWater >= 65 && hotWater < 70) {
				y1 = 10.375;
				y2 = 11;
				x1 = 65;
				x2 = 70;
			} else if (hotWater >= 70 && hotWater <= 75) {
				y1 = 11;
				y2 = 10.75;
				x1 = 70;
				x2 = 75;
			} else if (hotWater < 45) {
				return 2.75;
			} else if (hotWater > 75) {
				return 10.75;
			}
		}

		// 27 degree Celsius recooling temperature
		if (recooling == 27) {

			if (hotWater >= 55 && hotWater < 60) {
				y1 = 3.5;
				y2 = 5.75;
				x1 = 55;
				x2 = 60;
			} else if (hotWater >= 60 && hotWater < 65) {
				y1 = 5.75;
				y2 = 8.25;
				x1 = 60;
				x2 = 65;
			} else if (hotWater >= 65 && hotWater < 70) {
				y1 = 8.25;
				y2 = 9.25;
				x1 = 65;
				x2 = 70;
			} else if (hotWater >= 70 && hotWater <= 75) {
				y1 = 9.25;
				y2 = 9.5;
				x1 = 70;
				x2 = 75;
			} else if (hotWater < 55) {
				return 3.5;
			} else if (hotWater > 75) {
				return 9.5;
			}
		}

		// 32 degree Celsius recooling temperature
		if (recooling == 32) {

			if (hotWater >= 60 && hotWater < 65) {
				y1 = 3.25;
				y2 = 5.25;
				x1 = 60;
				x2 = 65;
			} else if (hotWater >= 65 && hotWater < 70) {
				y1 = 5.25;
				y2 = 7;
				x1 = 65;
				x2 = 70;
			} else if (hotWater >= 70 && hotWater <= 75) {
				y1 = 7;
				y2 = 7.25;
				x1 = 70;
				x2 = 75;
			} else if (hotWater < 60) {
				return 3.25;
			} else if (hotWater > 75) {
				return 7.25;
			}
		}

		// 37 degree Celsius recooling temperature
		if (recooling == 37) {
			if (hotWater >= 65 && hotWater < 70) {
				y1 = 2.5;
				y2 = 4.25;
				x1 = 65;
				x2 = 70;
			} else if (hotWater >= 70 && hotWater <= 75) {
				y1 = 4.25;
				y2 = 4.75;
				x1 = 70;
				x2 = 75;
			} else if (hotWater < 65) {
				return 2.5;
			} else if (hotWater > 75) {
				return 4.75;
			}
		}

		// interpolate
		y = y1 + ((y2 - y1) / (x2 - x1)) * (hotWater - x1);

		return y;
	}
	
	// Coefficient of performance (COP)
	private static double copHelp(double hotWater, double recooling) {
	      
		  double y1 = 0;
		  double y2 = 0;
		  double x1 = 0;
		  double x2 = 0;
		  
		  double y;
		  
		  // 22 degree Celsius recooling temperature
		  if(recooling == 22) {
		  
			  if(hotWater >= 45 && hotWater < 50) {
				  
				  y1 = 0.425;
				  y2 = 0.47;
				  x1 = 45;
				  x2 = 50;
			  }
			  else if(hotWater >= 50 && hotWater < 55) {
				  
				  y1 = 0.47;
				  y2 = 0.535;
				  x1 = 50;
				  x2 = 55;
			  }
			  else if(hotWater >= 55 && hotWater < 60) {
				  
				  y1 = 0.535;
				  y2 = 0.64;
				  x1 = 55;
				  x2 = 60;
			  }
			  else if(hotWater >= 60 && hotWater < 65) {
				  
				  y1 = 0.64;
				  y2 = 0.65;
				  x1 = 60;
				  x2 = 65;
			  }
			  else if(hotWater >= 65 && hotWater < 70) {
				  
				  y1 = 0.65;
				  y2 = 0.64;
				  x1 = 65;
				  x2 = 70;
			  }
			  else if(hotWater >= 70 && hotWater <= 75) {
				  
				  y1 = 0.64;
				  y2 = 0.63;
				  x1 = 70;
				  x2 = 75;
			  }
			  else if(hotWater < 45) {
				  
				  return 0.425;
			  }
			  else if(hotWater > 75) {
				  
				  return 0.63;
			  }
		  }
		  
		  // 27 degree Celsius recooling temperature
		  if(recooling == 27) {
		  
			  if(hotWater >= 55 && hotWater < 60) {
				  
				  y1 = 0.43;
				  y2 = 0.54;
				  x1 = 55;
				  x2 = 60;
			  }
			  else if(hotWater >= 60 && hotWater < 65) {
				  
				  y1 = 0.54;
				  y2 = 0.625;
				  x1 = 60;
				  x2 = 65;
			  }
			  else if(hotWater >= 65 && hotWater < 70) {
				  
				  y1 = 0.625;
				  y2 = 0.627;
				  x1 = 65;
				  x2 = 70;
			  }
			  else if(hotWater >= 70 && hotWater <= 75) {
				  
				  y1 = 0.627;
				  y2 = 0.63;
				  x1 = 70;
				  x2 = 75;
			  }
			  else if(hotWater < 55) {
				  
				  return 0.43;
			  }
			  else if(hotWater > 75) {
				  
				  return 0.63;
			  }
		  }
		  
		  // 32 degree Celsius recooling temperature
		  if(recooling == 32) {
		  
			  if(hotWater >= 60 && hotWater < 65) {
				  
				  y1 = 0.425;
				  y2 = 0.575;
				  x1 = 60;
				  x2 = 65;
			  }
			  else if(hotWater >= 65 && hotWater < 70) {
				  
				  y1 = 0.575;
				  y2 = 0.6125;
				  x1 = 65;
				  x2 = 70;
			  }
			  else if(hotWater >= 70 && hotWater <= 75) {
				  
				  y1 = 0.6125;
				  y2 = 0.5875;
				  x1 = 70;
				  x2 = 75;
			  }
			  else if(hotWater < 60) {
				  
				  return 0.425;
			  }
			  else if(hotWater > 75) {
				  
				  return 0.5875;
			  }
		  }
		  
		  // 37 degree Celsius recooling temperature
		  if(recooling == 37) {
		  
			  if(hotWater >= 65 && hotWater < 70) {
				  
				  y1 = 0.44;
				  y2 = 0.52;
				  x1 = 65;
				  x2 = 70;
			  }
			  else if(hotWater >= 70 && hotWater <= 75) {
				  
				  y1 = 0.52;
				  y2 = 0.505;
				  x1 = 70;
				  x2 = 75;
			  }
			  else if(hotWater < 65) {
				  
				  return 0.44;
			  }
			  else if(hotWater > 75) {
				  
				  return 0.505;
			  }
		  }
		   
		  // Interpolate
		  y =  y1 + ((y2 - y1) / (x2-x1)) * (hotWater - x1);
		   
	      return y; 
	}
	
	/**
	 * Chilled Water Power [W] (negative value)
	 * 
	 * @param hotWaterTemperature Hot Water Temperature [°C]
	 * @param outdoorTemperature Outdoor Temperature [°C]
	 * 
	 * @return Cold Water Power [W] (negative value)
	 */
	public static int chilledWaterPower(double hotWaterTemperature, double outdoorTemperature) {
		
		double recoolingTemperature = outdoorToCondenserTemperature(outdoorTemperature);
		
		double a;
		double b;
		double result = 0;
		
		double w1 = 0;
		double w2 = 0;
		
		if(recoolingTemperature < 22) {
			a = powerHelp(hotWaterTemperature, 22);
			result = a;
		}
		else if(recoolingTemperature >= 22 && recoolingTemperature < 27) {
			a = powerHelp(hotWaterTemperature, 22);
			b = powerHelp(hotWaterTemperature, 27);
			
			w1 = (recoolingTemperature - 27) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(recoolingTemperature >= 27 && recoolingTemperature < 32) {
			a = powerHelp(hotWaterTemperature, 27);
			b = powerHelp(hotWaterTemperature, 32);
						
			w1 = (recoolingTemperature - 32) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(recoolingTemperature >= 32 && recoolingTemperature <= 37) {
			a = powerHelp(hotWaterTemperature, 32);
			b = powerHelp(hotWaterTemperature, 37);
						
			w1 = (recoolingTemperature - 37) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(recoolingTemperature > 37) {
			a = powerHelp(hotWaterTemperature, 37);
			
			result = a;
		}
		
		int returnValue = (int) (result  * (-1000));
		return returnValue;
	}
	
	/**
	 * Empirical relation between outdoor temperature and condenser temperature
	 * @param z Outdoor temperature [°C]
	 * @return
	 */
	private static double outdoorToCondenserTemperature(double z) {
		double returnValue = Math.max(0.8781 * z + 4.365, z + 1);
		return returnValue;
	}

	/**
	 * (0..1]
	 * 
	 * @param x Hot Water Temperature [°C]
	 * @param z Outdoor Temperature [°C]
	 * 
	 * @return (0..1]
	 */
	public static double cop(double x, double z) {
		
		z = outdoorToCondenserTemperature(z);
		
		double a;
		double b;
		double result = 0;
		
		double w1 = 0;
		double w2 = 0;
		
		if(z < 22) {
			
			a = copHelp(x, 22);
			
			result = a;
		}
		if(z >= 22 && z < 27) {
			
			a = copHelp(x, 22);
			b = copHelp(x, 27);
			
			w1 = (z - 27) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(z >= 27 && z < 32) {
			
			a = copHelp(x, 27);
			b = copHelp(x, 32);
						
			w1 = (z - 32) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(z >= 32 && z <= 37) {
			
			a = copHelp(x, 32);
			b = copHelp(x, 37);
						
			w1 = (z - 37) / (-5);
			w2 = 1 - w1;
			
			result = w1 * a + w2 * b;
		}
		else if(z > 37) {
			
			a = copHelp(x, 37);
			
			result = a;
		}
		
		return result != 0 ? result : 1;
	}
	
}
