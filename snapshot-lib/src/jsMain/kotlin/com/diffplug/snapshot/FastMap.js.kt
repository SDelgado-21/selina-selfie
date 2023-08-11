/*
 * Copyright (C) 2023 DiffPlug
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
package com.diffplug.snapshot

internal actual abstract class ListBackedSet<T> actual constructor() : Set<T>, AbstractList<T>()

internal actual fun <K, V> entry(key: K, value: V): Map.Entry<K, V> = E(key, value)

class E<K, V>(override val key: K, override val value: V) : Map.Entry<K, V> {
  override fun equals(other: Any?): Boolean {
    if (other is Map.Entry<*, *>) {
      return key == other.key && value == other.value
    } else {
      return false
    }
  }
  override fun hashCode(): Int = key.hashCode() xor value.hashCode()
}
