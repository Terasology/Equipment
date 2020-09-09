// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.equipment.component.EquipmentSlot;

/**
 * This event is sent to indicate that an entity is about to unequip an item.
 */
@OwnerEvent
public class UnequipItemEvent implements Event {
    private EntityRef character;
    private EntityRef item;
    private EquipmentSlot equipmentSlot;

    public UnequipItemEvent() {
    }

    /**
     * Parameterized constructor.
     *
     * @param character the character who is unequipping an item
     * @param item the item being unequipped
     * @param equipmentSlot the equipment slot from wihch the item is being unequipped
     */
    public UnequipItemEvent(EntityRef character, EntityRef item, EquipmentSlot equipmentSlot) {
        this.character = character;
        this.item = item;
        this.equipmentSlot = equipmentSlot;
    }

    /**
     * Accessor function that returns the character who is unequipping an item.
     *
     * @returnan EntityRef pointing to the character who is unequipping an item
     */
    public EntityRef getCharacter() {
        return character;
    }

    /**
     * Accessor function that returns the item that is being unequipped
     *
     * @return an EntityRef pointing to the item being unequipped
     */
    public EntityRef getItem() {
        return item;
    }

    /**
     * Accessor function that returns the equipment slot from which the item is being unequipped.
     *
     * @return the equipment slot from which the item is being unequipped
     */
    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }
}
