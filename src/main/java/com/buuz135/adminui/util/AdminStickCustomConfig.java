package com.buuz135.adminui.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class AdminStickCustomConfig extends BlockingDiskFile {

    public HashMap<UUID, Config> config;

    public AdminStickCustomConfig() {
        super(Path.of("AdminUI/AdminStickConfiguration.json"));
        this.config = new HashMap<>();
    }

    @Override
    protected void read(BufferedReader bufferedReader) throws IOException {
        JsonParser.parseReader(bufferedReader).getAsJsonArray().forEach((entry) -> {
            JsonObject object = entry.getAsJsonObject();
            try {
                this.config.put(UUID.fromString(object.get("uuid").getAsString()),
                        new Config(object.get("ability1").getAsString(), object.get("ability2").getAsString(), object.get("ability3").getAsString(), object.get("primary").getAsString(), object.get("secondary").getAsString(), object.get("pick").getAsString()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse player tracker!", e);
            }
        });
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonArray array = new JsonArray();
        this.config.entrySet().forEach(configEntry -> {
            var object = new JsonObject();
            object.addProperty("uuid", configEntry.getKey().toString());
            object.addProperty("ability1", configEntry.getValue().ability1);
            object.addProperty("ability2", configEntry.getValue().ability2);
            object.addProperty("ability3", configEntry.getValue().ability3);
            object.addProperty("primary", configEntry.getValue().primary);
            object.addProperty("secondary", configEntry.getValue().secondary);
            object.addProperty("pick", configEntry.getValue().pick);
            array.add(object);
        });
        bufferedWriter.write(array.toString());
    }

    public void addNewPlayer(UUID uuid){
        this.addPlayer(uuid, new Config("","player", "server", "", "", ""));
    }

    public void addPlayer(UUID uuid, Config config){
        this.fileLock.writeLock().lock();
        this.config.put(uuid, config);
        this.fileLock.writeLock().unlock();
        this.syncSave();
    }

    public Config getPlayer(UUID uuid){
        if (!this.config.containsKey(uuid)) addNewPlayer(uuid);
        return this.config.get(uuid);
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        try (JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            jsonWriter.beginArray().endArray();
        }
    }

    public static  class Config {
        private  String ability1;
        private  String ability2;
        private  String ability3;
        private  String primary;
        private  String secondary;
        private  String pick;

        public Config(String ability1, String ability2, String ability3, String primary, String secondary, String pick) {
            this.ability1 = ability1;
            this.ability2 = ability2;
            this.ability3 = ability3;
            this.primary = primary;
            this.secondary = secondary;
            this.pick = pick;
        }

        public String ability1() {
            return ability1;
        }

        public String ability2() {
            return ability2;
        }

        public String ability3() {
            return ability3;
        }

        public String primary() {
            return primary;
        }

        public String secondary() {
            return secondary;
        }

        public String pick() {
            return pick;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Config) obj;
            return Objects.equals(this.ability1, that.ability1) &&
                    Objects.equals(this.ability2, that.ability2) &&
                    Objects.equals(this.ability3, that.ability3) &&
                    Objects.equals(this.primary, that.primary) &&
                    Objects.equals(this.secondary, that.secondary) &&
                    Objects.equals(this.pick, that.pick);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ability1, ability2, ability3, primary, secondary, pick);
        }

        @Override
        public String toString() {
            return "Config[" +
                    "ability1=" + ability1 + ", " +
                    "ability2=" + ability2 + ", " +
                    "ability3=" + ability3 + ", " +
                    "primary=" + primary + ", " +
                    "secondary=" + secondary + ", " +
                    "pick=" + pick + ']';
        }

        public void setAbility1(String ability1) {
            this.ability1 = ability1;
        }

        public void setAbility2(String ability2) {
            this.ability2 = ability2;
        }

        public void setAbility3(String ability3) {
            this.ability3 = ability3;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }

        public void setSecondary(String secondary) {
            this.secondary = secondary;
        }

        public void setPick(String pick) {
            this.pick = pick;
        }
    }
}
