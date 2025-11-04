package com.example.demo.unitTests.repositories

import com.example.demo.model.User
import com.example.demo.repositories.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `test findByUsername returns user when exists`() {
        val user = User(username = "testuser", password = "password123")
        entityManager.persist(user)
        entityManager.flush()

        val found = userRepository.findByUsername("testuser")

        assertNotNull(found)
        assertEquals("testuser", found?.username)
        assertEquals("password123", found?.password)
    }

    @Test
    fun `test findByUsername returns null when user does not exist`() {
        val found = userRepository.findByUsername("nonexistent")

        assertNull(found)
    }

    @Test
    fun `test existsByUsername returns true when user exists`() {
        val user = User(username = "existinguser", password = "pass")
        entityManager.persist(user)
        entityManager.flush()

        val exists = userRepository.existsByUsername("existinguser")

        assertTrue(exists)
    }

    @Test
    fun `test existsByUsername returns false when user does not exist`() {
        val exists = userRepository.existsByUsername("nothere")

        assertFalse(exists)
    }

    @Test
    fun `test save user successfully`() {
        val user = User(username = "newuser", password = "newpass")

        val saved = userRepository.save(user)

        assertNotNull(saved.id)
        assertEquals("newuser", saved.username)
    }

    @Test
    fun `test username is unique`() {
        val user1 = User(username = "uniqueuser", password = "pass1")
        entityManager.persist(user1)
        entityManager.flush()

        val user2 = User(username = "uniqueuser", password = "pass2")

        assertThrows(Exception::class.java) {
            entityManager.persist(user2)
            entityManager.flush()
        }
    }

    @Test
    fun `test findAll returns all users`() {
        val user1 = User(username = "user1", password = "pass1")
        val user2 = User(username = "user2", password = "pass2")
        entityManager.persist(user1)
        entityManager.persist(user2)
        entityManager.flush()

        val users = userRepository.findAll()

        assertTrue(users.size >= 2)
    }

    @Test
    fun `test delete user`() {
        val user = User(username = "deleteuser", password = "pass")
        entityManager.persist(user)
        entityManager.flush()

        userRepository.delete(user)

        val found = userRepository.findByUsername("deleteuser")
        assertNull(found)
    }
}

