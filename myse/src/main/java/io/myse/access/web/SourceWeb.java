package io.myse.access.web;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescSource;
import io.myse.exploration.SourceExplorer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;

/**
 * Web source. This source will allow to index standard website to more easily
 * add free content.
 *
 * It's quite different from the other implementations because it relies on the
 * file properties to store the exploration context. Which is actually only the
 * exploration getDepth at this stage.
 *
 */
public class SourceWeb extends Source {

	public static final String TYPE = "web";

	public static final String PROP_EXPLORE_URL_BASE = "explore_url_base",
			PROP_EXPLORE_URL_FIRST = "explore_url",
			PROP_EXPLORE_MAX_DEPTH = "explore_max_depth";

	public SourceWeb(DBDescSource desc) {
		super(desc);
	}

	private Map<String, String> props() {
		return getDesc().getProperties();
	}

	private int exploreMaxDepth;

	public int getMaxDepth() {
		if (exploreMaxDepth == 0) {
			exploreMaxDepth = Integer.parseInt(props().get(PROP_EXPLORE_MAX_DEPTH));
		}
		return exploreMaxDepth;
	}

	private String baseUrl;

	public String getBaseUrl() {
		if (baseUrl == null) {
			baseUrl = props().get(PROP_EXPLORE_URL_BASE);
		}
		return baseUrl;
	}

	@Override
	public File getRootDir() throws AccessException {
		return new FileWeb(props().get(PROP_EXPLORE_URL_FIRST), this);
	}

	@Override
	public File getFile(String path) throws AccessException {
		return new FileWeb(path, this);
	}

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_EXPLORE_URL_FIRST, PropertyDescription.Type.TEXT, "First URL to explore", null, "https://www.elastic.co/"),
				new PropertyDescription(PROP_EXPLORE_URL_BASE, PropertyDescription.Type.TEXT, "Base URL", null, "https://www.elastic.co/"),
				new PropertyDescription(PROP_EXPLORE_MAX_DEPTH, PropertyDescription.Type.TEXT, "Max depth", "2", "2")
		));
		list.addAll(getSharedProperties());
		return list;
	}

	private EntityManager em;

	public EntityManager getEntityManager() {
		if (em == null) {
			em = DBMgmt.getEntityManager();
		}
		return em;
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Override
	public SourceExplorer getExplorer() {
		return new WebExplorer(getDesc().getId());
	}
}
