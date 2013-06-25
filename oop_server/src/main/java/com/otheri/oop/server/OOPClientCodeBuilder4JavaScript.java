package com.otheri.oop.server;

import java.io.File;

public class OOPClientCodeBuilder4JavaScript {
	
	private File folder;

	public OOPClientCodeBuilder4JavaScript(String filePath) {
		folder = new File(filePath);
		folder.mkdirs();
	}

	public void build(OOPClass clazz) {

	}
}
