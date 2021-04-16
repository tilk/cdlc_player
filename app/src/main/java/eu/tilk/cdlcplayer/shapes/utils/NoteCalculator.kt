/*
 *     Copyright (C) 2021  Marek Materzok
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

package eu.tilk.cdlcplayer.shapes.utils

import android.content.Context
import androidx.preference.PreferenceManager

class NoteCalculator(context : Context, bass : Boolean = false) {
    val reversedFretboard = {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.getBoolean("reversedFretboard", false)
    }()
    val lastString = if (bass) 3 else 5
    private val startY = 1.5f + (if (reversedFretboard) 0f else lastString.toFloat())
    private val dirY = if (reversedFretboard) 1 else -1
    fun calcX(fret : Byte) = fret - 0.5f
    fun calcY(string : Byte, bend : Float = 0f) = 0.25f * (startY + (string + bend) * dirY)
    fun calcZ(time : Float, curTime : Float, scrollSpeed : Float) = (curTime - time) * scrollSpeed
    fun sort(string : Byte) : Int = if (reversedFretboard) string.toInt() else lastString - string
}