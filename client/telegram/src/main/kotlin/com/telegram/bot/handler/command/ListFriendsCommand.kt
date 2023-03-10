package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createInlineButton
import com.telegram.bot.handler.Buttons.createInlineMarkup
import com.telegram.bot.handler.Buttons.createRowInstance
import com.telegram.bot.service.UserRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.Paging
import com.telegram.bot.utils.Paging.getPage
import com.telegram.bot.utils.StorageParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import javax.inject.Named

@Component
@Named(Commands.LIST_FRIENDS)
class ListFriendsCommand(
    private val userRequestService: UserRequestService
) : Command {
    override fun description(): String {
        return "Shows a list of your friends"
    }

    override fun nextState(botState: TelegramBotStateDTO): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun execute(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): ClientResponseDTO {
        telegramBotState.addParamToStorage(StorageParams.CURRENT_PAGE, "1")
        return ClientResponseDTO(getMessage(user, message, telegramBotState), getButtons(telegramBotState))
    }

    override fun getMessage(user: UserDTO, message: String, telegramBotState: TelegramBotStateDTO): String {
        telegramBotState.removeParamFromStorage(StorageParams.FRIEND_ID)
        val friends = userRequestService.getFriendNames(user.id)
        return if (friends.isEmpty()) {
            "You have no friends :("
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder.append("List of your friends:\n\n")
            var index = 1
            val page = getCurrentPage(telegramBotState)
            if (page > 1) index = (page - 1) * Paging.ITEMS_PER_PAGE + 1
            friends.getPage(page).map { stringBuilder.append("$index. ${it}\n"); index++ }
            stringBuilder.toString()
        }
    }

    override fun getButtons(botState: TelegramBotStateDTO): ReplyKeyboard? {
        val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        val row: MutableList<InlineKeyboardButton> = ArrayList()

        val currentPage = getCurrentPage(botState)
        if (currentPage > 1) {
            row.add(createInlineButton("Previous page", Commands.PREVIOUS_PAGE))
        }
        if (userRequestService.getFriendNames(botState.userId).size > Paging.ITEMS_PER_PAGE * currentPage) {
            row.add(createInlineButton("Next page", Commands.NEXT_PAGE))
        }
        buttons.add(createRowInstance(row))
        row.clear()

        row.add(createInlineButton("Add friend", Commands.ADD_FRIEND))
        row.add(createInlineButton("Remove friend", Commands.REMOVE_FRIEND))
        buttons.add(createRowInstance(row))
        row.clear()

        row.add(createInlineButton("Get friend info", Commands.FRIEND_INFO))
        buttons.add(createRowInstance(row))

        return createInlineMarkup(buttons)
    }

    private fun getCurrentPage(botState: TelegramBotStateDTO): Int {
        return botState.getParamFromStorage(StorageParams.CURRENT_PAGE).toInt()
    }
}