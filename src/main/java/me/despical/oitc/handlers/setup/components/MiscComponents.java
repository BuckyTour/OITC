/*
 * OITC - Kill your opponents and reach 25 points to win!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.oitc.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.conversation.ConversationBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.oitc.ConfigPreferences;
import me.despical.oitc.arena.Arena;
import me.despical.oitc.handlers.setup.SetupInventory;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 02.07.2020
 */
public class MiscComponents implements SetupComponent {

	@Override
	public void registerComponent(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		ItemStack bungeeItem;

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			bungeeItem = new ItemBuilder(XMaterial.OAK_SIGN)
				.name("&e&lAdd Game Sign")
				.lore("&7Target a sign and click this.")
				.lore("&8(this will set target sign as game sign)")
				.build();
		} else {
			bungeeItem = new ItemBuilder(XMaterial.BARRIER)
				.name("&c&lAdd Game Sign")
				.lore("&7Option disabled in bungee cord mode.")
				.lore("&8Bungee mode is meant to be one arena per server")
				.lore("&8If you wish to have multi arena, disable bungee in config!")
				.build();
		}

		pane.addItem(GuiItem.of(bungeeItem, e -> {
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				return;
			}

			player.closeInventory();
			Block block = player.getTargetBlock(null, 10);

			if (!(block.getState() instanceof Sign)) {
				player.sendMessage(chatManager.prefixedMessage("commands.look_sign"));
				return;
			}

			if (block.getLocation().distance(player.getWorld().getSpawnLocation()) <= plugin.getServer().getSpawnRadius() && e.getClick() != ClickType.SHIFT_LEFT) {
				player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Server spawn protection is set to &6" + plugin.getServer().getSpawnRadius() + " &cand sign you want to place is in radius of this protection! &c&lNon OP players won't be able to interact with this sign and can't join the game so."));
				player.sendMessage(chatManager.coloredRawMessage("&cYou can ignore this warning and add sign with Shift + Left Click, but for now &c&loperation is cancelled"));
				return;
			}

			plugin.getSignManager().addArenaSign(block, arena);

			player.sendMessage(chatManager.prefixedMessage("signs.sign_created"));

			List<String> locations = config.getStringList("instances." + arena.getId() + ".signs");
			locations.add(LocationSerializer.toString(block.getLocation()));

			config.set("instances." + arena.getId() + ".signs", locations);
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 6, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.NAME_TAG)
			.name("&e&lSet Map Name")
			.lore("&7Click to set arena map name")
			.lore("", "&a&lCurrently: &e" + config.getString("instances." + arena.getId() + ".mapName"))
			.build(), e -> {

			player.closeInventory();

			new ConversationBuilder(plugin).withPrompt(new StringPrompt() {

				@Override
				@NotNull
				public String getPromptText(@NotNull ConversationContext context) {
					return chatManager.prefixedRawMessage("&ePlease type in chat arena name. You can use color codes.");
				}

				@Override
				public Prompt acceptInput(@NotNull ConversationContext context, String input) {
					String name = chatManager.coloredRawMessage(input);
					player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aName of arena &e" + arena.getId() + " &aset to &e" + name));
					arena.setMapName(name);

					config.set("instances." + arena.getId() + ".mapName", arena.getMapName());
					ConfigUtils.saveConfig(plugin, config, "arenas");

					new SetupInventory(plugin, arena, player).openInventory(false);
					return Prompt.END_OF_CONVERSATION;
				}
			}).buildFor(player);
		}), 7, 1);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP)
			.name("&e&lView Wiki Page")
			.lore("&7Having problems with setup or wanna")
			.lore("&7know some useful tips? Click to get wiki link!")
			.build(), e -> {

			player.closeInventory();
			player.sendMessage(chatManager.prefixedRawMessage("&7Check out our wiki: https://github.com/Despical/OITC/wiki"));
		}), 6, 3);
	}
}