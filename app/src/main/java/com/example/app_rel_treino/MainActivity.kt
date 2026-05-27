package com.example.app_rel_treino

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavegacaoApp() // app arranca na funcao navegacao app
        }
    }
}

// gestor navegacao
@Composable
fun NavegacaoApp() {
    val navController = rememberNavController() // O motorista

    // O mapa de ecras começa boas vindas
    NavHost(navController = navController, startDestination = "boas_vindas") {

        composable("boas_vindas") {
            BemVindoScreen(navController)
        }

        composable("ecra_cronometro") {
            TemporizadorScreen(navController)
        }
    }
}

// ecra principal
@Composable
fun BemVindoScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BEM-VINDO",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "APP DE TREINO",
            fontSize = 20.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 50.dp)
        )

        Button(
            onClick = {
                // Navega para o ecrã do temporizador
                navController.navigate("ecra_cronometro")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            modifier = Modifier.size(width = 200.dp, height = 60.dp)
        ) {
            Text("IR PARA CRONÓMETRO", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// --- ECRÃ DO TEMPORIZADOR ---
@Composable
fun TemporizadorScreen(navController: NavController? = null) {
    // Controlar lógica visual do temporizador, para não se perder ao atualizar (0L por ser um valor grande usamos long)
    var tempoRestante by remember {
        mutableLongStateOf(0L)
    }
    var estaARodar by remember {
        mutableStateOf(false)
    }
    var roundDeLuta by remember {
        mutableStateOf(true)
    }
    var estadoTexto by remember {
        mutableStateOf("CONFIGURAR")
    }

    // Estados default
    var roundMinInput by remember {
        mutableStateOf("3")
    }
    var roundSegInput by remember {
        mutableStateOf("00")
    }
    var pausaSegInput by remember {
        mutableStateOf("30")
    }

    // Guarda se o temporizador num "remember" para não se perder quando o ecrã atualiza
    // O ? indica que começa vazio (null).
    var timer: CountDownTimer? by remember {
        mutableStateOf(null)
    }

    // defenicao de cores
    val corVerde = Color.Green
    val corVermelha = Color.Red
    val corFundo = when {
        !estaARodar && estadoTexto == "PAUSADO" -> Color.Black
        !estaARodar && estadoTexto == "CONFIGURAR" -> Color.Black
        roundDeLuta -> corVerde
        else -> corVermelha
    }

    // funcao para o tempo
    fun formatarTempo(ms: Long): String {
        val totalSegundos = (ms + 999) / 1000 // quando metia 5 segundos começava apartir do 4 meti +999 para arredondar para cima ja n acontece este erro
        val min = totalSegundos / 60
        val seg = totalSegundos % 60
        return String.format(Locale.getDefault(), "%02d:%02d", min, seg)
    }

    fun iniciarTimer(tempoInicial: Long) {
        if (timer != null) {
            timer!!.cancel() // Se o temporizador já existir , então cancela-o
        }
        estaARodar = true

        timer = object : CountDownTimer(tempoInicial, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tempoRestante = millisUntilFinished
            }

            override fun onFinish() {
                estaARodar = false
                val proximaLuta = !roundDeLuta
                roundDeLuta = proximaLuta

                // Configura o próximo tempo
                val novoTempo = if (proximaLuta) {
                    val m = roundMinInput.toLongOrNull() ?: 0 // Converte texto para numero  Se a caixa estiver vazia assume 0 por segurança
                    val s = roundSegInput.toLongOrNull() ?: 0
                    estadoTexto = "FIGHT"
                    (m * 60 + s) * 1000
                } else {
                    val s = pausaSegInput.toLongOrNull() ?: 0
                    estadoTexto = "REST"
                    s * 1000
                }

                tempoRestante = novoTempo
                iniciarTimer(novoTempo) // Loop automático
            }
        }.start()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(corFundo)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botão de voltar atrás alinhado à esquerda
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            TextButton(
                onClick = {
                    timer?.cancel()
                    navController?.popBackStack()
                }
            ) {
                Text("<- VOLTAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // MOLA 1: Empurra o botão de voltar para o teto e o resto para baixo
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = estadoTexto,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 10.dp)
        )

        Text(
            text = formatarTempo(tempoRestante),
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // Inputs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            InputBox("Round (Min:Seg)", value1 = roundMinInput, onValue1Change = { roundMinInput = it }, value2 = roundSegInput, onValue2Change = { roundSegInput = it }, isDouble = true)
            InputBox("Pausa (Seg)", value1 = pausaSegInput, onValue1Change = { pausaSegInput = it }, isDouble = false)
        }

        // Botoes
        Row {
            Button(
                onClick = {
                    if (!estaARodar) {
                        if (tempoRestante <= 0L) {
                            val m = roundMinInput.toLongOrNull() ?: 0
                            val s = roundSegInput.toLongOrNull() ?: 0
                            tempoRestante = (m * 60 + s) * 1000
                            roundDeLuta = true
                            estadoTexto = "FIGHT"
                        }
                        iniciarTimer(tempoRestante)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = corVerde),
                modifier = Modifier.padding(5.dp)
            ) { Text("INICIAR", color = Color.Black, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    timer?.cancel()
                    estaARodar = false
                    estadoTexto = "PAUSADO"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                modifier = Modifier.padding(5.dp)
            ) { Text("PAUSAR", color = Color.Black, fontWeight = FontWeight.Bold) }

            Button(
                onClick = {
                    timer?.cancel()
                    tempoRestante = 0L
                    estaARodar = false
                    estadoTexto = "CONFIGURAR"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.padding(5.dp)
            ) { Text("RESET", color = Color.Black, fontWeight = FontWeight.Bold) }
        }

        // MOLA 2: Empurra tudo para cima contra a Mola 1, mantendo o relógio no centro
        Spacer(modifier = Modifier.weight(1f))
    }
}

// Função reutilizável para criar caixas de texto
@Composable
fun InputBox(label: String, value1: String, onValue1Change: (String) -> Unit, value2: String = "", onValue2Change: (String) -> Unit = {}, isDouble: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { // Organiza os elementos de cima para baixo
        Text(label, color = Color.LightGray, fontSize = 12.sp) // O título da secção
        Row(verticalAlignment = Alignment.CenterVertically) { // Organiza os elementos da esquerda para a direita
            TextField( // Primeira caixa de texto
                value = value1,
                onValueChange = onValue1Change,
                modifier = Modifier.width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            if (isDouble) { // Verifica se precisamos de uma caixa dupla
                Text(":", color = Color.White) // separar min de s
                TextField( // Segunda caixa de texto (para os segundos do Round)
                    value = value2,
                    onValueChange = onValue2Change,
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Pré-visualização do Relógio")
@Composable
fun TemporizadorPreview() {
    TemporizadorScreen(navController = null)
}

@Preview(showBackground = true, name = "Pré-visualização Boas Vindas")
@Composable
fun BemVindoPreview() {
    BemVindoScreen(navController = rememberNavController())
}