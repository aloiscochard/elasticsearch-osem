package org.elasticsearch.osem.core.impl;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.osem.pojo.complex.SimpleEntity;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TwitterMappingTestCase {

    private static ObjectContext context;
    private static String mapping;
    
    @BeforeMethod()
	public static void setUp() throws Exception {
        context = ObjectContextFactory.create();
        context.add(SimpleEntity.class);
		XContentBuilder xcb = context.getMapping(SimpleEntity.class);
		mapping = xcb.string();
	}

    /**
     * We should not have the ignorefield in mapping
     */
    @Test
	public void testIgnoreField() {
    	int pos = mapping.indexOf("ignorefield");
    	AssertJUnit.assertEquals(-1, pos);
	}

    /**
     * We should find an analyzer property
     */
    @Test
	public void testFrenchField() {
    	int pos = mapping.indexOf("analyzer");
    	AssertJUnit.assertTrue(pos >= 0);
	}
}
