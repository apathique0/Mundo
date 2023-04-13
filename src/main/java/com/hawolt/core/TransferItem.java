package com.hawolt.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class TransferItem {
    private final long itemId, releaseDate, rp;
    private final String inventoryType, name, destinationPlatform, iconUrl;
    private final boolean purchaseLimitReached, hasVelocityRules;
    private final JSONArray tags;
    private String type;
    private long ip;


    public TransferItem(JSONObject object) {
        if (object.has("type")) this.type = object.getString("type");
        if (object.has("ip")) this.ip = object.getLong("ip");
        this.rp = object.getLong("rp");
        this.itemId = object.getLong("itemId");
        this.inventoryType = object.getString("inventoryType");
        this.releaseDate = object.getLong("releaseDate");
        this.name = object.getString("name");
        this.destinationPlatform = object.getString("destinationPlatform");
        this.iconUrl = object.getString("iconUrl");
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

    public long getRP() {
        return rp;
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
        String currency = String.format("%s %s", rp, "RP");
        String item = String.format("[%s:%s]", itemId, name);
        return String.join(" - ", currency, item);
    }

}
