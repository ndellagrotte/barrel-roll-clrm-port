package io.github.ndellagrotte.doabarrelroll.impl.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import io.github.ndellagrotte.doabarrelroll.api.event.RollEvents;
import io.github.ndellagrotte.doabarrelroll.api.event.RollGroup;
import io.github.ndellagrotte.doabarrelroll.api.event.TriState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class RollGroupImpl implements RollGroup {
    public static final HashMap<ResourceLocation, RollGroup> instances = new HashMap<>();
    private final List<Entry> conditions = new ArrayList<>();

    public RollGroupImpl() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onShouldRollCheck(RollEvents.ShouldRollCheckEvent event) {
        if (get()) {
            event.unlock();
        }
    }

    @Override
    public void register(RollCondition condition) {
        register(condition, 0);
    }

    @Override
    public void register(RollCondition condition, int priority) {
        register(condition, priority, () -> true);
    }

    @Override
    public void register(RollCondition condition, Supplier<Boolean> enabled) {
        register(condition, 0, enabled);
    }

    @Override
    public void register(RollCondition condition, int priority, Supplier<Boolean> enabled) {
        conditions.add(new Entry(condition, priority, enabled));
    }

    @Override
    public void unregister(RollCondition condition) {
        conditions.removeIf(entry -> entry.condition == condition);
    }

    @Override
    public List<RollCondition> getConditions() {
        ImmutableList.Builder<RollCondition> result = ImmutableList.builder();
        conditions.sort(null);
        for (Entry entry : conditions) {
            if (entry.enabled.get()) {
                result.add(entry.condition);
            }
        }
        return result.build();
    }

    @Override
    public void trueIf(Supplier<Boolean> condition, int priority) {
        register(() -> condition.get() ? TriState.TRUE : TriState.PASS, priority);
    }

    @Override
    public void trueIf(Supplier<Boolean> condition) {
        trueIf(condition, 0);
    }

    @Override
    public void falseUnless(Supplier<Boolean> condition, int priority) {
        register(() -> condition.get() ? TriState.PASS : TriState.FALSE, priority);
    }

    @Override
    public void falseUnless(Supplier<Boolean> condition) {
        falseUnless(condition, 0);
    }

    @Override
    public Boolean get() {
        for (RollCondition condition : getConditions()) {
            var result = condition.shouldRoll();
            if (result != TriState.PASS) {
                return result == TriState.TRUE;
            }
        }
        return false;
    }

    private static final class Entry implements Comparable<Entry> {
        private final RollCondition condition;
        private final int priority;
        private final Supplier<Boolean> enabled;

        private Entry(RollCondition condition, int priority, Supplier<Boolean> enabled) {
            this.condition = condition;
            this.priority = priority;
            this.enabled = enabled;
        }

        @Override
        public int compareTo(Entry other) {
            return Integer.compare(priority, other.priority);
        }
    }
}
