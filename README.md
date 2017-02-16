# OSHv4, Version 4.0

Framework for Multi-commodity Energy Management in intelligent buildings.
Copyright (C) 2016

[Organic Smart Home](http://www.organicsmarthome.com) (OSH) is a free and open source energy management framework, which optimizes the energy provision, distribution, storage, conversion, and utilization in intelligent buildings, such as smart buildings and smart homes.

This framework was developed in response to the challenge of balancing supply and demand in the electric grid in spite of volatile, widely uncontrollable electrical power generation by means of renewable energy sources.

The building energy management system is based on the Extended Observer/Controller (O/C) architecture, which has been developed within the German priority research program on [Organic Computing](http://www.organic-computing.de) (DFG-Schwerpunktprogramm 1183).

It aims at realizing a mostly self-organizing and automated energy management system, reducing the need for interaction between the users (e.g., occupants) and the building's devices and systems (e.g., appliances, distributed generation, and building energy management system). However, the system still allows for explicit interference--if desired--and the users remain always in the loop.


## Technical Information

* Java 8+
* Eclipse Mars+


## Overview

* <b>source</b> : source code of the Organic Smart Home
* <b>source/osh_sim_builder</b> : OSH tools for the generation of XML configuration files (simulation mode) 
  * <code>constructSimulationPackage</code> : OSH configuration file generator
    * Output : OSH configuration files in <b>source/osh_sim_builder/configfiles/simulationPackages/[timestamp]</b> (has to be copied to <b>source/osh_sim_loader/configfiles/osh/</b>)
* <b>source/osh_sim_loader</b> : OSH in simulation mode
  * <code>runSimulationPackage</code> : OSH simulation launcher (simulation mode)
    * Input : OSH configuration files in <b>source/osh_sim_loader/configfiles/osh/[timestamp]</b>
* <b>source/osh_core</b> : core components of the OSH (e.g. generic components of the Observer/Controller architecture, Communication Registries, Energy Simulation Core)
* <b>source/osh_ems_ea</b> : components of the optimization layer (partly based on [jMetal 4.x](https://github.com/jMetal/jMetal))
* <b>source/osh_busdriver*</b> : bus drivers (Miele XGW2000 gateway, BacNet, EnOcean, WAGO 750-820*)
* <b>source/osh_comdriver*</b> : communication drivers (user interaction, logging, REST service, external signals, weather prediction)
* <b>source/osh_driver*</b> : device drivers (home appliances, baseload, battery storage, microCHP, HVAC, PV system, smart meter)
* <b>source/xbin_lib</b> : required libraries (not provided, please see README for list of libraries)


## How 2 Use

* Check out
* Get libraries, see <b>source/xbin_lib/README.md</b>
* Run <code>runSimulationPackage</code> in <b>source/osh_sim_loader</b>


## Contact and Main Contributors

* Kaibin Bao (bao@kit.edu)
* Sebastian Kochanneck (kochanneck@kit.edu)
* Sebastian Kramer
* Ingo Mauser (mauser@kit.edu) - <b>main contact</b>
* Jan Müller (jan.mueller@kit.edu)


## Literature

For further information, please consult the following publications:

Florian Allerding, Hartmut Schmeck: <br />
"Organic Smart Home - Architecture for Energy Management in Intelligent Buildings", <br />
Proceedings of the 2011 workshop on Organic computing, 2011.  <br />
http://dl.acm.org/citation.cfm?id=1998654

Florian Allerding: <br />
"Organic Smart Home - Energiemanagement für Intelligente Gebäude", <br />
KIT Scientific Publishing, 2013. <br />
http://www.ksp.kit.edu/9783731501817

Ingo Mauser, Jan Müller, Florian Allerding, Hartmut Schmeck: <br />
"Adaptive Building Energy Management with Multiple Commodities and Flexible Evolutionary Optimization", <br />
Renewable Energy, 87, Part 2, p.911-921, 2016. <br />
http://www.sciencedirect.com/science/article/pii/S0960148115302834


## Citing

If you use the Organic Smart Home in your research, please consider citing:
<pre><code>
@Article{mauser2016adaptive,
  Title                    = {{Adaptive Building Energy Management with Multiple Commodities and Flexible Evolutionary Optimization}},
  Author                   = {Ingo Mauser and Jan M{\"u}ller and Florian Allerding and Hartmut Schmeck},
  Journal                  = {Renewable Energy},
  Year                     = {2016},
  Pages                    = {911 - 921},
  Volume                   = {87, Part 2},
  Doi                      = {10.1016/j.renene.2015.09.003},
  ISSN                     = {0960-1481},
  nolanguage = {English}
}
</code></pre>


## License

The Organic Smart Home is licensed under the GPL. 

If you have licensed this product under the GPL, please see the LICENSE file for more information and the GPL version. 

The adapted version of jMetal is licensed under the LGPL as the original version of jMetal 4.5.
