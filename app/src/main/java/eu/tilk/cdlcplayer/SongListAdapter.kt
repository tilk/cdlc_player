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

package eu.tilk.cdlcplayer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import eu.tilk.cdlcplayer.data.Arrangement
import eu.tilk.cdlcplayer.data.Song
import eu.tilk.cdlcplayer.data.SongWithArrangements

class SongListAdapter internal constructor(
    private val context : Context,
    private val playCallback : (Song, Arrangement) -> Unit,
    private val deleteCallback : (SongWithArrangements) -> Unit
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    private val inflater : LayoutInflater = LayoutInflater.from(context)
    private var songs = emptyList<SongWithArrangements>()

    inner class SongViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val songTitleView : TextView = itemView.findViewById(R.id.titleView)
        val songArtistView : TextView = itemView.findViewById(R.id.artistView)
        val songAlbumView : TextView = itemView.findViewById(R.id.albumView)
        val songArrangementsView : RecyclerView = itemView.findViewById(R.id.arrangementsView)
        val deleteButton : ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : SongViewHolder {
        val itemView = inflater.inflate(R.layout.song_view_item, parent, false)
        return SongViewHolder(itemView)
    }

    override fun getItemCount() = songs.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder : SongViewHolder, position : Int) {
        val current = songs[position]
        holder.songTitleView.text = current.song.title
        holder.songArtistView.text = current.song.artistName
        holder.songAlbumView.text = "${current.song.albumName} (${current.song.albumYear})"
        holder.songArrangementsView.adapter = ArrangementListAdapter(context, current.arrangements)
            { playCallback(current.song, it) }
        holder.deleteButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder
                .setMessage(
                    context.getString(
                        R.string.are_you_sure_you_want_to_delete,
                        current.song.title
                    ))
                .setPositiveButton(context.getString(R.string.delete_song)) { dialog, which ->
                    deleteCallback(current)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                    // Do nothing
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    internal fun setSongs(songs : List<SongWithArrangements>) {
        this.songs = songs
        notifyDataSetChanged()
    }
}