package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.CommandsMap
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.HELP)
class HelpCommand : Command {

    private val notListedCommands = listOf(
        Commands.START, Commands.HELP, Commands.UNKNOWN,
        Commands.STORAGE, Commands.PREVIOUS_PAGE, Commands.NEXT_PAGE
    )

    override fun description(): String {
        return "Shows a list of all available commands"
    }

    override fun nextState(userId: Long): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun getMessage(user: UserDTO, message: String): String {
        val commandsWithDescription = CommandsMap.getDefaultCommands().filter { !notListedCommands.contains(it.key) }
            .map { it.key + " - " + it.value.description() }
            .joinToString("\n")
        return "Here is a list of all available commands:\n\n$commandsWithDescription"
    }
}