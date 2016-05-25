package io.github.kyzderp.channelfilter;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.OutboundChatFilter;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.core.LiteLoaderEventBroker.ReturnValue;

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
	private Config config;

	@Override
	public String getName() { return "TE Channel Filter"; }

	@Override
	public String getVersion() { return "2.1.0"; }

	@Override
	public void init(File configPath) 
	{
		this.whitelist = new Whitelist();
		this.config = new Config();

		this.cmdHandler = new CommandHandler(this, this.whitelist);
		this.ignoredFacs = new LinkedList<String>();
		this.ignoreWildy = false;
		this.ignoredRegex = "";
		this.onlyRegex = "HerpDerpThisWontBeUsed";
		this.configScreen = new ChannelFilterConfigScreen();
		this.autoReplyCooldown = 50;
		this.toSend = new LinkedList<String>();

		this.configKeyBinding = new KeyBinding("key.channel.config", Keyboard.KEY_SEMICOLON, "key.categories.litemods");
		LiteLoader.getInput().registerKeyBinding(this.configKeyBinding);
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	/**
	 * Filters out chat messages from the specified channels and with options
	 */
	@Override
	public boolean onChat(IChatComponent chat, String message, ReturnValue<IChatComponent> newMessage) 
	{ // "Global", "Help", "Trade", "Local", "Faction", "Ally"
		String playerName;
		String strippedMessage = message.replaceAll("\u00A7.", "");

		// Get the user's name
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null)
			playerName = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
		else
			return true;

		// Shop
		if (message.matches("§r§a\\[Shop\\].*"))
			return this.configScreen.getShown("Shop");
		// Announcer
		else if (message.matches(".*§8\\[§r§bAnnouncer§r§8\\].*"))
			return this.configScreen.getShown("MrLobaLoba");
		// PM's
		else if (message.matches(".*§r§8\\[§r§dPM§r§8\\]§r§7=.*"))
		{
			boolean isPMenabled = this.configScreen.getShown("PM");
			if (isPMenabled)
				return true;

			// Someone is sending us a PM, but we want to send an automated message to reply
			if (this.configScreen.getAutoReply()
					&& message.matches("(?i)(.*§r§8\\[§r§dPM§r§8\\]§r§7=.* me§.*)") 
					&& !message.matches("(?i)(.*§r§8\\[§r§dPM§r§8\\]§r§7=.*" + playerName + " -> me§.*)")
					&& !message.matches("(?i)(.*§r§8\\[§r§dPM§r§8\\].*This user has PM disabled.*)"))
			{
				// Get the name of whoever is PM'ing us
				String[] othername = message.split("(?i)(\\[§r§dPM§r§8\\]§r§7=§r§8\\[§r§e| -> me§)");
				if (othername.length == 3)
				{
					String user = othername[1].replaceAll("§.|§", "");
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
			else if (message.matches("(?i)(.*§r§8\\[§r§dPM§r§8\\]§r§7=.*me ->.*)")
					&& !message.matches("(?i)(.*§r§8\\[§r§dPM§r§8\\].*This user has PM disabled.*)"))
			{
				try {
					String user = message.split("(?i)(me -> )")[1].split("§r§8")[0];
					if (!this.whitelist.getList().contains(user.toLowerCase()))
						this.logError("You currently have PM disabled.");
				} catch (NullPointerException e) {}
				return true;
			}
			return false;
		}

		// Other normal channels
		else if (message.matches("§r§8\\[§r§...*"))
		{
			// Get the channel character
			char ch = message.charAt(9);
			if (this.configScreen.getShown("" + ch))
			{ // Shown, this part is just to check for player-set ignored
				// Show staff
				if (this.configScreen.getStaff() && message.matches(".*§8\\[§r§(6Admin|6Dev|eMod|bAssistant)§r§8\\].*"))
					return true;
				// Someone mentions your name
				else if (this.configScreen.getSelf()
						&& message.toLowerCase().matches(".*"+playerName.toLowerCase()+".*"))
					return true;
				// TODO: ignore users
				// Ignore wilderness
				else if (this.ignoreWildy //&& message.matches("§r§8\\[§r§f[GTHW]§r§8\\]§r§7=§r§8\\[§r§a.*"))
						&& strippedMessage.matches("\\[[GTHW](...?)?\\]=\\[[A-Za-z0-9]+\\] .*"))
					return false;
				// Ignore factions
				else if (!this.ignoredRegex.equals("") 
						&& message.toLowerCase().matches(".*§r§8\\[§r§9.?(" + this.ignoredRegex + ").*"))
					return false;
				return true;
			}
			else
			{ // Hidden
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
		else if (!this.onlyRegex.equals("HerpDerpThisWontBeUsed"))
		{
			String rawText = message.replaceAll("§.", "");
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
		if (inGame && minecraft.currentScreen == null && this.configKeyBinding.isPressed())
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