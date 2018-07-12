package com.fuller.home.musicmanagement;

public class SongProjection
{
	private String currentCanonicalPath;
	private String projectedCanonicalPath;
	
	public SongProjection(String currentPath, String newPath)
	{
		currentCanonicalPath = currentPath;
		projectedCanonicalPath = newPath;
	}
	
	public String getCurrentCanonicalPath()
	{
		return currentCanonicalPath;
	}

	public String getProjectedCanonicalPath()
	{
		return projectedCanonicalPath;
	}
}
