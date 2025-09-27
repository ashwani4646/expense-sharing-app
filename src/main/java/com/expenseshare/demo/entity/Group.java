package com.expenseshare.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity(name = "groups")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column
    String name;


}
