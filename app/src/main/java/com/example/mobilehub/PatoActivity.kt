package com.example.mobilehub

import android.content.ClipData
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import androidx.core.content.ContextCompat

class PatoActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    private var nivelFome = 100
    private var nivelFelicidade = 100
    private var estaVivo = true

    private lateinit var tvFome: TextView
    private lateinit var tvFelicidade: TextView
    private lateinit var patoAnimado: PatoView
    private lateinit var btnReiniciar: Button

    private var isArrastando = false

    private val handler = Handler(Looper.getMainLooper())

    private val handlerSom = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pato)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        audioManager = DefaultAudioManager.getInstance(this)

        tvFome = findViewById(R.id.tvFome)
        tvFelicidade = findViewById(R.id.tvFelicidade)
        patoAnimado = findViewById(R.id.patoAnimado)
        btnReiniciar = findViewById(R.id.btnReiniciar)

        val itemMilho: ImageView = findViewById(R.id.itemMilho)
        val itemCarinho: ImageView = findViewById(R.id.itemCarinho)


        val longClickListener = View.OnLongClickListener { v ->
            if (estaVivo) {
                patoAnimado.pausar()
                isArrastando = true
            }

            val data = ClipData.newPlainText(v.resources.getResourceEntryName(v.id), "")
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadow, v, 0)
            true
        }
        itemMilho.setOnLongClickListener(longClickListener)
        itemCarinho.setOnLongClickListener(longClickListener)

        patoAnimado.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val itemArrastado = event.clipDescription.label.toString()

                    if (estaVivo) {
                        audioManager.playSound(EnumSound.QUACK_DROP)

                        if (itemArrastado == "itemMilho") {
                            atualizarAtributos(15, 0) // Sobe Fome
                        } else if (itemArrastado == "itemCarinho") {
                            atualizarAtributos(0, 15) // Sobe Felicidade
                        }

                        iniciarSonsAleatorios()
                    }
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    if (estaVivo) {
                        patoAnimado.retomar()
                    }
                    isArrastando = false
                }
            }
            true
        }

        btnReiniciar.setOnClickListener { reiniciarPatinho() }

        findViewById<Button>(R.id.btn_voltar_hub).setOnClickListener {
            finish()
        }

        val btnVoltar = findViewById<Button>(R.id.btn_voltar_hub)
        btnVoltar.setTextColor(ContextCompat.getColor(this, R.color.text_hub_subtitle))

        iniciarSonsAleatorios()

        iniciarCicloVida()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        renderizar()
    }

    private fun iniciarCicloVida() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (estaVivo) {
                    atualizarAtributos(-11, -8)
                    handler.postDelayed(this, 2000)
                }
            }
        }, 2000)
    }

    private fun atualizarAtributos(fomeExtra: Int, feliciExtra: Int) {
        nivelFome = (nivelFome + fomeExtra).coerceIn(0, 100)
        nivelFelicidade = (nivelFelicidade + feliciExtra).coerceIn(0, 100)

        if (fomeExtra > 0) {
            // FEEDBACK COM A IMAGEM DO DRAWABLE (Milho Pixel Art)
            handler.postDelayed({ mostrarFeedbackImagem(R.drawable.milho) }, 0)
            handler.postDelayed({ mostrarFeedbackImagem(R.drawable.milho) }, 200)
        }

        if (feliciExtra > 0) {
            // FEEDBACK COM A IMAGEM DO CORAÇÃO
            handler.postDelayed({ mostrarFeedbackImagem(R.drawable.heart) }, 0)
            handler.postDelayed({ mostrarFeedbackImagem(R.drawable.heart) }, 200)
        }

        if (nivelFome <= 0 || nivelFelicidade <= 0) { morrer() }
        renderizar()
    }

    private fun renderizar() {
        tvFome.text = nivelFome.toString()
        tvFelicidade.text = nivelFelicidade.toString()

        if (estaVivo) {
            btnReiniciar.visibility = View.GONE

            if (nivelFome < 30 || nivelFelicidade < 30) {
                patoAnimado.setModoPerigo()

                tvFome.setTextColor(Color.RED)
                tvFelicidade.setTextColor(Color.RED)
            } else {
                patoAnimado.setModoNormal()

                tvFome.setTextColor(Color.BLACK)
                tvFelicidade.setTextColor(Color.BLACK)
            }

        } else {
            btnReiniciar.visibility = View.VISIBLE
            patoAnimado.setModoPerigo()
        }
    }

    private fun morrer() {
        if (estaVivo) {
            estaVivo = false
            patoAnimado.notificarMorte()
            renderizar()
        }
    }

    private fun reiniciarPatinho() {
        estaVivo = true
        nivelFome = 100
        nivelFelicidade = 100

        patoAnimado.notificarRenascer()

        renderizar()
        iniciarCicloVida()
        iniciarSonsAleatorios()
    }

    private fun iniciarSonsAleatorios() {
        handlerSom.removeCallbacksAndMessages(null)
        handlerSom.postDelayed(object : Runnable {
            override fun run() {
                if (estaVivo && !isArrastando) {
                    audioManager.playSound(EnumSound.RANDOM_QUACK)
                }

                val tempoAleatorio = (10000..25000).random().toLong()
                handlerSom.postDelayed(this, tempoAleatorio)
            }
        }, 15000)
    }

    private fun mostrarFeedbackImagem(drawableId: Int) {
        val containerPrincipal: ViewGroup = findViewById(R.id.areaPato)

        // 1. Criar a ImageView temporária
        val imageView = ImageView(this).apply {
            setImageResource(drawableId)
            alpha = 1f

           //normalizacao
            layoutParams = FrameLayout.LayoutParams(100, 100)

            scaleType = ImageView.ScaleType.FIT_CENTER

        }

        // 2. Pegar a posição exata do pato
        val centroX = patoAnimado.getCentroXAtual().toInt()
        val topoY = patoAnimado.getTopoYAtual().toInt()

        // 3. Calcular ponto de partida (centralizado melhor baseado no novo tamanho 100x100)
        val desvioX = (-30..30).random()

        // Se a imagem tem 100px de largura, subtraímos 50 (metade) para centralizar no ponto de origem
        val startX = centroX - 50 + desvioX
        val startY = topoY - 40

        // 4. Posicionar inicialmente
        imageView.x = startX.toFloat()
        imageView.y = startY.toFloat()

        // Adiciona ao container
        containerPrincipal.addView(imageView)

        // 5. Configurar animação (Subir e Sumir)
        // Subida: Vai de startY até 280 pixels para cima
        val subidaAnim = ObjectAnimator.ofFloat(imageView, View.Y, startY.toFloat(), startY.toFloat() - 280f)

        // Desaparecimento: Alpha vai de 1 (visível) para 0 (invisível)
        val desapareceAnim = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f, 0f)

        // Juntar as animações
        val conjuntoAnim = AnimatorSet().apply {
            playTogether(subidaAnim, desapareceAnim)
            duration = 2000 // Duração da animação (2 segundos)
            interpolator = AccelerateInterpolator() // Efeito de aceleração natural
        }

        // 6. Remover a view quando a animação acabar (para não travar o celular)
        conjuntoAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                containerPrincipal.removeView(imageView)
            }
        })

        conjuntoAnim.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        handlerSom.removeCallbacksAndMessages(null)
    }


}