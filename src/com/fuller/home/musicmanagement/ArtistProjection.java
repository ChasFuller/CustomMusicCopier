package com.fuller.home.musicmanagement;

import java.util.ArrayList;
import java.util.Hashtable;

public class ArtistProjection
{
	private String artist;
	private ArrayList<SongProjection> looseTracks;
	private Hashtable<String, ArrayList<SongProjection>> albums;
	private FLACStructure FLACSongsToConvertToMP3;
	
	public ArtistProjection(String artistName)
	{
		artist = artistName;
		looseTracks = new ArrayList<SongProjection>();
		albums = new Hashtable<String, ArrayList<SongProjection>>();
		FLACSongsToConvertToMP3 = new FLACStructure();
	}

	public boolean addLooseTrack(SongProjection newLooseTrack)
	{
		return looseTracks.add(newLooseTrack);
	}
	
	public boolean addAlbumTrack(String albumFolderName, SongProjection newAlbumTrack)
	{
		if (!albums.containsKey(albumFolderName))
		{
			albums.put(albumFolderName, new ArrayList<SongProjection>());		
		}
		
		return albums.get(albumFolderName).add(newAlbumTrack);
	}

	public FLACStructure getFLACConversionStructure()
	{
		return FLACSongsToConvertToMP3;
	}
	
	public void setFLACConversionStructure(FLACStructure FLACConversionStructure)
	{
		FLACSongsToConvertToMP3 = FLACConversionStructure;
	}

	public String getArtist()
	{
		return artist;
	}

	public ArrayList<SongProjection> getLooseTracks()
	{
		return looseTracks;
	}

	public Hashtable<String, ArrayList<SongProjection>> getAlbums()
	{
		return albums;
	}
}
