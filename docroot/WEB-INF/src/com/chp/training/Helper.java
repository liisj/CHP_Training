package com.chp.training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public final class Helper {
	

	static public String fullTextFromInputStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String ret = "";
        String oneline;
        while ((oneline=reader.readLine())!=null)
        	ret += oneline + "\r\n";
        reader.close();
        return ret;
	}
	

	public static String join(Iterable<? extends CharSequence> s, String delimiter) {
	    Iterator<? extends CharSequence> iter = s.iterator();
	    if (!iter.hasNext()) return "";
	    StringBuilder buffer = new StringBuilder(iter.next());
	    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
	    return buffer.toString();
	}


	static public String niceJsonPrint(Object thing, String offset) {
		if (thing == null)
			return "null";
		if (thing instanceof JSONObject) {
			JSONObject jsonthing = (JSONObject) thing;
			@SuppressWarnings("rawtypes")
			Set keys = jsonthing.keySet();
			String key;
			String ret = "";
			if (!offset.equals(""))
				ret += "\n";
			for (Object keyO : keys) {
				key = keyO.toString();
				ret += offset + key + ": ";
				ret += niceJsonPrint(jsonthing.get(key), offset + "\t") + "\n";
			}
			return ret;
		}
		if (thing instanceof JSONArray) {
			JSONArray jsonthing = (JSONArray) thing;
			String ret = "";
			for (int index = 0 ; index < jsonthing.size() ; index++) {
				ret += niceJsonPrint(jsonthing.get(index), offset + "\t");
			}
			return ret;
		}
		return thing.toString();
	}
		
	
	
}
