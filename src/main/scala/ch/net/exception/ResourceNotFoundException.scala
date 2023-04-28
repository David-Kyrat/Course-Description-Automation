package ch.net.exception

trait ResourceNotFoundException {
    // This is called a "self annotation". You can use "self" or "dog" or whatever you want.
    // It requires that those who extend this trait must also extend Throwable, or a subclass of it.
    self: Throwable =>
}

object ResourceNotFoundException {

    def fmtErr(resName: String) = {
        /* val tmp = courseYear.split("-")
        val year = tmp(0)
        val code = tmp(1) */
        f"$resName was not found. Please check spelling and retry."
    }
}

