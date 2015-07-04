package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

public class DeviceSupportPlugginMap implements PluginFactory{
	
	private Class<?> device;
	private Class<?> plugin;
	private PluginFactory factory=null;

	public DeviceSupportPlugginMap(Class<?> device,Class<?> plugin){
		this.setDevice(device);
		this.setPlugin(plugin);
		
	}
	public DeviceSupportPlugginMap(Class<?> device,Class<?> plugin, PluginFactory factory){
		this.factory = factory;
		this.setDevice(device);
		this.setPlugin(plugin);
		
	}
	public Class<?> getDevice() {
		return device;
	}
	
	private void setDevice(Class<?> device) {
		if(device.isInstance(BowlerAbstractDevice.class) )
			this.device = device;
		else
			throw new RuntimeException("Devices must subclass BowlerAbstractDevice or NonBowlerDevice");
	}

	public Class<?> getPlugin() {
		return plugin;
	}

	private void setPlugin(Class<?> plugin) {
		if(plugin.isInstance(AbstractBowlerStudioTab.class) )
			this.plugin = plugin;
		else
			throw new RuntimeException("Plugins must subclass AbstractBowlerStudioTab");
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
