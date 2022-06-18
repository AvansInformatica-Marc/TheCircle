package nl.marc.thecircle

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@DiApplication)
            modules(DiModules.viewModelsModule, DiModules.dataRepositoriesModule, DiModules.utilitiesModule)
        }
    }
}
