package com.backend.kyf.service

import com.backend.kyf.dto.UserBotDTO
import com.backend.kyf.repository.UserBotRepository
import com.backend.kyf.utils.UserBotMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserBotService(
    private val userBotRepository: UserBotRepository,
    private val userBotMapper: UserBotMapper
) {

    fun createUserBot(userBotDTO: UserBotDTO): UserBotDTO {
        val userBot = userBotMapper.toEntity(userBotDTO)
        userBotRepository.save(userBot)
        return userBotMapper.toDTO(userBot)
    }

    fun getUserBotById(chatId: Long): UserBotDTO {
        val userBot = userBotRepository.findByIdOrNull(chatId)
        return userBotMapper.toDTO(userBot!!)
    }

    fun exists(chatId: Long): Boolean {
        return userBotRepository.existsById(chatId)
    }
}