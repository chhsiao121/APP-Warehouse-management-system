package com.chhsiao.firebase.quickstart.database

import android.content.Intent
import com.chhsiao.firebase.quickstart.database.java.MainActivity
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Firebase Realtime Database quickstart written in Java.",
                        Intent(this, MainActivity::class.java))
//                Choice(
//                        "Kotlin",
//                        "Run the Firebase Realtime Database quickstart written in Kotlin.",
//                        Intent(this, com.google.firebase.quickstart.database.kotlin.MainActivity::class.java))
        )
    }
}
