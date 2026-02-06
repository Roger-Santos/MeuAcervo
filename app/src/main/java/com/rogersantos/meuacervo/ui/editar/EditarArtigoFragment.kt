package com.rogersantos.meuacervo.ui.editar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.ArtigoDatabase
import com.rogersantos.meuacervo.data.model.Artigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarArtigoFragment : Fragment(R.layout.fragment_editar_artigo) {

    private var artigoId: Int = -1
    private var artigo: Artigo? = null

    // Campos
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etAutores: TextInputEditText
    private lateinit var etPeriodico: TextInputEditText
    private lateinit var etAno: TextInputEditText
    private lateinit var etDoi: TextInputEditText
    private lateinit var etLink: TextInputEditText
    private lateinit var etResumo: TextInputEditText
    private lateinit var etPalavrasChave: TextInputEditText
    private lateinit var etPaginas: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var cbJaLido: CheckBox
    private lateinit var btnSalvar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artigoId = arguments?.getInt("artigoId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind
        etTitulo = view.findViewById(R.id.etTitulo)
        etAutores = view.findViewById(R.id.etAutores)
        etPeriodico = view.findViewById(R.id.etPeriodico)
        etAno = view.findViewById(R.id.etAno)
        etDoi = view.findViewById(R.id.etDoi)
        etLink = view.findViewById(R.id.etLink)
        etResumo = view.findViewById(R.id.etResumo)
        etPalavrasChave = view.findViewById(R.id.etPalavrasChave)
        etPaginas = view.findViewById(R.id.etPaginas)
        ratingBar = view.findViewById(R.id.ratingBar)
        cbJaLido = view.findViewById(R.id.cbJaLido)
        btnSalvar = view.findViewById(R.id.btnSalvar)

        carregarArtigo()

        btnSalvar.setOnClickListener { salvarAlteracoes() }
    }

    private fun carregarArtigo() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val dao = ArtigoDatabase.getInstance(requireContext()).artigoDao()
            val resultado = dao.buscarPorId(artigoId)

            withContext(Dispatchers.Main) {
                resultado?.let {
                    artigo = it
                    preencherCampos(it)
                }
            }
        }
    }

    private fun preencherCampos(artigo: Artigo) {
        etTitulo.setText(artigo.titulo)
        etAutores.setText(artigo.autores ?: "")
        etPeriodico.setText(artigo.periodico ?: "")
        etAno.setText(artigo.anoPublicacao?.toString() ?: "")
        etDoi.setText(artigo.doi ?: "")
        etLink.setText(artigo.link ?: "")
        etResumo.setText(artigo.resumo ?: "")
        etPalavrasChave.setText(artigo.palavrasChave ?: "")
        etPaginas.setText(artigo.paginas?.toString() ?: "")
        ratingBar.rating = artigo.nota?.toFloat() ?: 0f
        cbJaLido.isChecked = artigo.jaLido
    }

    private fun salvarAlteracoes() {
        val atualizado = artigo?.copy(
            titulo = etTitulo.text.toString().trim(),
            autores = etAutores.text.toString().trim().ifEmpty { null },
            periodico = etPeriodico.text.toString().trim().ifEmpty { null },
            anoPublicacao = etAno.text.toString().trim().toIntOrNull(),
            doi = etDoi.text.toString().trim().ifEmpty { null },
            link = etLink.text.toString().trim().ifEmpty { null },
            resumo = etResumo.text.toString().trim().ifEmpty { null },
            palavrasChave = etPalavrasChave.text.toString().trim().ifEmpty { null },
            paginas = etPaginas.text.toString().toIntOrNull(),
            nota = ratingBar.rating.toInt().takeIf { it > 0 },
            jaLido = cbJaLido.isChecked
        )

        atualizado?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val dao = ArtigoDatabase.getInstance(requireContext()).artigoDao()
                dao.atualizar(it)

                // Busca o registro atualizado para garantir a última versão do título
                val atualizadoDb = dao.buscarPorId(it.id)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.msg_artigo_atualizado_sucesso), Toast.LENGTH_SHORT).show()

                    // Atualiza a toolbar da Activity imediatamente
                    val activity = requireActivity()
                    if (activity is androidx.appcompat.app.AppCompatActivity) {
                        activity.supportActionBar?.title = atualizadoDb?.titulo ?: it.titulo
                    }

                    // Opcional: força o ViewPager a notificar os fragments (se necessário)
                    // parentFragmentManager.fragments.forEach { f -> f.onResume() }
                }
            }
        }
    }

    companion object {
        fun newInstance(artigoId: Int) = EditarArtigoFragment().apply {
            arguments = Bundle().apply { putInt("artigoId", artigoId) }
        }
    }
}