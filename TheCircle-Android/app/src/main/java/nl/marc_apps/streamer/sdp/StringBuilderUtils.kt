package nl.marc_apps.streamer.sdp

fun StringBuilder.appendCrlf(): StringBuilder = append("\r\n")

fun StringBuilder.appendSdpField(key: String, valueBuilder: StringBuilder.() -> Unit): StringBuilder {
    append(key)
    append("=")
    valueBuilder(this)
    appendCrlf()

    return this
}
