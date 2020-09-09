// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.component.effects;

import org.terasology.alterationEffects.resist.ResistDamageEffect;
import org.terasology.engine.network.Replicate;
import org.terasology.equipment.component.EquipmentEffectComponent;

import java.util.HashMap;
import java.util.Map;

public class ResistEffectComponent extends EquipmentEffectComponent {

    /**
     * Stores the map of damage type class name to resistances.
     */
    @Replicate
    public Map<String, ResistDamageEffect> resistances = new HashMap<>();
}
