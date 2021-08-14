// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * A component used to indicate that an entity is an equipment inventory holder.
 */
@Replicate
public class EquipmentInventoryComponent implements Component<EquipmentInventoryComponent> {
    @Override
    public void copyFrom(EquipmentInventoryComponent other) {

    }
}
