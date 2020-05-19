package kr.vhd.wifiscanmanager.listeners

import android.net.wifi.ScanResult

/**
 * Wireless Access Point scan complete
 * @author Kwon Kyung Min
 * @since 2018-07-03
 */
interface ScanFinishListener {
    fun onScanFinish(results: List<ScanResult>)
}