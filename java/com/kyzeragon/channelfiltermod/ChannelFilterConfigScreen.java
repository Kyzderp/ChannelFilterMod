package com.kyzeragon.channelfiltermod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class ChannelFilterConfigScreen extends GuiScreen 
{
	private String[] channels = {"Global", "Help", "Trade", "Local", "Faction", "Ally"};
	private boolean[] isChannelShown;
	
	public ChannelFilterConfigScreen()
	{
		super();
		this.isChannelShown = new boolean[channels.length];
		for (int i = 0; i < channels.length; i++)
			this.isChannelShown[i] = true;
		for (int i = 0; i < channels.length; i++)
		{
			this.buttonList.add(new GuiButton(i, this.width/2 - 100, this.height/2 - 75 + 25*i, 
					200, 20, channels[i] + ": Shown"));
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.drawDefaultBackground();
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) //no idea what the params are for :D
	{
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, "Channel Filter Options", 
				this.width/2, this.height/2 - 100, 0xFFFFFF);
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		int i = button.id;
		this.isChannelShown[i] = !this.isChannelShown[i];
		if (this.isChannelShown[i])
			button.displayString = this.channels[i] + ": Shown";
		else
			button.displayString = this.channels[i] + ": OFF";
		this.updateScreen();
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}
}
