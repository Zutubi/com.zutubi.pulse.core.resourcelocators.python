package com.zutubi.pulse.core.resourcelocators.python;


import java.io.File;
import java.util.List;

import org.junit.Test;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.SystemUtils;

public class PythonResourceLocatorTest extends PulseTestCase
{
	@Test
	public void testLocate()
	{
		PythonResourceLocator locator = new PythonResourceLocator();
		if (SystemUtils.IS_MAC)
		{
			// This test assumes a standard-ish Python setup.  It could be more robust by using
			// a fake filesystem.
			List<ResourceConfiguration> resources = locator.locate();
			assertEquals(1, resources.size());
			ResourceConfiguration resource = resources.get(0);
			assertEquals("python", resource.getName());
			assertNotNull(resource.getDefaultVersion());
			File versionsDir = new File("/System/Library/Frameworks/Python.framework/Versions");
			assertEquals(versionsDir.listFiles().length - 1, resource.getVersions().size());
			
			ResourceVersionConfiguration version = resource.getVersions().get(resource.getDefaultVersion());
			assertNotNull(version.getProperty("python.dir"));
			assertNotNull(version.getProperty("python.bin.dir"));
			assertNotNull(version.getProperty("python.bin"));
			assertNotNull(version.getProperty("python.include.dir"));
			assertNotNull(version.getProperty("python.lib.dir"));
		}
	}

}
