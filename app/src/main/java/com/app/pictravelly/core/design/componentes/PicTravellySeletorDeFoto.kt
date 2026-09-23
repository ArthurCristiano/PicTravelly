package com.app.pictravelly.core.design.componentes

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.util.UUID

/**
 * Bloco de foto usado no cadastro de viagens e de pontos turisticos:
 * mostra a imagem atual e oferece camera, galeria e remocao.
 *
 * A captura vai para um arquivo temporario no cache; quem decide guardar
 * de vez e a tela, atraves de [onFotoCapturada].
 */
@Composable
fun PicTravellySeletorDeFoto(
    caminhoImagem: String?,
    onFotoCapturada: (File) -> Unit,
    onFotoEscolhida: (Uri) -> Unit,
    onRemoverFoto: () -> Unit,
    modifier: Modifier = Modifier,
    textoVazio: String = "Nenhuma foto ainda"
) {
    val context = LocalContext.current
    val arquivoPendente = remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        val arquivo = arquivoPendente.value
        arquivoPendente.value = null
        if (sucesso && arquivo != null) onFotoCapturada(arquivo)
    }

    val abrirCamera = {
        val (arquivo, uri) = criarDestinoDeCaptura(context)
        arquivoPendente.value = arquivo
        cameraLauncher.launch(uri)
    }

    // Se a CAMERA esta declarada no manifesto, o Android exige a permissao
    // em tempo de execucao antes de abrir o app de camera.
    val permissaoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida -> if (concedida) abrirCamera() }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onFotoEscolhida) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (caminhoImagem != null) {
                AsyncImage(
                    model = File(caminhoImagem),
                    contentDescription = "Foto selecionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = textoVazio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(
                onClick = { permissaoLauncher.launch(Manifest.permission.CAMERA) }
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = "Câmera", modifier = Modifier.padding(start = 6.dp))
            }

            TextButton(
                onClick = {
                    galeriaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = "Galeria", modifier = Modifier.padding(start = 6.dp))
            }

            if (caminhoImagem != null) {
                TextButton(onClick = onRemoverFoto) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Remover",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

/** Arquivo de cache + Uri do FileProvider onde o app de camera vai gravar. */
private fun criarDestinoDeCaptura(context: Context): Pair<File, Uri> {
    val pasta = File(context.cacheDir, "capturas").apply { mkdirs() }
    val arquivo = File(pasta, "captura_${UUID.randomUUID()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        arquivo
    )
    return arquivo to uri
}
