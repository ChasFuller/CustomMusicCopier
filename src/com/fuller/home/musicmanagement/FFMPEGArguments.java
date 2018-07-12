package com.fuller.home.musicmanagement;

public class FFMPEGArguments
{
	private String bitrate;
	private String channels;
	private String samplingRate;
	private String inputFileCanonicalPath;
	private String outputFileCanonicalPath;
	
	public String getBitrate()
	{
		return bitrate;
	}
	
	public void setBitrate(String bitrate)
	{
		this.bitrate = bitrate;
	}
	
	public String getChannels()
	{
		return channels;
	}
	
	public void setChannels(String channels)
	{
		this.channels = channels;
	}
	
	public String getSamplingRate()
	{
		return samplingRate;
	}
	
	public void setSamplingRate(String samplingRate)
	{
		this.samplingRate = samplingRate;
	}
	
	public String getInputFileCanonicalPath()
	{
		return inputFileCanonicalPath;
	}
	
	public void setInputFileCanonicalPath(String inputFileCanonicalPath)
	{
		this.inputFileCanonicalPath = inputFileCanonicalPath;
	}
	
	public String getOutputFileCanonicalPath()
	{
		return outputFileCanonicalPath;
	}
	
	public void setOutputFileCanonicalPath(String outputFileCanonicalPath)
	{
		this.outputFileCanonicalPath = outputFileCanonicalPath;
	}
}
