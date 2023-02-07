package com.haidev.picturetotextapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haidev.picturetotextapp.databinding.ActivityEditResultBinding

class EditResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditResultBinding
    private lateinit var textResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textResult = intent.getStringExtra("textResult").toString()

        initUI()
    }

    private fun initUI() {
        binding.etEditResult.setText(textResult)
        binding.btnEditResult.setOnClickListener {
            val intent = Intent()
            intent.putExtra("textResult", binding.etEditResult.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}