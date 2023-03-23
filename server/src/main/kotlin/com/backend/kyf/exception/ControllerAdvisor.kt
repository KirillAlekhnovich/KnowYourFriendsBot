package com.backend.kyf.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ControllerAdvice
class ControllerAdvisor: ResponseEntityExceptionHandler() {

    private fun generateExceptionBody(exception: Exception): Map<String, Any?> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        body["message"] = exception.message
        body["data"] = exception.stackTraceToString()
        return body
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(exception: AccessDeniedException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AttributeAlreadyExistsException::class)
    fun handleAttributeAlreadyExistsException(exception: AttributeAlreadyExistsException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(AttributeDoesNotExistException::class)
    fun handleAttributeDoesNotExistException(exception: AttributeDoesNotExistException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(FriendAlreadyExistsException::class)
    fun handleFriendAlreadyExistsException(exception: FriendAlreadyExistsException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(FriendDoesNotExistException::class)
    fun handleFriendDoesNotExistException(exception: FriendDoesNotExistException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InvalidAttributeNameException::class)
    fun handleInvalidAttributeNameException(exception: InvalidAttributeNameException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(InvalidFriendNameException::class)
    fun handleInvalidFriendNameException(exception: InvalidFriendNameException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(exception: UserAlreadyExistsException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UserDoesNotExistException::class)
    fun handleUserDoesNotExistException(exception: UserDoesNotExistException): ResponseEntity<Any?>? {
        return ResponseEntity(generateExceptionBody(exception), HttpStatus.NOT_FOUND)
    }
}