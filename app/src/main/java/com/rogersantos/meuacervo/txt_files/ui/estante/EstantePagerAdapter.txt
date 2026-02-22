package com.rogersantos.meuacervo.ui.estante

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// EstantePagerAdapter.kt
class EstantePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> EstanteLivrosFragment()
        else -> EstanteArtigosFragment()
    }
}