package com.mrgoodcat.hitmeup.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

@ProvidedTypeConverter
class TypeRoomConverter @Inject constructor(val moshi: Moshi) {

    @TypeConverter
    fun fromStringMapMapToString(map: Map<String, Map<String, String>>): String {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Map::class.java,
            String::class.java,
            String::class.java,
        )
        val adapter: JsonAdapter<Map<String, Map<String, String>>> = moshi.adapter(type)
        return adapter.toJson(map)
    }

    @TypeConverter
    fun fromStringToMapMapString(value: String): Map<String, Map<String, String>> {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Map::class.java,
            String::class.java,
            String::class.java,
        )
        val adapter: JsonAdapter<Map<String, Map<String, String>>> = moshi.adapter(type)
        return adapter.fromJson(value)!!
    }

    @TypeConverter
    fun fromStringToMapBoolean(value: String): Map<String, Boolean> {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Boolean::class.javaObjectType,
        )
        val adapter: JsonAdapter<Map<String, Boolean>> = moshi.adapter(type)
        return adapter.fromJson(value)!!
    }

    @TypeConverter
    fun fromMapBooleanToString(map: Map<String, Boolean>): String {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Boolean::class.javaObjectType,
        )
        val adapter: JsonAdapter<Map<String, Boolean>> = moshi.adapter(type)
        return adapter.toJson(map)!!
    }

    @TypeConverter
    fun fromMessageContentToString(content: Map<String, String>): String {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java,
        )
        val adapter: JsonAdapter<Map<String, String>> = moshi.adapter(type)
        return adapter.toJson(content)!!
    }

    @TypeConverter
    fun fromStringToMessageContent(string: String): Map<String, String> {
        val type: ParameterizedType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java,
        )
        val adapter: JsonAdapter<Map<String, String>> = moshi.adapter(type)
        return adapter.fromJson(string)!!
    }
}