package io.github.kyzderp.channelfilter;

public class CommandHandler 
{
	private LiteModChannelFilter mod;
	private Whitelist whitelist;

	public CommandHandler(LiteModChannelFilter mod, Whitelist whitelist)
	{
		this.mod = mod;
		this.whitelist = whitelist;
	}

	public void handleCommand(String message)
	{
		String[] tokens = message.split(" ");

		if (tokens.length == 1)
		{
			LiteModChannelFilter.logMessage("\u00A72" + this.mod.getName() + " \u00A78[\u00A72v" + this.mod.getVersion() + "\u00A78] \u00A7aby Kyzeragon", false);
			LiteModChannelFilter.logMessage("Type \u00A72/channelfilter help \u00A7aor \u00A72/cf help \u00A7afor commands.", false);
			return;
		} // cf

		else if (tokens[1].equalsIgnoreCase("help"))
		{
			LiteModChannelFilter.logMessage("\u00A72" + this.mod.getName() + " \u00A78[\u00A72v" + this.mod.getVersion() + "\u00A78] \u00A7acommands (alias /channelfilter)", false);
			String[] commands = {"ignore \u00A77- \u00A7aSees the list of currently ignored factions.",
					"ignore <faction> \u00A77- \u00A7aAdds or removes a faction from ignore list.",
					"ignore wilderness \u00A77- \u00A7aIgnores players without a faction.",
					"ignore clear \u00A77- \u00A7aClears the ignore list.",
					"whitelist <add|remove|list|reload>",
			"help \u00A77- \u00A7aDisplays this help message. Herpaderp."};
			for (String command: commands)
				LiteModChannelFilter.logMessage("/cf " + command, false);
			LiteModChannelFilter.logMessage("\u00A72Wiki:\u00A7a https://github.com/Kyzderp/ChannelFilterMod/wiki", false);
		} // help

		else if (tokens[1].equalsIgnoreCase("only"))
		{
			if (tokens.length > 2 && tokens[2].equalsIgnoreCase("clear"))
			{
				this.mod.onlyRegex = "HerpDerpThisWontBeUsed";
				LiteModChannelFilter.logMessage("onlyRegex cleared.", true);
				return;
			}
			this.mod.onlyRegex = message.replaceFirst("/cf only |/channelfilter only", "");
			LiteModChannelFilter.logMessage("onlyRegex set to: " + this.mod.onlyRegex, true);
		} // only

		else if (tokens[1].equalsIgnoreCase("ignore"))
		{
			this.handleIgnore(tokens);
		} // ignore

		else if (tokens[1].equalsIgnoreCase("whitelist"))
		{
			this.handleWhitelist(tokens);
		} // whitelist

		else if (tokens[1].equalsIgnoreCase("debug"))
		{
			this.mod.DEBUG = !this.mod.DEBUG;
			LiteModChannelFilter.logMessage("Toggling debug mode: " + this.mod.DEBUG 
					+ ". Check client logs for information.", true);
		} // debug

		else
			LiteModChannelFilter.logError("Invalid parameters. See /cf help for usage information");

		this.mod.ignoredRegex = "";
		if (this.mod.ignoredFacs.size() > 0)
		{
			for (String fac: this.mod.ignoredFacs)
				this.mod.ignoredRegex += fac + "|";
			this.mod.ignoredRegex = this.mod.ignoredRegex.substring(0, this.mod.ignoredRegex.length() - 1);
		}
	}


	private void handleWhitelist(String[] tokens)
	{
		// /cf whitelist <add|remove|list|reload>
		if (tokens.length == 2) // /cf whitelist
		{
			LiteModChannelFilter.logError("Usage: /cf whitelist <add|remove|list|reload>");
		}
		else if (tokens[2].equalsIgnoreCase("add"))
		{
			if (tokens.length != 4)
				LiteModChannelFilter.logError("Usage: /cf whitelist add <name>");
			else
			{
				this.whitelist.getList().add(tokens[3].toLowerCase());
				this.whitelist.writeFile();
				LiteModChannelFilter.logMessage("Added to whitelist: " + tokens[3], true);
			}
		}
		else if (tokens[2].equalsIgnoreCase("remove"))
		{
			if (tokens.length != 4)
				LiteModChannelFilter.logError("Usage: /cf whitelist remove <name>");
			else
			{
				this.whitelist.getList().remove(tokens[3].toLowerCase());
				this.whitelist.writeFile();
				LiteModChannelFilter.logMessage("Removed from whitelist: " + tokens[3], true);
			}
		}
		else if (tokens[2].equalsIgnoreCase("list"))
		{
			String result = "PM Whitelist:";
			for (String name: this.whitelist.getList())
			{
				result += " " + name;
			}
			LiteModChannelFilter.logMessage(result, true);
		}
		else if (tokens[2].equalsIgnoreCase("reload"))
		{
			this.whitelist.loadFile();
			LiteModChannelFilter.logMessage("Whitelist reloaded from file.", true);
		}
		else
			LiteModChannelFilter.logError("Usage: /cf whitelist <add|remove|list|reload>");
	}

	/**
	 * @param tokens
	 */
	private void handleIgnore(String[] tokens) 
	{
		if (tokens.length == 2)
		{
			if (this.mod.ignoredFacs.isEmpty())
			{
				LiteModChannelFilter.logMessage("Not currently ignoring any factions.", true);
				return ;
			}
			String result = "Currently ignoring faction(s):";
			if (this.mod.ignoreWildy)
				result += " Wilderness,";
			for (String fac: this.mod.ignoredFacs)
				result += " " + fac + ",";
			LiteModChannelFilter.logMessage(result.substring(0, result.length() - 1), true);
			return;
		} // cf ignore

		else if (tokens[2].equalsIgnoreCase("clear"))
		{
			this.mod.ignoredFacs.clear();
			this.mod.ignoreWildy = false;
			LiteModChannelFilter.logMessage("Cleared ignore list.", true);
			return;
		} // cf ignore clear

		else if (tokens[2].equalsIgnoreCase("wilderness"))
		{
			if (this.mod.ignoreWildy)
			{
				this.mod.ignoreWildy = false;
				LiteModChannelFilter.logMessage("No longer ignoring players without a faction.", true);
				return;
			}
			this.mod.ignoreWildy = true;
			LiteModChannelFilter.logMessage("Now ignoring players without a faction.", true);
			return;
		} // cf ignore wilderness

		else if (tokens.length > 3)
		{
			LiteModChannelFilter.logError("Too many parameters! See /cf help for usage information");
			return;
		} // too many args

		else
		{
			String fac = tokens[2].toLowerCase();
			if (!fac.matches("[0-9A-Za-z]+"))
			{
				LiteModChannelFilter.logError("Invalid faction name; must be alphanumeric: " + fac);
				return;
			}
			if (this.mod.ignoredFacs.contains(fac))
			{
				this.mod.ignoredFacs.remove(fac);
				LiteModChannelFilter.logMessage("No longer ignoring faction " + fac, true);
			}
			else
			{
				this.mod.ignoredFacs.addFirst(fac);
				LiteModChannelFilter.logMessage("Now ignoring faction " + fac, true);
			}
		} // cf ignore <fac>
	}
}
