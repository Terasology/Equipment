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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.Replicate;

import java.util.List;

/**
 * A component that allows an entity to equip items.
 */
public final class EquipmentComponent implements Component {
    public EntityRef equipmentInventory = EntityRef.NULL;

    /** The total number of inventory slots. Should be updated when an element of 'equipmentSlots' (List) is changed. */
    public int numberOfSlots = 0;

    /** A List of equipment slots. */
    // Replace or add a map?
    @Replicate
    public List<EquipmentSlot> equipmentSlots = Lists.newArrayList();
}
