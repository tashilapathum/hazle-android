package com.tashila.hazle.utils

import com.tashila.hazle.BuildConfig

const val SERVER_DEV = "https://hazle.up.railway.app/"
const val SERVER_LIVE = "https://api.hazle.tashila.me/"
// "http://10.0.2.2:8080/" // For Android Emulator
// "http://192.168.1.115:8080/" // Local deploy
// "https://api.hazle.tashila.me/" // Live deploy

val SERVER_URL = if (BuildConfig.DEBUG) SERVER_DEV else SERVER_LIVE

const val PACKAGE_MONTHLY = $$"$rc_monthly"
const val PACKAGE_ANNUAL = $$"$rc_annual"
const val PACKAGE_LIFETIME = $$"$rc_lifetime"

const val ENTITLEMENT_PRO = "Hazle Pro"
const val ENTITLEMENT_VIP = "Hazle VIP"