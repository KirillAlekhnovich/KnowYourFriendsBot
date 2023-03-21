package com.telegram.bot.handler.command

import com.telegram.bot.dto.UserDTO
import com.telegram.bot.handler.BotState
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.Jedis.reset
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Named(Commands.RESET)
class ResetCommand(
    private val userRequestService: UserRequestService
) : Command {

    override fun description(): String {
        return "Resets your profile. Removes all your friends and general attributes"
    }

    override fun nextState(userId: Long): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun getMessage(user: UserDTO, message: String): String {
        reset(user.id)
        userRequestService.resetUser(user.id)
        return "Your profile has been reset"
    }
}