package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import com.telegram.bot.utils.Jedis
import com.telegram.bot.utils.Jedis.incrementCurrentPage
import com.telegram.bot.utils.MessageType
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import javax.inject.Named

@Component
@Named(Commands.PREVIOUS_PAGE)
class PreviousPageCommand : Command {
    override fun description(): String {
        return "Shows previous page of friends list"
    }

    override fun nextState(userId: Long): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun execute(user: UserDTO, message: String): ClientResponseDTO {
        return ClientResponseDTO(getMessage(user, message), getButtons(user.id), MessageType.EDIT)
    }

    override fun getMessage(user: UserDTO, message: String): String {
        Jedis.get().incrementCurrentPage(user.id, -1)
        return CommandsMap.get(Commands.LIST_FRIENDS).getMessage(user, message)
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        return CommandsMap.get(Commands.LIST_FRIENDS).getButtons(userId)
    }
}