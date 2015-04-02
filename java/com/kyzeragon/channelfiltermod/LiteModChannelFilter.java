package com.kyzeragon.channelfiltermod;

import java.io.File;
import java.util.LinkedList;

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

public class LiteModChannelFilter implements ChatFilter, Tickable, OutboundChatListener
{
	private ChannelFilterConfigScreen configScreen;
	private static KeyBinding configKeyBinding;
	private boolean sentCmd;
	private LinkedList<String> ignoredFacs;
	private boolean ignoreWildy;
	private String ignoredRegex;

	@Override
	public String getName() { return "TE Channel Filter"; }

	@Override
	public String getVersion() { return "1.2.0"; }

	@Override
	public void init(File configPath) 
	{
		this.sentCmd = false;
		this.ignoredFacs = new LinkedList<String>();
		this.ignoreWildy = false;
		this.ignoredRegex = "";
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
			return this.configScreen.getShown("Shop");
		else if (message.matches(".*§8\\[§r§bAnnouncer§r§8\\].*"))
			return this.configScreen.getShown("MrLobaLoba");
		else if (message.matches("§r§8\\[§r§f.§r§8\\]§r§7=.*"))
		{
			char ch = message.charAt(9);
			if (this.configScreen.getShown("" + ch))
			{
				if (this.configScreen.getStaff() && message.matches(".*§8\\[§r§(6Admin|6Dev|eMod|bAssistant)§r§8\\].*"))
					return true;
				else if (this.configScreen.getSelf()  // someone else mentions your name
						&& message.toLowerCase().matches(".*"+playerName.toLowerCase()+".*"))
					return true;
				else if (this.ignoreWildy && message.matches("§r§8\\[§r§f[GTH]§r§8\\]§r§7=§r§8\\[§r§a.*"))
					return false;
				else if (!this.ignoredRegex.equals("") 
						&& message.toLowerCase().matches(".*§r§8\\[§r§9.?(" + this.ignoredRegex + ").*"))
					return false;
				//§r§8[§r§fG§r§8]§r§7=§r§8[§r§9?Invision§r§8]§r§7=§r§8[§r§bVip§r§8]§r§7=§r§8[§r§aKyzer§r§8] §r§fugh so much chat§r
				return true;
			}
			else
			{
				if (message.matches(".*§a" + playerName + "§.*")) // talking in a hidden channel
				{
					this.logError("You are talking in a hidden channel: §8[§f" + ch + "§8]");
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
		else if (this.sentCmd && message.matches(".*nknown.*ommand.*"))
		{
			this.sentCmd = false;
			return false;
		}
		return true;
	}

	@Override
	public void onSendChatMessage(C01PacketChatMessage packet, String message) 
	{
		String[] tokens = message.split(" ");
		if (tokens[0].equalsIgnoreCase("/channelfilter") || tokens[0].equalsIgnoreCase("/cf"))
		{
			this.sentCmd = true;
			if (tokens.length == 1)
			{
				this.logMessage("§2" + this.getName() + " §8[§2v" + this.getVersion() + "§8] §aby Kyzeragon", false);
				this.logMessage("Type §2/channelfilter help §aor §2/cf help §afor commands.", false);
				return;
			} // cf
			else if (tokens[1].equalsIgnoreCase("help"))
			{
				this.logMessage("§2" + this.getName() + " §8[§2v" + this.getVersion() + "§8] §acommands (alias /channelfilter)", false);
				String[] commands = {"ignore §7- §aSees the list of currently ignored factions.",
						"ignore <faction> §7- §aAdds or removes a faction from ignore list.",
						"ignore wilderness §7- §aIgnores players without a faction.",
						"ignore clear §7- §aClears the ignore list.",
				"help §7- §aDisplays this help message. Herpaderp."};
				for (String command: commands)
					this.logMessage("/cf " + command, false);
			} // help
			else if (tokens[1].equalsIgnoreCase("ignore"))
			{
				if (tokens.length == 2)
				{
					if (this.ignoredFacs.isEmpty())
					{
						this.logMessage("Not currently ignoring any factions.", true);
						return;
					}
					String result = "Currently ignoring faction(s):";
					if (this.ignoreWildy)
						result += " Wilderness,";
					for (String fac: this.ignoredFacs)
						result += " " + fac + ",";
					this.logMessage(result.substring(0, result.length() - 1), true);
					return;
				} // cf ignore
				else if (tokens[2].equalsIgnoreCase("clear"))
				{
					this.ignoredFacs.clear();
					this.logMessage("Cleared ignore list.", true);
					return;
				} // cf ignore clear
				else if (tokens[2].equalsIgnoreCase("wilderness"))
				{
					this.ignoreWildy = true;
					this.logMessage("Now ignoring players without a faction.", true);
					return;
				} // cf ignore wilderness
				else if (tokens.length > 3)
				{
					this.logError("Too many parameters! See /cf help for usage information");
					return;
				} // too many args
				else
				{
					String fac = tokens[2].toLowerCase();
					if (!fac.matches("[0-9A-Za-z]+"))
					{
						this.logError("Invalid faction name; must be alphanumeric: " + fac);
						return;
					}
					if (this.ignoredFacs.contains(fac))
					{
						this.ignoredFacs.remove(fac);
						this.logMessage("No longer ignoring faction " + fac, true);
					}
					else
					{
						this.ignoredFacs.addFirst(fac);
						this.logMessage("Now ignoring faction " + fac, true);
					}
				} // cf ignore <fac>
			}
			else
				this.logError("Invalid parameters. See /cf help for usage information");

			this.ignoredRegex = "";
			if (this.ignoredFacs.size() > 0)
			{
				for (String fac: this.ignoredFacs)
					this.ignoredRegex += fac + "|";
				this.ignoredRegex = this.ignoredRegex.substring(0, this.ignoredRegex.length() - 1);
			}
			System.out.println("ignoredRegex: " + this.ignoredRegex);
		}
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
	 * Logs the message to the user
	 * @param message The message to log
	 */
	public static void logMessage(String message, boolean addPrefix)
	{// "§8[§2ChannelFilter§8] §a"
		if (addPrefix)
			message = "§8[§2ChannelFilter§8] §a" + message;
		ChatComponentText displayMessage = new ChatComponentText(message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	public static void logError(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText("§8[§4!§8] §c" + message + " §8[§4!§8]");
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}