package ru.objectcat.jsonstruct.parse;

import ru.objectcat.jsonstruct.JSONObject;
import ru.objectcat.jsonstruct.JSONArray;
import ru.objectcat.jsonstruct.JSONParserException;

public class JSONParser {
	
	public static JSONObject parseObject(String text) throws JSONParserException{
		int index = positionStart(0, text, '{');
		if(index == -1) {
			throw new JSONParserException("no initial submol {");
		}
		JSONObject json = new JSONObject();
		while(index < text.length() && text.charAt(index) != '}') {
			index++;
			int v = positionElement(index, text);
			if(v == -1) {
				throw new JSONParserException("no end sumbol }");
			}
			index = v;
			char el = text.charAt(index);
			if(el == '}') {
				break;
			}
			String value = getKeyValue(index, text);
			index += value.length();
			json.add(parseKeyValue(value));
		}
		return json;
	}
	
	public static JSONArray parseArray(String text) throws JSONParserException {
		int index = positionStart(0, text, '[');
		if(index == -1) {
			throw new JSONParserException("no initial sumbol [");
		}
		JSONArray json = new JSONArray();
		while(index < text.length() && text.charAt(index) != ']') {
			index++;
			index = positionElement(index, text);
			if(index == -1) {
				throw new JSONParserException("no end sumbol ]");
			}
			String value = getValue(index, text);
			index += value.length();
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
	
	private static Object parseValue(String value) throws JSONParserException{
		return switch(value.charAt(0)) {
		case '"' -> parseString(value);
		case '{' -> parseObject(value);
		case '[' -> parseArray(value);
		default -> parseOther(value);
		};
	}
	
	private static JSONObject.KeyValue parseKeyValue(String text) throws JSONParserException{
		int size = text.length();
		String keyPars = null;
		int i = 0;
		boolean isKey = true;
		for(; i < size; i++) {
			char el = text.charAt(i);
			if(!isKey) {
				if(el == ':') {
					break;
				}else if(isIndent(el)) {
					continue;
				}
				throw new JSONParserException(i);
			}else if(isIndent(el)) {
				continue;
			}else if(el != '"') {
				throw new JSONParserException(i);
			}
			i++;
			keyPars = "\"";
			for(; i < size; i++) {
				char v = text.charAt(i);
				if(v == '"') {
					isKey = false;
					break;
				}else if(v == '\\') {
					i++;
					v = text.charAt(i);
				}
				keyPars += v;
			}
		}
		i = positionElement(i + 1, text);
		String valPars = text.substring(i);
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
	
	private static int positionStart(int index, String text, char start) throws JSONParserException {
		for(int i = index; i < text.length(); i++) {
			char el = text.charAt(i);
			if(el == start) {
				return i;
			}else if(!isIndent(el)) {
				throw new JSONParserException(i);
			}
			i++;
		}
		return -1;
	}
	
	private static int positionElement(int index, String text) {
		for(int i = index; i < text.length(); i++) {
			char el = text.charAt(i);
			if(!isIndent(el)) {
				return i;
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
