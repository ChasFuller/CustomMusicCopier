package com.fuller.home.musicmanagement;

import java.io.File;

public class MusicCopierState
{
	private File artistDirectory;
	private String destinationPath;
	private Blacklist currentBlacklist;
	private FLACStructure artistFLACStructure;
	private ArtistProjection artistProjection;
	private MusicFileOutputType outputType;
	private String converterPath;
	
	public MusicCopierState(File anArtistDirectory, String aDestinationPath, Blacklist aBlacklist, FLACStructure aFLACStructure, String converterCanonicalPath,
			MusicFileOutputType desiredFormat)
	{
		artistDirectory = anArtistDirectory;
		destinationPath = aDestinationPath;
		currentBlacklist = aBlacklist;
		artistFLACStructure = aFLACStructure;
		artistProjection = new ArtistProjection(artistDirectory.getName());
		outputType = desiredFormat;
		converterPath = converterCanonicalPath;
	}

	public String getConverterPath()
	{
		return converterPath;
	}

	public ArtistProjection getArtistProjection()
	{
		return artistProjection;
	}

	public File getArtistDirectory()
	{
		return artistDirectory;
	}
	
	public String getDestinationPath()
	{
		return destinationPath;
	}

	public Blacklist getCurrentBlacklist()
	{
		return currentBlacklist;
	}
	
	public FLACStructure getArtistFLACStructure()
	{
		return artistFLACStructure;
	}

	public MusicFileOutputType getOutputType()
	{
		return outputType;
	} 
}
