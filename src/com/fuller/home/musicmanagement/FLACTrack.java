package com.fuller.home.musicmanagement;

public class FLACTrack
{
	private String canonicalPath;
	private String filename;
	private String basename;

	public FLACTrack(String FLACFilepath, String FLACFilename, String FLACBaseFilename)
	{
		canonicalPath = FLACFilepath;
		filename = FLACFilename;
		basename = FLACBaseFilename;
	}
	
	public String getCanonicalPath()
	{
		return canonicalPath;
	}

	public String getFilename()
	{
		return filename;
	}
	
	public String getBaseFilename()
	{
		return basename;
	}
}
