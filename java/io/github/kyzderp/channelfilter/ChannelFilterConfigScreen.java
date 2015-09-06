package io.github.kyzderp.channelfilter;

import java.util.LinkedList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class ChannelFilterConfigScreen extends GuiScreen 
{
	private final int offset = 15;
	private final int poffset = 10;
	
	private String[] channels = {"Global", "Help", "Trade", "Shop", "Local", "Faction", "Allies", "MrLobaLoba"};
	private boolean[] isChannelShown;
	private boolean showStaff;
	private boolean showSelf;
	private boolean autoMsg;
	private boolean pm;
	private LinkedList<String> ch = new LinkedList<String>();
	private int cooldown; // cooldown for clicking cuz buttons are dumb


	public ChannelFilterConfigScreen()
	{
		super();
		this.isChannelShown = new boolean[channels.length];
		String[] letters = {"G", "H", "T", "Shop", "L", "F", "A", "MrLobaLoba"};
		for (int i = 0; i < channels.length; i++)
		{
			this.isChannelShown[i] = true;
			this.ch.addLast(letters[i]);
		}
		this.showStaff = true;
		this.showSelf = true;
		this.autoMsg = true;
		this.pm = true;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.drawDefaultBackground();
		for (int i = 0; i < channels.length; i++)
		{
			if (i < 4)
				this.buttonList.add(new GuiButton(i, this.width/2 - 152, 
						this.height/2 - 60 - offset + 25*i, 150, 20, ""));
			else
				this.buttonList.add(new GuiButton(i, this.width/2 + 2, 
						this.height/2 - 160 - offset + 25*i, 150, 20, ""));
		}
		this.buttonList.add(new GuiButton(8, this.width/2 - 152, this.height/2 + 40 - offset, 124, 20, ""));
		this.buttonList.add(new GuiButton(9, this.width/2 - 24, this.height/2 + 40 - offset, 22, 20, ""));
		this.buttonList.add(new GuiButton(10, this.width/2 - 152, this.height/2 + 60 + poffset, 150, 20, ""));
		this.buttonList.add(new GuiButton(11, this.width/2 + 2, this.height/2 + 60 + poffset, 150, 20, ""));
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
		for (int i = 0; i < channels.length; i++)
		{
			display = channels[i] + ": Shown";
			color = 0x88FFAA;
			if (!this.isChannelShown[i])
			{
				color = 0xFF8888;
				display = channels[i] + ": Hidden";
			}
			if (i < 4)
				this.drawCenteredString(fontRendererObj, display, 
						this.width/2 - 152 + 75, this.height/2 - 60 - offset + 25*i + 6, color);
			else
				this.drawCenteredString(fontRendererObj, display, 
						this.width/2 + 2 + 75, this.height/2 - 160 - offset + 25*i + 6, color);
		}
		color = 0x88FFAA;
		display = "PM: Shown";
		if (!this.pm)
		{
			color = 0xFF8888;
			display = "PM: Hidden";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 - 152 + 61, this.height/2 + 40 - offset + 6, color);
		
		color = 0x88FFAA;
		display = "\u26A0";
		if (!this.autoMsg)
		{
			color = 0xFF8888;
			display = "\u26A0";
		}
		this.drawCenteredString(fontRendererObj, display, 
				this.width/2 - 12, this.height/2 + 40 - offset + 6, color);
		
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
		if (i < 8) // channel buttons
			this.isChannelShown[i] = !this.isChannelShown[i];
		else if (i == 8)
			this.pm = !this.pm;
		else if (i == 9)
			this.autoMsg = !this.autoMsg;
		else if (i == 10)
			this.showStaff = !this.showStaff;
		else if (i == 11)
			this.showSelf = !this.showSelf;
		this.updateScreen();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}

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
