package com.telegram.bot.repository

import com.telegram.bot.entity.TelegramBotState
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TelegramBotStateRepository: CrudRepository<TelegramBotState, Long>