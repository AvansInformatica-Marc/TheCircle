package nl.marc.thecircle.utils

import io.ktor.util.*
import java.security.Key
import java.security.PrivateKey

fun Key.toPem(): String {
    val content = encoded.encodeBase64()

    val kind = if (this is PrivateKey) {
        "PRIVATE KEY"
    } else {
        "PUBLIC KEY"
    }

    return buildString {
        append("-----BEGIN $kind-----")
        append("\r\n")
        wrapPemContent(content, this)
        append("\r\n")
        append("-----END $kind-----")
        append("\r\n")
    }
}

tailrec fun wrapPemContent(content: String, builder: StringBuilder) {
    if (content.length <= 64) {
        builder.append(content)
    } else {
        builder.append(content.substring(0, 64))
        builder.append("\r\n")
        wrapPemContent(content.substring(64), builder)
    }
}
