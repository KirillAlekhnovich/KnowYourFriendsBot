package com.backend.kyf.entity

import javax.persistence.*


@Entity
@Table(name = "users")
class User(
    @Id
    var id: Long,

    @OneToMany
    @JoinColumn(name = "friend_id")
    var friends: MutableList<Friend>?
) {}