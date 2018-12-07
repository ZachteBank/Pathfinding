package com.fontys.bramk.pathfinding;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {
    private BluetoothDevice device;
    private int strength;

    public MyBluetoothDevice() {
    }

    public MyBluetoothDevice(BluetoothDevice device, int strength) {
        this.device = device;
        this.strength = strength;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
}
