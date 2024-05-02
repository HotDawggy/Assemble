package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import okhttp3.internal.userAgent

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // init username
        val usernameEditText: EditText = findViewById(R.id.profilePageUsername)
        val savedUsername = sharedPrefs.getString("username", "")
        if (savedUsername == "") {
            usernameEditText.setHint("Enter Username :3")
        }
        else {
            usernameEditText.setText(savedUsername)
        }
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedPrefs.edit().putString("username", usernameEditText.text.toString()).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // init high score
        val highScoreTextView: TextView = findViewById(R.id.profilePageHighScore)
        highScoreTextView.text = "High Score: " + sharedPrefs.getInt("highScore", 0)

        // init games played
        val gamesPlayedTextView: TextView = findViewById(R.id.profilePageGamesPlayedCount)
        gamesPlayedTextView.text = "Total Games Played: " + sharedPrefs.getInt("gamesPlayed", 0).toString()

        // init fav asm instruction
        val famAsmTextView: TextView = findViewById(R.id.profilePageFavAsm)
        var regList = mutableListOf<String>()
        regList += resources.getStringArray(R.array.instr_r)
        regList += resources.getStringArray(R.array.instr_i)
        regList += resources.getStringArray(R.array.instr_j)

        var favOp = "Nothing"
        var favCount = 0
        for(op in regList) {
            val usedCount = sharedPrefs.getInt(op, 0)
            if (usedCount > favCount) {
                favCount = usedCount
                favOp = op
            }
        }
        famAsmTextView.text = "You used $favOp $favCount times!"

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // https://stackoverflow.com/questions/3141996/android-how-to-override-the-back-button-so-it-doesnt-finish-my-activity
                startActivity(Intent(this@ProfilePage,MainActivity::class.java))
                finishAffinity()
            }
        })

    }
}