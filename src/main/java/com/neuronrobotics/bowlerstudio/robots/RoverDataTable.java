package com.neuronrobotics.bowlerstudio.robots;

import com.neuronrobotics.sdk.common.NonBowlerDevice;

import java.util.ArrayList;

public class RoverDataTable extends NonBowlerDevice {
    private ArrayList<ObjectDetectionDataTableElement> detections = new ArrayList<>();

    private long startOfApp = System.currentTimeMillis();
    private double hungry = 0;

    @Override
    public void disconnectDeviceImp() {
    }

    @Override
    public boolean connectDeviceImp() {
        return false;
    }

    @Override
    public ArrayList<String> getNamespacesImp() {
        return new ArrayList<>();
    }

    public ArrayList<ObjectDetectionDataTableElement> getDetections() {
        return detections;
    }

    public void addDetector(ObjectDetectionDataTableElement el) {
        if (!detections.contains(el))
            detections.add(el);
    }

    public ObjectDetectionDataTableElement getSpecificDetection(String name) {
        for (ObjectDetectionDataTableElement o : detections) {
            if (o.getName().contains(name)) {
                return o;
            }
        }
        return null;
    }

    public double getHungry() {
        return hungry;
    }

    public void setHungry(double hungry) {
        this.hungry = hungry;
    }

    public long getStartOfApp() {
        return startOfApp;
    }

    public void setStartOfApp(long startOfApp) {
        this.startOfApp = startOfApp;
    }

}
