package com.totsuka.ftracker

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.totsuka.ftracker.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.footer.setMovementMethod(LinkMovementMethod.getInstance());
        binding.footer.setText(Html.fromHtml(getResources().getString(R.string.footer_text)));
    }
}