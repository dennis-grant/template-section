package com.dkg.dts;

public class TextBlock implements TemplatePart {
	private String value;

	public TextBlock() {
		this.value = "";
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}
	
	public TextBlock cloneTextBlock() {
		TextBlock copy;

		copy = new TextBlock();
		copy.setValue(getValue());

		return copy;
	}
}
