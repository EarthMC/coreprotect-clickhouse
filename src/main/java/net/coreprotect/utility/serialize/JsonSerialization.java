package net.coreprotect.utility.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerialization {
    public static final Gson DEFAULT_GSON = new Gson();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BannerData.class, new BannerData.Serializer())
            .registerTypeAdapter(SerializedBlockMeta.class, new SerializedBlockMeta.Serializer())
            .create();
}
