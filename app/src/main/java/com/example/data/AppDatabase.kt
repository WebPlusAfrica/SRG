package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.VehicleDao
import com.example.data.dao.BookingDao
import com.example.data.dao.ReviewDao
import com.example.data.dao.LoyaltyDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.UpcomingEventDao
import com.example.data.dao.CarTrackerDao
import com.example.data.model.Vehicle
import com.example.data.model.Booking
import com.example.data.model.CarReview
import com.example.data.model.LoyaltyProfile
import com.example.data.model.UserProfile
import com.example.data.model.UpcomingEvent
import com.example.data.model.CarTracker

@Database(
    entities = [
        Vehicle::class,
        Booking::class,
        CarReview::class,
        LoyaltyProfile::class,
        UserProfile::class,
        UpcomingEvent::class,
        CarTracker::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun bookingDao(): BookingDao
    abstract fun reviewDao(): ReviewDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun upcomingEventDao(): UpcomingEventDao
    abstract fun carTrackerDao(): CarTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "srg_car_hire_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
