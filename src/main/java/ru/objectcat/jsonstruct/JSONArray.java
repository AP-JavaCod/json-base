package ru.objectcat.jsonstruct;

import java.util.ArrayList;
import java.util.Collection;
import ru.objectcat.jsonstruct.parse.JSONFormat;
import ru.objectcat.jsonstruct.parse.JSONParser;

public class JSONArray extends ArrayList<Object> {

	
	public static JSONArray instalArray(Object... elements) {
		JSONArray array = new JSONArray();
		for(Object el : elements) {
			array.add(el);
		}
		return array;
	}
	
	public static JSONArray prse(String text) {
		return JSONParser.parseArray(text);
	}
	
	@Override
	public Object set(int index, Object element) {
		if(!JSONFormat.isValide(element)) {
			throw new IllegalArgumentException(element.getClass() +" is not a JSON type");
		}
		return super.set(index, element);
	}
	
	@Override
	public boolean add(Object element) {
		if(!JSONFormat.isValide(element)) {
			throw new IllegalArgumentException(element.getClass() + " is not a JSON type");
		}
		return super.add(element);
	}
	
	@Override
	public void add(int index, Object element) {
		if(!JSONFormat.isValide(element)) {
			throw new IllegalArgumentException(element.getClass() + " is not a JSON type");
		}
		super.add(index, element);
	}
	
	@Override
	public boolean addAll(Collection<? extends Object> collection) {
		collection.forEach(v -> add(v));
		return true;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends Object> collection) {
		int i = index;
		for(Object el : collection) {
			add(i, el);
			i++;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return JSONFormat.formatJSONArray(this);
	}
	
}
