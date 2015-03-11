package com.kyzeragon.channelfiltermod;

import java.io.File;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
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
	public String getName() { return "TE Channel Filter"; }

	@Override
	public String getVersion() { return "1.1.1"; }

	@Override
	public void init(File configPath) 
	{
		this.configScreen = new ChannelFilterConfigScreen();
		this.configKeyBinding = new KeyBinding("key.channel.config", Keyboard.KEY_SEMICOLON, "key.categories.litemods");
		LiteLoader.getInput().registerKeyBinding(this.configKeyBinding);
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	/**
	 * Filters out chat messages from the specified channels and with options
	 */
	@Override
	public boolean onChat(S02PacketChat chatPacket, IChatComponent chat, String message) 
	{ // "Global", "Help", "Trade", "Local", "Faction", "Ally"
		String playerName = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
		if (message.matches("§r§a\\[Shop\\].*"))
		{
			if (this.configScreen.getShown("Shop"))
				return true;
			else
				return false;
		} //§8[§r§bAnnouncer§r§8]§r§7=§r§8[§r§aMrLobaLoba§r§8] §r§6What is the airspeed velocity of an unladen swallow?§r

		else if (message.matches(".*§8\\[§r§bAnnouncer§r§8\\].*")) // TODO: check if correct
		{
			if (this.configScreen.getShown("MrLobaLoba"))
				return true;
			else
				return false;
		}
		else if (message.matches("§r§8\\[§r§f.§r§8\\]§r§7=.*"))
		{
			char ch = message.charAt(9);
			if (this.configScreen.getShown("" + ch))
				return true;
			else
			{
				if (message.matches(".*§a" + playerName + "§.*")) // talking in a hidden channel
				{
					this.logError("§8[§4!§8] §cYou are talking in a hidden channel: §8[§f" + ch + "§8]");
					return true;
				}
				else if (this.configScreen.getStaff() && message.matches(".*§8\\[§r§(6Admin|6Dev|eMod|bAssistant)§r§8\\].*"))
					return true;
				else if (this.configScreen.getSelf()  // someone else mentions your name
						&& message.toLowerCase().matches(".*"+playerName.toLowerCase()+".*"))
					return true;
				return false;
			}
		}
		return true;
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) 
	{
		if (inGame && minecraft.currentScreen == null && this.configKeyBinding.isPressed())
		{
			minecraft.displayGuiScreen(this.configScreen);
		}
	}

	/**
	 * Logs error message in red text to the user
	 * @param message The error message
	 */
	private void logError(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText(message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}
