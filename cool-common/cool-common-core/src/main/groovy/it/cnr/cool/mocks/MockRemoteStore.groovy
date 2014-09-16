package it.cnr.cool.mocks;

import it.cnr.cool.extensions.webscripts.RemoteStore;

import java.io.IOException;
import java.io.InputStream;

public class MockRemoteStore implements RemoteStore {

	private static final String JSON = "rbac.get.json.ftl";

	@Override
	public void updateDocument(String documentPath, String string)
			throws IOException {
		System.out.println("UNIMPLEMENTED");

	}

	@Override
	public InputStream getDocument(String documentPath)
			throws IOException {
		return MockRemoteStore.class.getResourceAsStream("/" + JSON);
	}

}

