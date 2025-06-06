package com.tashila.hazle.db

import com.tashila.hazle.features.chat.Message

// Convert Domain Message to Data Layer MessageEntity
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        text = this.text,
        isFromMe = this.isFromMe,
        timestamp = this.timestamp
    )
}

// Convert Data Layer MessageEntity to Domain Message
fun MessageEntity.toDomain(): Message {
    return Message(
        id = this.id,
        text = this.text,
        isFromMe = this.isFromMe,
        timestamp = this.timestamp
    )
}