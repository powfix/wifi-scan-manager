package kr.vhd.wifiscanmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import kr.vhd.wifiscanmanager.listeners.ScanFinishListener
import kr.vhd.wifiscanmanager.listeners.ScanResultSetListener
import java.util.concurrent.TimeUnit

class WifiScanManager(private val context: Context): ScanFinishListener, ScanResultSetListener {
    private val wifiManager by lazy { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val scanReceiver by lazy { ScanReceiver() }
    private val scanFinishListeners by lazy { ArrayList<ScanFinishListener>() }
    private val scanResultSetListeners by lazy { ArrayList<ScanResultSetListener>() }
    private val taskQue by lazy { ArrayList<Function<Unit>>() }

    private val scanResultSet by lazy { ArrayList<List<ScanResult>>() }
    private var scanCount: Int = 0

    // Options
    val scanOptions = ScanOption()

    fun setScanFinishListener(listener: ScanFinishListener?) {
        scanFinishListeners.clear()
        if (listener != null) {
            scanFinishListeners.add(listener)
        }
    }

    fun addScanFinishListener(listener: ScanFinishListener) {
        scanFinishListeners.add(listener)
    }

    fun setScanResultSetListener(listener: ScanResultSetListener?) {
        scanResultSetListeners.clear()
        if (listener != null) {
            scanResultSetListeners.add(listener)
        }
    }

    fun addScanResultSetListener(listener: ScanResultSetListener) {
        scanResultSetListeners.add(listener)
    }

    /**
     * 스캔 결과 사이의 최소 시간을 설정합니다.
     * 설정한 간격보다 이르게 수신되는 스캔 결과는 무시합니다.
     */
    fun setScanResultInterval(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        scanOptions.scanResultInterval = unit.toMillis(duration)
    }

    private fun registerReceiver() {
        try {
            context.registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun unregisterReceiver() {
        try {
            context.unregisterReceiver(scanReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startScan(scanLimit: Int = 1) {
        this.scanCount = 0
        this.scanOptions.scanLimit = scanLimit

        registerReceiver()
        wifiManager.startScan()
    }

    fun stopScan() {
        unregisterReceiver()
        onScanResultSetAvailable(scanResultSet)
    }

    fun cancelScan() {
        unregisterReceiver()
        scanResultSet.clear()
    }

    override fun onScanFinish(results: List<ScanResult>) {
        ++scanCount
        scanResultSet.add(results)

        // Call listeners method
        scanFinishListeners.forEach { listener ->
            listener.onScanFinish(ArrayList<ScanResult>().apply { addAll(results) })
        }

        if (scanCount >= scanOptions.scanLimit) {
            unregisterReceiver()
            onScanResultSetAvailable(scanResultSet)
            return
        }

        // Scan again
        wifiManager.startScan()
    }

    override fun onScanResultSetAvailable(results: List<List<ScanResult>>) {
        // Call listeners method
        scanResultSetListeners.forEach { listener ->
            listener.onScanResultSetAvailable(ArrayList<List<ScanResult>>().apply { addAll(results) })
        }

        if (results === scanResultSet) {
            scanResultSet.clear()
        }
    }

    private inner class ScanReceiver: BroadcastReceiver() {
        private var lastReceive: Long = 0L
        private var lastScanResults: List<ScanResult>? = null

        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent?.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                return
            }

            // Scan duration check
            val receiveTimeMillis: Long = System.currentTimeMillis()
            synchronized(lastReceive) {
                if ((receiveTimeMillis - lastReceive) < scanOptions.scanResultInterval) {
                    Log.w("onReceive", "")
                    wifiManager.startScan()
                    return
                }
                lastReceive = receiveTimeMillis
            }

            // Scan results
            val scanResults = wifiManager.scanResults
            val lastScanResults = this.lastScanResults

            // Duplicate scan results
            if (scanOptions.ignoreDuplicateScanResults) {
                if (lastScanResults != null) {
                    if (scanResults.size == lastScanResults.size) {
                        var duplicateCount = 0
                        for (i in 0 until scanResults.size) {
                            val o1 = scanResults[i]
                            val o2 = lastScanResults[i]
                            if (o1.BSSID != o2.BSSID ||
                                    o1.SSID != o2.SSID ||
                                    o1.frequency != o2.frequency ||
                                    o1.level != o2.level) {
                                break
                            }
                            ++duplicateCount
                        }

                        if (duplicateCount == scanResults.size) {
                            wifiManager.startScan()
                            return
                        }
                    }
                }
            }
            this.lastScanResults = scanResults

            val results = ArrayList<ScanResult>()
            for (scanResult: ScanResult? in scanResults) {
                if (scanResult == null) {
                    continue
                }
                if (scanResult.BSSID.isNullOrBlank()) {
                    continue
                }
                results.add(scanResult)
            }
            onScanFinish(results)
        }
    }
}