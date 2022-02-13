package com.mahigeeks.mynotesapp.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mahigeeks.mynotesapp.Models.Notes;

@Database(entities = Notes.class,version = 1,exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private static final String DATABASE_NAME = "NoteApp";
    private static RoomDB database;

    public synchronized static RoomDB getInstance(Context context){
        if (database == null){
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class,DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return database;
    }

    public abstract  MainDAO mainDAO();
}
