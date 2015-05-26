package io.myse.access.web;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @source
 * http://www.mkyong.com/regular-expressions/how-to-extract-html-links-with-regular-expression/
 */
public class HTMLLinkExtrator {

	private final Pattern patternTag, patternLink;
	private Matcher matcherTag, matcherLink;
	private static final String HTML_A_TAG_PATTERN
			= "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN
			= "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

	public HTMLLinkExtrator() {
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	}

	/**
	 * Validate html with regular expression
	 *
	 * @param html html content for validation
	 * @return Vector links and link text
	 */
	public Set<String> grabHTMLLinks(final String html) {

//		List<String> result = new LinkedList<String>();
		Set<String> set = new HashSet<>();

		matcherTag = patternTag.matcher(html);

		while (matcherTag.find()) {

			String href = matcherTag.group(1); //href
//			String linkText = matcherTag.group(2); //link text

			matcherLink = patternLink.matcher(href);

			while (matcherLink.find()) {

				String link = matcherLink.group(1); //link
				if (link.startsWith("'") || link.startsWith("\"")) {
					link = link.substring(1, link.length() - 1);
				}
				
				link = link.replace("&amp;", "&");
				
				set.add(link);
			}

		}

		return set;
	}
}
