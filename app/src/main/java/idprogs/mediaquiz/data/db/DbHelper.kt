package idprogs.mediaquiz.data.db

import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.utility.DataType

interface DbHelper {
  fun getCount(type: DataType): Int
  fun clearEntries(type: DataType)
  fun getRandomMovieEntry(): MovieEntry
  fun addMovieEntries(entries: List<MovieEntry>)
  fun getRandomMovieOptions(movie: Movie, count: Int, onlyId: Boolean = false): List<String>
  fun addArtistEntries(entries: List<ArtistEntry>)
  fun getRandomArtistEntry(): ArtistEntry
  fun getArtistEntry(entryId: Int): ArtistEntry
  fun getRandomArtistOptions(artist: Artist, count: Int): List<String>
}