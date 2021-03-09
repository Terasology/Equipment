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
package org.terasology.equipment.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public class EquipmentEffectComponent implements Component {
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
}
