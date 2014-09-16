package it.cnr.cool.service.typestree;

import java.util.List;

public class Type {

	private Attribute attr;
	private String data;	
	private List<Type> children;
	
	public Attribute getAttr() {
		return attr;
	}
	public void setAttr(Attribute attr) {
		this.attr = attr;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public List<Type> getChildren() {
		return children;
	}
	public void setChildren(List<Type> children) {
		this.children = children;
	}

}
