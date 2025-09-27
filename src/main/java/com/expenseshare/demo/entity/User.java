package com.expenseshare.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column
    String name;

    @Column
    String email;

    @Column
    String oauthId;

}
