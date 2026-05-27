package com.cit.pawnscan.shared.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cit.pawnscan.R
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.abs
import kotlin.math.min

class EvidencePreviewActivity : AppCompatActivity() {
    private var pdfRenderer: PdfRenderer? = null
    private var pdfPage: PdfRenderer.Page? = null
    private var pdfDescriptor: ParcelFileDescriptor? = null
    private var pdfPageIndex: Int = 0
    private var pdfPageCount: Int = 0

    private var pdfController: ZoomPanController? = null
    private var imageController: ZoomPanController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evidence_preview)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (url.isBlank()) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.evidence_preview_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.evidence_preview_title).text =
            intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { getString(R.string.evidence_preview_title) }

        val progress = findViewById<ProgressBar>(R.id.evidence_preview_progress)
        val imageContainer = findViewById<LinearLayout>(R.id.evidence_preview_image_container)
        val imagePreview = findViewById<ImageView>(R.id.evidence_preview_image)
        val pdfContainer = findViewById<LinearLayout>(R.id.evidence_preview_pdf_container)
        val pdfImage = findViewById<ImageView>(R.id.evidence_preview_pdf_image)
        val pdfPageLabel = findViewById<TextView>(R.id.evidence_preview_pdf_page)
        val pdfPrev = findViewById<Button>(R.id.evidence_preview_prev)
        val pdfNext = findViewById<Button>(R.id.evidence_preview_next)

        val fileType = intent.getStringExtra(EXTRA_TYPE)
        val isPdf = isPdfType(url, fileType)
        if (isPdf) {
            imageContainer.visibility = View.GONE
            pdfContainer.visibility = View.VISIBLE
            progress.visibility = View.VISIBLE

            pdfController = ZoomPanController(
                pdfImage,
                onSwipeLeft = { renderPdfPage(pdfPageIndex + 1, pdfImage, pdfPageLabel, pdfPrev, pdfNext) },
                onSwipeRight = { renderPdfPage(pdfPageIndex - 1, pdfImage, pdfPageLabel, pdfPrev, pdfNext) }
            )

            pdfPrev.setOnClickListener { renderPdfPage(pdfPageIndex - 1, pdfImage, pdfPageLabel, pdfPrev, pdfNext) }
            pdfNext.setOnClickListener { renderPdfPage(pdfPageIndex + 1, pdfImage, pdfPageLabel, pdfPrev, pdfNext) }

            Thread {
                try {
                    val file = downloadPdf(url)
                    runOnUiThread {
                        openPdf(file)
                        renderPdfPage(0, pdfImage, pdfPageLabel, pdfPrev, pdfNext)
                        progress.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        progress.visibility = View.GONE
                        Toast.makeText(this, "Unable to load PDF.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }.start()
            return
        }

        pdfContainer.visibility = View.GONE
        imageContainer.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        imageController = ZoomPanController(imagePreview)

        Thread {
            try {
                val bitmap = downloadImage(url)
                runOnUiThread {
                    imageController?.setBitmap(bitmap)
                    progress.visibility = View.GONE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    Toast.makeText(this, "Unable to load image.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfPage?.close()
        pdfRenderer?.close()
        pdfDescriptor?.close()
    }

    private fun isPdfType(url: String, fileType: String?): Boolean {
        return fileType?.equals("PDF", ignoreCase = true) == true || url.endsWith(".pdf", ignoreCase = true)
    }

    private fun downloadImage(url: String): Bitmap {
        URL(url).openStream().use { input ->
            return BitmapFactory.decodeStream(input) ?: throw IllegalStateException("Unable to decode image")
        }
    }

    private fun downloadPdf(url: String): File {
        val file = File.createTempFile("evidence_", ".pdf", cacheDir)
        URL(url).openStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun openPdf(file: File) {
        pdfDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(pdfDescriptor!!)
        pdfPageCount = pdfRenderer?.pageCount ?: 0
        pdfPageIndex = 0
    }

    private fun renderPdfPage(
        index: Int,
        imageView: ImageView,
        pageLabel: TextView,
        prevButton: Button,
        nextButton: Button
    ) {
        val renderer = pdfRenderer ?: return
        if (index < 0 || index >= renderer.pageCount) return

        pdfPage?.close()
        pdfPage = renderer.openPage(index)
        pdfPageIndex = index

        val page = pdfPage ?: return
        val width = if (imageView.width > 0) imageView.width else resources.displayMetrics.widthPixels
        val ratio = width.toFloat() / page.width
        val height = (page.height * ratio).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfController?.setBitmap(bitmap)

        pageLabel.text = getString(R.string.evidence_page, pdfPageIndex + 1, renderer.pageCount)
        prevButton.isEnabled = pdfPageIndex > 0
        nextButton.isEnabled = pdfPageIndex < renderer.pageCount - 1
    }

    private class ZoomPanController(
        private val imageView: ImageView,
        private val onSwipeLeft: (() -> Unit)? = null,
        private val onSwipeRight: (() -> Unit)? = null
    ) : View.OnTouchListener {
        private val matrix = Matrix()
        private var scale = 1f
        private var minScale = 1f
        private val maxScale = 4f
        private var lastX = 0f
        private var lastY = 0f
        private var isDragging = false

        private val scaleDetector = ScaleGestureDetector(imageView.context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val newScale = (scale * detector.scaleFactor).coerceIn(minScale, maxScale)
                    val applied = newScale / scale
                    matrix.postScale(applied, applied, detector.focusX, detector.focusY)
                    scale = newScale
                    imageView.imageMatrix = matrix
                    return true
                }
            })

        private val gestureDetector = GestureDetector(imageView.context,
            object : GestureDetector.OnGestureListener {
                override fun onDown(e: MotionEvent): Boolean = false

                override fun onShowPress(e: MotionEvent) = Unit

                override fun onSingleTapUp(e: MotionEvent): Boolean = false

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean = false

                override fun onLongPress(e: MotionEvent) = Unit

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (abs(velocityX) > abs(velocityY) && abs(velocityX) > 800) {
                        if (velocityX < 0) onSwipeLeft?.invoke() else onSwipeRight?.invoke()
                        return true
                    }
                    return false
                }
            })

        init {
            imageView.scaleType = ImageView.ScaleType.MATRIX
            imageView.setOnTouchListener(this)
        }

        fun setBitmap(bitmap: Bitmap) {
            imageView.setImageBitmap(bitmap)
            imageView.post {
                val viewWidth = imageView.width.toFloat().coerceAtLeast(1f)
                val viewHeight = imageView.height.toFloat().coerceAtLeast(1f)
                val scaleX = viewWidth / bitmap.width
                val scaleY = viewHeight / bitmap.height
                scale = min(scaleX, scaleY).coerceAtMost(1f)
                minScale = scale
                val dx = (viewWidth - bitmap.width * scale) / 2f
                val dy = (viewHeight - bitmap.height * scale) / 2f
                matrix.reset()
                matrix.postScale(scale, scale)
                matrix.postTranslate(dx, dy)
                imageView.imageMatrix = matrix
            }
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    isDragging = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!scaleDetector.isInProgress && isDragging) {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        matrix.postTranslate(dx, dy)
                        imageView.imageMatrix = matrix
                        lastX = event.x
                        lastY = event.y
                    }
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> isDragging = false
            }
            return true
        }
    }

    companion object {
        const val EXTRA_URL = "evidence_url"
        const val EXTRA_TYPE = "evidence_type"
        const val EXTRA_TITLE = "evidence_title"
    }
}
