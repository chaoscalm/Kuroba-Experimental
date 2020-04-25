package com.github.adamantcheese.model.mapper

import com.github.adamantcheese.model.data.serializable.spans.SerializableSpanInfoList
import com.github.adamantcheese.model.data.serializable.spans.SerializableSpannableString
import com.github.adamantcheese.model.entity.ChanTextSpanEntity
import com.google.gson.Gson

object TextSpanMapper {

    fun toEntity(
            gson: Gson,
            ownerPostId: Long,
            serializableSpannableString: SerializableSpannableString,
            chanTextType: ChanTextSpanEntity.TextType
    ): ChanTextSpanEntity? {
        if (serializableSpannableString.isEmpty) {
            return null
        }

        val spanInfoJson = gson.toJson(
                SerializableSpanInfoList(serializableSpannableString.spanInfoList)
        )

        return ChanTextSpanEntity(
                0L,
                ownerPostId,
                serializableSpannableString.text,
                spanInfoJson,
                chanTextType
        )
    }

    fun fromEntity(
            gson: Gson,
            chanTextSpanEntityList: List<ChanTextSpanEntity>?,
            chanTextType: ChanTextSpanEntity.TextType
    ): SerializableSpannableString? {
        if (chanTextSpanEntityList == null || chanTextSpanEntityList.isEmpty()) {
            return null
        }

        val filteredTextSpanEntityList = chanTextSpanEntityList.filter { textSpanEntity ->
            textSpanEntity.textType == chanTextType
        }

        if (filteredTextSpanEntityList.isEmpty()) {
            return null
        }

        if (filteredTextSpanEntityList.size > 1) {
            throw IllegalStateException(
                    "Expected one (or zero) TextSpanEntity with type (${chanTextType.name}). " +
                            "Got ${filteredTextSpanEntityList.size}."
            )
        }

        val textSpanEntity = filteredTextSpanEntityList.first()

        val serializableSpanInfoList = gson.fromJson(
                textSpanEntity.spanInfoJson,
                SerializableSpanInfoList::class.java
        )

        return SerializableSpannableString(
                serializableSpanInfoList.spanInfoList,
                textSpanEntity.originalText
        )
    }

}