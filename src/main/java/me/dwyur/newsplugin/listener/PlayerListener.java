package me.dwyur.newsplugin.listener;

import lombok.experimental.FieldDefaults;
import lombok.val;
import me.dwyur.newsplugin.NewsPlugin;
import me.dwyur.newsplugin.book.BookUtil;
import me.dwyur.newsplugin.book.impl.NewsBook;
import me.dwyur.newsplugin.database.player.DataPlayerService;
import me.dwyur.newsplugin.vk.models.PostService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PlayerListener implements Listener {
    NewsPlugin plugin;
    PostService postService;
    DataPlayerService playerService;

    public PlayerListener(NewsPlugin plugin) {
        this.plugin = plugin;
        this.playerService = plugin.getDataPlayerService();
        this.postService = plugin.getPostService();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        val player = e.getPlayer();
        val name = player.getName();

        playerService.getDataPlayer(name)
                .thenAccept(dataPlayer -> postService.getCachedLatestPost()
                        .thenAccept(post -> Bukkit.getScheduler().runTask(plugin, () -> {

                            if (post.getId() == 0 || dataPlayer.getLastPostId() == post.getId()) {
                                return;
                            }

                            NewsBook newsBook = new NewsBook(playerService);
                            newsBook.openNews(player, post, postService.getVkManager().getMaxLength());
                        })));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerService.invalidateDataPlayer(e.getPlayer().getName());
    }
}
