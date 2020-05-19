package kr.vhd.wifiscanmanager.utils

class Median {
    private val values: IntArray

    val median: Double
        get() {
            val length: Int = values.size
            return if (length % 2 == 0) {
                (values[length / 2].toDouble() + values[length / 2 - 1].toDouble()) / 2
            } else {
                values[length / 2].toDouble()
            }
        }

    constructor(values: List<Int>) {
        this.values = values.toIntArray().apply {
            sort()
        }
    }

    constructor(vararg values: Int) {
        this.values = values.apply {
            sort()
        }
    }
}