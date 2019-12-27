package com.dkg.dts;

import java.util.ArrayList;
import java.util.HashMap;

public class DtSection  implements TemplatePart {
	public ArrayList parts;
	private HashMap namedSections;
	private ArrayList dtSectionList;
	private StringBuffer value;
	private String id;
	private String preview;
	private String optional;

	public DtSection() {
		parts = new ArrayList();
		namedSections = new HashMap();
		dtSectionList = new ArrayList();
		value = null;
		id = "";
		preview = "no";
		optional = "no";
	}

	public void setId(String newId) {
		id = newId;
	}

	public String getId() {
		return id;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}
	
	public String getPreview() {
		return this.preview;
	}

	public boolean isPreview() {
		return (preview != null && "true>yes>1".indexOf(preview) >= 0);
	}
	
	public void setOptional(String optional) {
		this.optional = optional;
	}

	public String getOptional() {
		return this.optional;
	}

	public boolean isOptional() {
		return (optional != null && "true>yes>1".indexOf(optional) >= 0);
	}

	public void setValue(String newValue) {
		if (this.value == null)
			this.value = new StringBuffer();
		this.value.setLength(0);
		this.value.append(newValue);
	}

	public void appendValue(String newValue) {
		if (this.value == null)
			this.value = new StringBuffer();
		this.value.append(newValue);
	}

	public String getValue() {
		if (value == null)
			return evaluateValue();
		else
			return value.toString();
	}

	public String evaluateValue() {
		StringBuffer tmpValue;

		tmpValue = new StringBuffer();
		if (parts.size() == 0)
			tmpValue.append("dts:null");
		else {
			for (int i = 0; i < parts.size(); i++) {
				TemplatePart tmpPart = (TemplatePart)parts.get(i);
				tmpValue.append(tmpPart.getValue());
			}
		}

		return tmpValue.toString();
	}

	public void reset() {
		value = null;
		for (int i = 0; i < dtSectionList.size(); i++) {
			DtSection tmpSection = (DtSection)dtSectionList.get(i);
			tmpSection.reset();
		}
	}

	public TemplatePart addPart(TemplatePart part) {
		DtSection tmpSection;

		if (part instanceof DtSection) {
			tmpSection = (DtSection)part;
			if (!tmpSection.isPreview()) {
				parts.add(part);
				namedSections.put(tmpSection.getId(), tmpSection);
				dtSectionList.add(tmpSection);
			}
		}
		else {
			parts.add(part);
		}

		return part;
	}

	public DtSection getDtSection(String id) {
		return (DtSection)namedSections.get(id);
	}

	public int getDtSectionCount() {
		return dtSectionList.size();
	}

	public int getMandatoryDtSectionCount() {
		int count;
		DtSection tmpSection;
		
		count = 0;
		for (int i = 0; i < dtSectionList.size(); i++) {
			tmpSection = (DtSection)dtSectionList.get(i);
			if (!tmpSection.isOptional()) {
				count++;
			}
		}
		
		return count;
	}

	public DtSection getDtSection(int index) {
		if (index <= dtSectionList.size())
			return (DtSection)dtSectionList.get(index - 1);
		else
			return null;
	}

	public boolean canSubstituteFor(DtSection template) {
		boolean test;
		DtSection subTemplate1;
		DtSection subTemplate2;

		test = (template != null);
		test = (test && this.getMandatoryDtSectionCount() >= template.getMandatoryDtSectionCount());
		for (int index = 1; test == true && index <= template.getDtSectionCount(); index++) {
			subTemplate2 = template.getDtSection(index);
			subTemplate1 = this.getDtSection(subTemplate2.getId());
			test = (subTemplate1 != null || subTemplate2.isOptional());
			if (subTemplate1 != null) {
				test = test && subTemplate1.canSubstituteFor(subTemplate2);
			}
		}

		return test;
	}

	public DtSection cloneSection() {
		DtSection copy;
		TemplatePart tmpPart;
		DtSection tmpSection;
		TextBlock tmpTextBlock;

		copy = new DtSection();
		copy.setId(getId());
		copy.setPreview(getPreview());
		copy.setOptional(getOptional());
		if (value != null) {
			copy.setValue(getValue().toString());
		}
		for (int i = 0; i < parts.size(); i++) {
			tmpPart = (TemplatePart)parts.get(i);
			if (tmpPart instanceof DtSection) {
				tmpSection = (DtSection)tmpPart;
				copy.addPart(tmpSection.cloneSection());
			}
			else if (tmpPart instanceof TextBlock) {
				tmpTextBlock = (TextBlock)tmpPart;
				copy.addPart(tmpTextBlock.cloneTextBlock());
			}
		}

		return copy;
	}
}
