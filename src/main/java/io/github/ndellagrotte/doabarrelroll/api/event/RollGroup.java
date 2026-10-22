package io.github.ndellagrotte.doabarrelroll.api.event;

import net.minecraft.util.ResourceLocation;
import io.github.ndellagrotte.doabarrelroll.impl.event.RollGroupImpl;

import java.util.List;
import java.util.function.Supplier;

/**
 * A group of conditions that determine whether the camera should be rolling and what effects should be applied.
 * Instances can be directly used as conditions for Forge event subscribers.
 *
 * <p>Usually, you'll want to use {@link RollGroup#of(ResourceLocation)} to get an instance of this class.
 * You can then use {@link RollGroup#trueIf(Supplier)}, {@link RollGroup#falseUnless(Supplier)} or
 * {@link RollGroup#register(RollCondition)} to add conditions.
 *
 * <p>It is usually recommended to store your instance in a static final field, so you can easily use it in your
 * event registrations.
 *
 * <p>The conditions are checked in order of priority, and the first condition that returns {@link TriState#TRUE}
 * will cause the camera to roll. If no condition returns {@link TriState#TRUE}, the camera will not roll.
 * If a condition returns {@link TriState#FALSE}, the camera will not roll and no further conditions will be
 * checked. If a condition returns {@link TriState#PASS}, the next condition will be checked.
 */
public interface RollGroup extends Supplier<Boolean> {
    /**
     * Gets the RollGroup instance for the given identifier.
     * If no instance exists, a new one is created and registered on the Forge event bus.
     */
    static RollGroup of(ResourceLocation id) {
        return RollGroupImpl.instances.computeIfAbsent(id, id2 -> new RollGroupImpl());
    }

    void register(RollCondition condition);

    void register(RollCondition condition, int priority);

    void register(RollCondition condition, Supplier<Boolean> enabled);

    void register(RollCondition condition, int priority, Supplier<Boolean> enabled);

    void unregister(RollCondition condition);

    List<RollCondition> getConditions();

    void trueIf(Supplier<Boolean> condition, int priority);

    void trueIf(Supplier<Boolean> condition);

    void falseUnless(Supplier<Boolean> condition, int priority);

    void falseUnless(Supplier<Boolean> condition);

    interface RollCondition {
        TriState shouldRoll();
    }
}
