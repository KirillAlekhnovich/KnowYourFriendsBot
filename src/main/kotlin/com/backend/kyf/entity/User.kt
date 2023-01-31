package com.backend.kyf.entity

import javax.persistence.*


@Entity
@Table(name = "users")
data class User(
    @Id
    var id: Long,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "friend_id")
    val friends: MutableSet<Friend>?,

    @ElementCollection(fetch = FetchType.EAGER)
    val generalAttributes: MutableSet<String>?
)