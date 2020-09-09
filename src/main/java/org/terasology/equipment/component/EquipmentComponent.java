// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;

import java.util.List;

/**
 * A component that allows an entity to equip items.
 */
public final class EquipmentComponent implements Component {
    @Replicate
    public EntityRef equipmentInventory = EntityRef.NULL;

    /**
     * The total number of inventory slots. Should be updated when an element of 'equipmentSlots' (List) is changed.
     */
    public int numberOfSlots;

    /**
     * A List of equipment slots.
     */
    // Replace or add a map?
    @Replicate
    public List<EquipmentSlot> equipmentSlots = Lists.newArrayList();
}
