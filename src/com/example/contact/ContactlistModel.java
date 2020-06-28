package com.example.contact;

public class  ContactlistModel{
	private String name;
	private String phoneNo;
	private boolean selected;

	public ContactlistModel(String name, String phoneNo) {
		this.name = name;
		this.phoneNo = phoneNo;
		selected = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
