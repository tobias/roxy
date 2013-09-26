package org.tcrawley.roxy;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonArray;

import java.util.Iterator;
import java.util.Map;

public class Util {
    public static JsonArray serializeMultiMap(MultiMap map) {
        JsonArray ary = new JsonArray();
        for(Map.Entry<String, String> each: map.entries()) {
            JsonArray entry = new JsonArray();
            entry.addString(each.getKey());
            entry.addString(each.getValue());
            ary.add(entry);
        }

        return ary;
    }

    public static MultiMap deserializeMultiMap(JsonArray ary) {
        CaseInsensitiveMultiMap map = new CaseInsensitiveMultiMap();
        Iterator iterator = ary.iterator();
        while (iterator.hasNext()) {
            JsonArray entry = (JsonArray)iterator.next();
            map.add((String)entry.get(0), (String)entry.get(1));
        }

        return map;
    }
}

