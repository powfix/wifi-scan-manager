package kr.vhd.wifiscanmanager.listeners

import android.net.wifi.ScanResult

interface ScanResultSetListener {
    fun onScanResultSetAvailable(results: List<List<ScanResult>>)
}