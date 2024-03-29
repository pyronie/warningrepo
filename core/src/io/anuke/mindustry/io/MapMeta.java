package io.anuke.mindustry.io;

import com.badlogic.gdx.utils.ObjectMap;

public class MapMeta {
    public final int version;
    public final ObjectMap<String, String> tags;
    public final int width, height;

    public MapMeta(int version, ObjectMap<String, String> tags, int width, int height) {
        this.version = version;
        this.tags = tags;
        this.width = width;
        this.height = height;
    }

    public String author(){
        return tags.get("author", "unknown");
    }

    public String description(){
        return tags.get("description", "unknown");
    }

    public String name(){
        return tags.get("name", "unknown");
    }

    public boolean hasOreGen(){
        return tags.get("oregen", "1").equals("1");
    }
}
