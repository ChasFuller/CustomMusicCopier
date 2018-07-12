package com.fuller.home.musicmanagement;

import java.util.Hashtable;

import org.apache.commons.io.FilenameUtils;

public class FLACStructure
{
	private Hashtable <String, FLACTrack> looseSongs;
	private Hashtable<String, Hashtable <String, FLACTrack>> albums;

	public FLACStructure()
	{
		looseSongs = new Hashtable<String, FLACTrack>();
		albums = new Hashtable<String, Hashtable <String, FLACTrack>>();
	}
	
	public void addLooseFLACTrack(FLACTrack aTrack)
	{
		looseSongs.put(aTrack.getBaseFilename(), aTrack);
	}
	
	public void addFLACTrackToAlbum(String albumName, FLACTrack aTrack)
	{
		if (!albums.containsKey(albumName))
		{
			albums.put(albumName, new Hashtable <String, FLACTrack>());		
		}
		
		albums.get(albumName).put(aTrack.getBaseFilename(), aTrack);
	}
	
	public FLACTrack getLooseTrack(String trackname)
	{
		return looseSongs.get(FilenameUtils.getBaseName(trackname));
	}
	
	public FLACTrack getAlbumTrack(String albumDirectoryName, String trackname)
	{
		Hashtable<String, FLACTrack> albumSongs = albums.get(albumDirectoryName);
		
		if (albumSongs == null)
		{
			return null;
		}
		else
		{
			return albumSongs.get(FilenameUtils.getBaseName(trackname)); 
		}
	}
	
	public void removeLooseTrack(String trackname)
	{
		looseSongs.remove(FilenameUtils.getBaseName(trackname));
	}
	
	public void removeAlbumTrack(String albumDirectoryName, String trackname)
	{
		Hashtable<String, FLACTrack> albumSongs = albums.get(albumDirectoryName);
		
		if (albumSongs != null)
		{
			albumSongs.remove(FilenameUtils.getBaseName(trackname)); 
		}
	}
	
	public Hashtable<String, FLACTrack> getAllLooseTracks()
	{
		return looseSongs;
	}
	
	public Hashtable<String, Hashtable<String, FLACTrack>> getAllAlbumTracks()
	{
		return albums;
	}
}
