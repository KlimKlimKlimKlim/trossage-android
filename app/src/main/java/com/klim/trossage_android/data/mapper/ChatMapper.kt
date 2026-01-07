package com.klim.trossage_android.data.mapper

import com.klim.trossage_android.data.local.room.entity.ChatEntity
import com.klim.trossage_android.data.local.room.entity.MessageEntity
import com.klim.trossage_android.data.remote.dto.ChatDto
import com.klim.trossage_android.data.remote.dto.ChatResponse
import com.klim.trossage_android.data.remote.dto.MessageResponse
import com.klim.trossage_android.data.remote.dto.UserResponse
import com.klim.trossage_android.data.util.DateUtils
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.model.User

object ChatMapper {

    fun toChat(dto: ChatResponse): Chat {
        return Chat(
            chatId = dto.id,
            companionUserId = dto.otherUser.id,
            companionDisplayName = dto.otherUser.displayName,
            lastMessage = dto.lastMessage?.text,
            lastMessageSenderName = dto.lastMessage?.sender?.displayName,
            lastMessageTimestamp = DateUtils.parseIsoToMillis(dto.lastMessage?.createdAt),
            createdAt = DateUtils.parseIsoToMillis(dto.createdAt)
        )
    }

    fun toChatOld(dto: ChatDto): Chat {
        return Chat(
            chatId = dto.chatId.toIntOrNull() ?: 0,
            companionUserId = dto.companionUserId.toIntOrNull() ?: 0,
            companionDisplayName = dto.companionDisplayName,
            lastMessage = dto.lastMessage,
            lastMessageSenderName = null,
            lastMessageTimestamp = dto.lastMessageTimestamp,
            createdAt = 0L
        )
    }

    fun toMessage(dto: MessageResponse, currentUserId: Int, senderName: String): Message {
        return Message(
            messageId = dto.id,
            chatId = dto.chatId,
            senderId = dto.senderId,
            senderDisplayName = senderName,
            text = dto.text,
            timestamp = DateUtils.parseIsoToMillis(dto.createdAt),
            isMine = dto.senderId == currentUserId
        )
    }

    fun toUser(dto: UserResponse): User {
        return User(
            userId = dto.id.toString(),
            username = dto.login,
            displayName = dto.displayName
        )
    }

    fun toChatEntity(chat: Chat): ChatEntity {
        return ChatEntity(
            id = chat.chatId,
            otherUserId = chat.companionUserId,
            otherUserName = chat.companionDisplayName,
            lastMessageText = chat.lastMessage,
            lastMessageSenderName = chat.lastMessageSenderName,
            lastMessageTimestamp = chat.lastMessageTimestamp,
            createdAt = chat.createdAt
        )
    }

    fun fromChatEntity(entity: ChatEntity): Chat {
        return Chat(
            chatId = entity.id,
            companionUserId = entity.otherUserId,
            companionDisplayName = entity.otherUserName,
            lastMessage = entity.lastMessageText,
            lastMessageSenderName = entity.lastMessageSenderName,
            lastMessageTimestamp = entity.lastMessageTimestamp,
            createdAt = entity.createdAt
        )
    }

    fun toMessageEntity(message: Message): MessageEntity {
        return MessageEntity(
            id = message.messageId,
            chatId = message.chatId,
            senderId = message.senderId,
            senderName = message.senderDisplayName,
            text = message.text,
            timestamp = message.timestamp,
            isMine = message.isMine
        )
    }

    fun fromMessageEntity(entity: MessageEntity): Message {
        return Message(
            messageId = entity.id,
            chatId = entity.chatId,
            senderId = entity.senderId,
            senderDisplayName = entity.senderName,
            text = entity.text,
            timestamp = entity.timestamp,
            isMine = entity.isMine
        )
    }
}
