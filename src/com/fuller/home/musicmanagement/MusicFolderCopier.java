package com.fuller.home.musicmanagement;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class MusicFolderCopier
{
	private boolean isSimulation = true;
	private static final int CHECKPOINT_COUNT = 50;
	
	/* COPY LOGIC:
	 * 
	 * Loop over artist folders in directory
	 * 		If the artist is in the blacklist folders, skip the whole directory.
	 * 		If the artist is not in the blacklist folders:
	 * 			Loop over the loose songs in the artist folder, paying attention to the different file name format:
	 * 				If the song is listed in the blacklist, skip the song
	 * 				If the song is not listed in the blacklist:
	 * 					If we're using FLAC and a FLAC version of the file exists
	 * 						Add the FLAC version to the projection
	 * 					If we're not using FLAC or a FLAC version of the file doesn't exist
	 * 						Add the MP3 version to the projection
	 * 			Loop over the album directories for the artist
	 * 				If the album is listed in the blacklist, skip the album
	 * 				If the album is not listed in the blacklist:
	 * 					Loop over the songs in the album directory:
	 * 						If the song is listed in the blacklist, skip the song
	 * 						If the song is not listed in the blacklist:
	 * 							If we're using FLAC and a FLAC version of the file exists
	 * 								Add the FLAC version to the projection
	 * 							If we're not using FLAC or a FLAC version of the file doesn't exist
	 * 								Add the MP3 version to the projection
	 * 			Manage the leftover FLACTracks in the FLACStructure
	 * 				If we're using FLAC
	 * 					Loop over the loose songs left in the FLACStructure:
	 *						Add the FLACTrack to the projection
	 * 					Loop over the albums in the FLACStructure:
	 * 						Loop over the FLACTracks left for the album in the FLACStructure
	 * 							Add the FLACTrack to the projection
	 * 				If we're using MP3 add the FLACStructure to the projection as the songs to convert
	 * 			Use the projection to copy all the appropriate artist songs, MP3 and FLAC
	 * 			Manage FLAC to MP3 conversions
	 * 				Loop over the loose songs left in the FLACStructure:
	 *					Convert the FLACTrack to the destination (creating the artist folder if needed)
	 * 				Loop over the albums in the FLACStructure:
	 * 					Loop over the FLACTracks left for the album in the FLACStructure
	 * 						Convert the FLACTrack to the destination (creating the artist folder AND album folder if needed)		
	 * 			
	 */
	
	public void copy(String currentLocation, String newLocation, String blacklistPath, String converterPath, boolean useFLAC)
	{	
		// Default simulation value to true
		copy(currentLocation, newLocation, blacklistPath, useFLAC, converterPath, true);
	}
	
	public void copy(String currentLocation, String newLocation, String blacklistPath, boolean useFLAC, String converterPath, boolean simulate)
	{		
		isSimulation = simulate;
		int counter = 0;
		long timer = System.currentTimeMillis();
		
		if (currentLocation == null || currentLocation.isEmpty())
		{
			throw new IllegalArgumentException("Set root path before calling create");
		}

		Blacklist currentBlacklist = getCurrentBlacklist(blacklistPath);
		
		File[] files = getDirectoriesForFolder(currentLocation);
		Arrays.sort(files);

		if (files != null)
		{
			// Iterate over all artist folders
			for (File anArtistDirectory : files)
			{
				counter++;
				
				String artistDirectoryName = anArtistDirectory.getName();
				if (counter % CHECKPOINT_COUNT == 0)
				{
					long now = System.currentTimeMillis();
					long interval = now - timer;
					System.out.println("***** CHECKPOINT - Artist directory: " + artistDirectoryName + " (Elapsed time: " + interval/1000 +
							" seconds) *****");
				}
				
				if (currentBlacklist != null && currentBlacklist.containsFolder(artistDirectoryName))
				{
					// If the artist is in the blacklist folders, skip the whole directory.
					System.out.println("Skipping artist: " + artistDirectoryName);
				}
				else
				{
					FLACStructure artistFLACStructure = null;
		
					File FLACDirectory = getFLACDirectory(anArtistDirectory);
					if (FLACDirectory != null)
					{
						artistFLACStructure = buildFLACStructure(FLACDirectory);
					}
			
					MusicCopierState mcs = new MusicCopierState(anArtistDirectory, newLocation, currentBlacklist, artistFLACStructure, converterPath,
							useFLAC ? MusicFileOutputType.FLAC : MusicFileOutputType.MP3);
					manageCopyForArtist(mcs);				
				}
			}
		}
		
		long now = System.currentTimeMillis();
		long totalTime = now - timer;
		System.out.println("***************************************************************");
		System.out.println("TOTAL TIME: " + totalTime/1000 + " seconds");
	}
	
	private void manageCopyForArtist(MusicCopierState mcs)
	{
		// Because we need to handle FLAC->MP3 conversion we need to loop over each song individually to determine which (if any) files to copy
		// as well as any FLAC files that need to be converted to MP3s.
		
		// First, build an ArtistProjection representing the files we want to copy
		try
		{
			// Loop over the loose songs for the artist
			addArtistLooseSongsToProjection(mcs);
				
			// Now handle the songs in albums
			addArtistAlbumSongsToProjection(mcs);
			
			// If there's a FLAC directory we either need to add them (if we're using FLAC) or prepare them for conversion
			if (mcs.getArtistFLACStructure() != null)
			{
				addRemainingFLACSongsToProjection(mcs);
			}
		}
		catch (IOException projectionBuildException)
		{
			String artistDirectoryName = mcs.getArtistDirectory().getName();
			throw new RuntimeException("Building the copy set for " + artistDirectoryName + " blew up: ", projectionBuildException);
		}
		
		// If this isn't a simulation, do the actual copy and conversion as specified in the projection file
		if (!isSimulation)
		{
			doCopyForArtist(mcs.getArtistProjection());
			doFLACConversionsForArtist(mcs);
		}
	}
	
	private void addArtistLooseSongsToProjection(MusicCopierState mcs) throws IOException
	{
		// Manage the loose songs for an artist directory
		
		Blacklist currentBlacklist = mcs.getCurrentBlacklist();
		File anArtistDirectory = mcs.getArtistDirectory();
		String artistDirectoryName = anArtistDirectory.getName();
		
		File[] looseMp3Files = findMp3Files(anArtistDirectory);

		if (looseMp3Files != null)
		{
			for (File aLooseSong : looseMp3Files)
			{
				String looseSongName = aLooseSong.getName();
				String looseSongBaseName = FilenameUtils.getBaseName(looseSongName);
				
				if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, looseSongBaseName))
				{
					// If the song is listed in the blacklist, skip the song	
					System.out.println("Skipping song: " + looseSongBaseName);
				}
				else
				{
					// We're going to copy the track, need to determine if we should use the FLAC version or mp3
					boolean useMP3File = true;
					StringBuilder newCanonicalPathSB = new StringBuilder();
					newCanonicalPathSB.append(mcs.getDestinationPath());
					newCanonicalPathSB.append(File.separator);
					newCanonicalPathSB.append(artistDirectoryName);
					newCanonicalPathSB.append(File.separator);
					
					
					if (mcs.getOutputType().equals(MusicFileOutputType.FLAC) && mcs.getArtistFLACStructure() != null)
					{
						FLACTrack FLACVersion = mcs.getArtistFLACStructure().getLooseTrack(looseSongBaseName);
						if (FLACVersion != null)
						{
							// FLACVersion exists and we're preferring FLAC so copy the FLAC version
							useMP3File = false;
							newCanonicalPathSB.append(FLACVersion.getFilename());
							SongProjection flacDetails = new SongProjection(FLACVersion.getCanonicalPath(), newCanonicalPathSB.toString());
							mcs.getArtistProjection().addLooseTrack(flacDetails);
						}	
					}
					
					if (useMP3File)
					{
						// Add the MP3 file
						newCanonicalPathSB.append(looseSongName);	
						SongProjection mp3Details = new SongProjection(aLooseSong.getCanonicalPath(), newCanonicalPathSB.toString());
						mcs.getArtistProjection().addLooseTrack(mp3Details);					
					}
					
					
				}
				
				// No matter what (blacklisted, copied FLAC, copied MP3) we want the FLAC file removed from the structure so that all that is left
				// are FLAC files that we want that have no corresponding MP3 version
				if (mcs.getArtistFLACStructure() != null)
				{
					mcs.getArtistFLACStructure().removeLooseTrack(looseSongBaseName);
				}
			}
		}
	}
	
	private void addArtistAlbumSongsToProjection(MusicCopierState mcs) throws IOException
	{
		// Manage the songs in album directories for an artist
		
		File anArtistDirectory = mcs.getArtistDirectory();
		String artistDirectoryName = anArtistDirectory.getName();
		Blacklist currentBlacklist = mcs.getCurrentBlacklist();
		
		File[] albumDirectories = getDirectoriesForFolder(anArtistDirectory.getAbsolutePath());
		
		if (albumDirectories != null)
		{
			for (File anAlbumDirectory : albumDirectories)
			{
				String albumDirectoryName = anAlbumDirectory.getName();
				
				// Loop over the album directories for the artist
				if (currentBlacklist != null && currentBlacklist.containsAlbum(artistDirectoryName, albumDirectoryName))
				{
					// If the album is listed in the blacklist, skip the album
					System.out.println("Skipping album: " + artistDirectoryName + "/" + albumDirectoryName);
				}
				else
				{	
					// The album is not listed in the blacklist
					File[] albumMp3Files = findMp3Files(anAlbumDirectory);

					if (albumMp3Files != null)
					{
						for (File anAlbumSong : albumMp3Files)
						{
							String albumSongName = anAlbumSong.getName();
							String albumSongBaseName = FilenameUtils.getBaseName(albumSongName);
							
							// Loop over the songs in the album directory:
							if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, albumSongBaseName))
							{
								// If the song is listed in the blacklist, skip the song
								System.out.println("Skipping song: " + albumSongBaseName);
								
								
							}
							else
							{
								// If the song is not listed in the blacklist we're going to copy the track, need to determine
								// if we should use the FLAC version or mp3
								boolean useMP3File = true;
								StringBuilder newCanonicalPathSB = new StringBuilder();
								newCanonicalPathSB.append(mcs.getDestinationPath());
								newCanonicalPathSB.append(File.separator);
								newCanonicalPathSB.append(artistDirectoryName);
								newCanonicalPathSB.append(File.separator);
								newCanonicalPathSB.append(albumDirectoryName);
								newCanonicalPathSB.append(File.separator);
								
								
								if (mcs.getOutputType().equals(MusicFileOutputType.FLAC) && mcs.getArtistFLACStructure() != null)
								{
									FLACTrack FLACVersion = mcs.getArtistFLACStructure().getAlbumTrack(albumDirectoryName, albumSongBaseName);
									if (FLACVersion != null)
									{
										useMP3File = false;
										newCanonicalPathSB.append(FLACVersion.getFilename());
										SongProjection flacDetails = new SongProjection(FLACVersion.getCanonicalPath(), newCanonicalPathSB.toString());
										mcs.getArtistProjection().addAlbumTrack(albumDirectoryName, flacDetails);
									}	
								}
								
								if (useMP3File)
								{
									// No FLAC files, add the MP3
									newCanonicalPathSB.append(albumSongName);	
									SongProjection mp3Details = new SongProjection(anAlbumSong.getCanonicalPath(), newCanonicalPathSB.toString());
									mcs.getArtistProjection().addAlbumTrack(albumDirectoryName, mp3Details);
								}
							}

							// No matter what (blacklisted, copied FLAC, copied MP3) we want the FLAC file removed from the structure so that all that is left
							// are FLAC files that we want that have no corresponding MP3 version
							if (mcs.getArtistFLACStructure() != null)
							{
								mcs.getArtistFLACStructure().removeAlbumTrack(albumDirectoryName, albumSongBaseName);
							}
						}
					}
				}	
			}	
		}
	}
	
	private void addRemainingFLACSongsToProjection(MusicCopierState mcs)
	{
		// If we're using FLAC we'll add the remaining tracks to the projection file. If we're wanting MP3 we can just add the
		// FLACStructure to the projection and let the actual copy worry about it.
		
		if (mcs.getOutputType() == MusicFileOutputType.FLAC)
		{
			File anArtistDirectory = mcs.getArtistDirectory();
			String artistDirectoryName = anArtistDirectory.getName();
			Blacklist currentBlacklist = mcs.getCurrentBlacklist();
			StringBuilder newCanonicalPathSB = new StringBuilder();
			
			newCanonicalPathSB.append(mcs.getDestinationPath());
			newCanonicalPathSB.append(File.separator);
			newCanonicalPathSB.append(artistDirectoryName);
			newCanonicalPathSB.append(File.separator);
			String artistDirectoryPath = newCanonicalPathSB.toString();
			
			// First add any loose songs that aren't blacklisted
			FLACStructure FLACSongs = mcs.getArtistFLACStructure();
			for (FLACTrack FLACSong : FLACSongs.getAllLooseTracks().values())
			{
				String FLACSongBaseName = FLACSong.getBaseFilename();
				if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, FLACSongBaseName))
				{
					// If the song is listed in the blacklist, skip the song	
					System.out.println("Skipping song: " + FLACSongBaseName);
				}
				else
				{
					String newPath = artistDirectoryPath + FLACSong.getFilename();
					SongProjection FLACDetails = new SongProjection(FLACSong.getCanonicalPath(), newPath);
					mcs.getArtistProjection().addLooseTrack(FLACDetails);
				}
			}
		
			// Now add album songs that aren't blacklisted
			Hashtable<String, Hashtable<String, FLACTrack>> FLACAlbums = FLACSongs.getAllAlbumTracks();
			for (String FLACAlbumName : FLACAlbums.keySet())
			{
				Hashtable<String, FLACTrack> FLACAlbum = FLACAlbums.get(FLACAlbumName);
				
				for (String FLACSongName : FLACAlbum.keySet())
				{
					FLACTrack FLACSong = FLACAlbum.get(FLACSongName);
					String FLACSongBaseName = FLACSong.getBaseFilename();
					if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, FLACSongBaseName))
					{
						// If the song is listed in the blacklist, skip the song
						System.out.println("Skipping song: " + FLACSongBaseName);
					}
					else
					{
						String newPath = artistDirectoryPath + FLACAlbumName + File.separator + FLACSong.getFilename();
						SongProjection FLACDetails = new SongProjection(FLACSong.getCanonicalPath(), newPath);
						mcs.getArtistProjection().addAlbumTrack(FLACAlbumName, FLACDetails);
					}
				}
			}
		}
		else
		{
			mcs.getArtistProjection().setFLACConversionStructure(mcs.getArtistFLACStructure());
		}
	}
	
	private void doCopyForArtist(ArtistProjection currentArtistProjection)
	{
		// Now, if this isn't a simulation, do the actual copy for each file in the ArtistProjection
		try
		{	
			// First copy the loose songs for the artist
			for (SongProjection aSongToCopy : currentArtistProjection.getLooseTracks())
			{
				File sourceFile = new File(aSongToCopy.getCurrentCanonicalPath());
				File destinationFile = new File(aSongToCopy.getProjectedCanonicalPath());
				FileUtils.copyFile(sourceFile, destinationFile);
			}
			
			// Now copy the album songs for the artist
			// Loop over all the albums
			for (ArrayList<SongProjection> aListOfAlbumSongProjections: currentArtistProjection.getAlbums().values())
			{
				// Loop over each song on the album
				for (SongProjection anAlbumSongToCopy : aListOfAlbumSongProjections)
				{
					File sourceFile = new File(anAlbumSongToCopy.getCurrentCanonicalPath());
					File destinationFile = new File(anAlbumSongToCopy.getProjectedCanonicalPath());
					FileUtils.copyFile(sourceFile, destinationFile);
				}
			}		
		}
		catch (IOException copyException)
		{
			String artistName = currentArtistProjection.getArtist();
			throw new RuntimeException("Copying the files for " + artistName + " blew up: ", copyException);
		}
	}
	
	private void doFLACConversionsForArtist(MusicCopierState mcs)
	{
		// Now manage any FLAC to MP3 conversions that are needed	
		String artistDirectoryName = mcs.getArtistDirectory().getName();
		ArtistProjection currentArtistProjection = mcs.getArtistProjection();
		FLACStructure FLACSongsToConvert = currentArtistProjection.getFLACConversionStructure();
		Blacklist currentBlacklist = mcs.getCurrentBlacklist();
		MusicFileTypeConverter converter = new MusicFileTypeConverter(mcs.getConverterPath());
		
		// Build up our output path for the artist
		StringBuilder newCanonicalPathSB = new StringBuilder();
		newCanonicalPathSB.append(mcs.getDestinationPath());
		newCanonicalPathSB.append(File.separator);
		newCanonicalPathSB.append(artistDirectoryName);
		newCanonicalPathSB.append(File.separator);
		String artistDirectoryPath = newCanonicalPathSB.toString();
		
		// First convert loose songs
		for (FLACTrack FLACSong : FLACSongsToConvert.getAllLooseTracks().values())
		{
			String FLACSongBaseName = FLACSong.getBaseFilename();
			if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, FLACSongBaseName))
			{
				// If the song is listed in the blacklist, skip the song	
				System.out.println("Skipping song: " + FLACSongBaseName);
			}
			else
			{
				// Make sure the artist directory exists
				File artistDir = new File(artistDirectoryPath);
				if (!artistDir.exists())
				{
					try
					{
						Files.createDirectory(artistDir.toPath());
					}
					catch (IOException e)
					{
						throw new RuntimeException("Could not create artist directory " + artistDirectoryPath, e);
					}
				}
				String input = FLACSong.getCanonicalPath();
				String output = artistDirectoryPath + FLACSong.getBaseFilename() + ".mp3";
				System.out.println("Converting " + input + " to " + output);
				converter.convertFLACToMP3(input, output);
			}
		}
	
		// Now convert album songs
		Hashtable<String, Hashtable<String, FLACTrack>> FLACAlbumsToConvert = FLACSongsToConvert.getAllAlbumTracks();
		for (String FLACAlbumName : FLACAlbumsToConvert.keySet())
		{
			Hashtable<String, FLACTrack> FLACAlbum = FLACAlbumsToConvert.get(FLACAlbumName);
			
			for (String FLACSongName : FLACAlbum.keySet())
			{		
				FLACTrack FLACSong = FLACAlbum.get(FLACSongName);
				String FLACSongBaseName = FLACSong.getBaseFilename();
				if (currentBlacklist != null && currentBlacklist.containsSong(artistDirectoryName, FLACSongBaseName))
				{
					// If the song is listed in the blacklist, skip the song
					System.out.println("Skipping song: " + FLACSongBaseName);
				}
				else
				{
					// Make sure the artist directory exists
					File artistDir = new File(artistDirectoryPath);
					if (!artistDir.exists())
					{
						try
						{
							Files.createDirectory(artistDir.toPath());
						}
						catch (IOException e)
						{
							throw new RuntimeException("Could not create artist directory " + artistDirectoryPath, e);
						}
					}
					
					// Now make sure the album directory exists
					File albumDir = new File(artistDirectoryPath + FLACAlbumName);
					if (!albumDir.exists())
					{
						try
						{
							Files.createDirectory(albumDir.toPath());
						}
						catch (IOException e)
						{
							throw new RuntimeException("Could not create album directory " + artistDirectoryPath + FLACAlbumName, e);
						}
					}
					
					String input = FLACSong.getCanonicalPath();
					String output = artistDirectoryPath + FLACAlbumName + File.separator + FLACSong.getBaseFilename() + ".mp3";
					System.out.println("Converting " + input + " to " + output);
					converter.convertFLACToMP3(input, output);
				}
			}
		}
	}
	
	private Blacklist getCurrentBlacklist(String blacklistPath)
	{
		if (blacklistPath != null)
		{
			if (!new File(blacklistPath).exists())
			{
				// Handle the case where the file doesn't exist
				System.err.println("Blacklist file " + blacklistPath
						+ " doesn't exist, running without blacklist file!");;
				return null;
			}
			else
			{
				BlacklistParser myParser = new BlacklistParser();
				return myParser.parse(blacklistPath);
			}
		}
		else
		{
			return null;
		}
	}

	private File[] getDirectoriesForFolder(String folderPath)
	{
		File rootDir = new File(folderPath);

		File[] files = rootDir.listFiles(new FileFilter() {
			public boolean accept(File file)
			{
				return file.isDirectory();
			}
		});
		
		return files;
	}
	
	private File[] findMp3Files(File currentDirectory)
	{
		return currentDirectory.listFiles(new FileFilter() {
			public boolean accept(File file)
			{
				return file.getName().toLowerCase().endsWith(".mp3");
			}
		});
	}
	
	private File getFLACDirectory(File currentDirectory)
	{
		File[] FLACDirectories = currentDirectory.listFiles(new FileFilter() {
			public boolean accept(File file)
			{
				return (file.isDirectory() && file.getName().toLowerCase().equals("flac"));
			}
		});
		
		if (FLACDirectories.length == 1)
		{
			return FLACDirectories[0];
		}
		else if (FLACDirectories.length == 0)
		{
			return null;
		}
		else
		{
			String canonicalPath = "";
			try
			{
			canonicalPath = currentDirectory.getCanonicalPath();
			}
			catch (IOException e)
			{
				// Shit's broke, just throw runtime exception
			}
			throw new RuntimeException("Found multiple FLAC directories in directory " + canonicalPath);
		}
	}
	
	/**
	 * Builds a FLAC Structure for all the FLAC files in an artist directory
	 * 
	 * @param artistFlacDirectory The artist FLAC directory to look for FLAC files for the structure
	 * @return A FlacStructure object
	 */
	private FLACStructure buildFLACStructure(File artistFlacDirectory)
	{
		FLACStructure FLACFiles = new FLACStructure();
		
		// First load in all the loose FLAC files
		File[] looseFLACFiles = findFLACFiles(artistFlacDirectory);
		for (File aFLACFile : looseFLACFiles)
		{
			try
			{
				
				FLACTrack newFLACTrack = new FLACTrack(aFLACFile.getCanonicalPath(), aFLACFile.getName(), FilenameUtils.getBaseName(aFLACFile.getName()));
				FLACFiles.addLooseFLACTrack(newFLACTrack);
			}
			catch (IOException e)
			{
				throw new RuntimeException("Getting the canonical path for FLAC file " + aFLACFile.getName() + " went into the shitter.");
			}
		}
		
		// Now iterate over the albums in the directory
		File[] FLACAlbumDirectories = getDirectoriesForFolder(artistFlacDirectory.getAbsolutePath());
		for (File aFLACAlbumDirectory : FLACAlbumDirectories)
		{
			File[] FLACTracksForTheAlbum = findFLACFiles(aFLACAlbumDirectory);
			for (File aFLACAlbumFile : FLACTracksForTheAlbum)
			{
				try
				{
					FLACTrack newFLACTrack = new FLACTrack(aFLACAlbumFile.getCanonicalPath(), aFLACAlbumFile.getName(),
							FilenameUtils.getBaseName(aFLACAlbumFile.getName()));
					FLACFiles.addFLACTrackToAlbum(aFLACAlbumDirectory.getName(), newFLACTrack);
				}
				catch (IOException e)
				{
					throw new RuntimeException("Getting the canonical path for album FLAC file " + aFLACAlbumFile.getName() + " went into the shitter.");
				}
			}
		}
		
		return FLACFiles;
	}
	
	/**
	 * Convenience method to return all files that end with .flac in a specified directory.
	 * 
	 * @param currentDirectory The directory to look for .flac files
	 * @return A File array of files that end with .flac
	 */
	private File[] findFLACFiles(File currentDirectory)
	{
		return currentDirectory.listFiles(new FileFilter() {
			public boolean accept(File file)
			{
				return file.getName().toLowerCase().endsWith(".flac");
			}
		});
	}
}
