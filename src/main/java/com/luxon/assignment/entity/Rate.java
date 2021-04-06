package com.luxon.assignment.entity;

import com.luxon.assignment.enums.Instrument;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Rate {

    @Id
    @Column
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(value = EnumType.STRING)
    private Instrument instrument;

    private Double value;
}
