package io.github.kyzderp.channelfilter;

public class CommandHandler 
{
	private LiteModChannelFilter mod;
	
	public CommandHandler(LiteModChannelFilter mod)
	{
		this.mod = mod;
	}
	
	public void handleCommand(String message)
	{
		String[] tokens = message.split(" ");
		
		if (tokens.length == 1)
		{
			this.mod.logMessage("§2" + this.mod.getName() + " §8[§2v" + this.mod.getVersion() + "§8] §aby Kyzeragon", false);
			this.mod.logMessage("Type §2/channelfilter help §aor §2/cf help §afor commands.", false);
			return;
		} // cf
		
		else if (tokens[1].equalsIgnoreCase("help"))
		{
			this.mod.logMessage("§2" + this.mod.getName() + " §8[§2v" + this.mod.getVersion() + "§8] §acommands (alias /channelfilter)", false);
			String[] commands = {"ignore §7- §aSees the list of currently ignored factions.",
					"ignore <faction> §7- §aAdds or removes a faction from ignore list.",
					"ignore wilderness §7- §aIgnores players without a faction.",
					"ignore clear §7- §aClears the ignore list.",
			"help §7- §aDisplays this help message. Herpaderp."};
			for (String command: commands)
				this.mod.logMessage("/cf " + command, false);
			this.mod.logMessage("§2Wiki:§a https://github.com/Kyzderp/ChannelFilterMod/wiki", false);
		} // help
		else if (tokens[1].equalsIgnoreCase("only"))
		{
			if (tokens.length > 2 && tokens[2].equalsIgnoreCase("clear"))
			{
				this.mod.onlyRegex = "HerpDerpThisWontBeUsed";
				this.mod.logMessage("onlyRegex cleared.", true);
				return;
			}
			this.mod.onlyRegex = message.replaceFirst("/cf only |/channelfilter only", "");
			this.mod.logMessage("onlyRegex set to: " + this.mod.onlyRegex, true);
		} // only
		else if (tokens[1].equalsIgnoreCase("ignore"))
		{
			if (tokens.length == 2)
			{
				if (this.mod.ignoredFacs.isEmpty())
				{
					this.mod.logMessage("Not currently ignoring any factions.", true);
					return ;
				}
				String result = "Currently ignoring faction(s):";
				if (this.mod.ignoreWildy)
					result += " Wilderness,";
				for (String fac: this.mod.ignoredFacs)
					result += " " + fac + ",";
				this.mod.logMessage(result.substring(0, result.length() - 1), true);
				return;
			} // cf ignore
			
			else if (tokens[2].equalsIgnoreCase("clear"))
			{
				this.mod.ignoredFacs.clear();
				this.mod.ignoreWildy = false;
				this.mod.logMessage("Cleared ignore list.", true);
				return;
			} // cf ignore clear
			
			else if (tokens[2].equalsIgnoreCase("wilderness"))
			{
				if (this.mod.ignoreWildy)
				{
					this.mod.ignoreWildy = false;
					this.mod.logMessage("No longer ignoring players without a faction.", true);
					return;
				}
				this.mod.ignoreWildy = true;
				this.mod.logMessage("Now ignoring players without a faction.", true);
				return;
			} // cf ignore wilderness
			
			else if (tokens.length > 3)
			{
				this.mod.logError("Too many parameters! See /cf help for usage information");
				return;
			} // too many args
			
			else
			{
				String fac = tokens[2].toLowerCase();
				if (!fac.matches("[0-9A-Za-z]+"))
				{
					this.mod.logError("Invalid faction name; must be alphanumeric: " + fac);
					return;
				}
				if (this.mod.ignoredFacs.contains(fac))
				{
					this.mod.ignoredFacs.remove(fac);
					this.mod.logMessage("No longer ignoring faction " + fac, true);
				}
				else
				{
					this.mod.ignoredFacs.addFirst(fac);
					this.mod.logMessage("Now ignoring faction " + fac, true);
				}
			} // cf ignore <fac>
		}
		else
			this.mod.logError("Invalid parameters. See /cf help for usage information");

		this.mod.ignoredRegex = "";
		if (this.mod.ignoredFacs.size() > 0)
		{
			for (String fac: this.mod.ignoredFacs)
				this.mod.ignoredRegex += fac + "|";
			this.mod.ignoredRegex = this.mod.ignoredRegex.substring(0, this.mod.ignoredRegex.length() - 1);
		}
	}
}
