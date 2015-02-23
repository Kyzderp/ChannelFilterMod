package com.kyzeragon.channelfiltermod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.OutboundChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;

public class LiteModChannelFilter implements ChatFilter, Tickable
{
	private ChannelFilterConfigScreen configScreen;
	private static KeyBinding configKeyBinding;


	@Override
	public String getName() { return "Channel Filter"; }

	@Override
	public String getVersion() { return "0.9.0"; }

	@Override
	public void init(File configPath) 
	{
		this.configScreen = new ChannelFilterConfigScreen();
		this.configKeyBinding = new KeyBinding("key.channel.config", Keyboard.KEY_SEMICOLON, "key.categories.litemods");
		LiteLoader.getInput().registerKeyBinding(this.configKeyBinding);
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@Override
	public boolean onChat(S02PacketChat chatPacket, IChatComponent chat, String message) 
	{
		// TODO Auto-generated method stub
		// TODO: error if talking in hidden channel
		return true;
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) 
	{
		if (inGame && minecraft.currentScreen != null && minecraft.currentScreen.equals(this.configScreen))
		{
//			this.configScreen.updateScreen();
		}
		
		if (inGame && minecraft.currentScreen == null && this.configKeyBinding.isPressed())
		{
			minecraft.displayGuiScreen(this.configScreen);
		}
	}

}
