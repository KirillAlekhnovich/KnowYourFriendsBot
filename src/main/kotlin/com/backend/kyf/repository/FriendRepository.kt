package com.backend.kyf.repository

import com.backend.kyf.entity.Friend
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendRepository : CrudRepository<Friend, Long> {

}