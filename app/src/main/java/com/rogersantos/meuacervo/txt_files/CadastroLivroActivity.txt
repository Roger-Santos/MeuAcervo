package com.rogersantos.meuacervo.ui.cadastro

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexboxLayout
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.rogersantos.meuacervo.data.model.CrossRefItem
import com.rogersantos.meuacervo.data.model.CrossRefResponse
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.ui.scanner.ScannerIsbnActivity
import com.rogersantos.meuacervo.data.model.VolumeInfo
import com.rogersantos.meuacervo.data.model.VolumesResponse
import com.rogersantos.meuacervo.util.buscarSequencialAny
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.database.LivroDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Room / Retrofit
import com.rogersantos.meuacervo.data.model.Livro
import com.rogersantos.meuacervo.data.network.ApiClient
import com.rogersantos.meuacervo.data.network.OpenLibraryResponse

class CadastroLivroActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CadastroLivroActivity"
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002
        private const val REQUEST_ISBN = 1100

        private const val PREFS_NAME = "meuacervo_prefs"
        private const val KEY_FAB_X = "fab_x"
        private const val KEY_FAB_Y = "fab_y"
    }

    // UI
    private lateinit var containerPalavras: FlexboxLayout
    private lateinit var layoutCampos: LinearLayout
    private lateinit var ivCapa: ImageView
    private lateinit var btnCaptureContraCapa: ImageButton

    private lateinit var etIsbn: EditText
    private lateinit var btnIsbnFetch: ImageButton
    private lateinit var btnIsbnScan: ImageButton

    private lateinit var etTitulo: EditText
    private lateinit var etCategoria: EditText
    private lateinit var etAutores: EditText
    private lateinit var etEditora: EditText
    private lateinit var etDataPublicacao: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etPalavrasChave: EditText
    private lateinit var etPaginas: EditText

    private lateinit var ratingBar: RatingBar

    // Campos embutidos no layout
    private lateinit var cbJaLido: CheckBox
    private lateinit var etDataAquisicao: EditText

    private lateinit var progressLoading: ProgressBar
    private lateinit var layoutConteudo: View

    // FAB
    private lateinit var fabCard: CardView
    private lateinit var fabSalvar: View

    // Outros
    private val mapaBlocos = mutableMapOf<String, FrameLayout>()
    private val palavrasUtilizadas = mutableListOf<String>()
    private var palavraSelecionada: TextView? = null

    private var capaPath: String? = null
    private var ultimaUrlCapa: String? = null
    private var isCapaSelection: Boolean = false

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private lateinit var database: LivroDatabase
    private lateinit var livroDao: LivroDao
    private var campoFocado: EditText? = null

    private var dX = 0f
    private var dY = 0f
    private lateinit var prefs: SharedPreferences

    // --- Helper data class para mesclagem parcial ---
    private data class PartialBook(
        val title: String? = null,
        val categories: String? = null,
        val authors: String? = null,
        val publisher: String? = null,
        val publishedDate: String? = null,
        val description: String? = null,
        val pageCount: Int? = null,
        val thumbnailUrl: String? = null
    ) {
        fun merge(other: PartialBook?): PartialBook {
            if (other == null) return this
            return PartialBook(
                title = this.title ?: other.title,
                categories = this.categories ?: other.categories,
                authors = this.authors ?: other.authors,
                publisher = this.publisher ?: other.publisher,
                publishedDate = this.publishedDate ?: other.publishedDate,
                description = this.description ?: other.description,
                pageCount = this.pageCount ?: other.pageCount,
                thumbnailUrl = this.thumbnailUrl ?: other.thumbnailUrl
            )
        }

        fun isAllEmpty(): Boolean {
            return title.isNullOrBlank()
                    && categories.isNullOrBlank()
                    && authors.isNullOrBlank()
                    && publisher.isNullOrBlank()
                    && publishedDate.isNullOrBlank()
                    && description.isNullOrBlank()
                    && pageCount == null
                    && thumbnailUrl.isNullOrBlank()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_livro)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_cadastrar_livro)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        toolbar.setTitleTextColor(Color.WHITE)

        // binds
        containerPalavras = findViewById(R.id.containerPalavras)
        layoutCampos = findViewById(R.id.mainLayout)
        ivCapa = findViewById(R.id.ivCapa)
        btnCaptureContraCapa = findViewById(R.id.btnCaptureContraCapa)

        etIsbn = findViewById(R.id.etIsbn)
        btnIsbnFetch = findViewById(R.id.btnIsbnFetch)
        btnIsbnScan = findViewById(R.id.btnIsbnScan)

        etTitulo = findViewById(R.id.etTitulo)
        etCategoria = findViewById(R.id.etCategoria)
        etAutores = findViewById(R.id.etAutores)
        etEditora = findViewById(R.id.etEditora)
        etDataPublicacao = findViewById(R.id.etDataPublicacao)
        etDescricao = findViewById(R.id.etDescricao)
        etPalavrasChave = findViewById(R.id.etPalavrasChave)
        etPaginas = findViewById(R.id.etPaginas)

        ratingBar = findViewById(R.id.ratingBar)

        // novos campos do layout
        cbJaLido = findViewById(R.id.cbJaLido)
        etDataAquisicao = findViewById(R.id.etDataAquisicao)

        progressLoading = findViewById(R.id.progressLoading)
        layoutConteudo = findViewById(R.id.layoutConteudo)

        database = LivroDatabase.getInstance(applicationContext)
        livroDao = database.livroDao()

        // FAB
        fabCard = findViewById(R.id.fabCard)
        fabSalvar = findViewById(R.id.fabSalvar)

        // Salvar diretamente (sem popups)
        fabSalvar.setOnClickListener {
            confirmarESalvar()
        }

        // restaurar posição do FAB
        val savedX = prefs.getFloat(KEY_FAB_X, Float.NaN)
        val savedY = prefs.getFloat(KEY_FAB_Y, Float.NaN)
        if (!savedX.isNaN() && !savedY.isNaN()) {
            fabCard.post {
                val parent = fabCard.parent as View
                val maxX = (parent.width - fabCard.width).toFloat()
                val maxY = (parent.height - fabCard.height).toFloat()
                fabCard.x = savedX.coerceIn(0f, maxX)
                fabCard.y = savedY.coerceIn(0f, maxY)
            }
        }

        // arrastar FAB
        fabCard.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val parent = v.parent as View
                    var newX = event.rawX + dX
                    var newY = event.rawY + dY
                    val maxX = (parent.width - v.width).toFloat()
                    val maxY = (parent.height - v.height).toFloat()
                    v.x = newX.coerceIn(0f, maxX)
                    v.y = newY.coerceIn(0f, maxY)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    true
                }
                else -> false
            }
        }

        // listeners de imagem
        ivCapa.setOnClickListener {
            isCapaSelection = true
            mostrarEscolhaImagemComUrl()
        }
        btnCaptureContraCapa.setOnClickListener {
            isCapaSelection = false
            mostrarEscolhaImagem()
        }

        // Abrir calendário ao clicar na data de aquisição
        etDataAquisicao.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { abrirDatePickerEt(this) }
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) abrirDatePickerEt(v as EditText)
            }
        }

        setupIsbnButtons()
        setupCamposDragInsert()
    }

    private fun abrirDatePickerEt(targetEt: EditText) {
        val cal = Calendar.getInstance()

        // Se já houver data preenchida, usa como base
        val atual = targetEt.text.toString().trim()
        if (atual.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsed = sdf.parse(atual)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) { }
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, y, m, d ->
                val selCal = Calendar.getInstance().apply { set(y, m, d) }
                val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                targetEt.setText(fmt.format(selCal.time))
            },
            year, month, day
        ).show()
    }

    private fun setupIsbnButtons() {
        btnIsbnFetch.setOnClickListener {
            val raw = etIsbn.text.toString().trim()
            if (raw.isEmpty()) {
                etIsbn.error = getString(R.string.msg_digite_isbn)
                etIsbn.requestFocus()
            } else {
                btnIsbnFetch.isEnabled = false
                btnIsbnFetch.alpha = 0.6f
                lifecycleScope.launch {
                    try { fetchBookByIsbn(raw) } finally {
                        btnIsbnFetch.isEnabled = true
                        btnIsbnFetch.alpha = 1f
                    }
                }
            }
        }
        btnIsbnScan.setOnClickListener {
            val intent = Intent(this, ScannerIsbnActivity::class.java)
            startActivityForResult(intent, REQUEST_ISBN)
        }
    }

    private fun setupCamposDragInsert() {
        val campos = listOf(
            "Título" to etTitulo,
            "Categoria" to etCategoria,
            "Autores" to etAutores,
            "Editora" to etEditora,
            "Data de Publicação" to etDataPublicacao,
            "Descrição" to etDescricao,
            "Palavras-chave" to etPalavrasChave
        )
        campos.forEach { (nome, campo) ->
            configurarCampo(campo, nome)
            campo.setOnFocusChangeListener { _, hasFocus -> campoFocado = if (hasFocus) campo else campoFocado }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cadastro_livro, menu)
        menu?.findItem(R.id.action_save)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_save -> { confirmarESalvar(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarEscolhaImagem() {
        val options = arrayOf(getString(R.string.opcao_tirar_foto), getString(R.string.opcao_selecionar_galeria))
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_escolha_opcao))
            .setItems(options) { _, which -> if (which == 0) abrirCamera() else abrirGaleria() }
            .show()
    }

    private fun mostrarEscolhaImagemComUrl() {
        val options = arrayOf(getString(R.string.opcao_tirar_foto), getString(R.string.opcao_selecionar_galeria), getString(R.string.opcao_inserir_url))
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_alterar_capa))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> abrirCamera()
                    1 -> abrirGaleria()
                    2 -> mostrarDialogoInserirUrl()
                }
            }
            .show()
    }

    private fun mostrarDialogoInserirUrl() {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_url_capa)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_inserir_url_capa))
            .setView(input)
            .setPositiveButton(getString(R.string.btn_ok)) { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    ultimaUrlCapa = url
                    showLoading()
                    exibirCapaPorUrlWithListener(url)
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val local = downloadAndSaveImage(url)
                            if (local != null) capaPath = local
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro salvando URL como imagem", e)
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.btn_cancelar), null)
            .show()
    }

    private fun abrirCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    private fun abrirGaleria() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) { isCapaSelection = false; return }

        when (requestCode) {
            REQUEST_CAMERA -> {
                val bitmap = data.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val local = salvarImagemLocalFromBitmap(it)
                    local?.let { path -> capaPath = path; carregarCapaLocal(path) }
                    if (!isCapaSelection) processarImagemBitmap(it)
                }
                isCapaSelection = false
            }
            REQUEST_GALLERY -> {
                val uri: Uri? = data.data
                uri?.let {
                    val bitmap = contentResolver.openInputStream(it)?.use { s -> BitmapFactory.decodeStream(s) }
                    bitmap?.let { bmp ->
                        val local = salvarImagemLocalFromBitmap(bmp)
                        local?.let { path -> capaPath = path; carregarCapaLocal(path) }
                        if (!isCapaSelection) processarImagemBitmap(bmp)
                    }
                }
                isCapaSelection = false
            }
            REQUEST_ISBN -> {
                val isbn = data.getStringExtra("isbn")
                isbn?.let {
                    etIsbn.setText(it)
                    lifecycleScope.launch {
                        btnIsbnFetch.isEnabled = false
                        btnIsbnFetch.alpha = 0.6f
                        try { fetchBookByIsbn(it) } finally {
                            btnIsbnFetch.isEnabled = true
                            btnIsbnFetch.alpha = 1f
                        }
                    }
                }
                isCapaSelection = false
            }
        }
    }

    private fun salvarImagemLocalFromBitmap(bitmap: Bitmap): String? {
        return try {
            val file = File(filesDir, "capa_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erro salvando imagem", e)
            null
        }
    }

    private fun carregarCapaLocal(path: String) {
        try {
            showLoading()
            Glide.with(this)
                .load(path)
                .centerCrop()
                .placeholder(R.drawable.ic_capa_placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.w(TAG, "Glide onLoadFailed: ${e?.message}")
                        hideLoading()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        hideLoading()
                        return false
                    }
                })
                .into(ivCapa)
        } catch (e: Exception) {
            Log.e(TAG, "carregarCapaLocal: $e")
            ivCapa.setImageResource(R.drawable.ic_capa_placeholder)
            hideLoading()
        }
    }

    private fun exibirCapaPorUrlWithListener(url: String) {
        if (url.isBlank()) {
            hideLoading()
            return
        }
        runOnUiThread {
            Glide.with(this@CadastroLivroActivity)
                .load(if (url.startsWith("//")) "https:$url" else url)
                .placeholder(R.drawable.ic_capa_placeholder)
                .error(R.drawable.ic_capa_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.w(TAG, "Glide onLoadFailed (url): ${e?.message}")
                        hideLoading()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        hideLoading()
                        return false
                    }
                })
                .into(ivCapa)
        }
    }

    private fun processarImagemBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText -> exibirPalavras(visionText.text) }
            .addOnFailureListener { Toast.makeText(this, getString(R.string.msg_erro_reconhecer_texto), Toast.LENGTH_SHORT).show() }
    }

    private fun exibirPalavras(texto: String) {
        containerPalavras.removeAllViews()
        mapaBlocos.clear()
        val palavras = texto.trim().split("\\s+".toRegex()).map { it.trim() }.filter { it.isNotBlank() }
        palavras.forEach { palavra ->
            val bloco = criarBlocoPalavra(palavra)
            mapaBlocos[palavra] = bloco
            containerPalavras.addView(bloco)
        }
        containerPalavras.visibility = View.VISIBLE
    }

    private fun criarBlocoPalavra(palavra: String): FrameLayout {
        val context = this
        val textView = TextView(context).apply {
            text = palavra
            tag = palavra
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setBackgroundResource(R.drawable.bg_palavra)
            isClickable = true
            isFocusable = true

            setOnLongClickListener {
                palavraSelecionada = this
                val item = ClipData.Item(text)
                val dragData = ClipData(text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                val shadow = View.DragShadowBuilder(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    startDragAndDrop(dragData, shadow, null, 0)
                } else {
                    @Suppress("DEPRECATION")
                    startDrag(dragData, shadow, null, 0)
                }
                true
            }

            setOnClickListener {
                campoFocado?.apply {
                    append(" $palavra")
                    mapaBlocos.remove(palavra)?.let { containerPalavras.removeView(it) }
                    if (!palavrasUtilizadas.any { it.equals(palavra, true) }) {
                        palavrasUtilizadas.add(palavra)
                    }
                }
            }
        }
        return FrameLayout(context).apply { addView(textView) }
    }

    private fun configurarCampo(campo: EditText, nomeCampo: String) {
        campo.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val palavra = event.clipData.getItemAt(0).text.toString().trim()
                    (v as EditText).append("$palavra ")
                    mapaBlocos.remove(palavra)?.let { containerPalavras.removeView(it) }
                    if (!palavrasUtilizadas.any { it.equals(palavra, true) }) {
                        palavrasUtilizadas.add(palavra)
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun validarCampos(): Boolean {
        val titulo = etTitulo.text.toString().trim()
        if (titulo.isEmpty()) {
            etTitulo.error = getString(R.string.msg_titulo_obrigatorio)
            etTitulo.requestFocus()
            return false
        }
        return true
    }

    private fun montarLivroDaTela(): Livro {
        val titulo = etTitulo.text.toString().trim()
        val categoria = etCategoria.text.toString().trim().ifEmpty { null }
        val autores = etAutores.text.toString().trim().ifEmpty { null }
        val editora = etEditora.text.toString().trim().ifEmpty { null }
        val dataPublicacao = etDataPublicacao.text.toString().trim().ifEmpty { null }
        val descricao = etDescricao.text.toString().trim().ifEmpty { null }
        val paginas = etPaginas.text.toString().trim().toIntOrNull()
        val nota = ratingBar.rating.toInt().takeIf { it > 0 }

        val palavrasChaveRaw = etPalavrasChave.text.toString().trim().ifEmpty {
            if (palavrasUtilizadas.isNotEmpty()) palavrasUtilizadas.joinToString(", ") else null
        }
        val palavrasChave = palavrasChaveRaw
            ?.replace(Regex("\\s*\\n\\s*"), ", ")
            ?.replace(Regex("\\s*,\\s*"), ", ")

        val jaLido = cbJaLido.isChecked
        val dataAquisicao = etDataAquisicao.text.toString().trim().ifEmpty { null }

        return Livro(
            titulo = titulo,
            categoria = categoria,
            autores = autores,
            editora = editora,
            dataPublicacao = dataPublicacao,
            paginas = paginas,
            descricao = descricao,
            urlCapa = ultimaUrlCapa,
            capaPath = capaPath,
            palavrasChave = palavrasChave,
            nota = nota,
            jaLido = jaLido,
            dataAquisicao = dataAquisicao
        )
    }

    private fun confirmarESalvar() {
        if (!validarCampos()) return
        val livro = montarLivroDaTela()
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { livroDao.inserir(livro) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_livro_salvo), Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, getString(R.string.msg_erro_salvar_livro), e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_erro_salvar_livro) + " ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Extração parcial por API (OpenLibrary: busca de autores sequencial) ---
    private fun fromOpenLibraryBasic(resp: OpenLibraryResponse?): PartialBook? {
        if (resp == null) return null
        val title = resp.title
        val publisher = resp.publishers?.firstOrNull()
        val publishedDate = resp.publishDate
        val description = when (resp.description) {
            is String -> resp.description as String
            is Map<*, *> -> (resp.description as Map<*, *>)["value"] as? String
            else -> null
        }
        val thumbnail = resp.covers?.firstOrNull()?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" }

        return PartialBook(
            title = title,
            categories = null,
            authors = null, // autores serão resolvidos separadamente
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = null,
            thumbnailUrl = thumbnail
        )
    }

    // Opção B: busca sequencial de autores (mais simples, sem async)
    private suspend fun fetchAuthorsFromOpenLibrary(resp: OpenLibraryResponse?, limit: Int = 5): String? {
        if (resp?.authors.isNullOrEmpty()) return null

        val refs = resp!!.authors!!.take(limit)
        val nomes = mutableListOf<String>()

        for (authorRef in refs) {
            val rawKey = authorRef.key?.removePrefix("/authors/")?.trim()
            if (rawKey.isNullOrBlank()) continue
            try {
                val detalhe = withContext(Dispatchers.IO) {
                    try { ApiClient.openLibraryAuthor.getAuthor(rawKey).body() } catch (e: Exception) { null }
                }
                detalhe?.name?.let { nomes.add(it) }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao buscar autor $rawKey: ${e.message}")
            }
        }
        return if (nomes.isNotEmpty()) nomes.joinToString(", ") else null
    }

    private fun fromGoogleVolumes(info: VolumeInfo?): PartialBook? {
        if (info == null) return null
        val categories = info.categories?.joinToString(", ")
        val authors = info.authors?.joinToString(", ")
        val thumbnail = info.imageLinks?.thumbnail ?: info.imageLinks?.smallThumbnail
        return PartialBook(
            title = info.title,
            categories = categories,
            authors = authors,
            publisher = info.publisher,
            publishedDate = info.publishedDate,
            description = info.description,
            pageCount = info.pageCount,
            thumbnailUrl = thumbnail
        )
    }

    private fun fromCrossRef(item: CrossRefItem?): PartialBook? {
        if (item == null) return null
        val authors = item.author?.joinToString(", ") { "${it.given} ${it.family}" }
        val title = item.title?.firstOrNull()
        val publishedDate = item.created?.dateTime
        return PartialBook(
            title = title,
            categories = null,
            authors = authors,
            publisher = item.publisher,
            publishedDate = publishedDate,
            description = item.abstractText,
            pageCount = null,
            thumbnailUrl = null
        )
    }

    // --- Aplica PartialBook na UI sem sobrescrever campos já preenchidos ---
    private fun applyPartialToUi(partial: PartialBook) {
        if (!partial.title.isNullOrBlank() && etTitulo.text.toString().isBlank()) {
            etTitulo.setText(partial.title)
        }
        if (!partial.categories.isNullOrBlank() && etCategoria.text.toString().isBlank()) {
            etCategoria.setText(partial.categories)
        }
        if (!partial.authors.isNullOrBlank() && etAutores.text.toString().isBlank()) {
            etAutores.setText(partial.authors)
        }
        if (!partial.publisher.isNullOrBlank() && etEditora.text.toString().isBlank()) {
            etEditora.setText(partial.publisher)
        }
        if (!partial.publishedDate.isNullOrBlank() && etDataPublicacao.text.toString().isBlank()) {
            etDataPublicacao.setText(partial.publishedDate)
        }
        if (!partial.description.isNullOrBlank() && etDescricao.text.toString().isBlank()) {
            etDescricao.setText(partial.description)
        }
        if (partial.pageCount != null && etPaginas.text.toString().isBlank()) {
            etPaginas.setText(partial.pageCount.toString())
        }
        // Capa: só altera se não houver capa local nem URL já definida
        if (!partial.thumbnailUrl.isNullOrBlank() && ultimaUrlCapa.isNullOrBlank() && capaPath.isNullOrBlank()) {
            ultimaUrlCapa = if (partial.thumbnailUrl.startsWith("//")) "https:${partial.thumbnailUrl}" else partial.thumbnailUrl
            exibirCapaPorUrlWithListener(ultimaUrlCapa!!)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val local = downloadAndSaveImage(ultimaUrlCapa!!)
                    if (local != null) capaPath = local
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao baixar capa: ${e.message}")
                }
            }
        }
    }

    // --- Busca em cascata: OpenLibrary -> Google Books -> CrossRef ---
    private suspend fun fetchBookByIsbn(rawIsbn: String) {
        val isbn = rawIsbn.filter { it.isDigit() }
        if (isbn.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_isbn_invalido), Toast.LENGTH_SHORT).show()
            }
            return
        }

        withContext(Dispatchers.Main) { showLoading() }

        try {
            var merged = PartialBook()

            // 1) OpenLibrary (extrai campos básicos)
            val openResp = try { ApiClient.openLibrary.buscarPorIsbn(isbn).body() } catch (e: Exception) {
                Log.w(TAG, "OpenLibrary request falhou: ${e.message}")
                null
            }
            val pOpenBasic = fromOpenLibraryBasic(openResp)
            if (pOpenBasic != null) merged = merged.merge(pOpenBasic)
            withContext(Dispatchers.Main) { pOpenBasic?.let { applyPartialToUi(it) } }

            // Se autores faltarem, tentar resolver nomes via endpoint de autores (sequencial)
            val needAuthorsAfterOpen = etAutores.text.toString().isBlank()
            if (needAuthorsAfterOpen && openResp?.authors?.isNotEmpty() == true) {
                val nomesAutores = try {
                    fetchAuthorsFromOpenLibrary(openResp, limit = 6)
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao buscar autores OpenLibrary: ${e.message}")
                    null
                }
                if (!nomesAutores.isNullOrBlank()) {
                    merged = merged.merge(PartialBook(authors = nomesAutores))
                    withContext(Dispatchers.Main) {
                        if (etAutores.text.toString().isBlank()) etAutores.setText(nomesAutores)
                    }
                }
            }

            // Função local para checar se ainda precisamos de mais dados
            fun needsMore(): Boolean {
                val needTitle = etTitulo.text.toString().isBlank()
                val needAuthors = etAutores.text.toString().isBlank()
                val needPublisher = etEditora.text.toString().isBlank()
                val needPublishedDate = etDataPublicacao.text.toString().isBlank()
                val needDescription = etDescricao.text.toString().isBlank()
                val needPages = etPaginas.text.toString().isBlank()
                val needThumbnail = ultimaUrlCapa.isNullOrBlank() && capaPath.isNullOrBlank()
                return needTitle || needAuthors || needPublisher || needPublishedDate || needDescription || needPages || needThumbnail
            }

            // 2) Google Books se necessário
            if (needsMore()) {
                val googleResp = try { ApiClient.googleBooks.volumesByIsbn("isbn:$isbn").body() } catch (e: Exception) {
                    Log.w(TAG, "Google Books request falhou: ${e.message}")
                    null
                }
                val info = googleResp?.items?.firstOrNull()?.volumeInfo
                val pGoogle = fromGoogleVolumes(info)
                if (pGoogle != null) {
                    merged = merged.merge(pGoogle)
                    withContext(Dispatchers.Main) { applyPartialToUi(pGoogle) }
                }
            }

            // 3) CrossRef se ainda faltar
            if (needsMore()) {
                val crossResp = try { ApiClient.crossRef.buscarPorIsbn("isbn:$isbn").body() } catch (e: Exception) {
                    Log.w(TAG, "CrossRef request falhou: ${e.message}")
                    null
                }
                val item = crossResp?.message?.items?.firstOrNull()
                val pCross = fromCrossRef(item)
                if (pCross != null) {
                    merged = merged.merge(pCross)
                    withContext(Dispatchers.Main) { applyPartialToUi(pCross) }
                }
            }

            // Resultado final
            val finalEmpty = run {
                etTitulo.text.toString().isBlank()
                        && etAutores.text.toString().isBlank()
                        && etEditora.text.toString().isBlank()
                        && etDataPublicacao.text.toString().isBlank()
                        && etDescricao.text.toString().isBlank()
                        && etPaginas.text.toString().isBlank()
                        && (ultimaUrlCapa.isNullOrBlank() && capaPath.isNullOrBlank())
            }

            withContext(Dispatchers.Main) {
                hideLoading()
                if (finalEmpty) {
                    Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_nenhum_livro_encontrado), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_campos_atualizados_apis), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro na busca em cascata: ${e.message}", e)
            withContext(Dispatchers.Main) {
                hideLoading()
                Toast.makeText(this@CadastroLivroActivity, getString(R.string.msg_erro_buscar_livro, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadAndSaveImage(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpsURLConnection
            conn.connect()
            val bitmap = BitmapFactory.decodeStream(conn.inputStream)
            conn.disconnect()
            val file = File(filesDir, "capa_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao baixar imagem", e)
            null
        }
    }

    private fun showLoading() {
        progressLoading.isVisible = true
        layoutConteudo.isVisible = false
    }

    private fun hideLoading() {
        progressLoading.isVisible = false
        layoutConteudo.isVisible = true
    }
}