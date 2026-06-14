package com.example.evaluacion2dam.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.evaluacion2dam.model.Cliente;
import com.example.evaluacion2dam.model.Servicio;

import java.util.List;

@Dao
public interface AppDao {
    @Insert
    void insertarCliente(Cliente cliente);

    @Update
    void actualizarCliente(Cliente cliente);

    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    List<Cliente> obtenerTodosLosClientes();

    @Query("SELECT * FROM clientes WHERE nombre LIKE :nombreBuscado")
    List<Cliente> buscarClientesPorNombre(String nombreBuscado);

    @Insert
    void insertarServicio(Servicio servicio);

    @Delete
    void eliminarServicio(Servicio servicio);

    @Query("SELECT * FROM servicios")
    List<Servicio> obtenerTodosLosServicios();

    @Query("SELECT COUNT(*) FROM clientes")
    int contarClientes();

    @Query("SELECT COUNT(*) FROM servicios WHERE estado = 'Pendiente'")
    int contarServiciosPendientes();

    @Query("SELECT SUM(costoManoObra + costoMateriales) FROM servicios")
    double obtenerIngresosTotales();
}