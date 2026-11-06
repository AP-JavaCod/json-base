package ru.objectcat.jsonstruct.parse;

import ru.objectcat.jsonstruct.JSONObject;
import ru.objectcat.jsonstruct.JSONArray;
import ru.objectcat.jsonstruct.JSONParserException;

public class JSONParser {
	
	public static JSONObject parseObject(String text) throws JSONParserException{
		JSONObject json = new JSONObject();
		if(parseJSON(text, '{', '}', JSONParser::getKeyValue, s -> json.add(parseKeyValue(s)))) {
			return json;
		}
		throw new JSONParserException("no end sumbol '}'");
	}
	
	public static JSONArray parseArray(String text) throws JSONParserException {
		JSONArray json = new JSONArray();
		if(parseJSON(text, '[', ']', JSONParser::getValue, s -> json.add(parseValue(s)))) {
			return json;
		}
		throw new JSONParserException("no end sumbol ']'");
	}
	
	public static String parseString(String value) throws JSONParserException {
		String result = "";
		for(int i = 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == '"') {
				return result;
			}else if(el == '\\') {
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		throw new JSONParserException("no end sumbol '\"'");
	}
	
	public static Object parseOther(String value) {
		if(value.equals("true")) {
			return true;
		}else if(value.equals("false")) {
			return false;
		}else if(value.equals("null")) {
			return null;
		}else if(value.indexOf('.') == -1) {
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
			throw new JSONParserException("There is no ket and value separator");
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
				return result + el;
			}else if(el == '\\') {
				result += el;
				i++;
				el = value.charAt(i);
			}
			result += el;
		}
		throw new JSONParserException("no end sumbel '\"'");
	}
	
	private static String getValueObject(int index, String value) throws JSONParserException{
		String result = "{";
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == '}') {
				return result + el;
			}else if(el == ',' || el == ']' || isIndent(el)) {
				result += el;
			}else {
				String v = getKeyValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		throw new JSONParserException("no end sumbel '}'");
	}
	
	private static String getValueArray(int index, String value) throws JSONParserException {
		String result = "[";
		for(int i = index + 1; i < value.length(); i++) {
			char el = value.charAt(i);
			if(el == ']') {
				return result + el;
			}else if(el == ',' || el == '}' || isIndent(el)) {
				result += el;
			}else {
				String v = getValue(i, value);
				i += v.length() - 1;
				result += v;
			}
		}
		throw new JSONParserException("no end sumbel ']'");
	}
	
	private static String getValueOther(int index, String value) throws JSONParserException {
		String result = "";
		for(int i = index; i < value.length(); i++) {
			char el = value.charAt(i);
			if(isIndent(el) || el == ',' || el == '}' || el == ']') {
				return result;
			}
			result += el;
		}
		throw new JSONParserException("not end json");
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
				return result + getValue(i, value);
			}
			result += el;
		}
		throw new JSONParserException("no end json");
	}
	
	private static int positionStart(String text, char start){
		int position = positionElement(0, text);
		return position != -1 && text.charAt(position) != start ?
			-1 : 
			position;
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
	
	private static int positionEnd(int index, String text, char end){
		int position = positionElement(index, text);
		return position != -1 && (text.charAt(position) != ',' && text.charAt(position) != end) ?
			-1:
			position;
	}
	
	private static boolean isIndent(char element) {
		return element == ' ' || 
				element == '\t' || 
				element == '\n' ||
				element == '\r';
	}
	
	private static boolean parseJSON(String text, char start, char end, ParseString pars, ParseValue val) throws JSONParserException {
		int index = positionStart(text, start);
		if(index == -1) {
			throw new JSONParserException("no initial sumbol '" + start + "'");
		}
		for(int i = index + 1; i < text.length(); i++) {
			i = positionElement(i, text);
			if(i == -1) {
				throw new JSONParserException("no end sumbol '" + end + "'");
			}else if(text.charAt(i) == end) {
				return true;
			}
			String value = pars.parse(i, text);
			val.parse(value);
			i = positionEnd(i + value.length(), text, end);
			if(i == -1) {
				throw new JSONParserException("no sumbol ',' or '" + end + "'");
			}else if(text.charAt(i) == end) {
				return true;
			}
		}
		return false;
	}
	
	@FunctionalInterface
	private interface ParseString{
		
		String parse(int index, String text) throws JSONParserException;
		
	}
	
	@FunctionalInterface
	private interface ParseValue{
		
		void parse(String text) throws JSONParserException;
		
	}
	
}
