package nl.marc_apps.streamer.codecs

interface H26xMetadata : VideoMetadata {
    val sequenceParameterSet: ByteArray

    val pictureParameterSet: ByteArray
}
