package com.example.my

data class Subject(val sub_code: Array<String>, val max_chapter: Array<Int>)
    /**
     * SampleData for Jetpack Compose Tutorial
     */
    object SampleData {
        // Sample conversation data
        val conversationSample = listOf(
            Subject(
                arrayOf("Maths", "mh"),
                arrayOf(1,2)
            ),
            Subject(
                arrayOf("Science", "sc"),
                arrayOf(1,2)
            ),

        )
    }
