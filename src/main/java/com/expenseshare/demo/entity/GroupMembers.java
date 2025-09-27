package com.expenseshare.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "group_members")
public class GroupMembers {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

}
