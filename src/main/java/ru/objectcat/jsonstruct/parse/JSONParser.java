package ru.objectcat.jsonstruct.parse;

import ru.objectcat.jsonstruct.JSONObject;
import ru.objectcat.jsonstruct.JSONArray;
import ru.objectcat.jsonstruct.JSONParserException;

public class JSONParser {
	
	public static JSONObject parseObject(String text) throws JSONParserException{
		int index = positionStart(0, text, '{');
		if(index == -1) {
			throw new JSONParserException("no initial submol '{'");
		}
		JSONObject json = new JSONObject();
		while(index < text.length() && text.charAt(index) != '}') {
			index++;
			index = positionElement(index, text);
			if(index == -1) {
				throw new JSONParserException("no end sumbol '}'");
			}else if(text.charAt(index) == '}') {
				break;
			}
			String value = getKeyValue(index, text);
			index += value.length();
			json.add(parseKeyValue(value));
			index = positionEnd(index, text);
			if(index == -1) {
				throw new JSONParserException("no sumbol ',' or '}'");
			}
		}
		if(index == text.length()) {
			throw new JSONParserException("no end sumbol '}'");
		}
		return json;
	}
	
	public static JSONArray parseArray(String text) throws JSONParserException {
		int index = positionStart(0, text, '[');
		if(index == -1) {
			throw new JSONParserException("no initial sumbol '['");
		}
		JSONArray json = new JSONArray();
		while(index < text.length() && text.charAt(index) != ']') {
			index++;
			index = positionElement(index, text);
			if(index == -1) {
				throw new JSONParserException("no end sumbol ']'");
			}else if(text.charAt(index) == ']') {
				break;
			}
			String value = getValue(index, text);
			index += value.length();
			json.add(parseValue(value));
			index = positionEnd(index, text);
			if(index == -1) {
				throw new JSONParserException("no sumbol ',' or ']' ");
			}
		}
		if(index == text.length()) {
			throw new JSONParserException("no end sumbol ']'");
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
		if(value.equals("true")) {
			return true;
		}else if(value.equals("false")) {
			return false;
		}else if(value.equals("null")) {
			return null;
		}
		int point = value.indexOf('.');
		if(point == -1) {
			return Long.parseLong(value);
		}
		return Double.parseDouble(value);
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
		String keyPars = getValueString(0, text);
		int i = keyPars.length();
		for(; i < size; i++) {
			char el = text.charAt(i);
			if(el == ':') {
				break;	
			}else if(isIndent(el)) {
				continue;	
			}
			throw new JSONParserException(i);
		}
		i = positionElement(i + 1, text);
		String valPars = text.substring(i);
		String key = parseString(keyPars);
		Object val = parseValue(valPars);
		return new JSONObject.KeyValue(key, val);
	}
	
	private static String getValueString(int index, String value) throws JSONParserException {
		String result = "\"";
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == '"') {
				result += el;
				break;
			}else if(el == '\\') {
				result += el;
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		return result;
	}
	
	private static String getValueObject(int index, String value) throws JSONParserException{
		String result = "{";
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == '}') {
				result += el;
				break;
			}else if(el == ',' || el == ']' || isIndent(el)) {
				result += el;
			}else {
				String v = getKeyValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		return result;
	}
	
	private static String getValueArray(int index, String value) throws JSONParserException {
		String result = "[";
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == ']') {
				result += ']';
				break;
			}else if(el == ',' || el == '}' || isIndent(el)) {
				result += el;
			}else {
				String v = getValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		return result;
	}
	
	private static String getValueOther(int index, String value) throws JSONParserException {
		String result = "";
		for(int i = index; i < value.length(); i++) {
			char el = value.charAt(i);
			if(isIndent(el) || el == ',' || el == '}' || el == ']') {
				break;
			}
			result += el;
		}
		return result;
	}
	
	private static String getValue(int index, String value) throws JSONParserException {
		return switch(value.charAt(index)) {
		case '"' -> getValueString(index, value);
		case '{' -> getValueObject(index, value);
		case '[' -> getValueArray(index, value);
		default -> getValueOther(index, value);
		};
	}
	
	private static String getKeyValue(int index, String value) throws JSONParserException {
		String result = getValueString(index, value);
		boolean isValue = false;
		for(int i = index + result.length(); i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == ':') {
				isValue = true;
			}else if(!isIndent(el) && isValue){
				result += getValue(i, value);
				break;
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
	
	private static int positionEnd(int index, String text) throws JSONParserException{
		for(int i = index; i < text.length(); i++) {
			char el = text.charAt(i);
			if(el == ',' || el == '}' || el == ']' ) {
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
