package ru.objectcat.jsonstruct.parse;

import ru.objectcat.jsonstruct.JSONObject;
import ru.objectcat.jsonstruct.JSONArray;
import ru.objectcat.jsonstruct.JSONAdapter;

public class JSONFormat {
	
	public static String formatJSONValue(Object value) {
		if(value instanceof String s) {
			return formatJSONString(s);
		}else if(value instanceof JSONObject j) {
			return formatJSONObject(j);
		}else if(value instanceof JSONArray a) {
			return formatJSONArray(a);
		}else if(value instanceof JSONAdapter ad) {
			return formatJSONObject(ad.toJSON());
		}else if(isValideOther(value)) {
			return String.valueOf(value);
		}
		throw new IllegalArgumentException(value.getClass() + " is not a JSON type");
	}
	
	public static String formatJSONString(String value) {
		String result = "";
		for(char el : value.toCharArray()) {
			switch(el) {
			case '\\':
			case '"':
				result += '\\';
			default:
				result += el;	
			}
		}
		return '"' + result + '"';
	}
	
	public static String formatJSONObject(JSONObject value) {
		String result = "";
		for(JSONObject.KeyValue el : value) {
			if(!result.equals("")) {
				result += ",";
			}
			result += el.toString();
		}
		return "{" + result + "}";
	}
	
	public static String formatJSONArray(JSONArray value) {
		String result = "";
		for(Object el : value) {
			String strValue = formatJSONValue(el);
			if(!result.equals("")) {
				result += ",";
			}
			result += strValue;
		}
		return '['  + result + ']';
	}
	
	public static boolean isValide(Object value) {
		return value instanceof String ||
				value instanceof JSONObject ||
				value instanceof JSONArray ||
				value instanceof JSONAdapter ||
				isValideOther(value);
	}
	
	private static boolean isValideOther(Object value) {
		return value == null ||
				value instanceof Byte ||
				value instanceof Short ||
				value instanceof Integer ||
				value instanceof Long ||
				value instanceof Float ||
				value instanceof Double ||
				value instanceof Boolean;
	}

}
