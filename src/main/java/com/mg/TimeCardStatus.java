package com.mg;

public enum TimeCardStatus {

    ON,

    OFF;

    public boolean isOn() {
        return this.equals(TimeCardStatus.ON);
    }

    public boolean isOff() {
        return this.equals(TimeCardStatus.OFF);
    }
}
