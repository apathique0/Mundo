package com.hawolt.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class TransferItem {
    private final long itemId, releaseDate, ip;
    private final String inventoryType, name, destinationPlatform, iconUrl, type;
    private final boolean purchaseLimitReached, hasVelocityRules;
    private final JSONArray tags;


    public TransferItem(JSONObject object) {
        this.itemId = object.getLong("itemId");
        this.inventoryType = object.getString("inventoryType");
        this.releaseDate = object.getLong("releaseDate");
        this.ip = object.getLong("ip");
        this.name = object.getString("name");
        this.destinationPlatform = object.getString("destinationPlatform");
        this.iconUrl = object.getString("iconUrl");
        this.type = object.getString("type");
        this.tags = object.getJSONArray("tags");
        this.purchaseLimitReached = object.getBoolean("purchaseLimitReached");
        this.hasVelocityRules = object.getBoolean("hasVelocityRules");
    }

    public long getItemId() {
        return itemId;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public long getIp() {
        return ip;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public String getName() {
        return name;
    }

    public String getDestinationPlatform() {
        return destinationPlatform;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getType() {
        return type;
    }

    public JSONArray getTags() {
        return tags;
    }

    public boolean isPurchaseLimitReached() {
        return purchaseLimitReached;
    }

    public boolean isHasVelocityRules() {
        return hasVelocityRules;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, releaseDate, ip, inventoryType, name, destinationPlatform, iconUrl, type, tags, purchaseLimitReached, hasVelocityRules);
    }

    @Override
    public String toString() {
        String currency = String.format("%s %s", ip, "BE");
        String item = String.format("[%s:%s]", itemId, name);
        return String.join(" - ", currency, item);
    }

}
