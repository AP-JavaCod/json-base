package ru.objectcat.jsonstruct;

public class JSONParserException extends Exception{
	
	public JSONParserException(String message) {
		super(message);
	}
	
	@Deprecated
	public JSONParserException(int index) {
		super("index: " + index + " syntax error JSON");
	}
	
	public JSONParserException(String message, int index) {
		super(message + " index: " + index + " syntax error JSON");
	}

}
