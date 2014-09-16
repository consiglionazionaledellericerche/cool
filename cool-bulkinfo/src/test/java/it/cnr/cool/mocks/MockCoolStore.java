package it.cnr.cool.mocks;
import it.cnr.cool.cmis.service.CoolStore;

public class MockCoolStore implements CoolStore {

	@Override
	public String[] getAllDocumentPaths() {
		return new String[0];
	}

}
