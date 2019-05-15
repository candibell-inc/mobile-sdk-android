package com.candibell.sdk


class CandibellException(val errorCode: CandibellError, val errorMessage: String) : Throwable()
