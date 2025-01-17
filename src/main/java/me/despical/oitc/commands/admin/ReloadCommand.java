package me.despical.oitc.commands.admin;

import me.despical.commons.util.LogUtils;
import me.despical.oitc.arena.ArenaManager;
import me.despical.oitc.arena.ArenaRegistry;
import me.despical.oitc.commands.SubCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2022
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super ("reload");

		setPermission("oitc.admin.reload");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		LogUtils.log("Initiated plugin reload by {0}", sender.getName());
		long start = System.currentTimeMillis();

		plugin.reloadConfig();
		chatManager.reloadConfig();

		ArenaRegistry.getArenas().forEach(arena -> ArenaManager.stopGame(true, arena));
		ArenaRegistry.registerArenas();

		sender.sendMessage(chatManager.prefixedMessage("commands.admin_commands.success_reload"));

		LogUtils.log("[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public String getTutorial() {
		return "Reloads all of the system configuration and arenas";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return BOTH;
	}
}