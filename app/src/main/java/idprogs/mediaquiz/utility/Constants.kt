package idprogs.mediaquiz.utility

import idprogs.mediaquiz.BuildConfig

enum class QuizMode(val id: Int) {
    MODE_LETTER(1),
    MODE_OPTIONS(2)
}

enum class DataType(val id: Int, val typeName: String) {
    DATA_MOVIES(0, "movie"),
    DATA_SERIES(1, "tv"),
    DATA_ARTISTS(2, "artist")
}


const val PROGRAM_VERSION_INFO_URL = "https://idgames064.github.io/progs/MediaQuiz/data.json"
const val PROGRAM_SHA1_FINGERPRINT = "A7:C8:C6:BC:C8:CC:D0:0D:70:A6:94:47:D6:01:E6:CB:96:AB:65:16"

const val LANGUAGE_DEFAULT = "ru"

const val MOVIES_VOTE_COUNT_DEFAULT = 1000
const val MOVIES_VOTE_AVERAGE_DEFAULT = 5.5f
const val SERIES_VOTE_COUNT_DEFAULT = 300
const val SERIES_VOTE_AVERAGE_DEFAULT = 6.5f

const val ARTIST_COUNT_DEFAULT = 3000
const val PAGE_SIZE = 200


var DATA_TYPE = DataType.DATA_MOVIES
var QUIZ_MODE = QuizMode.MODE_OPTIONS

var MOVIES_VOTE_COUNT = MOVIES_VOTE_COUNT_DEFAULT
var MOVIES_VOTE_AVERAGE = MOVIES_VOTE_AVERAGE_DEFAULT
var SERIES_VOTE_COUNT = SERIES_VOTE_COUNT_DEFAULT
var SERIES_VOTE_AVERAGE = SERIES_VOTE_AVERAGE_DEFAULT
var LANGUAGE = LANGUAGE_DEFAULT
var ARTIST_COUNT = ARTIST_COUNT_DEFAULT

const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
const val LASTFM_API_KEY = BuildConfig.LASTFM_API_KEY
const val TMDB_API_BASE_URL = "https://api.themoviedb.org/3/"
const val LASTFM_API_BASE_URL = "https://ws.audioscrobbler.com/2.0/"
const val ARTIST_SUFFIX = "/+partial/tracks?top_tracks_date_preset=ALL"
const val LASTFM_DEFAULT_COUNTRY = "Russian Federation"


const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500"
const val BASE_VIDEO_URL = "https://www.youtube.com/watch?v="
const val IMDB_BASE_URL = "https://www.imdb.com/title/"
const val TMDB_BASE_URL = "https://www.themoviedb.org/"
const val PLAYBACK_TIME = 30000
const val SEEK_TO = 10000L
const val MUSIC_SEEK_TO = 50000L
const val DB_NAME = "media_quiz.db"
const val MAX_LENGTH_IN_LETTER_MODE = 30
const val NUMBER_OF_OPTIONS = 6

val ARTIST_EXCEPTIONS = listOf(
    "5dfdca28-9ddc-4853-933c-8bc97d87beec",
    "f2dc6fea-f006-46d2-89ee-8a5b813b4f7a",
    "cb413cfa-2d4f-4fe3-b7a8-d8c36e090b79")

