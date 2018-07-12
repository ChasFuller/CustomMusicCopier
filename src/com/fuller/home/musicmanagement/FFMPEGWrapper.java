package com.fuller.home.musicmanagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FFMPEGWrapper
{
	private String ffmpegLocation;
	private Process ffmpegProcess = null;
	private InputStream errorStream = null;
	
	public FFMPEGWrapper(String ffmpegCanonicalPath)
	{
		ffmpegLocation = ffmpegCanonicalPath;
	}

	public void encode(FFMPEGArguments arguments)
	{
		try
		{
			execute(arguments);
			eatFFMPEGTextOutputHeader();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			ffmpegProcess.destroy();
			try
			{
				errorStream.close();
			}
			catch (IOException e)
			{
				System.out.println("Blew Up Trying To Close Streams");
			}
		}
	}

	private void execute(FFMPEGArguments args) throws IOException
	{
		ArrayList<String> cmdLineArgs = new ArrayList<String>();
		cmdLineArgs.add(ffmpegLocation);
		
		String input = args.getInputFileCanonicalPath();
		if (input == null || input.isEmpty())
		{
			throw new IllegalArgumentException();
		}
		else
		{
			cmdLineArgs.add("-i");
			cmdLineArgs.add(input);
		}
		
		cmdLineArgs.add("-vsync");
		cmdLineArgs.add("2");
		
		String bitrate = args.getBitrate();
		if (bitrate == null || bitrate.isEmpty())
		{
			throw new IllegalArgumentException();
		}
		else
		{
			cmdLineArgs.add("-ab");
			cmdLineArgs.add(bitrate);
		}
		
		String channels = args.getChannels();
		if (channels != null && !channels.isEmpty())
		{
			cmdLineArgs.add("-ac");
			cmdLineArgs.add(channels);
		}
		
		String samplingRate = args.getSamplingRate();
		if (samplingRate != null && !samplingRate.isEmpty())
		{
			cmdLineArgs.add("-ar");
			cmdLineArgs.add(samplingRate);
		}
		
		String output = args.getOutputFileCanonicalPath();
		if (output == null || output.isEmpty())
		{
			throw new IllegalArgumentException();
		}
		else
		{
			cmdLineArgs.add(output);
		}
		
		cmdLineArgs.add("-y");
		
		String[] cmd = cmdLineArgs.toArray(new String[cmdLineArgs.size()]);
		
		Runtime runtime = Runtime.getRuntime();
		ffmpegProcess = runtime.exec(cmd);
		runtime.addShutdownHook(new Thread() {
            public void run() {
                 ffmpegProcess.destroy();  
            }
        });

		errorStream = ffmpegProcess.getErrorStream();
	}
	
	private void eatFFMPEGTextOutputHeader() throws IOException
	{
		boolean sawOutputFile = false;
		StringBuilder sb = new StringBuilder();
		
		BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(errorStream));
		
		while (true)
		{
			String anErrorLine = errorStreamReader.readLine();
			
			if (anErrorLine == null)
			{
				errorStreamReader.close();
				throw new RuntimeException("Reached the end of the FFMPEG output without seeing evidence of a file being created:\n"
						+ sb.toString());
			}
			else if (anErrorLine.startsWith("Output #0"))
			{
				sawOutputFile = true;
			}
			else if (sawOutputFile && anErrorLine.startsWith("video") && anErrorLine.contains("global headers") && anErrorLine.contains("muxing overhead"))
			{
				// Reached the end
				errorStreamReader.close();
				break;
			}
			sb.append(anErrorLine);
		}
	}
}
