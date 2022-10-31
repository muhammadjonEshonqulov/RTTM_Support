package uz.rttm.support.utils

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.collectLA(
    owner: LifecycleOwner,
    crossinline onCollect: suspend (T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        collect {
            onCollect(it)
        }
    }
}

inline fun <T> Flow<T>.collectLA(
    owner: LifecycleCoroutineScope,
    crossinline onCollect: suspend (T) -> Unit
) = owner.launch {

    collect {
        onCollect(it)
    }

}

inline fun <T> Flow<T>.collectLatestLA(
    owner: LifecycleOwner,
    crossinline onCollect: suspend (T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        collectLatest {
            onCollect(it)
        }
    }
}

inline fun <T> Flow<T>.collectLatestLA(
    owner: LifecycleCoroutineScope,
    crossinline onCollect: suspend (T) -> Unit
) = owner.launch {

    collectLatest {
        onCollect(it)
    }

}

fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    transform: (data: T) -> K
): StateFlow<K> {
    return mapLatest { transform(it) }.stateIn(scope, SharingStarted.Eagerly, transform(value))
}

fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    initialValue: K,
    transform: suspend (data: T) -> K
): StateFlow<K> {
    return mapLatest { transform(it) }.stateIn(scope, SharingStarted.Eagerly, initialValue)
}