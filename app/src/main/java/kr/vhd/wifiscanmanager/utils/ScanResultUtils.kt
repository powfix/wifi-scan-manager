package kr.vhd.wifiscanmanager.utils

import android.net.wifi.ScanResult
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject

object ScanResultUtils {
    fun scanResultToJSONArray(items: List<ScanResult>): JSONArray {
        val results = JSONArray()
        for (item: ScanResult in items) {
            try {
                results.put(JSONObject().apply {
                    put("SSID", item.SSID)
                    put("BSSID", item.BSSID)
                    put("capabilities", item.capabilities)
                    put("frequency", item.frequency)
                    put("level", item.level)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        put("timestamp", item.timestamp)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return results
    }

    fun resultSetToMedianResult(scanResultSet: List<List<ScanResult>>): List<ScanResult> {
        // BSSID 별로 해당 AP에 대한 ScanResult 객체를 보관한다.
        val apLevels = HashMap<String, ArrayList<ScanResult>>(scanResultSet.size)
        scanResultSet.forEach { scan: List<ScanResult> ->
            scan.forEach { apData: ScanResult ->
                // BSSID 값을 Key로 사용
                if (apLevels.containsKey(apData.BSSID)) {
                    apLevels[apData.BSSID]!!.add(apData)
                } else {
                    // Not contain AP Data
                    apLevels[apData.BSSID] = ArrayList<ScanResult>(scan.size).apply {
                        add(apData)
                    }
                }
            }
        }

        // AP 별로 중앙값을 산출한다.
        val apList = ArrayList<ScanResult>(apLevels.size)
        val apLevelsIterator = apLevels.keys.iterator()
        while (apLevelsIterator.hasNext()) {
            apLevelsIterator.next().let { key: String ->
                if (apLevels.containsKey(key)) {
                    val levels: List<Int> = apLevels[key]!!.let { scanResultSet: List<ScanResult> ->
                        ArrayList<Int>(scanResultSet.size).apply {
                            scanResultSet.forEach { scanResults ->
                                add(scanResults.level)
                            }
                        }
                    }

                    // WI-FI Signal level 값이 1개 이상일 때 추가한다.
                    if (apLevels[key]!!.size > 0) {
                        apList.add(apLevels[key]!![0].apply {
                            level = Median(levels).median.toInt()
                        })
                    }
                }
            }
        }

        return apList
    }
}