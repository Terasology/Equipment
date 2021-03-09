/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.equipment.system;

import org.terasology.alterationEffects.AlterationEffects;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;
import org.terasology.equipment.component.EquipmentEffectComponent;
import org.terasology.equipment.component.EquipmentEffectsListComponent;

import java.util.Iterator;
import java.util.Map;

/**
 * This authority system manages the duration updates of every equipment effect modifier in every entity.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class EquipmentEffectsAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    /** Integer storing when to check each effect. */
    private static final int CHECK_INTERVAL = 100;

    /** Last time the list of regen effects were checked. */
    private long lastUpdated;

    @In
    private Time time;
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;

    /**
     * For every update, check to see if the time's been over the CHECK_INTERVAL. If so, subtract the delta time from
     * the remaining duration of each equipment effect modifier.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        final long currentTime = time.getGameTimeInMs();

        // If the current time passes the CHECK_INTERVAL threshold, continue.
        if (currentTime >= lastUpdated + CHECK_INTERVAL) {
            // Set the lastUpdated time to be the currentTime.
            lastUpdated = currentTime;

            // Iterate through all of the entities that have equipment-based effects, and reduce the duration remaining
            // on each (as long as they have a finite amount of time).
            for (EntityRef entity : entityManager.getEntitiesWith(EquipmentEffectsListComponent.class)) {
                final EquipmentEffectsListComponent effectsList = entity.getComponent(EquipmentEffectsListComponent.class);

                // Search through each type of equipment-based AlterationEffects.
                Iterator<Map.Entry<String, Map<String, EquipmentEffectComponent>>> typeIter = effectsList.effects.entrySet().iterator();
                while (typeIter.hasNext()) {

                    // Search through each equipment-based effect under this effect type.
                    Iterator<Map.Entry<String, EquipmentEffectComponent>> effectIter = typeIter.next().getValue().entrySet().iterator();
                    while (effectIter.hasNext()) {
                        Map.Entry<String, EquipmentEffectComponent> effect = effectIter.next();

                        // Only reduce the time remaining for this equipment effect if its not indefinite.
                        if (effect.getValue().duration != AlterationEffects.DURATION_INDEFINITE) {
                            effect.getValue().duration -= (int) (delta * 1000);

                            // If this effect has no remaining time, remove it from the equipment effects map.
                            if (effect.getValue().duration <= 0) {
                                effect.getValue().duration = 0;
                                effectIter.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
