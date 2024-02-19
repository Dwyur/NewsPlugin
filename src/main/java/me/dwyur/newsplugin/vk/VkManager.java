package me.dwyur.newsplugin.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.ConfigurationSection;

import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class VkManager {

    int updateTime;
    String domain;
    boolean formatText;
    int maxLength;
    UserActor actor;
    VkApiClient apiClient;

    public VkManager(ConfigurationSection config) {
        this.updateTime = config.getInt("updater");
        this.actor = new UserActor(config.getInt("userId"), config.getString("accessToken"));
        this.domain = config.getString("domain");
        this.formatText = config.getBoolean("formatText");
        this.apiClient = new VkApiClient(HttpTransportClient.getInstance());
        this.maxLength = config.getInt("maxLength");
    }
}
