package com.fuller.home.musicmanagement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Blacklist
{
	private HashSet <String> artistFolders;
	private HashMap <String, HashSet<String>> songs;
	private HashMap <String, HashSet<String>> albums;
	
	public Blacklist()
	{
		artistFolders = new HashSet<String>();
		songs = new HashMap <String, HashSet<String>>();
		albums = new HashMap <String, HashSet<String>>();
	}
	
	public void addFolder(String folderName)
	{
		artistFolders.add(folderName);
	}
	
	public void addFolders(Collection<String> folderNames)
	{
		artistFolders.addAll(folderNames);
	}
	
	public void addSong(String artistFolder, String songName)
	{
		if (!songs.containsKey(artistFolder))
		{
			songs.put(artistFolder, new HashSet<String>());
		}
		
		songs.get(artistFolder).add(songName);
	}
	
	public void addAlbum(String artistFolder, String albumFolder)
	{
		if (!albums.containsKey(artistFolder))
		{
			albums.put(artistFolder, new HashSet<String>());
		}
		
		albums.get(artistFolder).add(albumFolder);
	}
	
	public boolean containsFolder(String folderName)
	{
		return artistFolders.contains(folderName);
	}
	
	public boolean containsSongsForArtist(String artistFolder)
	{
		return songs.containsKey(artistFolder);
	}
	
	public boolean containsAlbumsForArtist(String artistFolder)
	{
		return albums.containsKey(artistFolder);
	}
	
	public boolean containsSong(String artistFolder, String songName)
	{
		HashSet<String> artistSongs = songs.get(artistFolder);
		
		if (artistSongs != null)
		{
			return artistSongs.contains(songName);
		}
		else
		{
			return false;
		}
	}
	
	public boolean containsAlbum(String artistFolder, String albumFolder)
	{		
		HashSet<String> blacklistedAlbumsForArtist = albums.get(artistFolder);
		
		if (blacklistedAlbumsForArtist != null)
		{
			return blacklistedAlbumsForArtist.contains(albumFolder);
		}
		else
		{
			return false;
		}
	}
}
