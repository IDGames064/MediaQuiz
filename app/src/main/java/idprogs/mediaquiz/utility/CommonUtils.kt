package idprogs.mediaquiz.utility

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.api.model.Video
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.math.roundToInt


class CommonUtils {
  companion object {

      fun getTrailer(movie: Movie?, index: Int): String {
          fun match(video: Video) = video.type == "Trailer" && video.site == "YouTube"
          val videos = movie?.videos?.results
          if (!videos.isNullOrEmpty()) {
              var idx = 0
              var foundIdx = 0
              while ( (idx < videos.size) && (!match(videos[idx]) || (match(videos[idx]) && foundIdx < index)) ) {
                  if (match(videos[idx++])) foundIdx++
              }
              return if (idx < videos.size) videos[idx].key else ""
          }
          return ""
      }

      fun String.getSymbols(): String {
          var result = ""
          for (symbol in this) {
              if (symbol.isLetterOrDigit()) result += symbol.toUpperCase()
          }
          return result
      }

      fun String.getWidth(): Float {
          var result = 0f
          for (symbol in this) {
              if (symbol.isLetterOrDigit()) result += 1 else result += 0.5f
          }
          return result
      }

      fun vibratePhone(context: Context) {
          val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
          if (Build.VERSION.SDK_INT >= 26) {
              vibrator.vibrate(VibrationEffect.createOneShot(50, 10))
          } else {
              vibrator.vibrate(50)
          }
      }

      fun Char?.isEnglish() = if (this == null || !this.isLetterOrDigit()) false else Charset.forName("US-ASCII").newEncoder().canEncode(this)

      fun String.isCyrillic(): Boolean {
          return Pattern.matches(".*\\p{InCyrillic}.*", this)
      }


      fun dp2px(dp: Int): Int {
          val scale: Float = Resources.getSystem().displayMetrics.density
          return (dp * scale + 0.5f).toInt()
      }

      fun px2dp(px: Float): Int {
          val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
          val dp = px / (metrics.densityDpi / 160f)
          return dp.roundToInt()
      }


  }
 }
