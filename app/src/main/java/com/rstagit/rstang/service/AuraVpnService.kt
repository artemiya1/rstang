package com.rstagit.rstang.service

import android.net.VpnService
import android.os.PowerManager
import android.content.Context

class AuraVpnService : VpnService() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        // بهینه‌سازی مصرف پردازنده در پس‌زمینه با مدیریت هوشمند مصرف انرژی
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AuraVpn::WakeLock").apply {
            setReferenceCounted(false)
        }
    }

    private fun startTunnel() {
        // اختصاص دادن بهینه‌ترین حالت اندازه بسته (MTU) برای کاهش بازپخش داده‌ها و ذخیره باتری
        val builder = Builder()
            .setSession("AuraTunnel")
            .setMtu(1400) // مقدار ۱۴۰۰ بهترین هماهنگی را با شبکه‌های موبایل دارد
            .addAddress("172.19.0.1", 30)
            .addDnsServer("1.1.1.1")
            
        // فعال‌سازی قابلیت Bypass برای اپلیکیشن‌های پس‌زمینه جهت کاهش بار پردازشی غیرضروری
        builder.addBypassApp("com.android.vending")
        
        // مدیریت هوشمند قفل پردازنده برای جلوگیری از تخلیه ناگهانی باتری
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    override fun onDestroy() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }
}
