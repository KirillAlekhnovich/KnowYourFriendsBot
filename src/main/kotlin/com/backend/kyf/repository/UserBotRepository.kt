package com.backend.kyf.repository

import com.backend.kyf.entity.UserBot
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserBotRepository: CrudRepository<UserBot, Long>