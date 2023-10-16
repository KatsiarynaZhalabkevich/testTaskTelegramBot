package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;


@Entity
@Table
@Data
@EqualsAndHashCode
@ToString
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String email;
    private Date timeOfStart;
    private Date timeOfFinish;
    private Long chatId;
    //List of subscriptions is out of scope

}
