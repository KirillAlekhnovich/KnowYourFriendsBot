package com.backend.kyf.entity

import com.sun.istack.NotNull
import javax.persistence.*


@Entity
data class Friend(
    @Id
    @GeneratedValue
    var id: Long,

    @NotNull
    var name: String,

    @ElementCollection(fetch = FetchType.EAGER)
    val attributes: MutableMap<String, String?>
)