package eu.tilk.wihajster.song

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "Song")
data class Song2014(
    val title : String,
    val arrangement : String,
    val part : Int,
    val offset : Float,
    val centOffset : String,
    val songLength : Float,
    val songNameSort : String,
    val startBeat : Float,
    val averageTempo : Float,
    val tuning : Tuning,
    val capo : Int,
    val artistName : String,
    val artistNameSort : String,
    val albumName : String,
    val albumNameSort : String,
    val albumYear : String,
    val albumArt : String,
    val crowdSpeed : String,
    val arrangementProperties : ArrangementProperties2014,
    val lastConversionDateTime : String,
    @JacksonXmlProperty(localName = "phrase")
    @JacksonXmlElementWrapper(localName = "phrases")
    val phrases : List<Phrase>,
    @JacksonXmlProperty(localName = "newLinkedDiff")
    @JacksonXmlElementWrapper(localName = "newLinkedDiffs")
    val newLinkedDiffs : List<NewLinkedDiff>,
    @JacksonXmlProperty(localName = "phraseIteration")
    @JacksonXmlElementWrapper(localName = "phraseIterations")
    val phraseIterations : List<PhraseIteration2014>,
    @JacksonXmlProperty(localName = "linkedDiff")
    @JacksonXmlElementWrapper(localName = "linkedDiffs")
    val linkedDiffs : List<LinkedDiff>,
    @JacksonXmlProperty(localName = "phraseProperty")
    @JacksonXmlElementWrapper(localName = "phraseProperties")
    val phraseProperties : List<PhraseProperty>,
    @JacksonXmlProperty(localName = "chordTemplate")
    @JacksonXmlElementWrapper(localName = "chordTemplates")
    val chordTemplates : List<ChordTemplate2014>,
    @JacksonXmlProperty(localName = "ebeat")
    @JacksonXmlElementWrapper(localName = "ebeats")
    val eBeats : List<EBeat>,
    @JacksonXmlProperty(localName = "tonebase")
    val toneBase : String,
    @JacksonXmlProperty(localName = "tonea")
    val toneA : String,
    @JacksonXmlProperty(localName = "toneb")
    val toneB : String,
    @JacksonXmlProperty(localName = "tonec")
    val toneC : String,
    @JacksonXmlProperty(localName = "toned")
    val toneD : String,
    @JacksonXmlProperty(localName = "tone")
    @JacksonXmlElementWrapper(localName = "tones")
    val tones : List<Tone2014>,
    @JacksonXmlProperty(localName = "section")
    @JacksonXmlElementWrapper(localName = "sections")
    val sections : List<Section>,
    @JacksonXmlProperty(localName = "event")
    @JacksonXmlElementWrapper(localName = "events")
    val events : List<Event>,
    @JacksonXmlProperty(localName = "control")
    @JacksonXmlElementWrapper(localName = "controls")
    val controls : List<Control>,
    val transcriptionTrack : TranscriptionTrack2014,
    @JacksonXmlProperty(localName = "level")
    @JacksonXmlElementWrapper(localName = "levels")
    val levels : List<Level2014>
    )