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

package eu.tilk.cdlcplayer.shapes

import eu.tilk.cdlcplayer.viewer.Event
import eu.tilk.cdlcplayer.viewer.SortLevel

abstract class EventShape<out T : Event>(
    vertexCoords : FloatArray,
    drawOrder : ShortArray,
    companion : CompanionBase,
    val event : T
) : StaticShape(vertexCoords, drawOrder, companion) {
    open val endTime : Float get() = event.endTime
    abstract val sortLevel : SortLevel
    open val derived = false
}