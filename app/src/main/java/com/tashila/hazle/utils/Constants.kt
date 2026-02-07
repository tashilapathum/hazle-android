package com.tashila.hazle.utils

import com.tashila.hazle.BuildConfig

val SERVER_URL = if (BuildConfig.DEBUG)
    "https://hazle.up.railway.app/"
else
    "https://api.hazle.tashila.me/"

// "http://10.0.2.2:8080/" // For Android Emulator
// "http://192.168.1.115:8080/" // Local deploy
// "https://api.hazle.tashila.me/" // Live deploy