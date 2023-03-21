package com.backend.kyf.utils.auth

import com.backend.kyf.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(id: String): UserDetails {
        val user = userRepository.findByIdOrNull(id.toLong())
            ?: throw UsernameNotFoundException("User not found with id: $id")
        return User.builder()
            .username(user.id.toString())
            .password("")
            .authorities(emptyList())
            .build()
    }
}
