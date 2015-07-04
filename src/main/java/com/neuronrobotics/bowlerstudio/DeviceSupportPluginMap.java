package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.namespace.bcs.pid.IPidControlNamespace;

public class DeviceSupportPluginMap implements PluginFactory{
	
	private Class<?> device;
	private Class<?> plugin;
	private PluginFactory factory=null;
	

	DeviceSupportPluginMap(Class<?> device,Class<?> plugin){
		this.setDevice(device);
		this.setPlugin(plugin);
		
	}
	public DeviceSupportPluginMap(Class<?> device,Class<?> plugin, PluginFactory factory){
		this.factory = factory;
		this.setDevice(device);
		this.setPlugin(plugin);
		
	}
	
	public boolean isFactoryProvided(){
		return factory!=null;
	}
	
	public Class<?> getDevice() {
		return device;
	}
	
	private void setDevice(Class<?> device) {
		if(BowlerAbstractDevice.class.isAssignableFrom(device) || IPidControlNamespace.class.isAssignableFrom(device) )
			this.device = device;
		else
			throw new RuntimeException("Devices must subclass BowlerAbstractDevice or NonBowlerDevice");
	}

	public Class<?> getPlugin() {
		return plugin;
	}

	private void setPlugin(Class<?> plugin) {
		if(AbstractBowlerStudioTab.class.isAssignableFrom(plugin) )
			this.plugin = plugin;
		else
			throw new RuntimeException("Plugins must subclass AbstractBowlerStudioTab");
	}
	
	@Override
	public String toString(){
		return "Device: "+device.getCanonicalName()+" Plugin: "+plugin.getCanonicalName();
	}
	
	@Override
	public AbstractBowlerStudioTab generateNewPlugin() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if(factory!=null)
			return factory.generateNewPlugin(); 
		return (AbstractBowlerStudioTab) Class.forName(
				plugin.getName()
			).cast(plugin.newInstance()// This is where the new tab allocation is called
					)
			;
	}
}
