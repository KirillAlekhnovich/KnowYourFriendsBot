package com.backend.kyf.dto

class UserDTO(
    var id: Long,
    var friends: MutableSet<FriendSlimDTO>?
)