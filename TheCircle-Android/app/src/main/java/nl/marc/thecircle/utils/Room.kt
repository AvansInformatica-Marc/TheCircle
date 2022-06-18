package nl.marc.thecircle.utils

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

inline fun <reified T : RoomDatabase> buildRoomDatabase(context: Context, name: String): T {
    return Room.databaseBuilder(context, T::class.java, name).build()
}
