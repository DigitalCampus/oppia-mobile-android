package org.digitalcampus.oppia.exception

class WrongServerException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}