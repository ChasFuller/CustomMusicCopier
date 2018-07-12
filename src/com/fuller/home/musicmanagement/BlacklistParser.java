package com.fuller.home.musicmanagement;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class BlacklistParser
{
	private static final String MAIN = "blacklist";
	private static final String FOLDERS = "artistFolders";
	private static final String ALBUMS = "albums";
	private static final String ARTIST_FOLDER = "artistFolder";
	private static final String ALBUM_FOLDER = "albumFolder";
	private static final String SONGS = "songs";
	private static final String SONG_NAME = "songName";
	
	public Blacklist parse(String blacklistFilePath)
	{
		Blacklist aBlacklist = new Blacklist();

		
		JSONParser parser = new JSONParser();
		
		try
		{		 
			JSONObject mainDocument = (JSONObject) parser.parse(new FileReader(blacklistFilePath));
 
            JSONObject blacklist = (JSONObject) mainDocument.get(MAIN);
            
            // Get the artist folders for the blacklist
			JSONArray foldersList = (JSONArray) blacklist.get(FOLDERS);
			aBlacklist.addFolders(convertJSONArrayToStringCollection(foldersList));
			
			// Get the albums for the blacklist
			JSONArray albumsList = (JSONArray) blacklist.get(ALBUMS);
			Iterator<JSONObject> albumsIterator = convertJSONArrayToJSONObjectCollection(albumsList).iterator();
			while (albumsIterator.hasNext())
			{
				JSONObject albumDetails = albumsIterator.next();
				String artistFolder = (String) albumDetails.get(ARTIST_FOLDER);	
				String albumFolder = (String) albumDetails.get(ALBUM_FOLDER);
				aBlacklist.addAlbum(artistFolder, albumFolder);
			}
			
			// Get the albums for the blacklist
			JSONArray songsList = (JSONArray) blacklist.get(SONGS);
			Iterator<JSONObject> songsIterator = convertJSONArrayToJSONObjectCollection(songsList).iterator();
			while (songsIterator.hasNext())
			{
				JSONObject songDetails = songsIterator.next();
				String artistFolder = (String) songDetails.get(ARTIST_FOLDER);
				String songName = (String) songDetails.get(SONG_NAME);
				aBlacklist.addSong(artistFolder, songName);
			}
 
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }
		
		return aBlacklist;
	}
	
	private Collection<String> convertJSONArrayToStringCollection(JSONArray input)
	{
		ArrayList<String> stringCollection = new ArrayList<String>();
		for (Object o : input)
		{
			String aString = (String) o;
			stringCollection.add(aString);
		}
		
		return stringCollection;
	}
	
	private Collection<JSONObject> convertJSONArrayToJSONObjectCollection(JSONArray input)
	{
		ArrayList<JSONObject> stringCollection = new ArrayList<JSONObject>();
		for (Object o : input)
		{
			JSONObject aJSONObject = (JSONObject) o;
			stringCollection.add(aJSONObject);
		}
		
		return stringCollection;
	}
}
