package ru.objectcat.jsonstruct.parse;

import ru.objectcat.jsonstruct.JSONObject;
import ru.objectcat.jsonstruct.JSONArray;
import ru.objectcat.jsonstruct.JSONParserException;

public class JSONParser {
	
	public static JSONObject parseObject(String text) {
		String jsonStr = getBody(text, '{', '}');
		int size = jsonStr.length();
		JSONObject json = new JSONObject();
		for(int i = 0; i < size; i++) {
			char el = jsonStr.charAt(i);
			if(isIndent(el)) {
				continue;
			}
			String value = getKeyValue(i, jsonStr);
			i += value.length();
			json.add(parseKeyValue(value));
		}
		return json;
	}
	
	public static JSONArray parseArray(String text) {
		String jsonStr = getBody(text, '[', ']');
		int size = jsonStr.length();
		JSONArray json = new JSONArray();
		for(int i = 0; i < size; i++) {
			char el = jsonStr.charAt(i);
			if(isIndent(el)) {
				continue;
			}
			String value = getValue(i, jsonStr);
			i += value.length();
			json.add(parseValue(value));
		}
		return json;
	}
	
	public static String parseString(String value) {
		String result = "";
		for(int i = 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == '"') {
				break;
			}else if(el == '\\') {
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		return result;
	}
	
	public static Object parseOther(String value) {
		String pars = "";
		for(char el : value.toCharArray()) {
			if(isIndent(el)) {
				break;
			}
			pars += el;
		}
		if(pars.equals("true")) {
			return true;
		}else if(pars.equals("false")) {
			return false;
		}else if(pars.equals("null")) {
			return null;
		}
		int point = pars.indexOf('.');
		if(point == -1) {
			return Long.parseLong(pars);
		}
		return Double.parseDouble(pars);
	}
	
	private static Object parseValue(String value) {
		return switch(value.charAt(0)) {
		case '"' -> parseString(value);
		case '{' -> parseObject(value);
		case '[' -> parseArray(value);
		default -> parseOther(value);
		};
	}
	
	private static JSONObject.KeyValue parseKeyValue(String value){
		int size = value.length();
		String keyPars = null;
		int i = 0;
		boolean isKey = true;
		for(; i < size; i++) {
			char el = value.charAt(i);
			if(!isKey) {
				if(isIndent(el)) {
					continue;
				}
				break;
			}else if(isIndent(el)) {
				continue;
			}else if(el != '"') {
				throw new JSONParserException(i);
			}
			i++;
			keyPars = "\"";
			boolean isStr = true;
			for(; i < size; i++) {
				char v = value.charAt(i);
				if(!isStr && v == ':') {
					isKey = false;
					break;
				}else if(v == '"') {
					isStr = false;
				}
				keyPars += v;
			}
		}
		String valPars = value.substring(i);
		String key = parseString(keyPars);
		Object val = parseValue(valPars);
		return new JSONObject.KeyValue(key, val);
	}
	
	private static String getValueString(int index, String value) {
		String result = "\"";
		boolean isStr = true;
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(!isStr && (el == ',' || el == '}' || el == ']')) {
				break;
			}else if(el == '"') {
				isStr = false;
			}else if(el == '\\') {
				result += el;
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		return result;
	}
	
	private static String getValueObject(int index, String value) {
		String result = "{";
		boolean isObject = true;
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(!isObject && (el == ',' || el == '}' || el == ']')) {
				break;
			}else if(el == ',' || el == ']' || isIndent(el)) {
				result += el;
			}else if(el == '}') {
				result += el;
				isObject = false;
			}else {
				String v = getKeyValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		return result;
	}
	
	private static String getValueArray(int index, String value) {
		String result = "[";
		boolean isArray = true;
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(!isArray && (el == ',' || el == '}' || el == ']')) {
				break;
			}else if(el == ',' || el == '}' || isIndent(el)) {
				result += el;
			}else if(el == ']'){
				isArray = false;
				result += el;
			}else {
				String v = getValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		return result;
	}
	
	private static String getValueOther(int index, String value) {
		String result = "";
		for(int i = index; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == ',' || el == '}' || el == ']') {
				break;
			}
			result += el;
		}
		return result;
	}
	
	private static String getValue(int index, String value) {
		return switch(value.charAt(index)) {
		case '"' -> getValueString(index, value);
		case '{' -> getValueObject(index, value);
		case '[' -> getValueArray(index, value);
		default -> getValueOther(index, value);
		};
	}
	
	private static String getKeyValue(int index, String value) {
		String result = "\"";
		boolean isStr = true;
		boolean isValue = false;
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(!isStr && el == ':') {
				isValue = true;
			}else if(!isIndent(el) && isValue){
				result += getValue(i, value);
				break;
			}else if(el == '"') {
				isStr = false;
			}else if(el == '\\') {
				result += el;
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		return result;
	}
	
	private static String getBody(String text, char start, char end) {
		int startPos = positionStart(text, start);
		if(startPos == -1) {
			throw new JSONParserException("no initial submol '" + start + "'");
		}
		int endPos = positionEnd(text, end);
		if(endPos == -1) {
			throw new JSONParserException("no end submol '" + end + "'");
		}
		String value = text.substring(startPos + 1, endPos - 1);
		return value;
	}
	
	private static int positionStart(String text, char start) {
		int i = 0;
		for(char el : text.toCharArray()) {
			if(el == start) {
				return i;
			}else if(!isIndent(el)) {
				throw new JSONParserException(i);
			}
			i++;
		}
		return -1;
	}
	
	private static int positionEnd(String text, char end) {
		for(int i = text.length(); i > 0; i--) {
			char el = text.charAt(i - 1);
			if(el == end) {
				return i;
			}else if(!isIndent(el)) {
				throw new JSONParserException(i);
			}
		}
		return -1;
	}
	
	private static boolean isIndent(char element) {
		return element == ' ' || 
				element == '\t' || 
				element == '\n' ||
				element == '\r';
	}

}
