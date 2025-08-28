package com.github.ai.simplesplit.android.data.repository

inline fun <T> mergeEntities(
    localEntities: List<T>,
    remoteEntities: List<T>,
    entityToUidMapper: (T) -> String,
    isEqual: (local: T, remote: T) -> Boolean,
    onInsert: (T) -> Unit,
    onUpdate: (local: T, remote: T) -> Unit,
    onDelete: (T) -> Unit
): Boolean {
    val uidToLocalEntityMap = localEntities
        .associateBy { entity -> entityToUidMapper.invoke(entity) }
        .toMutableMap()

    var isDataChanged = false

    for (remote in remoteEntities) {
        val uid = entityToUidMapper.invoke(remote)
        val local = uidToLocalEntityMap.remove(uid)
        if (local != null) {
            if (!isEqual.invoke(local, remote)) {
                onUpdate.invoke(local, remote)
                isDataChanged = true
            }
        } else {
            onInsert.invoke(remote)
            isDataChanged = true
        }
    }

    for (entity in uidToLocalEntityMap.values) {
        onDelete.invoke(entity)
        isDataChanged = true
    }

    return isDataChanged
}