package com.expenseshare.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "settlements")
@Data
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    Long payerId;

    @Column
    Long receiverId;

    @Column
    Long oauthId;

}
