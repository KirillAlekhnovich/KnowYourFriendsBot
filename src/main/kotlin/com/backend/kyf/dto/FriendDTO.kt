package com.backend.kyf.dto

import java.time.LocalDate

class FriendDTO(
    var id: Long,
    var name: String,
    var birthdayDate: LocalDate?,
    var attributes: Map<String, String>?
) {}