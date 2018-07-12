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
		boolean simulate = false;
		String converterPath = null;
		
		boolean isReadyToRun = false;
		
		System.out.println("CustomMusicCopier creates a copy of a music folder, optionally applying a specific blacklist to exclude artists and/or tracks");
			
		if (args.length == 6)
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
			else

			converterPath = args[4];
			
			if (args[5].toUpperCase().equals("YES"))
			{
				simulate = true;
			}
			
			printCurrentState(musicPath, copyPath, blacklistPath, useFLAC, converterPath, simulate);
			isReadyToRun = true;
		}
		else
		{			
			Scanner scanner = new Scanner(System.in);
		
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
					System.out.println("Dlease type in the full path to the file converter to use to convert FLAC files into MP3s:");
					converterPath = scanner.nextLine();
				}
				
				System.out.println("Do you want to simulate the copy without actually copying the files? (yes/no)");
				String doSimulation = scanner.nextLine();
				if (doSimulation.toUpperCase().equals("YES"))
				{
					simulate = true;
				}
				printCurrentState(musicPath, copyPath, blacklistPath, useFLAC, converterPath, simulate);
				
				System.out.println("Is this right? (yes/no/exit)");
				String readyToGo = scanner.nextLine();
				if (readyToGo.toUpperCase().equals("YES"))
				{
					scanner.close();
					isReadyToRun = true;
				}
				else if (readyToGo.toUpperCase().equals("EXIT"))
				{
					scanner.close();
					break;
				}
			}
		}
		
		if (isReadyToRun)
		{
			MusicFolderCopier copier = new MusicFolderCopier();
			copier.copy(musicPath, copyPath, blacklistPath, useFLAC, converterPath, simulate);
			System.out.println("Done!");
		}
		
		System.out.println("Exiting...");
	}
	
	private static void printCurrentState(String musicPath, String copyPath, String blacklistPath, boolean useFLAC, String converterPath, boolean simulate)
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
