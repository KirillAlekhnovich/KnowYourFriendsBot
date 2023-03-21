package com.backend.kyf.exception

class AccessDeniedException: RuntimeException(
    "You have no access to this data"
)