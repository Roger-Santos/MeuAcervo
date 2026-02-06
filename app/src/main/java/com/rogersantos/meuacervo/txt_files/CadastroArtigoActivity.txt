package com.rogersantos.meuacervo.ui.cadastro

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.rogersantos.meuacervo.data.model.CrossRefIssnResponse
import com.rogersantos.meuacervo.data.model.CrossRefResponse
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.ArtigoDatabase
import com.rogersantos.meuacervo.data.database.PeriodicoDatabase
import com.rogersantos.meuacervo.data.model.Artigo
import com.rogersantos.meuacervo.data.model.Periodico
import com.rogersantos.meuacervo.data.network.ApiClient
import com.rogersantos.meuacervo.util.logApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import retrofit2.Response

class CadastroArtigoActivity : AppCompatActivity() {

    // UI: Periódico como AutoCompleteTextView e ISSN
    private lateinit var tilPeriodico: TextInputLayout
    private lateinit var actPeriodico: AutoCompleteTextView
    private lateinit var etIssn: EditText
    private lateinit var btnBuscarIssn: Button

    // UI: Campos do artigo
    private lateinit var etTitulo: EditText
    private lateinit var etDoi: EditText
    private lateinit var btnBuscarDoi: Button
    private lateinit var etAutores: EditText
    private lateinit var etAnoPublicacao: EditText
    private lateinit var etLink: EditText
    private lateinit var etResumo: EditText
    private lateinit var etPalavrasChave: EditText
    private lateinit var etPaginas: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var cbJaLido: CheckBox
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button

    // Estado
    private var artigoId: Int? = null
    private var artigoExistente: Artigo? = null

    // Periódicos salvos para popular o AutoComplete
    private var periodicosSalvos: List<Periodico> = emptyList()
    private lateinit var periodicosAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_artigo)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarCadastroArtigo)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_cadastrar_artigo)
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationOnClickListener { finish() }

        // Bind dos campos
        tilPeriodico = findViewById(R.id.tilPeriodico) // TextInputLayout no XML
        actPeriodico = findViewById(R.id.actPeriodico) // AutoCompleteTextView no XML
        etIssn = findViewById(R.id.etIssn)
        btnBuscarIssn = findViewById(R.id.btnBuscarIssn)

        etTitulo = findViewById(R.id.etTituloArtigo)
        etDoi = findViewById(R.id.etDoi)
        btnBuscarDoi = findViewById(R.id.btnBuscarDoi)
        etAutores = findViewById(R.id.etAutoresArtigo)
        etAnoPublicacao = findViewById(R.id.etAnoPublicacaoArtigo)
        etLink = findViewById(R.id.etLinkArtigo)
        etResumo = findViewById(R.id.etResumo)
        etPalavrasChave = findViewById(R.id.etPalavrasChaveArtigo)
        etPaginas = findViewById(R.id.etPaginasArtigo)
        ratingBar = findViewById(R.id.ratingBarArtigo)
        cbJaLido = findViewById(R.id.cbJaLidoArtigo)
        btnSalvar = findViewById(R.id.btnSalvarArtigo)
        btnCancelar = findViewById(R.id.btnCancelarArtigo)

        // Carregar periódicos salvos para o AutoComplete
        carregarPeriodicos()

        // Se veio com ID (edição)
        artigoId = intent.getIntExtra("artigoId", -1).takeIf { it != -1 }
        artigoId?.let { carregarArtigo(it) }

        // Ações
        btnSalvar.setOnClickListener { salvarArtigo() }
        btnCancelar.setOnClickListener { finish() }

        btnBuscarDoi.setOnClickListener {
            val doi = etDoi.text.toString().trim()
            if (doi.isNotEmpty()) buscarPorDoi(doi)
            else Toast.makeText(this, getString(R.string.msg_digite_doi_valido), Toast.LENGTH_SHORT).show()
        }

        btnBuscarIssn.setOnClickListener {
            val issn = etIssn.text.toString().trim()
            if (issn.isNotEmpty()) buscarPorIssn(issn)
            else Toast.makeText(this, getString(R.string.msg_digite_issn_valido), Toast.LENGTH_SHORT).show()
        }

        // Ao selecionar uma sugestão de periódico, preencher o ISSN
        actPeriodico.setOnItemClickListener { _, _, position, _ ->
            val nomeSelecionado = periodicosAdapter.getItem(position)
            val p = periodicosSalvos.firstOrNull { it.nome == nomeSelecionado }
            etIssn.setText(p?.issn.orEmpty())
        }
    }

    private fun carregarPeriodicos() {
        lifecycleScope.launch {
            val dao = PeriodicoDatabase.getInstance(applicationContext).periodicoDao()
            periodicosSalvos = withContext(Dispatchers.IO) { dao.listarTodos() }

            val nomes = periodicosSalvos.map { it.nome }
            periodicosAdapter = ArrayAdapter(this@CadastroArtigoActivity, android.R.layout.simple_list_item_1, nomes)
            actPeriodico.setAdapter(periodicosAdapter)
        }
    }

    private fun carregarArtigo(id: Int) {
        lifecycleScope.launch {
            val dao = ArtigoDatabase.getInstance(applicationContext).artigoDao()
            val artigo = withContext(Dispatchers.IO) { dao.buscarPorId(id) }
            artigo?.let {
                artigoExistente = it
                preencherCampos(it)
            }
        }
    }

    private fun preencherCampos(artigo: Artigo) {
        etTitulo.setText(artigo.titulo)
        etAutores.setText(artigo.autores)
        etAnoPublicacao.setText(artigo.anoPublicacao?.toString() ?: "")
        etDoi.setText(artigo.doi)
        etLink.setText(artigo.link)
        etResumo.setText(artigo.resumo)
        etPalavrasChave.setText(artigo.palavrasChave)
        etPaginas.setText(artigo.paginas?.toString() ?: "")
        ratingBar.rating = artigo.nota?.toFloat() ?: 0f
        cbJaLido.isChecked = artigo.jaLido

        // Preencher periódico no campo de texto
        actPeriodico.setText(artigo.periodico ?: "", false)

        // Se existir um periódico salvo com esse nome, preencher ISSN
        artigo.periodico?.let { nome ->
            val p = periodicosSalvos.firstOrNull { it.nome == nome }
            if (p?.issn != null) etIssn.setText(p.issn)
        }
    }

    private fun montarArtigoDaTela(): Artigo {
        val titulo = etTitulo.text.toString().trim()
        val autores = etAutores.text.toString().trim().ifEmpty { null }
        val periodico = actPeriodico.text.toString().trim().ifEmpty { null }
        val ano = etAnoPublicacao.text.toString().trim().toIntOrNull()
        val doi = etDoi.text.toString().trim().ifEmpty { null }
        val link = etLink.text.toString().trim().ifEmpty { null }
        val resumo = etResumo.text.toString().trim().ifEmpty { null }
        val palavrasChave = etPalavrasChave.text.toString().trim().ifEmpty { null }
        val paginas = etPaginas.text.toString().trim().toIntOrNull()
        val nota = ratingBar.rating.toInt().takeIf { it > 0 }
        val jaLido = cbJaLido.isChecked

        return Artigo(
            id = artigoExistente?.id ?: 0,
            titulo = titulo,
            autores = autores,
            periodico = periodico,
            anoPublicacao = ano,
            doi = doi,
            link = link,
            resumo = resumo,
            palavrasChave = palavrasChave,
            paginas = paginas,
            nota = nota,
            jaLido = jaLido
        )
    }

    private fun salvarArtigo() {
        val artigo = montarArtigoDaTela()
        lifecycleScope.launch {
            // Opcional: salvar novo periódico digitado se não existir
            garantirPeriodicoSalvo(
                nome = artigo.periodico,
                issn = etIssn.text.toString().trim().ifEmpty { null }
            )

            val dao = ArtigoDatabase.getInstance(applicationContext).artigoDao()
            withContext(Dispatchers.IO) {
                if (artigoExistente == null) dao.inserir(artigo) else dao.atualizar(artigo)
            }
            Toast.makeText(this@CadastroArtigoActivity, getString(R.string.msg_artigo_salvo_sucesso), Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private suspend fun garantirPeriodicoSalvo(nome: String?, issn: String?) {
        if (nome.isNullOrBlank()) return
        val dao = PeriodicoDatabase.getInstance(applicationContext).periodicoDao()
        val existente = withContext(Dispatchers.IO) { dao.buscarPorNome(nome) }
        if (existente == null) {
            withContext(Dispatchers.IO) { dao.inserir(Periodico(nome = nome, issn = issn)) }
            // recarrega lista local para sugerir corretamente nas próximas interações
            carregarPeriodicos()
        }
    }

    private fun buscarPorDoi(doi: String) {
        lifecycleScope.launch {
            val encontrado = buscarSequencialAny(
                chamadas = listOf({ ApiClient.crossRef.getWork(doi) })
            ) { body ->
                if (body is CrossRefResponse && body.message != null) {
                    val msg = body.message
                    etTitulo.setText(msg.title?.firstOrNull().orEmpty())
                    etAutores.setText(msg.author?.joinToString(", ") { "${it.given} ${it.family}" }.orEmpty())

                    val ano = msg.publishedPrint?.dateParts?.firstOrNull()?.firstOrNull()
                        ?: msg.publishedOnline?.dateParts?.firstOrNull()?.firstOrNull()
                    etAnoPublicacao.setText(ano?.toString().orEmpty())

                    etDoi.setText(msg.doi.orEmpty())
                    etLink.setText(msg.url.orEmpty())
                    etPaginas.setText(msg.page.orEmpty())
                    etResumo.setText(msg.abstractText.orEmpty())

                    val periodicoNome = msg.containerTitle?.firstOrNull()?.trim().orEmpty()
                    if (periodicoNome.isNotEmpty()) {
                        actPeriodico.setText(periodicoNome, false)
                        val p = periodicosSalvos.firstOrNull { it.nome.equals(periodicoNome, true) }
                        etIssn.setText(p?.issn.orEmpty())
                    }

                    Toast.makeText(this@CadastroArtigoActivity, getString(R.string.msg_artigo_encontrado_crossref), Toast.LENGTH_SHORT).show()
                    true
                } else false
            }

            if (!encontrado) {
                Toast.makeText(this@CadastroArtigoActivity, getString(R.string.msg_doi_nao_encontrado), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarPorIssn(issn: String) {
        lifecycleScope.launch {
            val encontrado = buscarSequencialAny(
                chamadas = listOf({ ApiClient.crossRefIssn.getJournal(issn) })
            ) { body ->
                if (body is CrossRefIssnResponse && body.message != null) {
                    val msg = body.message

                    val periodicoNome = msg.title?.toString()?.trim().orEmpty()
                    if (periodicoNome.isNotEmpty()) {
                        actPeriodico.setText(periodicoNome, false)
                    }

                    val issnRetornado = msg.issnList?.firstOrNull()?.trim().orEmpty()
                    if (issnRetornado.isNotEmpty()) {
                        etIssn.setText(issnRetornado)
                    }

                    // Retorna true porque a resposta possui message válida
                    true
                } else false
            }

            if (!encontrado) {
                Toast.makeText(this@CadastroArtigoActivity, getString(R.string.msg_issn_nao_encontrado), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Utilitário correto: trata Response<...> e passa o body desserializado ao handler
    private suspend fun buscarSequencialAny(
        chamadas: List<suspend () -> Response<out Any?>>,
        handler: (Any?) -> Boolean
    ): Boolean {
        for (call in chamadas) {
            try {
                val response = withContext(Dispatchers.IO) { call() }
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        try { logApiResponse("buscarSequencialAny", body) } catch (_: Exception) {}
                        val ok = handler(body)
                        if (ok) return true
                    }
                }
            } catch (e: Exception) {
                try { android.util.Log.e("buscarSequencialAny", "Exceção: ${e.message}", e) } catch (_: Exception) {}
            }
        }
        return false
    }
}