/*
 * Copyright (C) 2023-2024 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.selfie

import com.diffplug.selfie.guts.CallStack
import com.diffplug.selfie.guts.DiskStorage
import com.diffplug.selfie.guts.SnapshotSystem
import com.diffplug.selfie.guts.initSnapshotSystem
import kotlin.jvm.JvmStatic

/** Static methods for creating snapshots. */
object Selfie {
  internal val system: SnapshotSystem = initSnapshotSystem()
  private val deferredDiskStorage =
      object : DiskStorage {
        override fun readDisk(sub: String, call: CallStack): Snapshot? =
            system.diskThreadLocal().readDisk(sub, call)
        override fun writeDisk(actual: Snapshot, sub: String, call: CallStack) =
            system.diskThreadLocal().writeDisk(actual, sub, call)
        override fun keep(subOrKeepAll: String?) = system.diskThreadLocal().keep(subOrKeepAll)
      }

  /**
   * Sometimes a selfie is environment-specific, but should not be deleted when run in a different
   * environment.
   */
  @JvmStatic
  fun preserveSelfiesOnDisk(vararg subsToKeep: String) {
    val disk = system.diskThreadLocal()
    if (subsToKeep.isEmpty()) {
      disk.keep(null)
    } else {
      subsToKeep.forEach { disk.keep(it) }
    }
  }

  @JvmStatic
  fun <T> expectSelfie(actual: T, camera: Camera<T>) = expectSelfie(camera.snapshot(actual))

  @JvmStatic
  fun expectSelfie(actual: ByteArray) = BinarySelfie(Snapshot.of(actual), deferredDiskStorage, "")
  @JvmStatic fun expectSelfie(actual: String) = expectSelfie(Snapshot.of(actual))
  @JvmStatic fun expectSelfie(actual: Snapshot) = StringSelfie(actual, deferredDiskStorage)
  @JvmStatic fun expectSelfie(actual: Long) = LongSelfie(actual)
  @JvmStatic fun expectSelfie(actual: Int) = IntSelfie(actual)
  @JvmStatic fun expectSelfie(actual: Boolean) = BooleanSelfie(actual)
  @JvmStatic fun memoize(toMemoize: () -> String) = memoize(Roundtrip.identity(), toMemoize)

  @JvmStatic
  fun <T> memoize(roundtrip: Roundtrip<T, String>, toMemoize: () -> T) =
      LazySelfie(deferredDiskStorage, roundtrip, toMemoize)
  /**
   * Memoizes any type which is marked with `@kotlinx.serialization.Serializable` as pretty-printed
   * json.
   */
  inline fun <reified T> memoizeAsJson(noinline toMemoize: () -> T) =
      memoize(RoundtripJson.of<T>(), toMemoize)

  @JvmStatic
  fun memoizeBinary(toMemoize: () -> ByteArray) = memoizeBinary(Roundtrip.identity(), toMemoize)

  @JvmStatic
  fun <T> memoizeBinary(roundtrip: Roundtrip<T, ByteArray>, toMemoize: () -> T) =
      LazySelfieBinary<T>(deferredDiskStorage, roundtrip, toMemoize)
}
