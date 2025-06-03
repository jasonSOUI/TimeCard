package com.mg.enums;

public enum TimeCardStatus {

    ON,

    OFF;

    public boolean isOn() {
        return this.equals(TimeCardStatus.ON);
    }

    public boolean isOff() {
        return this.equals(TimeCardStatus.OFF);
    }

    public static TimeCardStatus findByCode(String code) {
         for(TimeCardStatus t : TimeCardStatus.values()) {
             if(t.name().toUpperCase().equals(code.toUpperCase())) {
                 return t;
             }
         }
         return null;
    }
}
