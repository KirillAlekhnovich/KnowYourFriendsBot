package com.backend.kyf.entity

import com.sun.istack.NotNull
import java.time.LocalDate
import javax.persistence.*


@Entity
class Friend(
    @Id
    @GeneratedValue
    var id: Long,

    @NotNull
    var name: String,

    var birthdayDate: LocalDate?,

    @ElementCollection
    var attributes: Map<String, String>?
) {}