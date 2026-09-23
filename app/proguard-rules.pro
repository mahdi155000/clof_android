# Room generates implementation classes and keeps the required runtime metadata
# through its consumer rules. Keep the application database API stable so
# migrations and generated adapters remain discoverable in optimized builds.
-keep class com.mahdi155000.clof_android.data.AppDatabase { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# Compose discovers no application UI through reflection; no broad Compose
# keep rule is needed.
