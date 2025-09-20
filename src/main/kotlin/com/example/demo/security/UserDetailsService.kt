package com.example.demo.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import com.example.demo.repositories.UserRepository
import org.springframework.security.core.userdetails.User

@Service
class MyUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Usuario no encontrado")

        return User(
            user.username,
            user.password,
            emptyList() // aquí podrías mapear roles
        )
    }
}
