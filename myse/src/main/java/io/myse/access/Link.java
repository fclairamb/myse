package io.myse.access;

public class Link {

	public String address;
	public LinkType type;

	public enum LinkType {

		DIRECT,
		COPY_PASTE
	}
	
	public Link() {
		
	}

	public Link(String url) {
		this.address = url;
		this.type = LinkType.DIRECT;
	}
}
