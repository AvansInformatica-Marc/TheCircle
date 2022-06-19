package nl.marc.thecircle

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import nl.marc.thecircle.data.*
import nl.marc.thecircle.data.api.TheCircleChatApi
import nl.marc.thecircle.data.api.TheCircleStreamApi
import nl.marc.thecircle.data.api.TheCircleUserApi
import nl.marc.thecircle.ui.setup.PermissionsViewModel
import nl.marc.thecircle.ui.setup.SignUpViewModel
import nl.marc.thecircle.ui.streaming.StreamingViewModel
import nl.marc.thecircle.utils.retrofit
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DiModules {
    private const val APP_SETTINGS_NAME = "settings"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = APP_SETTINGS_NAME)

    val dataRepositoriesModule = module {
        single {
            get<Context>().dataStore
        }

        single {
            retrofit<TheCircleUserApi> {
                baseUrl("http://${BuildConfig.THE_CIRCLE_HOST}:${BuildConfig.THE_CIRCLE_PORT}/")
                addConverterFactory(get<HttpClient>().jsonConverter)
                client(get<HttpClient>().okHttpClient)
            }
        }

        single {
            retrofit<TheCircleStreamApi> {
                baseUrl("http://${BuildConfig.THE_CIRCLE_HOST}:${BuildConfig.THE_CIRCLE_PORT}/")
                addConverterFactory(get<HttpClient>().jsonConverter)
                client(get<HttpClient>().okHttpClient)
            }
        }

        single {
            retrofit<TheCircleChatApi> {
                baseUrl("http://${BuildConfig.THE_CIRCLE_HOST}:${BuildConfig.THE_CIRCLE_PORT}/")
                addConverterFactory(get<HttpClient>().jsonConverter)
                client(get<HttpClient>().okHttpClient)
            }
        }

        single {
            UserRepository(get(), get())
        }

        single {
            StreamRepository(get(), get())
        }

        single {
            ChatRepository(get(), get())
        }
    }

    val viewModelsModule = module {
        viewModel {
            StreamingViewModel(get(), get(), get())
        }

        viewModel {
            SignUpViewModel(get())
        }

        viewModel {
            PermissionsViewModel(get())
        }
    }

    val utilitiesModule = module {
        single<HttpClient> { HttpClientImpl(get()) }
    }
}
