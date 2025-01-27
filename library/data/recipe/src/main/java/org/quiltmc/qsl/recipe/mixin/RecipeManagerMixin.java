/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.recipe.mixin;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.recipe.impl.ImmutableMapBuilderUtil;
import org.quiltmc.qsl.recipe.impl.RecipeManagerImpl;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Shadow
	private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

	@Inject(
			method = "apply",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 1),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onReload(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler,
	                      CallbackInfo ci, Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap) {
		RecipeManagerImpl.apply(map, builderMap);
	}

	/**
	 * Synthetic method in {@link RecipeManager#apply(Map, ResourceManager, Profiler)} as an argument of {@code toImmutableMap}.
	 *
	 * @author QuiltMC, LambdAurora
	 * @reason Replaces immutable maps for mutable maps instead.
	 */
	@Overwrite
	private static Map<Identifier, Recipe<?>> method_20703(Map.Entry<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> entry) {
		// This is cursed. Do not look.
		return ImmutableMapBuilderUtil.specialBuild(entry.getValue());
	}

	@Inject(
			method = "apply",
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false)
	)
	private void onReloadEnd(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler,
	                         CallbackInfo ci) {
		RecipeManagerImpl.applyModifications((RecipeManager) (Object) this, this.recipes);
	}
}
