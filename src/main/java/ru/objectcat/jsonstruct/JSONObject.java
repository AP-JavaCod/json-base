package ru.objectcat.jsonstruct;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import ru.objectcat.jsonstruct.parse.JSONFormat;
import ru.objectcat.jsonstruct.parse.JSONParser;

public class JSONObject extends HashMap<String, Object> implements Iterable<JSONObject.KeyValue> {
	
	public static JSONObject parse(String text) throws JSONParserException {
		return JSONParser.parseObject(text);
	}
	
	public Object add(KeyValue element) {
		return put(element.KEY, element.VALUE);
	}
	
	public Object getValue(String... path) {
		return getValue(0, path);
	}
	
	@Override
	public Object put(String key, Object value) {
		if(!JSONFormat.isValide(value)) {
			throw new IllegalArgumentException(value.getClass() + " is not a JSON type");
		}
		return super.put(key, value);
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends Object> map) {
		for(String key : map.keySet()) {
			put(key, map.get(key));
		}
	}
	
	@Override
	public String toString() {
		return JSONFormat.formatJSONObject(this);
	}
	
	@Override
	public Iterator<KeyValue> iterator(){
		return new JSONIterator(this.keySet().iterator());
	}
	
	private Object getValue(int index, String[] path) {
		Object value = get(path[index]);
		if(index == path.length - 1) {
			return value;
		}else if(value instanceof JSONObject j) {
			return j.getValue(index + 1, path);
		}else if(value instanceof JSONAdapter a) {
			return a.toJSON().getValue(index + 1, path);
		}
		return null;
	}
	
	public record KeyValue(String KEY, Object VALUE) {
		
		@Override
		public String toString() {
			String key = JSONFormat.formatJSONString(KEY);
			String value = JSONFormat.formatJSONValue(VALUE);
			return key + ":" + value;
		}
		
	};
	
	public static class JSONBuilder implements JSONAdapter{
		
		private final JSONObject JSON= new JSONObject();
		
		public JSONBuilder put(String key, Object value) {
			JSON.put(key, value);
			return this;
		}
		
		public JSONBuilder add(KeyValue element) {
			return put(element.KEY, element.VALUE);
		}
		
		@Override
		public JSONObject toJSON() {
			return JSON;
		}
		
	}
	
	private class JSONIterator implements Iterator<KeyValue>{
		
		private final Iterator<String> KEYS;
		
		private JSONIterator(Iterator<String> keys) {
			KEYS = keys;
		}
		
		@Override
		public boolean hasNext() {
			return KEYS.hasNext();
		}
		
		@Override
		public KeyValue next() {
			String key = KEYS.next();
			Object value = JSONObject.this.get(key);
			return new KeyValue(key, value);
		}
		
	}
	
}
