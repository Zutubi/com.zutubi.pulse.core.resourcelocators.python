package com.zutubi.pulse.core.resourcelocators.python;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;

public class PythonResourceLocator implements ResourceLocator
{
	private static final Logger LOG = Logger.getLogger(PythonResourceLocator.class);

	private static final String PATTERN_VERSION = "[0-9]+\\.[0-9]+";

	@Override
	public List<ResourceConfiguration> locate()
	{
		ResourceConfiguration resource = null;
		String defaultVersion = null;
		// TODO: add support for other OS's.
		if (SystemUtils.IS_MAC)
		{
			File versionsDir = new File("/System/Library/Frameworks/Python.framework/Versions");
			List<ResourceVersionConfiguration> versions = new LinkedList<ResourceVersionConfiguration>();
			for (File child: versionsDir.listFiles())
			{
				if (child.isDirectory() && child.getName().matches(PATTERN_VERSION))
				{
					try
					{
						versions.add(createVersion(child));
					}
					catch (IOException e)
					{
						// Give up on this version.
						LOG.warning(e);
					}
				}
				else if (child.getName().equals("Current"))
				{
					try
					{
						File canonical = child.getCanonicalFile();
						if (canonical.getName().matches(PATTERN_VERSION))
						{
							defaultVersion = canonical.getName();
						}
					}
					catch (IOException e)
					{
						// Ignore.
					}
				}
			}
			
			if (!versions.isEmpty())
			{
				resource = new ResourceConfiguration("python");
				
				// Just to be nice, sort by version.  All versions are major.minor, no two are the same.
				Collections.sort(versions, new Comparator<ResourceVersionConfiguration>()
				{
					@Override
					public int compare(ResourceVersionConfiguration v1, ResourceVersionConfiguration v2)
					{
						if (Float.parseFloat(v1.getValue()) > Float.parseFloat(v2.getValue()))
						{
							return 1;
						}
						else
						{
							return -1;
						}
					}
					
				});
				
				for (ResourceVersionConfiguration version: versions)
				{
					resource.addVersion(version);
				}
			}
		}

		if (resource != null)
		{
			if (defaultVersion != null && resource.hasVersion(defaultVersion))
			{
				resource.setDefaultVersion(defaultVersion);
			}
			
			return Arrays.asList(resource);
		}
		else
		{
			return Collections.emptyList();
		}
	}

	private ResourceVersionConfiguration createVersion(File dir) throws IOException
	{
		ResourceVersionConfiguration version = new ResourceVersionConfiguration(dir.getName());
		version.addProperty(new ResourcePropertyConfiguration("python.dir", dir.getCanonicalPath()));
		addSubdir(dir, "bin", version);
		File binary = new File(new File(dir, "bin"), "python");
		if (binary.isFile())
		{
			version.addProperty(new ResourcePropertyConfiguration("python.bin", binary.getCanonicalPath()));
		}
		
		addSubdir(dir, "include", version);
		addSubdir(dir, "lib", version);
		
		return version;
	}

	private void addSubdir(File dir, String name, ResourceVersionConfiguration version) throws IOException
	{
		File subDir = new File(dir, name);
		if (subDir.isDirectory())
		{
			version.addProperty(new ResourcePropertyConfiguration("python." + name + ".dir", subDir.getCanonicalPath()));
		}
	}
}
