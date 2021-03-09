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
package org.terasology.equipment.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.HashMap;
import java.util.Map;

/**
 * This component is used for storing a mapped list of physical stats modifiers that are applied to the entity that
 * this component's attached to. Each map entry is a reference to an item's or effect's physical stat modifiers. This is
 * intended to be attached to entities, not items. Use PhysicalStatsModifierComponent for items.
 *
 * Note: Make sure that the entity you are attaching this to has a PhysicalStatsComponent.
 */
public class EquipmentEffectsListComponent implements Component {
    /** A map of equipment-based effects being applied to an entity. */
    @Replicate
    public Map<String, Map<String, EquipmentEffectComponent>> effects =
            new HashMap<String, Map<String, EquipmentEffectComponent>>();
}
