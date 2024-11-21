package app.akiho.media_compressor

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
          DiskCache.Builder()
              .directory(cacheDir.resolve("image_cache"))
              .maxSizePercent(0.02)
              .build()
        }
        .crossfade(true)
        .components { add(VideoFrameDecoder.Factory()) }
        .build()
  }
}
