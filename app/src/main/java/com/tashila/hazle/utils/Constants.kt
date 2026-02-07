package com.tashila.hazle.utils

import com.tashila.hazle.BuildConfig

const val SERVER_DEV = "https://hazle.up.railway.app/"
const val SERVER_LIVE = "https://api.hazle.tashila.me/"

val SERVER_URL = if (BuildConfig.DEBUG) SERVER_DEV else SERVER_LIVE


// "http://10.0.2.2:8080/" // For Android Emulator
// "http://192.168.1.115:8080/" // Local deploy
// "https://api.hazle.tashila.me/" // Live deploy