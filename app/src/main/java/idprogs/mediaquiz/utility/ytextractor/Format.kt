package idprogs.mediaquiz.utility.ytextractor

data class Format(
    val itag: Int,
    val ext: String,
    val height: Int,
    val videoCodec: VCodec? = null,
    val audioCodec: ACodec? = null,
    val audioBitrate: Int,
    val isDashContainer: Boolean
) {
    val fps: Int = -1
    val isHlsContent: Boolean = false
    enum class VCodec { H263, H264, MPEG4, VP8, VP9, NONE }
    enum class ACodec { MP3, AAC, VORBIS, OPUS, NONE }
}