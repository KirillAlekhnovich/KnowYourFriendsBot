package com.telegram.bot.handler.command

import com.telegram.bot.dto.*
import com.telegram.bot.handler.BotState
import com.telegram.bot.handler.Buttons.createInlineButton
import com.telegram.bot.handler.Buttons.createInlineMarkup
import com.telegram.bot.handler.Buttons.createRowInstance
import com.telegram.bot.service.FriendRequestService
import com.telegram.bot.utils.Commands
import com.telegram.bot.utils.Jedis.deleteValue
import com.telegram.bot.utils.Jedis.getCurrentPage
import com.telegram.bot.utils.Jedis.setValue
import com.telegram.bot.utils.Paging
import com.telegram.bot.utils.Paging.getPage
import com.telegram.bot.utils.RedisParams
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import kotlin.math.ceil
import java.util.ArrayList
import javax.inject.Named

/**
 * Command that shows a list of your friends.
 */
@Component
@Named(Commands.LIST_FRIENDS)
class ListFriendsCommand(
    private val friendRequestService: FriendRequestService
) : Command {
    override fun description(): String {
        return "Shows a list of your friends"
    }

    override fun nextState(userId: Long): BotState {
        return BotState.EXPECTING_COMMAND
    }

    override fun execute(user: UserDTO, message: String): ClientResponseDTO {
        setValue(user.id, RedisParams.CURRENT_PAGE.name, "1")
        deleteValue(user.id, RedisParams.FRIEND_ID.name)
        return ClientResponseDTO(getMessage(user, message), getButtons(user.id))
    }

    override fun getMessage(user: UserDTO, message: String): String {
        return try {
            val friends = friendRequestService.getFriendNames(user.id)
            if (friends.isEmpty()) "You have no friends :("
            else generateFriendsList(user.id, friends)
        } catch (e: RuntimeException) {
            setValue(user.id, RedisParams.STATE.name, BotState.ERROR.name)
            e.message!!
        }
    }

    override fun getButtons(userId: Long): ReplyKeyboard? {
        val buttons: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
        val row: MutableList<InlineKeyboardButton> = ArrayList()

        val currentPage = getCurrentPage(userId)
        if (currentPage > 1) {
            row.add(createInlineButton("Previous page", Commands.PREVIOUS_PAGE))
        }
        if (friendRequestService.getFriendNames(userId).size > Paging.ITEMS_PER_PAGE * currentPage) {
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

    private fun generateFriendsList(userId: Long, friends: List<String>): String {
        val stringBuilder = StringBuilder()
        var index = 1
        val page = getCurrentPage(userId)
        val totalPages = ceil(friends.size.toDouble() / Paging.ITEMS_PER_PAGE).toInt()
        stringBuilder.append("List of your friends:\n\n")
        if (page > 1) index = (page - 1) * Paging.ITEMS_PER_PAGE + 1
        friends.getPage(page).map { stringBuilder.append("$index. ${it}\n"); index++ }
        stringBuilder.append("\nPage: $page of $totalPages")
        return stringBuilder.toString()
    }
}