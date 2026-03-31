package io.github.kraftlin.message

/** Marks Kraftlin message DSL scopes to prevent accidental nesting. */
@DslMarker
@Target(AnnotationTarget.CLASS)
public annotation class ChatMarker
