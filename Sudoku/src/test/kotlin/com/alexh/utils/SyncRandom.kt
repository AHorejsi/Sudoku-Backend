package com.alexh.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class SyncRandom(private val rng: Random, private val mutex: Mutex) : Random() {
    constructor(seed: Long, mutex: Mutex = Mutex()) : this(Random(seed), mutex)

    override fun nextBits(bitCount: Int): Int = runBlocking(Dispatchers.Default) {
        this@SyncRandom.mutex.withLock {
            this@SyncRandom.rng.nextBits(bitCount)
        }
    }
}
