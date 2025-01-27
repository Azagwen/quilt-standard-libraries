/*
 * Copyright 2021-2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.registry.attachment.impl;

import java.util.Optional;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.DefaultValueProvider;

public final class ComputedDefaultRegistryEntryAttachmentImpl<R, V> extends RegistryEntryAttachmentImpl<R, V> {
	private static final Logger COMPUTE_LOGGER = LoggerFactory.getLogger("RegistryEntryAttachment|Compute");

	private final @NotNull DefaultValueProvider<R, V> defaultValueProvider;

	public ComputedDefaultRegistryEntryAttachmentImpl(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec, Side side, @NotNull DefaultValueProvider<R, V> defaultValueProvider) {
		super(registry, id, valueClass, codec, side);
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	protected Optional<V> getDefaultValue(R entry) {
		var result = this.defaultValueProvider.computeDefaultValue(entry);

		if (result.hasFailed()) {
			COMPUTE_LOGGER.error("Failed to compute value for entry {}: {}", this.registry.getId(entry), result.error());
			return Optional.empty();
		} else {
			var value = result.get();
			RegistryEntryAttachmentHolder.getBuiltin(this.registry).putValue(this, entry, value);
			return Optional.of(value);
		}
	}
}
