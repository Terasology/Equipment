// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public class EquipmentEffectComponent implements Component<EquipmentEffectComponent> {
    /**
     * This stores this particular effect's ID. Each effect is normally intended to have a different ID, barring
     * the scenario where certain effects can replace older ones.
     */
    @Replicate
    public String effectID;

    /** The duration for which the effect lasts */
    @Replicate
    public int duration;

    /** The magnitude of the effect */
    @Replicate
    public float magnitude;

    /** Whether the effect affects the entity which equips the item */
    @Replicate
    public boolean affectsUser;

    /** Whether the effect affects enemies damaged by the item */
    @Replicate
    public boolean affectsEnemies;

    /** Optional id for certain effects */
    @Replicate
    public String id = "";

    @Override
    public void copy(EquipmentEffectComponent other) {
        this.effectID = other.effectID;
        this.duration = other.duration;
        this.magnitude = other.magnitude;
        this.affectsUser = other.affectsUser;
        this.affectsEnemies = other.affectsEnemies;
        this.id = other.id;
    }
}
