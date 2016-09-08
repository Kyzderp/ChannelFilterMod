package io.github.kyzderp.channelfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.client.Minecraft;

import com.mumfrey.liteloader.util.log.LiteLoaderLogger;

public class Config 
{
	private final File dirs = new File(Minecraft.getMinecraft().mcDataDir, "liteconfig" + File.separator 
			+ "config.1.10.2" + File.separator + "ChannelFilter");
	private final File path = new File(dirs.getPath() + File.separator + "config.txt");
	
	public Config()
	{
		if (!path.exists())
		{
			this.dirs.mkdirs();
			if (!this.writeFile(new HashSet<String>()))
				LiteLoaderLogger.warning("Cannot write to ChannelFilter file!");
			else
				LiteLoaderLogger.info("Created new ChannelFilter configuration file.");
		}
	}
	
	public boolean writeFile(Set<String> off)
	{
		System.out.println("Writing to file...");
		PrintWriter writer;
		try {
			writer = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		if (!off.isEmpty())
		{
			for (String channel: off)
				writer.println(channel);
		}
		
		writer.close();
		return true;
	}

	public Set<String> loadFile()
	{
		if (!path.exists())
			return new HashSet<String>();
		Scanner scan;
		try {
			scan = new Scanner(path);
		} catch (FileNotFoundException e) {
			return new HashSet<String>();
		}
		
		Set<String> off = new HashSet<String>();
		while (scan.hasNext())
		{
			String line = scan.nextLine();
			off.add(line);
		}
		scan.close();
		
		return off;
	}
}
