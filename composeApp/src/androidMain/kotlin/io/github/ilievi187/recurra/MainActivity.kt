package io.github.ilievi187.recurra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import io.github.ilievi187.recurra.data.DocumentScanner
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.data.VaultStorage
import io.github.ilievi187.recurra.ui.RecurraApp
import io.github.ilievi187.recurra.ui.state.RecurraViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RecurraViewModel

    private val openDocLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        DocumentScanner.onDocumentPicked(uri)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        DocumentScanner.onCameraCaptured(success)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        VaultStorage.init(applicationContext)
        DocumentScanner.bind(
            context = applicationContext,
            pickerLauncher = {
                openDocLauncher.launch(arrayOf("image/*", "application/pdf", "text/plain"))
            },
            cameraLauncher = { outputUri ->
                takePictureLauncher.launch(outputUri)
            },
        )
        viewModel = RecurraViewModel(VaultRepository())

        setContent {
            RecurraApp(viewModel = viewModel, isDesktop = false)
        }
    }
}
