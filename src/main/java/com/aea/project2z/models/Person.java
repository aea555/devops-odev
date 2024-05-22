package com.aea.project2z.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name", length = 16)
    private String name;
    @Column(name = "address", length = 32)
    private String address;
    @Column(name = "img_url", length = 1024)
    private String imgUrl;
}
