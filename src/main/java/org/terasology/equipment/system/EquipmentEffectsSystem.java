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

import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.alterationEffects.boost.HealthBoostAlterationEffect;
import org.terasology.alterationEffects.breath.WaterBreathingAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.CureAllDamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.DamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.decover.DecoverAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.resist.ResistDamageAlterationEffect;
import org.terasology.alterationEffects.speed.ItemUseSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.MultiJumpAlterationEffect;
import org.terasology.alterationEffects.speed.StunAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.equipment.component.effects.BoostEffectComponent;
import org.terasology.equipment.component.effects.BreathingEffectComponent;
import org.terasology.equipment.component.effects.CureDamageOverTimeEffectComponent;
import org.terasology.equipment.component.effects.DamageOverTimeEffectComponent;
import org.terasology.equipment.component.effects.DecoverEffectComponent;
import org.terasology.equipment.component.effects.ItemUseSpeedEffectComponent;
import org.terasology.equipment.component.effects.JumpSpeedEffectComponent;
import org.terasology.equipment.component.effects.MultiJumpEffectComponent;
import org.terasology.equipment.component.effects.RegenEffectComponent;
import org.terasology.equipment.component.effects.ResistEffectComponent;
import org.terasology.equipment.component.effects.StunEffectComponent;
import org.terasology.equipment.component.EquipmentComponent;
import org.terasology.equipment.component.EquipmentEffectComponent;
import org.terasology.equipment.component.effects.SwimSpeedEffectComponent;
import org.terasology.equipment.component.effects.WalkSpeedEffectComponent;
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

    /**
     * Maps the EquipmentEffectComponents to their corresponding EffectComponents so that
     * 1. the system knows which EquipmentEffectComponents to look out for
     * 2. the system knows which AlterationEffect to apply
     */
    private Map<Class, AlterationEffect> effectComponents = new HashMap<>();

    @Override
    public void initialise() {
        addEffect(BoostEffectComponent.class, new HealthBoostAlterationEffect(context));
        addEffect(BreathingEffectComponent.class, new WaterBreathingAlterationEffect(context));
        addEffect(CureDamageOverTimeEffectComponent.class, new CureAllDamageOverTimeAlterationEffect(context));
        addEffect(DamageOverTimeEffectComponent.class, new DamageOverTimeAlterationEffect(context));
        addEffect(DecoverEffectComponent.class, new DecoverAlterationEffect(context));
        addEffect(RegenEffectComponent.class, new RegenerationAlterationEffect(context));
        addEffect(ResistEffectComponent.class, new ResistDamageAlterationEffect(context));
        addEffect(ItemUseSpeedEffectComponent.class, new ItemUseSpeedAlterationEffect(context));
        addEffect(JumpSpeedEffectComponent.class, new JumpSpeedAlterationEffect(context));
        addEffect(MultiJumpEffectComponent.class, new MultiJumpAlterationEffect(context));
        addEffect(SwimSpeedEffectComponent.class, new SwimSpeedAlterationEffect(context));
        addEffect(StunEffectComponent.class, new StunAlterationEffect(context));
        addEffect(WalkSpeedEffectComponent.class, new WalkSpeedAlterationEffect(context));
    }

    @ReceiveEvent
    public void onEquip(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        //loops through known EquipmentEffectComponents
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
                if (eec != null && eec.affectsUser) {
                    applyEffect(entry.getValue(), eec, entity, entity);
                }
            }
        }
    }

    @ReceiveEvent
    public void onUnequip(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
                if (eec != null && eec.affectsUser) {
                    removeEffect(entry.getValue(), eec, entity, entity);
                }
            }
        }
    }

    @ReceiveEvent
    public void takingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        EntityRef item = event.getDirectCause();
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getDirectCause().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) item.getComponent(entry.getKey());
                if (eec != null && eec.affectsEnemies) {
                    applyEffect(entry.getValue(), eec, event.getInstigator(), damageTarget);
                }
            }
        }
    }

    public void addEffect(Class eec, AlterationEffect alterationEffect) {
        effectComponents.put(eec, alterationEffect);
    }

    private void applyEffect(AlterationEffect alterationEffect, EquipmentEffectComponent eec, EntityRef instigator, EntityRef entity) {
            if (alterationEffect != null) {
                if (eec.id != null) {
                    alterationEffect.applyEffect(instigator, entity, eec.id, eec.magnitude, eec.duration);
                } else {
                    alterationEffect.applyEffect(instigator, entity, eec.magnitude, eec.duration);
                }
            }
    }

    //workaround to remove effect by setting duration to 0
    private void removeEffect(AlterationEffect alterationEffect, EquipmentEffectComponent eec, EntityRef instigator, EntityRef entity) {
        if (alterationEffect != null) {
            if (eec.id != null) {
                alterationEffect.applyEffect(instigator, entity, eec.id, eec.magnitude, 0);
            } else {
                alterationEffect.applyEffect(instigator, entity, eec.magnitude, 0);
            }
        }
    }

}
