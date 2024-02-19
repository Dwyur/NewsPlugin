package me.dwyur.newsplugin;

import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.dwyur.newsplugin.book.impl.NewsBook;
import me.dwyur.newsplugin.command.NewsCommand;
import me.dwyur.newsplugin.database.DataPlayerLoader;
import me.dwyur.newsplugin.database.player.DataPlayerService;
import me.dwyur.newsplugin.listener.PlayerListener;
import me.dwyur.newsplugin.vk.VkManager;
import me.dwyur.newsplugin.vk.models.Post;
import me.dwyur.newsplugin.vk.models.PostService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldDefaults(level = PRIVATE)
public final class NewsPlugin extends JavaPlugin {

    DataPlayerService dataPlayerService;
    PostService postService;
    VkManager vkManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataPlayerService = new DataPlayerService(getLoader());
        vkManager = new VkManager(getConfig().getConfigurationSection("vk"));
        postService = new PostService(vkManager);

        new PlayerListener(this);
        new NewsCommand(this);

        startScheduler();
    }

    DataPlayerLoader getLoader() {
        val config = getConfig().getConfigurationSection("database");

        assert config != null;
        return new DataPlayerLoader(
                config.getString("host"),
                config.getString("password"),
                config.getString("username"),
                config.getString("database"),
                config.getInt("port")
        );
    }

    Post lastChecked;

    void startScheduler() { // мне похуй, я панк
        getServer().getScheduler().runTaskTimer(this,() -> postService.getLatestPost().thenAccept(post -> {
            if (lastChecked == null) {
                lastChecked = post;
                return;
            }

            if (post.getId() == 0 || post.getId() == lastChecked.getId())
                return;

            lastChecked = post;
            postService.setLatestPost(post);

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(getConfig().getString("messages.news"));

                NewsBook newsBook = new NewsBook(dataPlayerService);

                getServer().getScheduler().runTask(this, () -> newsBook.openNews(player, post, vkManager.getMaxLength())); // в основном потоке
            });
        }), 0L, vkManager.getUpdateTime() * 20L);
    }
}
