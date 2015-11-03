package ua.naiksoftware.phprunner.editor.language;

import java.util.regex.Pattern;

public class PatternColorPair {

	private Pattern pattern;
	private int color;

	public PatternColorPair(Pattern p, int c) {
        pattern = p;
		color = c;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Pattern getPattern() {
		return pattern;
	}}
