package com.example.evaluacion2dam.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "servicios",
        foreignKeys = @ForeignKey(entity = Cliente.class,
                parentColumns = "id",
                childColumns = "clienteId",
                onDelete = ForeignKey.CASCADE))
public class Servicio {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int clienteId;
    public String tipoServicio;
    public String estado;
    public String descripcion;
    public double costoManoObra;
    public double costoMateriales;

    public Servicio() {}
}