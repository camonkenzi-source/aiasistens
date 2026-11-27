package com.ziimodsx.ai

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    // GANTI DENGAN API KEY KAMU dari aistudio.google.com
    private val API_KEY = "API_KEY_KAMU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.inputPertanyaan)
        val tombol = findViewById<Button>(R.id.btnTanya)
        val jawabanView = findViewById<TextView>(R.id.txtJawaban)
        val btnFloating = findViewById<Button>(R.id.btnFloating)

        btnFloating.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                startService(Intent(this, FloatingService::class.java))
                Toast.makeText(this, "Ikon melayang aktif", Toast.LENGTH_SHORT).show()
            }
        }

        tombol.setOnClickListener {
            val pertanyaan = input.text.toString()
            if (pertanyaan.isEmpty()) return@setOnClickListener
            jawabanView.text = "AI lagi mikir..."
            tanyaAI(pertanyaan) { jawaban ->
                runOnUiThread { jawabanView.text = jawaban }
            }
        }
    }

    private fun tanyaAI(pertanyaan: String, callback: (String) -> Unit) {
        val json = JSONObject()
        val parts = JSONObject().put("text", pertanyaan)
        val content = JSONObject().put("parts", org.json.JSONArray().put(parts))
        json.put("contents", org.json.JSONArray().put(content))
        val body = RequestBody.create("application/json".toMediaType(), json.toString())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$API_KEY")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { callback("Gagal: ${e.message}") }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val res = JSONObject(response.body?.string() ?: "")
                    val text = res.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                    callback(text)
                } catch (e: Exception) { callback("Gagal baca jawaban: ${e.message}") }
            }
        })
    }
}
