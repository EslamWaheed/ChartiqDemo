package com.eslamwaheed.chartiqdemo

data class NetworkException(override val message: String?, val code: Int) : Exception()