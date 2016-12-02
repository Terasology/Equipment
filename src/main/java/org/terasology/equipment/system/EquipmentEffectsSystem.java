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
package org.terasology.equipment.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationComponent;
import org.terasology.alterationEffects.speed.*;
import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.equipment.component.effects.RegenEffectComponent;
import org.terasology.equipment.component.effects.StunEffectComponent;
import org.terasology.equipment.component.EquipmentComponent;
import org.terasology.equipment.component.EquipmentEffectComponent;
import org.terasology.equipment.event.EquipItemEvent;
import org.terasology.equipment.event.UnequipItemEvent;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.registry.In;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@RegisterSystem
public class EquipmentEffectsSystem extends BaseComponentSystem {

    @In
    private Context context;

    Logger logger = LoggerFactory.getLogger(EquipmentEffectsSystem.class);

    /**
     * Maps the EquipmentEffectComponents to their corresponding EffectComponent so that
     * 1. the system knows which EquipmentEffectComponents to look out for
     * 2. the system knows which EffectComponents to remove from the entity
     */
    private Map<Class, Class<? extends  Component>> effectComponents = new HashMap<>();

    @Override
    public void initialise() {
        effectComponents.put(StunEffectComponent.class, StunComponent.class);
        effectComponents.put(RegenEffectComponent.class, RegenerationComponent.class);
    }

    @ReceiveEvent
    public void onEquip(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        for (Entry<Class, Class<? extends Component>> entry: effectComponents.entrySet()){
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
                if (eec != null && eec.affectsUser){
                    applyEffect(eec,entity,entity);
                }
            }
        }
    }

    @ReceiveEvent
    public void onUnequip(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        for (Entry<Class, Class<? extends Component>> entry: effectComponents.entrySet()){
            EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
            if (eec != null && eec.affectsUser){
                entity.removeComponent(entry.getValue());
            }
        }
    }

    @ReceiveEvent
    public void takingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        EntityRef item = event.getDirectCause();
        for (Entry<Class, Class<? extends Component>> entry: effectComponents.entrySet()){
            EquipmentEffectComponent eec = (EquipmentEffectComponent) item.getComponent(entry.getKey());
            if (eec != null && eec.affectsEnemies ){
                applyEffect(eec,event.getInstigator(),damageTarget);
            }
        }
    }

    public void applyEffect(EquipmentEffectComponent eec, EntityRef instigator, EntityRef entity){
        AlterationEffect alterationEffect = null;
        if (eec.getClass() == StunEffectComponent.class)
            alterationEffect = new StunAlterationEffect(context);
        else if (eec.getClass() == RegenEffectComponent.class)
            alterationEffect = new RegenerationAlterationEffect(context);

        if (alterationEffect != null)
            alterationEffect.applyEffect(instigator,entity,eec.magnitude,eec.duration);
    }
}
