package com.backend.kyf.utils

interface Mapper<D, E> {
    fun toDTO(entity: E): D
    fun toEntity(dto: D): E
}