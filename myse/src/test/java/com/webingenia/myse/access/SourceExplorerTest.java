package com.webingenia.myse.access;

import com.webingenia.myse.exploration.SourceExplorer;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class SourceExplorerTest {

	@Test
	public void testWildCardRule() throws Exception {
		Pattern rule = SourceExplorer.compileWildcardRule("*.doc,*.docx,*.pdf");
		Assert.assertEquals("Regex pattern", ".*\\.doc|.*\\.docx|.*\\.pdf", rule.pattern());
		Assert.assertTrue("file.doc test", rule.matcher("file.doc").matches());
		Assert.assertTrue("file.dOc test", rule.matcher("file.dOc").matches());
		Assert.assertFalse("file.xls test", rule.matcher("file.xls").matches());
	}
}
