package com.github.ai.simplesplit.android.data.json

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.model.exception.ParsingException
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

class JsonSerializer {

    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    inline fun <reified T> deserialize(text: String): Either<ParsingException, T> =
        either {
            try {
                gson.fromJson(text, T::class.java)
            } catch (exception: JsonSyntaxException) {
                raise(ParsingException(cause = exception))
            }
        }

    inline fun <reified T> serialize(data: T): String {
        return gson.toJson(data)
    }
}