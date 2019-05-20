package com.prizmoid.setup_ble.model;

public  class  Attr {
    public String a_name;
    public String a_value;
    int a_min = Integer.MIN_VALUE;
    int a_max = Integer.MAX_VALUE;

    Attr (String name) {
        a_name = name;
        a_value = "";
    }


    Attr (String name, String value) {
        a_name = name;
        a_value = value;
    }

    Attr (String name, String value, int min, int max) {
        a_name = name;
        a_value = value;
        a_min = min;
        a_max = max;
    }
}
