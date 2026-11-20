package io.github.ilievi187.recurra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.ilievi187.recurra.data.VaultRepository
import io.github.ilievi187.recurra.data.VaultStorage
import io.github.ilievi187.recurra.ui.RecurraApp
import io.github.ilievi187.recurra.ui.state.RecurraViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RecurraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        VaultStorage.init(applicationContext)
        viewModel = RecurraViewModel(VaultRepository())

        setContent {
            RecurraApp(viewModel = viewModel, isDesktop = false)
        }
    }
}
