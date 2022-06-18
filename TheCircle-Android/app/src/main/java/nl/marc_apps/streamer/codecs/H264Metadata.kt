package nl.marc_apps.streamer.codecs

import nl.marc_apps.streamer.rtsp.VideoCodec

class H264Metadata(
    override val sequenceParameterSet: ByteArray,
    override val pictureParameterSet: ByteArray
) : H26xMetadata {
    override val codec = VideoCodec.H264
}
