package com.buuz135.adminui.util;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;

import javax.annotation.Nullable;import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTracker extends BlockingDiskFile {

    private List<PlayerTracker.Players> playerTrackerList;

    public PlayerTracker() {
        super(Path.of("AdminUI/PlayerTracker.json"));
        this.playerTrackerList = new ArrayList<>();
    }

    @Override
    protected void read(BufferedReader bufferedReader) throws IOException {
        JsonParser.parseReader(bufferedReader).getAsJsonArray().forEach((entry) -> {
            JsonObject object = entry.getAsJsonObject();
            try {
                this.playerTrackerList.add(new PlayerTracker.Players(object.get("name").getAsString(), UUID.fromString(object.get("uuid").getAsString()), Instant.parse(object.get("lastSeen").getAsString())));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse player tracker!", e);
            }
        });
    }

    public void addPlayer(String name, UUID uuid){
        this.fileLock.writeLock().lock();
        this.playerTrackerList.removeIf(player -> player.uuid.equals(uuid));
        this.playerTrackerList.add(new PlayerTracker.Players(name, uuid, Instant.now()));
        this.fileLock.writeLock().unlock();
        this.syncSave();
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonArray array = new JsonArray();
        this.playerTrackerList.forEach(playerTracker -> {
            var object = new JsonObject();
            object.addProperty("name", playerTracker.name);
            object.addProperty("uuid", playerTracker.uuid.toString());
            object.addProperty("lastSeen", playerTracker.lastSeen.toString());
            array.add(object);
        });
        bufferedWriter.write(array.toString());
    }

    @Nullable
    public PlayerTracker.Players getPlayer(UUID uuid){
        return this.playerTrackerList.stream().filter(player -> player.uuid.equals(uuid)).findFirst().orElse(null);
    }

    @Nullable
    public PlayerTracker.Players getPlayer(String playerName){
        return this.playerTrackerList.stream().filter(player -> player.name.equals(playerName)).findFirst().orElse(null);
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            jsonWriter.beginArray().endArray();
        }
    }

    public record Players(String name, UUID uuid, Instant lastSeen){

    }
}
