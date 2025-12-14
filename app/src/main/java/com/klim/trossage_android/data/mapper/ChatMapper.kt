package com.klim.trossage_android.data.mapper

import com.klim.trossage_android.data.remote.dto.ChatDto
import com.klim.trossage_android.data.remote.dto.MessageDto
import com.klim.trossage_android.data.remote.dto.UserDto
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.model.User

object ChatMapper {

    fun toChat(dto: ChatDto): Chat {
        return Chat(
            chatId = dto.chatId,
            companionUserId = dto.companionUserId,
            companionDisplayName = dto.companionDisplayName,
            lastMessage = dto.lastMessage,
            lastMessageTimestamp = dto.lastMessageTimestamp,
            isRead = dto.isRead
        )
    }

    fun toMessage(dto: MessageDto, currentUserId: String): Message {
        return Message(
            messageId = dto.messageId,
            chatId = dto.chatId,
            senderId = dto.senderId,
            senderDisplayName = dto.senderDisplayName,
            text = dto.text,
            timestamp = dto.timestamp,
            isMine = dto.senderId == currentUserId
        )
    }

    fun toUser(dto: UserDto): User {
        return User(
            userId = dto.id.toString(),        // ← Int → String
            username = dto.login,               // ← login → username
            displayName = dto.displayName
        )
    }
}
