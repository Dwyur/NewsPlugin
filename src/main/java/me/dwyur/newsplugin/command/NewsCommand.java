package me.dwyur.newsplugin.command;

import me.dwyur.newsplugin.NewsPlugin;
import me.dwyur.newsplugin.book.impl.NewsBook;
import me.dwyur.newsplugin.database.player.DataPlayerService;
import me.dwyur.newsplugin.vk.models.PostService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;

public class NewsCommand implements CommandExecutor {

    private final ConfigurationSection config;
    private final PostService postService;
    private final DataPlayerService playerService;

    public NewsCommand(NewsPlugin plugin) {
        this.config = plugin.getConfig().getConfigurationSection("messages");
        this.postService = plugin.getPostService();
        this.playerService = plugin.getDataPlayerService();

        Objects.requireNonNull(plugin.getCommand("news")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(config.getString("only-players"));
            return false;
        }

        Player player = (Player) commandSender;

        postService.getCachedLatestPost().thenAccept(post -> {
            if (post.getId() == 0) {
                player.sendMessage(config.getString("no-news"));
                return;
            }

            player.sendMessage(config.getString("open-news"));

            NewsBook newsBook = new NewsBook(playerService);
            newsBook.openNews(player, post, postService.getVkManager().getMaxLength());
        });

        return true;
    }
}
