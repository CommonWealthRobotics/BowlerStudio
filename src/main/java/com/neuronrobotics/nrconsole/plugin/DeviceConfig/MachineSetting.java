package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

public class MachineSetting<V> {
	 
    private final String name;
    private V value;
 
    public MachineSetting(String _name,V v) {  
        name = _name;
        value = v;   
    }
    public MachineSetting(String _name) {  
        name = _name;
      
    }
    public String getName() {
        return name;
    }
 
    public V getValue() {
        return value;
    }
    public void setValue(V _value) {
        value = _value;
    }
 
    public String toString() { 
        return "(" + name + ", " + value + ")";  
    }
 
}
