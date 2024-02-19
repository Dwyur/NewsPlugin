package me.dwyur.newsplugin.database.player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.dwyur.newsplugin.database.DataPlayerLoader;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DataPlayerService {

    private final DataPlayerLoader loader;
    private final LoadingCache<String, CompletableFuture<DataPlayer>> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, CompletableFuture<DataPlayer>>() {
                @Override
                public CompletableFuture<DataPlayer> load(String name) {
                    return loader.getDataPlayer(name);
                }
            });

    @SneakyThrows
    public CompletableFuture<DataPlayer> getDataPlayer(String name) {
        return cache.get(name);
    }

    public void setLastPostId(DataPlayer player, long postId) {
        loader.setLastPostId(player, postId);
        cache.refresh(player.getName());
    }

    public void invalidateDataPlayer(String name) {
        cache.invalidate(name);
    }
}
