package app.akiho.media_compressor.di

import app.akiho.media_compressor.ui.EventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides @Singleton fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides @Singleton fun provideEventBus(): EventBus = EventBus()
}
