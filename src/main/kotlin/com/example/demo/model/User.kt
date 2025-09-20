package com.example.demo.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
open class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false, unique = true)
    open var username: String = "",

    @Column(nullable = false)
    open var password: String = ""

) {
    // ctor sin args ya cubierto por los valores por defecto;
    // si quieres, puedes dejar este explícito:
    constructor() : this(null, "", "")
}
