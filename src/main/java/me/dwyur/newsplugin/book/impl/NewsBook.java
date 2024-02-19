package me.dwyur.newsplugin.book.impl;

import lombok.RequiredArgsConstructor;
import me.dwyur.newsplugin.NewsPlugin;
import me.dwyur.newsplugin.book.BookUtil;
import me.dwyur.newsplugin.database.player.DataPlayerService;
import me.dwyur.newsplugin.utility.StringUtils;
import me.dwyur.newsplugin.vk.models.Post;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

@RequiredArgsConstructor
public class NewsBook {

    private final DataPlayerService service;

    public void openNews(Player player, Post post, int maxLength) {
        BookUtil.openPlayer(player,
                BookUtil.writtenBook()
                        .author("Dwyur")
                        .title("News book")
                        .pagesRaw(StringUtils.splitStringByLength(post.getText(), maxLength))
                        .generation(BookMeta.Generation.ORIGINAL)
                        .build()
        );


        service.getDataPlayer(player.getName())
                .thenAccept(dataPlayer -> service.setLastPostId(dataPlayer, post.getId()));
    }
}
