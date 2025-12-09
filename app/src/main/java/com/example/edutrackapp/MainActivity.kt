package com.example.edutrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.edutrackapp.cms.core.data.local.EduTrackDatabase
import com.example.edutrackapp.cms.core.data.local.entity.UserEntity
import com.example.edutrackapp.cms.ui.navigation.EduTrackNavGraph
import com.example.edutrackapp.cms.ui.theme.EduTrackAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject the Database to create the default Admin
    @Inject
    lateinit var database: EduTrackDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- SEED DEFAULT ADMIN USER ---
        // This runs every time the app opens, ensuring the Admin always exists
        CoroutineScope(Dispatchers.IO).launch {
            val adminUser = UserEntity(
                userId = "ADMIN_001",
                name = "Super Admin",
                email = "admin@edutrack.com",
                password = "admin", // Default Password
                role = "ADMIN"
            )
            database.userDao.insertUser(adminUser)
        }
        // -------------------------------

        setContent {
            EduTrackAppTheme {
                val navController = rememberNavController()
                EduTrackNavGraph(navController = navController)
            }
        }
    }
}