// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * A component that indicates that an entity is an equipment item. It stores all attributes of the item.
 */
public class EquipmentItemComponent implements Component {
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
}
