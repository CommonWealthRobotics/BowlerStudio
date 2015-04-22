package com.neuronrobotics.nrconsole.plugin.bootloader.gui;


public class NR_BootloaderDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0){
			//Start gui
			new NRBootLoaderApp();
		}else{
			new NRBootLoaderApp(args);
		}
		
	}

	
}
