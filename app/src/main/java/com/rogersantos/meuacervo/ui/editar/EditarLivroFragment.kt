package com.rogersantos.meuacervo.ui.editar

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.AppDatabase
import com.rogersantos.meuacervo.data.database.LivroDatabase
import com.rogersantos.meuacervo.data.model.Livro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditarLivroFragment : Fragment(R.layout.fragment_editar_livro) {

    private var livroId: Int = -1
    private var livro: Livro? = null

    private lateinit var ivCapa: ImageView
    private lateinit var btnAlterarCapa: MaterialButton

    private lateinit var etTitulo: TextInputEditText
    private lateinit var etAutores: TextInputEditText
    private lateinit var etCategoria: TextInputEditText
    private lateinit var etEditora: TextInputEditText
    private lateinit var etAno: TextInputEditText
    private lateinit var etPaginas: TextInputEditText
    private lateinit var etDescricao: TextInputEditText
    private lateinit var etPalavrasChave: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var swJaLido: SwitchMaterial
    private lateinit var ivLidoAnim: ImageView
    private lateinit var tilDataAquisicao: TextInputLayout
    private lateinit var etDataAquisicao: TextInputEditText
    private lateinit var btnSalvar: MaterialButton

    private var capaSelecionadaUri: Uri? = null

    private val selecionarImagem = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            capaSelecionadaUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_capa_placeholder)
                .error(R.drawable.ic_capa_placeholder)
                .centerCrop()
                .into(ivCapa)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        livroId = arguments?.getInt("livroId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivCapa = view.findViewById(R.id.ivCapa)
        btnAlterarCapa = view.findViewById(R.id.btnAlterarCapa)

        etTitulo = view.findViewById(R.id.etTitulo)
        etAutores = view.findViewById(R.id.etAutores)
        etCategoria = view.findViewById(R.id.etCategoria)
        etEditora = view.findViewById(R.id.etEditora)
        etAno = view.findViewById(R.id.etAno)
        etPaginas = view.findViewById(R.id.etPaginas)
        etDescricao = view.findViewById(R.id.etDescricao)
        etPalavrasChave = view.findViewById(R.id.etPalavrasChave)
        ratingBar = view.findViewById(R.id.ratingBar)
        swJaLido = view.findViewById(R.id.swJaLido)
        ivLidoAnim = view.findViewById(R.id.ivLidoAnim)
        tilDataAquisicao = view.findViewById(R.id.tilDataAquisicao)
        etDataAquisicao = view.findViewById(R.id.etDataAquisicao)
        btnSalvar = view.findViewById(R.id.btnSalvar)

        ratingBar.progressTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.primaryColor)

        btnAlterarCapa.setOnClickListener { selecionarImagem.launch("image/*") }

        // DatePicker ao tocar no campo ou no ícone do TextInputLayout
        etDataAquisicao.setOnClickListener { abrirDatePicker() }
        tilDataAquisicao.setStartIconOnClickListener { abrirDatePicker() }

        // Animação do ícone “lido”
        swJaLido.setOnCheckedChangeListener { _, isChecked ->
            animarIconeLido(isChecked)
        }

        carregarLivro()

        btnSalvar.setOnClickListener { salvarAlteracoes() }
    }

    private fun abrirDatePicker() {
        val cal = Calendar.getInstance()
        val atual = etDataAquisicao.text?.toString()?.trim().orEmpty()
        if (atual.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsed = sdf.parse(atual)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) { /* ignora parsing */ }
        }

        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                cal.set(y, m, d)
                val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val data = fmt.format(cal.time)
                etDataAquisicao.setText(data)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun animarIconeLido(isChecked: Boolean) {
        val corAtivo = ContextCompat.getColor(requireContext(), R.color.primaryColor)
        val corInativo = ContextCompat.getColor(requireContext(), R.color.disabledIcon)

        ivLidoAnim.setColorFilter(if (isChecked) corAtivo else corInativo)

        // Pulse animation (escala + fade suave)
        ivLidoAnim.apply {
            scaleX = 0.9f
            scaleY = 0.9f
            alpha = 0.8f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(220)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun carregarLivro() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).livroDao()
            val resultado = dao.buscarPorId(livroId)

            withContext(Dispatchers.Main) {
                resultado?.let {
                    livro = it
                    preencherCampos(it)
                }
            }
        }
    }

    private fun preencherCampos(livro: Livro) {
        val url = livro.capaPath ?: livro.urlCapa
        if (!url.isNullOrBlank()) {
            val finalUrl = if (url.startsWith("//")) "https:$url" else url
            Glide.with(this)
                .load(finalUrl)
                .placeholder(R.drawable.ic_capa_placeholder)
                .error(R.drawable.ic_capa_placeholder)
                .centerCrop()
                .into(ivCapa)
        } else {
            ivCapa.setImageResource(R.drawable.ic_capa_placeholder)
        }

        etTitulo.setText(livro.titulo)
        etAutores.setText(livro.autores ?: "")
        etCategoria.setText(livro.categoria ?: "")
        etEditora.setText(livro.editora ?: "")
        etAno.setText(livro.dataPublicacao ?: "")
        etPaginas.setText(livro.paginas?.toString() ?: "")
        etDescricao.setText(livro.descricao ?: "")
        etPalavrasChave.setText(livro.palavrasChave ?: "")
        ratingBar.rating = livro.nota?.toFloat() ?: 0f
        swJaLido.isChecked = livro.jaLido
        etDataAquisicao.setText(livro.dataAquisicao ?: "")

        // Ajusta ícone conforme estado inicial
        animarIconeLido(swJaLido.isChecked)
    }

    private fun salvarAlteracoes() {
        val novoCapaPath = capaSelecionadaUri?.toString() ?: livro?.capaPath

        val atualizado = livro?.copy(
            titulo = etTitulo.text.toString().trim(),
            autores = etAutores.text.toString().trim().ifEmpty { null },
            categoria = etCategoria.text.toString().trim().ifEmpty { null },
            editora = etEditora.text.toString().trim().ifEmpty { null },
            dataPublicacao = etAno.text.toString().trim().ifEmpty { null },
            paginas = etPaginas.text.toString().toIntOrNull(),
            descricao = etDescricao.text.toString().trim().ifEmpty { null },
            palavrasChave = etPalavrasChave.text.toString().trim().ifEmpty { null },
            nota = ratingBar.rating.toInt().takeIf { it > 0 },
            jaLido = swJaLido.isChecked,
            dataAquisicao = etDataAquisicao.text.toString().trim().ifEmpty { null },
            capaPath = novoCapaPath
        )

        atualizado?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val dao = LivroDatabase.getInstance(requireContext()).livroDao()
                dao.atualizar(it)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.msg_livro_atualizado_sucesso), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        fun newInstance(livroId: Int) = EditarLivroFragment().apply {
            arguments = Bundle().apply { putInt("livroId", livroId) }
        }
    }
}