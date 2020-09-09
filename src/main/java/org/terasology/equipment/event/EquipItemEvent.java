// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.equipment.component.EquipmentSlot;

/**
 * This event is sent to indicate that an entity is about to equip an item.
 */
@OwnerEvent
public class EquipItemEvent implements Event {
    private EntityRef character;
    private EntityRef item;
    private EquipmentSlot equipmentSlot;

    public EquipItemEvent() {
    }

    /**
     * Parameterized constructor.
     *
     * @param character an EntityRef pointing to the character who is equipping an item
     * @param item an EntityRef pointing to the item that is being equipped
     * @param equipmentSlot the equipment slot being used to equip the item
     */
    public EquipItemEvent(EntityRef character, EntityRef item, EquipmentSlot equipmentSlot) {
        this.character = character;
        this.item = item;
        this.equipmentSlot = equipmentSlot;
    }

    /**
     * Accessor function that returns the character who is equipping an item.
     *
     * @return an EntityRef pointing to the character who is equipping an item
     */
    public EntityRef getCharacter() {
        return character;
    }

    /**
     * Accessor function that returns the item that is being equipped
     *
     * @return an EntityRef pointing to the item being equipped
     */
    public EntityRef getItem() {
        return item;
    }

    /**
     * Accessor function that returns the equipment slot being used to equip the item
     *
     * @return the equipment slot being used to equip the item
     */
    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }
}
