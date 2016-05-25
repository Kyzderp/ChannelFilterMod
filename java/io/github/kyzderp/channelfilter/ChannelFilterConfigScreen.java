package io.github.kyzderp.channelfilter;

import java.util.LinkedList;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class ChannelFilterConfigScreen extends GuiScreen 
{
	private Config config;
	private Set<String> off;
	
	private final int offset = 15;
	private final int poffset = 10;
	
	private String[] channels = {"Global", "World", "Help", "Trade", "Shop", "Local", 
			"Faction", "Allies", "MrLobaLoba"};
	public boolean[] isChannelShown;
	private boolean showStaff;
	private boolean showSelf;
	private boolean autoMsg;
	private boolean pm;
	private LinkedList<String> ch = new LinkedList<String>();
	private int cooldown; // cooldown for clicking cuz buttons are dumb
	
	private final int numChannels = this.channels.length;


	public ChannelFilterConfigScreen()
	{
		super();
		
		this.config = new Config();
		this.off = this.config.loadFile();
		
		this.isChannelShown = new boolean[this.numChannels];
		String[] letters = {"G", "W", "H", "T", "Shop", "L", "F", "A", "MrLobaLoba"};
		for (int i = 0; i < this.numChannels; i++)
		{
			this.ch.addLast(letters[i]);
			this.isChannelShown[i] = !off.contains(this.channels[i]);
		}
		this.showStaff = true;
		this.showSelf = true;
		this.autoMsg = true;
		this.pm = !this.off.contains("PM");
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.drawDefaultBackground();
		for (int i = 0; i < channels.length; i++)
		{
			if (i < (this.numChannels + 1)/2)
				this.buttonList.add(new GuiButton(i, this.width/2 - 152, 
						this.height/2 - 60 - offset + 25*i, 150, 20, ""));
			else
				this.buttonList.add(new GuiButton(i, this.width/2 + 2, 
						this.height/2 - 160 - 25 - offset + 25*i, 150, 20, ""));
		}
		// PM
		this.buttonList.add(new GuiButton(this.numChannels, this.width/2 + 2, this.height/2 + 40 - offset, 124, 20, ""));
		this.buttonList.add(new GuiButton(this.numChannels + 1, this.width/2 + 2 + 128, this.height/2 + 40 - offset, 22, 20, ""));
		// Show staff
		this.buttonList.add(new GuiButton(this.numChannels + 2, this.width/2 - 152, this.height/2 + 60 + poffset, 150, 20, ""));
		// Show own name
		this.buttonList.add(new GuiButton(this.numChannels + 3, this.width/2 + 2, this.height/2 + 60 + poffset, 150, 20, ""));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) //no idea what the params are for :D
	{
		if (this.cooldown > 0)
			this.cooldown--;
		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);
		
		this.drawCenteredString(fontRendererObj, "Channel Filter Options", 
				this.width/2, this.height/2 - 90 - offset, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "Channels",
				this.width/2, this.height/2 - 75 - offset, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "Other Options",
				this.width/2, this.height/2 + 45 + poffset, 0xFFFFFF);

		String display;
		int color = 0x88FFAA;
		GuiButton button;
		for (int i = 0; i < this.numChannels; i++)
		{
			display = channels[i] + ": Shown";
			color = 0x88FFAA;
			if (!this.isChannelShown[i])
			{
				color = 0xFF8888;
				display = channels[i] + ": Hidden";
			}
			if (i < (this.numChannels + 1)/2)
				this.drawCenteredString(fontRendererObj, display, 
						this.width/2 - 152 + 75, this.height/2 - 60 - offset + 25*i + 6, color);
			else
				this.drawCenteredString(fontRendererObj, display, 
						this.width/2 + 2 + 75, this.height/2 - 25 - 160 - offset + 25*i + 6, color);
		}
		
		color = 0x88FFAA;
		display = "PM: Shown";
		if (!this.pm)
		{
			color = 0xFF8888;
			display = "PM: Hidden";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 + 2 + 65, this.height/2 + 40 - offset + 6, color);
		
		color = 0x88FFAA;
		display = "\u26A0";
		if (!this.autoMsg)
		{
			color = 0xFF8888;
			display = "\u26A0";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 + 2 + 140, this.height/2 + 40 - offset + 6, color);
		
		color = 0x88FFAA;
		display = "Always Show Staff: True";
		if (!this.showStaff)
		{
			color = 0xFF8888;
			display = "Always Show Staff: False";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 - 152 + 75, this.height/2 + 60 + poffset + 6, color);

		color = 0x88FFAA;
		display = "Show Own Name: True";
		if (!this.showSelf)
		{
			color = 0xFF8888;
			display = "Show Own Name: False";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 + 2 + 75, this.height/2 + 60 + poffset + 6, color);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		int i = button.id;
		if (i < this.numChannels) // channel buttons
		{
			this.isChannelShown[i] = !this.isChannelShown[i];
			if (this.isChannelShown[i])
				this.off.remove(this.channels[i]);
			else
				this.off.add(this.channels[i]);
			this.config.writeFile(this.off);
		}
		else if (i == this.numChannels)
		{
			this.pm = !this.pm;
			if (this.pm)
				this.off.remove("PM");
			else
				this.off.add("PM");
			this.config.writeFile(this.off);
		}
		else if (i == this.numChannels + 1)
			this.autoMsg = !this.autoMsg;
		else if (i == this.numChannels + 2)
			this.showStaff = !this.showStaff;
		else if (i == this.numChannels + 3)
			this.showSelf = !this.showSelf;
		this.updateScreen();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}

	/**
	 * Get whether the channel is shown or not, by its prefix
	 * @param c
	 * @return
	 */
	public boolean getShown(String c)
	{
		if (c.equalsIgnoreCase("pm"))
			return this.pm;
		int index = ch.indexOf(c);
		if (index == -1)
			return true;
		return this.getShown(index);
	}

	public boolean getAutoReply() { return this.autoMsg; }
	private boolean getShown(int i) { return this.isChannelShown[i]; }
	public boolean getStaff() { return this.showStaff; }
	public boolean getSelf() { return this.showSelf; }
}
