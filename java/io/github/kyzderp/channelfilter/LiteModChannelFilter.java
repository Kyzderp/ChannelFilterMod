package io.github.kyzderp.channelfilter;

import java.io.File;
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
	private boolean sentCmd;
	private LinkedList<String> ignoredFacs;
	private boolean ignoreWildy;
	private String ignoredRegex;
	private String onlyRegex;
	private int autoReplyCooldown;
	private LinkedList<String> toSend;

	@Override
	public String getName() { return "TE Channel Filter"; }

	@Override
	public String getVersion() { return "1.4.0"; }

	@Override
	public void init(File configPath) 
	{
		this.sentCmd = false;
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
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null)
			playerName = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
		else
			return true;
		if (message.matches("§r§a\\[Shop\\].*"))
			return this.configScreen.getShown("Shop");
		else if (message.matches(".*§8\\[§r§bAnnouncer§r§8\\].*"))
			return this.configScreen.getShown("MrLobaLoba");
		else if (message.matches(".*§r§8\\[§r§dPM§r§8\\]§r§7=.*"))
		{
			boolean result = this.configScreen.getShown("PM");
			if (this.configScreen.getAutoReply() && !result
					&& message.matches(".*§r§8\\[§r§dPM§r§8\\]§r§7=.* me§.*") 
					&& !message.matches(".*§r§8\\[§r§dPM§r§8\\]§r§7=.*" + playerName + " -> me§.*"))
			{//§r §r§8[§r§dPM§r§8]§r§7=§r§8[§r§eKyzer
				String[] othername = message.split("\\[§r§dPM§r§8\\]§r§7=§r§8\\[§r§e| -> me§");
				if (othername.length == 3)
				{
					othername[1] = othername[1].replaceAll("§.|§", "");
					this.toSend.addLast("/m " + othername[1] + " This user has PM disabled and cannot see your messages.");
				}
			}
			else if (!result && message.matches(".*§r§8\\[§r§dPM§r§8\\]§r§7=.*me ->.*")
					&& !message.matches(".*§r§8\\[§r§dPM§r§8\\].*This user has PM disabled.*"))
			{
				this.logError("You currently have PM disabled.");
				return true;
			}
			return result;
		}//§r §r§8[§r§dPM§r§8]§r§7=§r§8[§r§eme -> Kyzeragon§r§8]§r§d a§r
		// §r §r§8[§r§dPM§r§8]§r§7=§r§8[§r§eme -> Kyzer§r§8]§r§d a§r

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
			this.sentCmd = true;
			if (tokens.length == 1)
			{
				this.logMessage("§2" + this.getName() + " §8[§2v" + this.getVersion() + "§8] §aby Kyzeragon", false);
				this.logMessage("Type §2/channelfilter help §aor §2/cf help §afor commands.", false);
				return false;
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
				this.logMessage("§2Wiki:§a https://github.com/Kyzderp/ChannelFilterMod/wiki", false);
			} // help
			else if (tokens[1].equalsIgnoreCase("only"))
			{
				if (tokens.length > 2 && tokens[2].equalsIgnoreCase("clear"))
				{
					this.onlyRegex = "HerpDerpThisWontBeUsed";
					this.logMessage("onlyRegex cleared.", true);
					return false;
				}
				this.onlyRegex = message.replaceFirst("/cf only |/channelfilter only", "");
				this.logMessage("onlyRegex set to: " + this.onlyRegex, true);
			} // only
			else if (tokens[1].equalsIgnoreCase("ignore"))
			{
				if (tokens.length == 2)
				{
					if (this.ignoredFacs.isEmpty())
					{
						this.logMessage("Not currently ignoring any factions.", true);
						return false;
					}
					String result = "Currently ignoring faction(s):";
					if (this.ignoreWildy)
						result += " Wilderness,";
					for (String fac: this.ignoredFacs)
						result += " " + fac + ",";
					this.logMessage(result.substring(0, result.length() - 1), true);
					return false;
				} // cf ignore
				else if (tokens[2].equalsIgnoreCase("clear"))
				{
					this.ignoredFacs.clear();
					this.ignoreWildy = false;
					this.logMessage("Cleared ignore list.", true);
					return false;
				} // cf ignore clear
				else if (tokens[2].equalsIgnoreCase("wilderness"))
				{
					if (this.ignoreWildy)
					{
						this.ignoreWildy = false;
						this.logMessage("No longer ignoring players without a faction.", true);
						return false;
					}
					this.ignoreWildy = true;
					this.logMessage("Now ignoring players without a faction.", true);
					return false;
				} // cf ignore wilderness
				else if (tokens.length > 3)
				{
					this.logError("Too many parameters! See /cf help for usage information");
					return false;
				} // too many args
				else
				{
					String fac = tokens[2].toLowerCase();
					if (!fac.matches("[0-9A-Za-z]+"))
					{
						this.logError("Invalid faction name; must be alphanumeric: " + fac);
						return false;
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