package ru.objectcat.jsonstruct;

public class JSONParserException extends Exception{
	
	public JSONParserException(String message) {
		super(message);
	}
	
	public JSONParserException(int index) {
		super("index: " + index + " syntax error JSON");
	}

}
