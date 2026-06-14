package com.example.evaluacion2dam.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.evaluacion2dam.model.Cliente;
import com.example.evaluacion2dam.model.Servicio;

@Database(entities = {Cliente.class, Servicio.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();
    private static AppDatabase instancia;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "taller_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instancia;
    }
}