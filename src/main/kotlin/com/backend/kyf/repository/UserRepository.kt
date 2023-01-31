package com.backend.kyf.repository

import com.backend.kyf.entity.User
import org.springframework.data.repository.CrudRepository

interface UserRepository: CrudRepository<User, Long>