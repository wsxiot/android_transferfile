package com.example.testtab;

public class ItemBean {

    public int itemImage;
    public String itemName;
    public String itemPath;
    public int type;
	public ItemBean(int itemImage, String itemName, String itemPath) {
		this.itemImage = itemImage;
		this.itemName = itemName;
		this.itemPath = itemPath;
	}
	public int getType() {
		return type;
	}
	public String getItemPath() {
		return itemPath;
	}
	


}