// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.nui.reflection.MappedContainer;

import java.util.List;

/**
 * This component represents a single equipment slot, and contains the slot's attributes.
 */
@MappedContainer
public class EquipmentSlot {
    // * Associated Anatomy part (optional)
    @Replicate
    public String name;
    @Replicate
    public String type;
    public EntityRef itemRef = EntityRef.NULL;
    @Replicate
    public int numSlotsOfSameType = 1;
    @Replicate
    public List<EntityRef> itemRefs = Lists.newArrayList();
}
