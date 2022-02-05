package com.totsuka.ftracker

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.totsuka.ftracker.ui.main.SectionsPagerAdapter
import com.totsuka.ftracker.databinding.ActivityMainBinding
import com.totsuka.ftracker.ui.main.DbHelper
import android.view.Menu
import android.view.MenuItem


var db: DbHelper? = null

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true);

        db = DbHelper(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.settings -> {
//                val intent = Intent(this, AboutActivity::class.java).apply {}
//                startActivity(intent)
                true
            }
            R.id.about -> {
                val intent = Intent(this, AboutActivity::class.java).apply {}
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
