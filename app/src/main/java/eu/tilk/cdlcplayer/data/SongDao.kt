/*
 *     Copyright (C) 2020  Marek Materzok
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.tilk.cdlcplayer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongDao {
    @Transaction
    @Query("SELECT * FROM Song ORDER BY songNameSort")
    fun getSongsByTitle() : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY artistNameSort, songNameSort")
    fun getSongsByArtist() : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY albumNameSort, songNameSort")
    fun getSongsByAlbumName() : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("SELECT * FROM Song ORDER BY albumYear, songNameSort")
    fun getSongsByAlbumYear() : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("""SELECT * FROM Song 
        WHERE title LIKE :search OR albumName LIKE :search OR artistName LIKE :search
        ORDER BY songNameSort""")
    fun getSongsByTitleSearch(search : String) : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("""SELECT * FROM Song
        WHERE title LIKE :search OR albumName LIKE :search OR artistName LIKE :search
        ORDER BY artistNameSort, songNameSort""")
    fun getSongsByArtistSearch(search : String) : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("""SELECT * FROM Song
        WHERE title LIKE :search OR albumName LIKE :search OR artistName LIKE :search
        ORDER BY albumNameSort, songNameSort""")
    fun getSongsByAlbumNameSearch(search : String) : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("""SELECT * FROM Song
        WHERE title LIKE :search OR albumName LIKE :search OR artistName LIKE :search
        ORDER BY albumYear, songNameSort""")
    fun getSongsByAlbumYearSearch(search : String) : LiveData<List<SongWithArrangements>>

    @Transaction
    @Query("""DELETE FROM Song WHERE `key` = :key""")
    fun deleteSong(key : String) : Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song : Song)
}