package com.devsu.backendbank.infrastructure.output.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "persona")
public class Person extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Gender genero;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "identificacion", nullable = false, length = 50, unique = true)
    private String identificacion;

    @Column(name = "direccion", nullable = false, length = 180)
    private String direccion;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @OneToOne(mappedBy = "person")
    private Client client;
}
