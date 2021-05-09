package com.mypractice;

public class UseDto {
	private String name;
	private String mobileNo;
	
	public UseDto(String name, String mobileNo) {
		super();
		this.name = name;
		this.mobileNo = mobileNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	
}
