package com.rogersantos.meuacervo.ui.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.rogersantos.meuacervo.R
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class ScannerIsbnActivity : AppCompatActivity() {

    private val TAG = "ScannerIsbnActivity"
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var scanningPaused = false
    private var lastScannedValue: String? = null
    private val cooldownMs = 1500L

    private lateinit var previewView: PreviewView
    private lateinit var btnClose: ImageButton
    private lateinit var focusOverlay: View

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else {
                Toast.makeText(this, getString(R.string.msg_permissao_camera_necessaria), Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_isbn)

        previewView = findViewById(R.id.previewView)
        btnClose = findViewById(R.id.btnCloseScanner)
        focusOverlay = findViewById(R.id.viewFocusOverlay)

        // fechar sem resultado
        btnClose.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // pedir permissão e iniciar câmera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Analysis
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)

            analysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                if (scanningPaused) {
                    imageProxy.close()
                    return@Analyzer
                }

                @androidx.camera.core.ExperimentalGetImage
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue ?: continue
                                    val normalized = rawValue.trim()
                                    if (isPossibleIsbn(normalized)) {
                                        if (normalized == lastScannedValue) break
                                        lastScannedValue = normalized
                                        scanningPaused = true
                                        highlightDetection()
                                        returnIsbn(normalized)
                                        GlobalScope.launch {
                                            delay(cooldownMs)
                                            scanningPaused = false
                                        }
                                        break
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "@string/msg_falha_scanner ${e.message}", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao ligar câmera: ${e.message}", e)
                Toast.makeText(this, getString(R.string.msg_erro_iniciar_camera), Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun highlightDetection() {
        // breve feedback visual
        runOnUiThread {
            focusOverlay.isVisible = true
            focusOverlay.animate().alpha(1f).setDuration(150).withEndAction {
                focusOverlay.animate().alpha(0f).setDuration(600)
            }
        }
    }

    private fun isPossibleIsbn(value: String): Boolean {
        val digits = value.filter { it.isDigit() }
        return (digits.length == 13 || digits.length == 10 || digits.length == 12 || digits.length == 8)
    }

    private fun returnIsbn(isbn: String) {
        val result = Intent().apply { putExtra("isbn", isbn) }
        setResult(Activity.RESULT_OK, result)
        Toast.makeText(this, "ISBN: $isbn", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}