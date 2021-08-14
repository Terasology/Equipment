// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * A component that indicates that an entity is an equipment item. It stores all attributes of the item.
 */
public class EquipmentItemComponent implements Component<EquipmentItemComponent> {
    // Dummy stats for now.
    @Replicate
    public int level;
    @Replicate
    public int quality;
    @Replicate
    public String type;
    @Replicate
    public String location;
    @Replicate
    public int attack;
    @Replicate
    public int defense;
    @Replicate
    public int weight;
    @Replicate
    public int speed;

    @Override
    public void copyFrom(EquipmentItemComponent other) {
        this.level = other.level;
        this.quality = other.quality;
        this.type = other.type;
        this.location = other.location;
        this.attack = other.attack;
        this.defense = other.defense;
        this.weight = other.weight;
        this.speed = other.speed;
    }
}
