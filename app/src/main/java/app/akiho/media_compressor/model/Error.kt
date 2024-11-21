package app.akiho.media_compressor.model

sealed class AppError : Exception() {
  data class Plain(private val msg: String? = null) : AppError() {
    override val message: String
      get() = msg ?: DEFAULT
  }

  companion object {
    const val DEFAULT = "エラーが発生しました"

    fun from(error: AppError?): AppError {
      return error ?: Plain(DEFAULT)
    }
  }
}
