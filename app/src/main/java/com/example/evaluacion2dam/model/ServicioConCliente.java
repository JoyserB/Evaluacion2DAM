package com.example.evaluacion2dam.model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ServicioConCliente {
    @Embedded
    public Servicio servicio;

    public String nombreCliente;
}