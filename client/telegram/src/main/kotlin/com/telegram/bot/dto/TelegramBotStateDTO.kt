package com.telegram.bot.dto

import com.telegram.bot.handler.BotState
import com.telegram.bot.utils.StorageParams

data class TelegramBotStateDTO(
    var userId: Long,
    var currCommand: String,
    var state: BotState,
    var commandsQueue: MutableList<String>,
    val storage: MutableMap<String, String>
)

fun TelegramBotStateDTO.addParamToStorage(key: StorageParams, value: String) {
    this.storage[key.name] = value
}

fun TelegramBotStateDTO.paramIsInStorage(parameter: StorageParams): Boolean {
    return this.storage.containsKey(parameter.name)
}

fun TelegramBotStateDTO.getParamFromStorage(parameter: StorageParams): String {
    if (!this.storage.containsKey(parameter.name)) {
        throw RuntimeException("I can't determine ${parameter.name}. Please try again")
    }
    return this.storage[parameter.name]!!
}

fun TelegramBotStateDTO.removeParamFromStorage(parameter: StorageParams) {
    this.storage.remove(parameter.name)
}

fun TelegramBotStateDTO.incrementPage(value: Int) {
    val page = this.getParamFromStorage(StorageParams.CURRENT_PAGE).toInt()
    this.addParamToStorage(StorageParams.CURRENT_PAGE, (page + value).toString())
}