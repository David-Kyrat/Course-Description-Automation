package ch.net.exception

sealed trait ResourceNotFoundException {
    // This is called a "self annotation". You can use "self" or "dog" or whatever you want.
    // It requires that those who extend this trait must also extend Throwable, or a subclass of it.
    self: Throwable =>
    val message: String

}
