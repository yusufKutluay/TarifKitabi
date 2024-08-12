package com.yusufkutluay.tarifkitabim.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.yusufkutluay.tarifkitabim.adapter.PageAdapter
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var pageAdapter: PageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.sleep(2000)
        installSplashScreen()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //altbar rengi değiştirme işlemi
        val window = window
        window.navigationBarColor = ContextCompat.getColor(this, R.color.color_app)

        // SplashScreen sonrası animasyonu ayarlayın
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val splashExitAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
                splashScreenView.iconView!!.startAnimation(splashExitAnimation)

                splashExitAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        splashScreenView.remove()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }


        pageAdapter = PageAdapter(supportFragmentManager,lifecycle)
        binding.viewPager.adapter = pageAdapter
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }
    fun floatingActionButton(view: View){

        val intent = Intent(this@MainActivity,TarifEkleme::class.java)
        startActivity(intent)

    }
}