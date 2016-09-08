package io.github.kyzderp.channelfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;

import net.minecraft.client.Minecraft;

import com.mumfrey.liteloader.util.log.LiteLoaderLogger;

public class Whitelist 
{
	private HashSet<String> whitelist;
	
	private final File dirs = new File(Minecraft.getMinecraft().mcDataDir, "liteconfig" + File.separator 
			+ "config.1.10.2" + File.separator + "ChannelFilter");
	private final File path = new File(dirs.getPath() + File.separator + "whitelist.txt");
	
	public Whitelist()
	{
		this.whitelist = new HashSet<String>();
		if (!path.exists())
		{
			this.dirs.mkdirs();
			if (!this.writeFile())
				LiteLoaderLogger.warning("Cannot write to ChannelFilter whitelist file!");
			else
				LiteLoaderLogger.info("Created new ChannelFilter whitelist file.");
		}
		if (!this.loadFile())
			LiteLoaderLogger.warning("Cannot read from ChannelFilter whitelist file!");
		else
			LiteLoaderLogger.info("ChannelFilter whitelist loaded.");
	}
	
	public boolean writeFile()
	{
		System.out.println("Writing to file...");
		PrintWriter writer;
		try {
			writer = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		for (String name: this.whitelist)
		{
			writer.println(name.toLowerCase());
		}
		writer.close();
		return true;
	}

	public boolean loadFile()
	{
		if (!path.exists())
			return false;
		Scanner scan;
		try {
			scan = new Scanner(path);
		} catch (FileNotFoundException e) {
			return false;
		}
		this.whitelist = new HashSet<String>();
		while (scan.hasNext())
		{
			String line = scan.nextLine();
			this.whitelist.add(line.toLowerCase());
		}
		scan.close();
		
		String p = "Loaded whitelist:";
		for (String name: this.whitelist)
			p += " " + name;
		System.out.println(p);
		return true;
	}
	
	///// Getters /////
	public HashSet<String> getList() { return this.whitelist; }
}
