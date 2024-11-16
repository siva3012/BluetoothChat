package com.example.bluetooth.database

import android.net.MacAddress
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun Insert(data:EntityClass)

    @Query("Select * from chatdata where deviceAddress = :macAddress")
     fun getDataByMAcAddress(macAddress:String) : Flow<List<EntityClass>>

    @Query("Select distinct(deviceAddress) from chatdata")
     fun getAllMAcAddress() : Flow<List<String>>
}