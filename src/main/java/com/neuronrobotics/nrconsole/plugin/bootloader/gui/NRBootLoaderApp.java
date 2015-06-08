package com.neuronrobotics.nrconsole.plugin.bootloader.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.neuronrobotics.sdk.bootloader.Core;
import com.neuronrobotics.sdk.bootloader.Hexml;
import com.neuronrobotics.sdk.bootloader.NRBoot;
import com.neuronrobotics.sdk.bootloader.NRBootCoreType;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

public class NRBootLoaderApp {
	public NRBootLoaderApp(String[] args){
		String port = null;
		Hexml hex=null;
		ArrayList<Core> cores = new ArrayList<Core>();
		for (int i=0;i<args.length;i++){
			if(args[i].contains("--core")){
				BootloaderParams param = new BootloaderParams();
				try{
					Integer coreNum= new Integer(args[i+1]);
					param.setCore(coreNum.intValue());
				}catch(Exception e){
					fail();
				}
				String coreType=args[i+2];
				if(coreType.contains("AVR")){
					param.setType(NRBootCoreType.AVRxx4p);
				}else if(coreType.contains("PIC")){
					param.setType(NRBootCoreType.PIC32);
				}else{
					System.err.println("Core Types are:\nAVR\nPIC32");
					fail();
				}
				String hexFile = args[i+3];
				try {
					new FileInputStream(hexFile);
					param.setHexFilePath(hexFile);
				} catch (FileNotFoundException e) {
					System.err.println("File "+hexFile+" Does not exist");
					fail();
				}
				cores.add(new Core(param.getCore(),param.getHexFilePath(),param.getType()));
			}
			if (args[i].contains("--port")){
				port = args[i+1];
			}
			if (args[i].contains("--xml")){
				try {
					hex = new Hexml(new File(args[i+1]));
				}catch (Exception e) {
					e.printStackTrace();
	        		String message = "Invalid xml file";
	        		JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
	        		return;
				}
			}
		}
		if (hex != null){
			cores = hex.getCores();
		}
		NRBoot blApp;
		blApp = new NRBoot(port);
		for (Core b:cores){
			blApp.load(b);
		}
		blApp.reset();
		System.exit(0);
	}
//	public NRBootLoaderApp(){
//		NR_Bootloader_GUI gui = new NR_Bootloader_GUI();
//		////System.out.println("Waiting for port selection");
//		boolean getAp = false;
//		boolean wasSelected = false;
//		while (true){
//			if(gui.isPortSelect() && !getAp){
//				try {
//					NRBoot b = new NRBoot(new ConnectionDialog.promptConnection());
//					gui.setBlApp(b);
//					getAp=true;
//				}catch(Exception e) {
//					gui.resetPort();
//				}
//			}
//			if(wasSelected && !gui.isPortSelect()){
//				getAp = false;
//			}
//			wasSelected =gui.isPortSelect();
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {}
//		}
//		
//	}
	
	private static void fail() {
		System.err.println("Paramaters are:\n(Can be more then one core)\n--core <num> <type> <path to hex>\n--xml <path to xml>");
		System.exit(1);	
	}

}
