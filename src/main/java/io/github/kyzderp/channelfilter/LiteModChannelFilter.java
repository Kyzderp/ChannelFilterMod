package io.github.kyzderp.channelfilter;

import java.io.File;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.OutboundChatFilter;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.core.LiteLoaderEventBroker.ReturnValue;
import com.mumfrey.liteloader.util.log.LiteLoaderLogger;

public class LiteModChannelFilter implements ChatFilter, Tickable, OutboundChatFilter
{
	private ChannelFilterConfigScreen configScreen;
	private static KeyBinding configKeyBinding;

	private CommandHandler cmdHandler;
	public LinkedList<String> ignoredFacs;
	public boolean ignoreWildy;
	public String ignoredRegex;
	public String onlyRegex;

	private int autoReplyCooldown;
	private LinkedList<String> toSend;

	private Whitelist whitelist;
	public boolean DEBUG = false;

	@Override
	public String getName() { return "TE Channel Filter"; }

	@Override
	public String getVersion() { return "2.2.1"; }

	@Override
	public void init(File configPath) 
	{
		this.whitelist = new Whitelist();

		this.cmdHandler = new CommandHandler(this, this.whitelist);
		this.ignoredFacs = new LinkedList<String>();
		this.ignoreWildy = false;
		this.ignoredRegex = "";
		this.onlyRegex = "HerpDerpThisWontBeUsed";
		this.configScreen = new ChannelFilterConfigScreen();
		this.autoReplyCooldown = 50;
		this.toSend = new LinkedList<String>();

		LiteModChannelFilter.configKeyBinding = new KeyBinding("key.channel.config", Keyboard.KEY_SEMICOLON, "key.categories.litemods");
		LiteLoader.getInput().registerKeyBinding(LiteModChannelFilter.configKeyBinding);
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	/**
	 * Filters out chat messages from the specified channels and with options
	 */
	@Override
	public boolean onChat(ITextComponent chat, String message, ReturnValue<ITextComponent> newMessage) 
	{ // "Global", "Help", "Trade", "Local", "Faction", "Ally"
		String playerName;
		String strippedMessage = message.replaceAll("\u00A7.", "");
		
		if (this.DEBUG)
			LiteLoaderLogger.getLogger().info(message);

		// Get the user's name
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null)
			playerName = Minecraft.getMinecraft().thePlayer.getName();
		else
			return true;

		// Shop
		if (message.matches("\u00A7r\u00A7a\\[Shop\\].*"))
			return this.configScreen.getShown("Shop");
		// Announcer
		else if (message.matches(".*\u00A78\\[\u00A7r\u00A7bAnnouncer\u00A7r\u00A78\\].*"))
			return this.configScreen.getShown("MrLobaLoba");
		// PM's
		else if (message.matches(".*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\]\u00A7r\u00A77=.*"))
		{
			if (this.DEBUG)
				LiteLoaderLogger.getLogger().info("This is a PM.");
				
			boolean isPMenabled = this.configScreen.getShown("PM");
			if (isPMenabled)
				return true;

			// Someone is sending us a PM, but we want to send an automated message to reply
			if (this.configScreen.getAutoReply()
					&& message.matches("(?i)(.*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\]\u00A7r\u00A77=.* me\u00A7.*)") 
					&& !message.matches("(?i)(.*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\]\u00A7r\u00A77=.*" + playerName + " -> me\u00A7.*)")
					&& !message.matches("(?i)(.*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\].*This user has PM disabled.*)"))
			{
				// Get the name of whoever is PM'ing us
				String[] othername = message.split("(?i)(\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\]\u00A7r\u00A77=\u00A7r\u00A78\\[\u00A7r\u00A7e| -> me\u00A7)");
				if (othername.length == 3)
				{
					String user = othername[1].replaceAll("\u00A7.|\u00A7", "");
					if (this.whitelist.getList().contains(user.toLowerCase()))
					{
						// The user is in the whitelist
						return true;
					}
					else
					{
						// User is not in the whitelist
						// Also don't want to spam disable messages back and forth
						if (!message.contains("This user has PM disabled and cannot see your messages"))
							this.toSend.addLast("/m " + user + " This user has PM disabled and cannot see your messages.");
						return false;
					}
				}
			}
			// We are sending a PM, but we have PM disabled. Still show it, but also display a warning
			// if target is not whitelisted
			else if (message.matches("(?i)(.*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\]\u00A7r\u00A77=.*me ->.*)")
					&& !message.matches("(?i)(.*\u00A7r\u00A78\\[\u00A7r\u00A7dPM\u00A7r\u00A78\\].*This user has PM disabled.*)"))
			{
				try {
					String user = message.split("(?i)(me -> )")[1].split("\u00A7r\u00A78")[0];
					if (!this.whitelist.getList().contains(user.toLowerCase()))
						LiteModChannelFilter.logError("You currently have PM disabled.");
				} catch (NullPointerException e) {}
				return true;
			}
			return false;
		}

		// Other normal channels
		else if (message.matches("\u00A7r\u00A78\\[\u00A7r\u00A7...*"))
		{
			// Get the channel character
			char ch = message.charAt(9);
			if (this.configScreen.getShown("" + ch))
			{ // Shown, this part is just to check for player-set ignored
				// Show staff
				if (this.configScreen.getStaff() && message.matches(".*\u00A78\\[\u00A7r\u00A7(6Admin|6Dev|eMod|bAssistant)\u00A7r\u00A78\\].*"))
					return true;
				// Someone mentions your name
				else if (this.configScreen.getSelf()
						&& message.toLowerCase().matches(".*"+playerName.toLowerCase()+".*"))
					return true;
				// TODO: ignore users
				// Ignore wilderness
				else if (this.ignoreWildy //&& message.matches("\u00A7r\u00A78\\[\u00A7r\u00A7f[GTHW]\u00A7r\u00A78\\]\u00A7r\u00A77=\u00A7r\u00A78\\[\u00A7r\u00A7a.*"))
						&& strippedMessage.matches("\\[[GTHW](...?)?\\]=\\[[A-Za-z0-9]+\\] .*"))
					return false;
				// Ignore factions
				else if (!this.ignoredRegex.equals("") 
						&& message.toLowerCase().matches(".*\u00A7r\u00A78\\[\u00A7r\u00A79.?(" + this.ignoredRegex + ").*"))
					return false;
				return true;
			}
			else
			{ // Hidden
				if (message.matches(".*\u00A7a" + playerName + "\u00A7.*")) // talking in a hidden channel
				{
					LiteModChannelFilter.logError("You are talking in a hidden channel: \u00A78[\u00A7f" + ch + "\u00A78]");
					return true;
				}
				else if (this.configScreen.getStaff() && message.matches(".*\u00A78\\[\u00A7r\u00A7(6Admin|6Dev|eMod|bAssistant)\u00A7r\u00A78\\].*"))
					return true;
				else if (this.configScreen.getSelf()  // someone else mentions your name
						&& message.toLowerCase().matches(".*"+playerName.toLowerCase()+".*"))
					return true;
				return false;
			}
		}
		else if (!this.onlyRegex.equals("HerpDerpThisWontBeUsed"))
		{
			String rawText = message.replaceAll("\u00A7.", "");
			if (!rawText.matches(this.onlyRegex))
				return false;
		}
		return true;
	}

	@Override
	public boolean onSendChatMessage(String message) 
	{
		String[] tokens = message.split(" ");
		if (tokens[0].equalsIgnoreCase("/channelfilter") || tokens[0].equalsIgnoreCase("/cf"))
		{
			this.cmdHandler.handleCommand(message);
			return false;
		} // if channelfilter command
		return true;
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) 
	{
		if (inGame && minecraft.currentScreen == null && LiteModChannelFilter.configKeyBinding.isPressed())
		{
			minecraft.displayGuiScreen(this.configScreen);
		}

		// Queue for processing auto replies to PM's to avoid being kicked for spam
		if (this.autoReplyCooldown < 50)
			this.autoReplyCooldown++;
		if (inGame && !this.toSend.isEmpty() && this.autoReplyCooldown == 50)
		{
			Minecraft.getMinecraft().thePlayer.sendChatMessage(this.toSend.removeFirst());
			this.autoReplyCooldown = 0;
		}
	}

	/**
	 * Logs the message to the user
	 * @param message The message to log
	 * @param addPrefix Whether to add the mod-specific prefix or not
	 */
	public static void logMessage(String message, boolean addPrefix)
	{// "\u00A78[\u00A72ChannelFilter\u00A78] \u00A7a"
		if (addPrefix)
			message = "\u00A78[\u00A72ChannelFilter\u00A78] \u00A7a" + message;
		TextComponentString displayMessage = new TextComponentString(message);
		displayMessage.setStyle((new Style()).setColor(TextFormatting.GREEN));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	public static void logError(String message)
	{
		TextComponentString displayMessage = new TextComponentString("\u00A78[\u00A74!\u00A78] \u00A7c" + message + " \u00A78[\u00A74!\u00A78]");
		displayMessage.setStyle((new Style()).setColor(TextFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}