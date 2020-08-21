package com.meisolsson.githubsdk.core;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

public class CaseInsensitiveEnumJsonAdapter<T extends Enum<T>> extends JsonAdapter<T> {
    private final Class<T> enumType;
    private final String[] nameStrings;
    private final T[] constants;

    public CaseInsensitiveEnumJsonAdapter(Class<T> enumType) {
        this.enumType = enumType;
        try {
            constants = enumType.getEnumConstants();
            nameStrings = new String[constants.length];
            for (int i = 0; i < constants.length; i++) {
                T constant = constants[i];
                Json annotation = enumType.getField(constant.name()).getAnnotation(Json.class);
                String name = annotation != null ? annotation.name() : constant.name();
                nameStrings[i] = name;
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Missing field in " + enumType.getName(), e);
        }
    }

    @Override public T fromJson(JsonReader reader) throws IOException {
        if (reader.peek() != JsonReader.Token.STRING) {
            throw new JsonDataException("Expected a string value at path "
                    + reader.getPath() + " but got " + reader.peek());
        }

        String value = reader.nextString();
        for (int i = 0; i < nameStrings.length; i++) {
            if (nameStrings[i].compareToIgnoreCase(value) == 0) {
                return constants[i];
            }
        }

        throw new JsonDataException("Expected one of "
                + Arrays.asList(nameStrings) + " but was " + value + " at path "
                + reader.getPath());
    }

    @Override public void toJson(JsonWriter writer, T value) throws IOException {
        writer.value(nameStrings[value.ordinal()]);
    }
}