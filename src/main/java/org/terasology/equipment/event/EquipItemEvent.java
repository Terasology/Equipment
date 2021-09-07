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
package org.terasology.equipment.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.equipment.component.EquipmentSlot;
import org.terasology.gestalt.entitysystem.event.Event;

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
     * @param character      an EntityRef pointing to the character who is equipping an item
     * @param item           an EntityRef pointing to the item that is being equipped
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
