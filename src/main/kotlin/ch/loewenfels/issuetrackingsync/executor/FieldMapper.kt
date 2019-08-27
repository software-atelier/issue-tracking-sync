package ch.loewenfels.issuetrackingsync.executor

/**
 * A [FieldMapper] takes care of synchronizing a single data instance. Often this will be a simple String field-to-field
 * mapping, but there are cases where one tracking application has multiple fields requiring synchronization into
 * a single field in the target tracking application.
 *
 * All implementations must be state-less and thread-safe, as instances might be re-used.
 */
interface FieldMapper {
    fun dosomething()
}