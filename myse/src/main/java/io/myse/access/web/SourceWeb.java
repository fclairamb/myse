package io.myse.access.web;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.db.model.DBDescSource;
import java.util.List;
import java.util.Map;

/**
 * Web source. This source will allow to index standard website to more easily
 * add free content.
 */
public class SourceWeb extends Source {

	private String exploreUrlBase, exploreUrlFirst;
	private int exploreMaxDepth;

	public static final String PROP_EXPLORE_URL_BASE = "explore_url_base",
			PROP_EXPLORE_URL_FIRST = "explore_url",
			PROP_EXPLORE_MAX_DEPTH = "explore_max_depth";

	public SourceWeb(DBDescSource desc) {
		super(desc);
		Map<String, String> props = getDesc().getProperties();
		exploreMaxDepth = Integer.parseInt(props.get(PROP_EXPLORE_MAX_DEPTH));
	}

	@Override
	public File getRootDir() throws AccessException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public File getFile(String path) throws AccessException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<PropertyDescription> getProperties() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
