package ru.objectcat.jsonstruct;

public class JSONParserException extends RuntimeException{
	
	public JSONParserException(String message) {
		super(message);
	}
	
	public JSONParserException(int index) {
		super("index: " + index + " syntax error JSON");
	}

}
