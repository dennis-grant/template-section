package com.dkg.dts;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.StringBuffer;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Parser {
	private String template;
	private Pattern attributePattern;
	private Pattern textBlockPattern1;
	private Pattern textBlockPattern2;
	private Pattern startDtSectionPattern;
	private Pattern endDtSectionPattern;

	public Parser() {
		this.attributePattern       = Pattern.compile("\\s*([a-zA-Z]*)\\s*=\\s*\"([^\">]*)\"", Pattern.DOTALL);
		this.textBlockPattern1      = Pattern.compile("(.*?)<\\s*/?\\s*dts\\s*", Pattern.DOTALL);
		this.textBlockPattern2      = Pattern.compile("(.*)$", Pattern.DOTALL);
		this.startDtSectionPattern  = Pattern.compile("\\G<\\s*dts\\s*([^/>]*)(/?)\\s*>", Pattern.DOTALL);
		this.endDtSectionPattern    = Pattern.compile("\\G<\\s*/\\s*dts\\s*>", Pattern.DOTALL);
	}

	public DtSection parseFile(String fileName) throws IOException {
		return parseReader(new FileReader(fileName));
	}

	public DtSection parseInputStream(InputStream in) throws IOException {
		return parseReader(new InputStreamReader(in));
	}

	public DtSection parseReader(Reader in) throws IOException {
		StringBuffer content;
		BufferedReader reader;
		String line;

		reader = new BufferedReader(in);
		content = new StringBuffer();
		line = reader.readLine();
		while (line != null) {
			content.append(line);
			content.append("\n");
			line = reader.readLine();
		}
		reader.close();

		return parseTemplate(content.toString());
	}

	public DtSection parseTemplate(String template) {
		DtSection mainSection;

		this.template = template;
		mainSection = new DtSection();
		parseSubParts(mainSection, 0);

		return mainSection;
	}

	private int parseSubParts(DtSection dtSection, int startPos) {
		int newStartPos;

		newStartPos = startPos;
		do {
			startPos = newStartPos;
			newStartPos = parseTextBlock(dtSection, startPos);
			newStartPos = parseDtSection(dtSection, newStartPos);
		} while (newStartPos > startPos);

		return newStartPos;
	}

	private int parseTextBlock(DtSection parentSection, int startPos) {
		TextBlock textBlock;
		Matcher textBlockMatcher;
		Matcher startDtSectionMatcher;
		Matcher endDtSectionMatcher;
		boolean textBlockFound;

		startDtSectionMatcher = startDtSectionPattern.matcher(this.template);
		endDtSectionMatcher = endDtSectionPattern.matcher(this.template);
		if (!startDtSectionMatcher.find(startPos) && !endDtSectionMatcher.find(startPos)) {
			textBlockFound = false;

			textBlockMatcher = textBlockPattern1.matcher(this.template);
			if (textBlockMatcher.find(startPos)) {
				textBlockFound = true;
			}
			else {
				textBlockMatcher = textBlockPattern2.matcher(this.template);
				if (textBlockMatcher.find(startPos)) {
					textBlockFound = true;
				}
			}

			if (textBlockFound == true) {
				textBlock = new TextBlock();
				textBlock.setValue(textBlockMatcher.group(1));
				startPos = textBlockMatcher.end(1);
				parentSection.addPart(textBlock);
			}
		}

		return startPos;
	}

	private int parseDtSection(DtSection parentSection, int startPos) {
		DtSection tmpSection;
		Matcher tmpMatcher;

		tmpMatcher = startDtSectionPattern.matcher(this.template);
		if (tmpMatcher.find(startPos)) {
			tmpSection = new DtSection();
			parseAttributes(tmpSection, tmpMatcher.group(1));

			// simple dtsection
			if (tmpMatcher.group(2).equals("/")) {
				parentSection.addPart(tmpSection);
				startPos = tmpMatcher.end(0);
			}
			// not simple dtsection
			else {
				startPos = parseSubParts(tmpSection, tmpMatcher.end(0));
				tmpMatcher = endDtSectionPattern.matcher(this.template);
				if (tmpMatcher.find(startPos)) {
					parentSection.addPart(tmpSection);
					startPos = tmpMatcher.end(0);
				}
			}
		}

		return startPos;
	}

	private void parseAttributes(DtSection section, String attributes) {
		Matcher tmpMatcher;
		String attributeName;

		tmpMatcher = attributePattern.matcher(attributes);
		while (tmpMatcher.find()) {
			attributeName = tmpMatcher.group(1);
			if (attributeName.equals("id"))
				section.setId(tmpMatcher.group(2));
			else if (attributeName.equals("preview"))
				section.setPreview(tmpMatcher.group(2));
			else if (attributeName.equals("optional"))
				section.setOptional(tmpMatcher.group(2));
		}
	}
}
