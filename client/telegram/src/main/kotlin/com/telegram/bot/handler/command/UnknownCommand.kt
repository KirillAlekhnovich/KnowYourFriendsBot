package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.utils.Commands
import org.springframework.stereotype.Component
import javax.inject.Named

/**
 * Class that handles unknown command.
 */
@Component
@Named(Commands.UNKNOWN)
class UnknownCommand : Command {
    override fun description(): String {
        return "Unknown command"
    }

    override fun nextState(userId: Long): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun getMessage(user: UserDTO, message: String): String {
        return "I don't know this command. You can check available commands by typing /help"
    }
}