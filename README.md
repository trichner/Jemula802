Jemula802
=========

The emulation software to model 802.11 and other 802 wireless communication systems. Builds on jemula emulation kernel.

This project requires [Jemula](https://github.com/schmist/Jemula) as kernel.

Overview
--------

Jemula 802 is a tool for simulating IEEE 802.11 wireless networks. It is also capable of simulating WIFI mesh networks as in the 802.11s draft. This page gives a general overview of the tool. If you are interested in using it, you can read the user guide.
Simulator

The tool is written in Java and has an event-based architecture. The connectivity model used is a unit disk model which means that whenever a station is inside a specified radius of another, the stations have a perfect, error free connection, outside this radius the stations are not connected. Simulations are configured using an XML file. In this XML file it is possible to specify values for almost every parameter in the system. The configuration parameters of the MAC layer are configurable separately for each station. 
The results of the simulation currently include throughput (end to end and per hop), offered throughput and delay (end to end and per hop).

GUI
---

Jemula provides a GUI to visualize packet traffic among stations. It displays the timeline of packet traffic for each station. Additionally it displays network allocation vectors and backoff times for each station. 


Animation
---------

Another type of visualization is the Google Earth animation of a scenario. In contrast to the packet accurate GUI, it provides visualization of aggregated throughput and averaged delay. The animation illustrates the throughput/delay per station or per link. It also displays the ratio between offered throughput and action throughput (red and blue blocks). 


The tool is published as is under a BSD free software license.
