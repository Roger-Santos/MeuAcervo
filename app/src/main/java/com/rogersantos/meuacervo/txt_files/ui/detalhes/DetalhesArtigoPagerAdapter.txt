package com.rogersantos.meuacervo.ui.detalhes

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rogersantos.meuacervo.ui.editar.EditarArtigoFragment

class DetalhesArtigoPagerAdapter(
    activity: FragmentActivity,
    private val artigoId: Int
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Duas abas: Detalhes e Editar

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetalhesArtigoFragment.newInstance(artigoId)
            else -> EditarArtigoFragment.newInstance(artigoId)
        }
    }
}