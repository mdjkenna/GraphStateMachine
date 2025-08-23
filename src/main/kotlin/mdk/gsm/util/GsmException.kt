package mdk.gsm.util

sealed class GsmException : Throwable() {
    class PreviousActionUnsupported() : GsmException()

    class ResetActionUnsupported() : GsmException()

    class TracePathUnsupported() : GsmException()
}