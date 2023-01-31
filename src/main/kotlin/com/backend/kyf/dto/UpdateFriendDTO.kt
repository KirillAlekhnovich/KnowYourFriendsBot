package com.backend.kyf.dto

import java.time.LocalDate

class UpdateFriendDTO(
    var name: String?,
    var birthdayDate: LocalDate?,
    var attributes: MutableMap<String, String?>?
)