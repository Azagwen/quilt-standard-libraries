package net.fabricmc.fabric.api.event;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.event.impl.EventFactoryImpl;
import org.quiltmc.qsl.event.impl.FabricEventDelegate;

/**
 * Helper for creating {@link Event} classes.
 *
 * @deprecated this exists for the purpose of being compatible with existing fabric mods and will be removed during the
 * 1.18 snapshot cycle.
 *
 * <p>There is a replacement for this API inside of the {@code qsl-base} module.
 *
 * @see org.quiltmc.qsl.event.api.EventFactory
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
public final class EventFactory {
	private static boolean profilingEnabled = true;

	private EventFactory() {
	}

	/**
	 * @return True if events are supposed to be profiled.
	 */
	public static boolean isProfilingEnabled() {
		return profilingEnabled;
	}

	/**
	 * Invalidate and re-create all existing "invoker" instances across
	 * events created by this EventFactory. Use this if, for instance,
	 * the profilingEnabled field changes.
	 */
	// TODO: Turn this into an event?
	public static void invalidate() {
		// TODO: Do we want to deprecate this or redesign this?
		//EventFactoryImpl.invalidate();
	}

	/**
	 * Create an "array-backed" Event instance.
	 *
	 * <p>If your factory simply delegates to the listeners without adding custom behavior,
	 * consider using {@linkplain #createArrayBacked(Class, Object, Function) the other overload}
	 * if performance of this event is critical.
	 *
	 * @param type           The listener class type.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new FabricEventDelegate<>(EventFactoryImpl.createArrayEvent(type, invokerFactory));
	}

	/**
	 * Create an "array-backed" Event instance with a custom empty invoker,
	 * for an event whose {@code invokerFactory} only delegates to the listeners.
	 * <ul>
	 *   <li>If there is no listener, the custom empty invoker will be used.</li>
	 *   <li><b>If there is only one listener, that one will be used as the invoker
	 *   and the factory will not be called.</b></li>
	 *   <li>Only when there are at least two listeners will the factory be used.</li>
	 * </ul>
	 *
	 * <p>Having a custom empty invoker (of type (...) -&gt; {}) increases performance
	 * relative to iterating over an empty array; however, it only really matters
	 * if the event is executed thousands of times a second.
	 *
	 * @param type           The listener class type.
	 * @param emptyInvoker   The custom empty invoker.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return new FabricEventDelegate<>(createArrayBacked(type, listeners -> {
			if (listeners.length == 0) {
				return emptyInvoker;
			} else if (listeners.length == 1) {
				return listeners[0];
			} else {
				return invokerFactory.apply(listeners);
			}
		}));
	}

	/**
	 * Get the listener object name. This can be used in debugging/profiling
	 * scenarios.
	 *
	 * @param handler The listener object.
	 * @return The listener name.
	 */
	public static String getHandlerName(Object handler) {
		return handler.getClass().getName();
	}
}
