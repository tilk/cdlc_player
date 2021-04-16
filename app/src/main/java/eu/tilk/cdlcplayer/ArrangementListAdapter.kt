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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tilk.cdlcplayer.data.Arrangement

class ArrangementListAdapter internal constructor(
    context : Context,
    private val arrangements : List<Arrangement>,
    private val playCallback : (Arrangement) -> Unit
) : RecyclerView.Adapter<ArrangementListAdapter.ArrangementViewHolder>() {

    private val inflater : LayoutInflater = LayoutInflater.from(context)

    inner class ArrangementViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val songArrangementView : TextView = itemView.findViewById(R.id.arrangementView)
        val songTuningView : TextView = itemView.findViewById(R.id.tuningView)
        val playButton : ImageButton = itemView.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ArrangementViewHolder {
        val itemView = inflater.inflate(R.layout.arrangement_view_item, parent, false)
        return ArrangementViewHolder(itemView)
    }

    override fun getItemCount() = arrangements.size

    override fun onBindViewHolder(holder : ArrangementViewHolder, position : Int) {
        val current = arrangements[position]
        holder.songArrangementView.text = current.arrangement
        holder.songTuningView.text = current.tuning
        holder.playButton.setOnClickListener { playCallback(current) }
    }
}
