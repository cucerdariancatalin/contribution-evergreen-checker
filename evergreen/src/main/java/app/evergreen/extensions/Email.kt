package app.evergreen.extensions

import android.net.Uri

object Email {
  fun createEmailUri(recipientAddress: String, subject: String, body: String): Uri =
    Uri.parse("mailto:$recipientAddress?subject=${subject.encodeForEmail()}&body=${body.encodeForEmail()}")
}
