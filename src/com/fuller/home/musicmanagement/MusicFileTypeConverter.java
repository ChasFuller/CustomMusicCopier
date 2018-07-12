package com.fuller.home.musicmanagement;

public class MusicFileTypeConverter
{
	private FFMPEGWrapper wrapper;
	
	public MusicFileTypeConverter(String pathToConversionExecutable)
	{
		wrapper = new FFMPEGWrapper(pathToConversionExecutable);
	}
	
	public void convertFLACToMP3(String FLACFilePath, String MP3FilePath)
	{
		try
		{
			FFMPEGArguments args = new FFMPEGArguments();
			args.setInputFileCanonicalPath(FLACFilePath);
			args.setOutputFileCanonicalPath(MP3FilePath);
			args.setBitrate("192K");
			args.setChannels("2");
			args.setSamplingRate("44100");
			
			wrapper.encode(args);	
		}
		catch (Exception e)
		{
			System.out.println("FLAC to MP3 encoding blew up: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
