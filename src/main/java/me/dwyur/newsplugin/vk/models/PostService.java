package me.dwyur.newsplugin.vk.models;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.dwyur.newsplugin.utility.StringUtils;
import me.dwyur.newsplugin.vk.VkManager;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor
public class PostService {

    @Getter
    final VkManager vkManager;
    long lastTime;

    @Setter
    Post latestPost;

    public CompletableFuture<Post> getCachedLatestPost() {
        if (latestPost == null || (lastTime + vkManager.getUpdateTime()) >= System.currentTimeMillis()) {
            return getLatestPost().thenApply(value -> {
                lastTime = System.currentTimeMillis();
                latestPost = value;
                return latestPost;
            });
        }

        return CompletableFuture.completedFuture(latestPost);
    }

    public CompletableFuture<Post> getLatestPost() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                val response = vkManager.getApiClient().wall().get(vkManager.getActor())
                        .domain(vkManager.getDomain())
                        .count(2) // учитывая закрепленные посты
                        .execute();

                val posts = response.getItems();

                if (posts.isEmpty()) {
                    return new Post(0, 0, "");
                }

                posts.sort(Comparator.comparing(WallpostFull::getDate).reversed());

                val post = posts.get(0);

                if (post.getText() == null) {
                    return new Post(0, 0, "");
                }

                String postText = post.getText();

                if (vkManager.isFormatText()) {
                    postText = StringUtils.removeUnsupportedCharacters(post.getText());
                }

                return new Post(post.getId(), post.getDate(), postText);
            } catch (ApiException | ClientException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
