package com.fuller.home.musicmanagement;

import java.util.Scanner;

public class CustomMusicCopier
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String musicPath = null;;
		String copyPath = null;
		String blacklistPath = null;
		boolean useFLAC = false;
		boolean flattenDirectories = false;
		boolean simulate = false;
		String converterPath = null;
		
		boolean isReadyToRun = false;
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("CustomMusicCopier creates a copy of a music folder, optionally applying a specific blacklist to exclude artists and/or tracks");
			
		if (args.length == 7)
		{		
			musicPath = args[0];
			copyPath = args[1];
			
			blacklistPath = args[2];
			if (blacklistPath.toUpperCase().equals("NONE"))
			{
				blacklistPath = null;
			}

			if (args[3].toUpperCase().equals("YES"))
			{
				useFLAC = true;
			}

			converterPath = args[4];
			
			if (args[5].toUpperCase().equals("YES"))
			{
				flattenDirectories = true;
			}
			
			if (args[6].toUpperCase().equals("YES"))
			{
				simulate = true;
			}
			
			printCurrentState(musicPath, copyPath, blacklistPath, useFLAC, converterPath, flattenDirectories, simulate);
			isReadyToRun = true;
		}
		else
		{				
			while(!isReadyToRun)
			{
				System.out.println("Please type in the full path to the music directory:");
				musicPath = scanner.nextLine();
				System.out.println("Please type in the full path to where you'd like to copy the music folder:");
				copyPath = scanner.nextLine();
				System.out.println("Please type in the full path to the blacklist file you'd like to use during the copy or NONE if you "
						+ " don't want to apply a blacklist:");
				blacklistPath = scanner.nextLine();
				if (blacklistPath.toUpperCase().equals("NONE"))
				{
					blacklistPath = null;
				}
				System.out.println("Do you want FLAC versions of music files if available? (yes/no)");
				String useFLACString = scanner.nextLine();
				if (useFLACString.toUpperCase().equals("YES"))
				{
					useFLAC = true;
				}
				else
				{
					useFLAC = false;
					System.out.println("Please type in the full path to the file converter to use to convert FLAC files into MP3s:");
					converterPath = scanner.nextLine();
				}
				
				System.out.println("Do you want to flatten the album directories to just artist directories? (yes/no)");
				String flattenDirs = scanner.nextLine();
				if (flattenDirs.toUpperCase().equals("YES"))
				{
					flattenDirectories = true;
				}
				
				System.out.println("Do you want to simulate the copy without actually copying the files? (yes/no)");
				String doSimulation = scanner.nextLine();
				if (doSimulation.toUpperCase().equals("YES"))
				{
					simulate = true;
				}
				
				printCurrentState(musicPath, copyPath, blacklistPath, useFLAC, converterPath, flattenDirectories,
						simulate);
				
				System.out.println("Is this right? (yes/no/exit)");
				String readyToGo = scanner.nextLine();
				if (readyToGo.toUpperCase().equals("YES"))
				{
					isReadyToRun = true;
				}
				else if (readyToGo.toUpperCase().equals("EXIT"))
				{
					System.out.println("Exiting...");
					break;
				}
			}
		}
		
		if (isReadyToRun)
		{
			MusicFolderCopier copier = new MusicFolderCopier();
			try
			{
				copier.copy(musicPath, copyPath, blacklistPath, useFLAC, converterPath, flattenDirectories, simulate);
				System.out.println("Done!");
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("Error: Values specified are not valid.");
				System.out.println(e.getMessage());
			}

			System.out.println("Hit enter to exit.");
			scanner.nextLine();
		}
		
		scanner.close();
	}
	
	private static void printCurrentState(String musicPath, String copyPath, String blacklistPath, boolean useFLAC, String converterPath,
			boolean flattenDirectories, boolean simulate)
	{
		System.out.println("OK, here's what I have:");
		System.out.println("\tMain music folder = " + musicPath);
		System.out.println("\tLocation to copy to = " + copyPath);
		if (blacklistPath != null)
		{
			System.out.println("\tLocation of blacklist file = " + blacklistPath);
		}
		else
		{
			System.out.println("\tNo blacklist file used.");
		}
		if (useFLAC)
		{
			System.out.println("\tUse FLAC files if available.");
		}
		else
		{
			System.out.println("\tMP3 files only.");
			System.out.println("\tFLAC->MP3 converter location: " + converterPath);
		}
		if (flattenDirectories)
		{
			System.out.println("\tFlatten directories");
		}
		else
		{
			System.out.println("\tKeep album directories");
		}
		if (simulate)
		{
			System.out.println("\tRun simulation only - don't actually copy the files.");
		}
		else
		{
			System.out.println("\tFor realsies - simulation is for sissies.");
		}
	}
}
