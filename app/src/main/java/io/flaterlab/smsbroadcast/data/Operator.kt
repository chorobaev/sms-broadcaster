package io.flaterlab.smsbroadcast.data

enum class Operator {
    O {
        override val networkCode = 9
        override val codes = setOf(
            "500",
            "501",
            "502",
            "504",
            "505",
            "507",
            "508",
            "509",
            "700",
            "701",
            "702",
            "703",
            "704",
            "705",
            "706",
            "707",
            "708",
            "709",
        )
    },
    MEGACOM {
        override val networkCode: Int = 5
        override val codes = setOf(
            "999",
            "755",
            "550",
            "551",
            "552",
            "553",
            "554",
            "555",
            "556",
            "557",
            "558",
            "559",
        )
    };

    abstract val networkCode: Int
    abstract val codes: Set<String>

    companion object {
        const val code = "996"

        fun getCode(phone: String): String {
            return phone.takeLast(9).take(3)
        }
    }
}