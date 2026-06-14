package com.example.evaluacion2dam.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientes")
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nombre;
    public String telefono;
    public String email;
    public String direccion;
    public String municipio;
    public String notas;

    public Cliente() {}

    @Override
    public String toString() {
        return nombre;
    }
}