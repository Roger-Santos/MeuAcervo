package com.rogersantos.meuacervo.ui.controle

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rogersantos.meuacervo.worker.LembreteWorker
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.dao.EmprestimoDao
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.database.EmprestimoDatabase
import com.rogersantos.meuacervo.data.database.LivroDatabase
import com.rogersantos.meuacervo.data.model.Emprestimo
import com.rogersantos.meuacervo.data.model.Livro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EmprestimosFragment : Fragment(R.layout.fragment_emprestimos) {

    private lateinit var recycler: RecyclerView
    private lateinit var empty: View
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var adapter: EmprestimoAdapter
    private lateinit var dao: EmprestimoDao
    private lateinit var livroDao: LivroDao
    private var listaLivrosDisponiveis: List<Livro> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerEmprestimos)
        empty = view.findViewById(R.id.txtEmpty)
        fabAdd = view.findViewById(R.id.fabAddEmprestimo)

        dao = EmprestimoDatabase.getInstance(requireContext()).emprestimoDao()
        livroDao = LivroDatabase.getInstance(requireContext()).livroDao()

        adapter = EmprestimoAdapter { emprestimo ->
            devolverEmprestimo(emprestimo)
        }

        // ✅ Agora apenas uma linha por item
        recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        fabAdd.setOnClickListener { abrirDialogNovoEmprestimo() }

        carregarEmprestimos()
    }

    private fun carregarEmprestimos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val lista = withContext(Dispatchers.IO) { dao.listarAtivos() }
            adapter.submitList(lista)
            empty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun abrirDialogNovoEmprestimo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_novo_emprestimo, null)
        val spLivro = dialogView.findViewById<Spinner>(R.id.spLivro)
        val etPessoa = dialogView.findViewById<EditText>(R.id.etPessoa)
        val etTelefone = dialogView.findViewById<EditText>(R.id.etTelefone)
        val etDataPrevista = dialogView.findViewById<EditText>(R.id.etDataPrevista)
        val etHoraAlarme = dialogView.findViewById<EditText>(R.id.etHoraAlarme)

        // Carregar livros disponíveis
        lifecycleScope.launch {
            val todosLivros = withContext(Dispatchers.IO) { livroDao.listarTodos() }
            val emprestimosAtivos = withContext(Dispatchers.IO) { dao.listarAtivos() }
            val idsEmprestados = emprestimosAtivos.map { it.livroId }.toSet()
            listaLivrosDisponiveis = todosLivros.filter { it.id !in idsEmprestados }

            val adapterSpinner = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,   // texto preto
                listaLivrosDisponiveis.map { it.titulo }
            )
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spLivro.adapter = adapterSpinner
        }

        // Seleção de data prevista
        etDataPrevista.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    etDataPrevista.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Seleção de data + hora para alarme
        etHoraAlarme.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    // depois da data, abre o seletor de hora
                    TimePickerDialog(requireContext(),
                        { _, h, m ->
                            calendar.set(Calendar.HOUR_OF_DAY, h)
                            calendar.set(Calendar.MINUTE, m)
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            etHoraAlarme.setText(sdf.format(calendar.time))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Diálogo Material sem setTitle (título vem do layout com fundo azul)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton( getString(R.string.btn_salvar)) { _, _ ->
                val pessoa = etPessoa.text.toString().trim()
                val telefone = etTelefone.text.toString().trim()
                val dataPrevista = etDataPrevista.text.toString().trim()
                val horaAlarme = etHoraAlarme.text.toString().trim()
                val livroSelecionado = spLivro.selectedItemPosition.takeIf { it >= 0 }
                    ?.let { listaLivrosDisponiveis[it] }

                if (pessoa.isEmpty() || livroSelecionado == null) {
                    Toast.makeText(requireContext(),  getString(R.string.msg_preencha_campos), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().time)

                val emprestimo = Emprestimo(
                    livroId = livroSelecionado.id,
                    pessoa = pessoa,
                    telefone = telefone.ifEmpty { null },
                    dataEmprestimo = dataAtual,
                    dataPrevistaDevolucao = dataPrevista.ifEmpty { null },
                    devolvido = false
                )

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { dao.inserir(emprestimo) }
                    Toast.makeText(requireContext(),  getString(R.string.msg_emprestimo_registrado), Toast.LENGTH_SHORT).show()

                    // ✅ Agendar lembrete
                    if (horaAlarme.isNotEmpty()) {
                        agendarLembrete(livroSelecionado.titulo, pessoa, horaAlarme)
                    }

                    carregarEmprestimos()
                }
            }
            .setNegativeButton( getString(R.string.btn_cancelar), null)
            .show()
    }

    private fun agendarLembrete(tituloLivro: String, pessoa: String, dataHora: String) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(dataHora)
            val triggerAtMillis = date?.time ?: return

            val delay = triggerAtMillis - System.currentTimeMillis()
            if (delay <= 0) return // já passou

            val data = Data.Builder()
                .putString("titulo", tituloLivro)
                .putString("pessoa", pessoa)
                .build()

            val request = OneTimeWorkRequestBuilder<LembreteWorker>()
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(requireContext()).enqueue(request)

            // Guardar o ID do WorkRequest no banco (para cancelar depois)
            lifecycleScope.launch(Dispatchers.IO) {
                val emprestimos = dao.listarAtivos()
                val ultimo = emprestimos.maxByOrNull { it.id }
                if (ultimo != null) {
                    val atualizado = ultimo.copy(lembreteWorkId = request.id.toString())
                    dao.atualizar(atualizado)
                }
            }

            Toast.makeText(requireContext(),  getString(R.string.msg_lembrete_agendado, dataHora), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.msg_erro_agendar_lembrete, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun devolverEmprestimo(emprestimo: Emprestimo) {
        lifecycleScope.launch {
            val dataDevolucao = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().time)

            val atualizado = emprestimo.copy(
                devolvido = true,
                dataDevolucao = dataDevolucao
            )

            withContext(Dispatchers.IO) { dao.atualizar(atualizado) }

            // ✅ Cancelar lembrete se existir
            emprestimo.lembreteWorkId?.let { id ->
                try {
                    WorkManager.getInstance(requireContext())
                        .cancelWorkById(UUID.fromString(id))
                } catch (_: Exception) { }
            }

            Toast.makeText(requireContext(), getString(R.string.msg_livro_devolvido), Toast.LENGTH_SHORT).show()
            carregarEmprestimos()
        }
    }
}