package com.tashila.hazle.utils

import com.tashila.hazle.BuildConfig

val SERVER_URL = if (BuildConfig.DEBUG)
    "https://hazle-backend-dev.onrender.com/"
else
    "https://api.hazle.tashila.me/"

// "http://10.0.2.2:8080/" // For Android Emulator
// "http://192.168.100.80:8080/" // Hutch wifi
// "http://192.168.0.101:8080/" // Dialog wifi
// "https://hazle.onrender.com/" // Render deploy
// "https://api.hazle.tashila.me/" // Live deploy