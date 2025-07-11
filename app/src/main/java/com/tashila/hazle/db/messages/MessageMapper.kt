package com.tashila.hazle.db.messages

import com.tashila.hazle.features.chat.Message

// Convert Domain Message to Data Layer MessageEntity
fun Message.toEntity(localThreadId: Long): MessageEntity {
    return MessageEntity(
        id = this.id,
        text = this.text,
        isFromMe = this.isFromMe,
        timestamp = this.timestamp,
        aiThreadId = this.aiThreadId,
        localThreadId = localThreadId,
    )
}

// Convert Data Layer MessageEntity to Domain Message
fun MessageEntity.toDomain(): Message {
    return Message(
        id = this.id,
        text = this.text,
        isFromMe = this.isFromMe,
        timestamp = this.timestamp,
        aiThreadId = this.aiThreadId
    )
}