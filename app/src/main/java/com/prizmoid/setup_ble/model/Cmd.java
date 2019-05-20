package com.prizmoid.setup_ble.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Cmd {
    public String cmdName;
    private HashMap<String, Attr> attrList;
    public String fmt;

    public HashMap<String, Attr> getAttributes() {
        return attrList;
    }

    public void addAttr(Attr a) {
        if (attrList == null) {
            attrList = new HashMap<String, Attr>();
        }
        attrList.put(a.a_name, a);
    }

    public void addAttr(String a) {
        if (attrList == null) {
            attrList = new HashMap<String, Attr>();
        }
        attrList.put(a, new Attr(a));
    }

    public Attr getAttr(String attrName) {
        return attrList.get(attrName);
    }

    public Cmd(String cmd) {
        cmdName = cmd;
        attrList = null;
    }


}
