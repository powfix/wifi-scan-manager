package kr.vhd.wifiscanmanager

class ScanOption {
    var scanLimit: Int = 1                              // 스캔횟수
    var scanResultInterval: Long = 2000L                // 최근 WIFI 스캔 후 설정한 시간(Millisecond)안에 수집된 스캔결과는 사용하지 않음
    var ignoreDuplicateScanResults: Boolean = true      // 최근 WIFI 스캔결과와 완전히 똑같은 경우 스캔결과를 사용하지 않음
    var useLogging: Boolean = BuildConfig.DEBUG
}