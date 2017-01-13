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
import org.terasology.alterationEffects.AlterationEffects;
import org.terasology.alterationEffects.OnEffectModifyEvent;
import org.terasology.alterationEffects.OnEffectRemoveEvent;
import org.terasology.alterationEffects.boost.HealthBoostAlterationEffect;
import org.terasology.alterationEffects.boost.HealthBoostComponent;
import org.terasology.alterationEffects.breath.WaterBreathingAlterationEffect;
import org.terasology.alterationEffects.breath.WaterBreathingComponent;
import org.terasology.alterationEffects.damageOverTime.CureAllDamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.DamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.DamageOverTimeComponent;
import org.terasology.alterationEffects.decover.DecoverAlterationEffect;
import org.terasology.alterationEffects.decover.DecoverComponent;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationComponent;
import org.terasology.alterationEffects.resist.ResistDamageAlterationEffect;
import org.terasology.alterationEffects.resist.ResistDamageComponent;
import org.terasology.alterationEffects.speed.ItemUseSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.ItemUseSpeedComponent;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedComponent;
import org.terasology.alterationEffects.speed.MultiJumpAlterationEffect;
import org.terasology.alterationEffects.speed.MultiJumpComponent;
import org.terasology.alterationEffects.speed.StunAlterationEffect;
import org.terasology.alterationEffects.speed.StunComponent;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedComponent;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedComponent;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.equipment.component.EquipmentEffectsListComponent;
import org.terasology.equipment.component.EquipmentSlot;
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
import org.terasology.protobuf.EntityData;
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

    /**
     * Maps the AlterationEffect class type names to their corresponding EquipmentEffectComponents so that
     * 1. The system knows what class type String maps to EquipmentEffectComponent.
     */
    private Map<String, Class> alterationEffectComponents = new HashMap<>();

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
        // Loop through known EquipmentEffectComponents.
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());

                if (eec != null) {
                    // Add the equipment effects list to the character if it doesn't exist.
                    if (entity.getComponent(EquipmentEffectsListComponent.class) == null) {
                        entity.addComponent(new EquipmentEffectsListComponent());
                    }

                    // Get the list of equipment effects on this entity, and set the effectID of this current equipment
                    // effect to be the associated equipment item's full JSON description.
                    EquipmentEffectsListComponent eqEffectsList = entity.getComponent(EquipmentEffectsListComponent.class);
                    eec.effectID = event.getItem().toFullDescription();

                    // In case of effects that use IDs, have another check. This so that that stuff like individual ResistEffects
                    // with different types of resists (e.g. Poison vs Fire vs Physical) are distinguished and tallied
                    // correctly.
                    if (eec.id.equals("")) {
                        if (eqEffectsList.effects.containsKey(entry.getKey().getTypeName())) {
                            eqEffectsList.effects.get(entry.getKey().getTypeName()).put(eec.effectID, eec);
                        } else {
                            eqEffectsList.effects.put(entry.getKey().getTypeName(), new HashMap<String, EquipmentEffectComponent>());
                            eqEffectsList.effects.get(entry.getKey().getTypeName()).put(eec.effectID, eec);
                        }
                    }
                    else {
                        if (eqEffectsList.effects.containsKey(entry.getKey().getTypeName() + eec.id)) {
                            eqEffectsList.effects.get(entry.getKey().getTypeName() + eec.id).put(eec.effectID, eec);
                        } else {
                            eqEffectsList.effects.put(entry.getKey().getTypeName() + eec.id, new HashMap<String, EquipmentEffectComponent>());
                            eqEffectsList.effects.get(entry.getKey().getTypeName() + eec.id).put(eec.effectID, eec);
                        }
                    }

                    // If this effect affects the user, apply it to the user.
                    if (eec.affectsUser) {
                        applyEffect(entry.getValue(), eec, entity, entity);
                    }
                }
            }
        }
    }

    // Tallies up the magnitude and duration of one type of equipment effect and returns it in one combined EquipmentEffectComponent.
    private EquipmentEffectComponent combineEffectValues(EquipmentEffectComponent eec, EquipmentEffectsListComponent eqEffectsList,
                                     Class effectClass, EntityRef entity) {
        int duration = 0;
        int magnitude = 0;
        int smallestDuration = Integer.MAX_VALUE;
        boolean affectsUser = true; // Assume this is always true for now.
        boolean affectsEnemies = false; // Assume this is always false for now.
        String effectID = eec.effectID;

        // This flag is used for tracking whether an effect with infinite duration has been found.
        boolean foundInfDuration = false;

        EquipmentEffectComponent eecCombined = new EquipmentEffectComponent();

        // TODO: What happens if we have a poison DOT applied on the user AND enemies?
        // How will that be managed?
        // If this equip effect has an ID (like Resist or DOT), treat the looping differently.

        // In case of effects that use IDs, have another check. This so that that stuff like individual ResistEffects
        // with different types of resists (e.g. Poison vs Fire vs Physical) are distinguished and tallied
        // correctly.
        if (eec.id.equals("")) {
            // Iterate through all effects that are under this particular effect class or type.
            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                    eqEffectsList.effects.get(effectClass.getTypeName()).entrySet()) {
                // As long as it affects the user, tally up the duration and magnitude, as well as determine the effect
                // with the shortestDuration and its effectIU.
                if (effectOfThisType.getValue().affectsUser) {
                    // If the duration of this new effect is below the current tally, and the new duration is not
                    // infinite, set the smallestDuration and effectID to refer to this effect.
                    if (effectOfThisType.getValue().duration < smallestDuration
                            && effectOfThisType.getValue().duration != AlterationEffects.DURATION_INDEFINITE) {
                        smallestDuration = effectOfThisType.getValue().duration;
                        effectID = effectOfThisType.getKey();
                    }

                    // If the duration of this new effect is infinite, set the foundInfDuration flag to true.
                    if (effectOfThisType.getValue().duration == AlterationEffects.DURATION_INDEFINITE) {
                        foundInfDuration = true;
                    }

                    // If the duration of this new effect is non-zero, tally up the total duration and magnitude.
                    if (effectOfThisType.getValue().duration != 0) {
                        duration += effectOfThisType.getValue().duration;
                        magnitude += effectOfThisType.getValue().magnitude;
                    }
                }
            }
        } else {
            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                    eqEffectsList.effects.get(effectClass.getTypeName() + eec.id).entrySet()) {
                // As long as the effect has the correct type and it affects the user, tally up the duration and
                // magnitude, as well as determine the effect with the shortestDuration and its effectIU.
                if (effectOfThisType.getValue().id.equalsIgnoreCase(eec.id) && effectOfThisType.getValue().affectsUser) {
                    // If the duration of this new effect is below the current tally, and the new duration is not
                    // infinite, set the smallestDuration and effectID to refer to this effect.
                    if (effectOfThisType.getValue().duration < smallestDuration
                            && effectOfThisType.getValue().duration != AlterationEffects.DURATION_INDEFINITE) {
                        smallestDuration = effectOfThisType.getValue().duration;
                        effectID = effectOfThisType.getKey();
                    }

                    // If the duration of this new effect is infinite, set the foundInfDuration flag to true.
                    if (effectOfThisType.getValue().duration == AlterationEffects.DURATION_INDEFINITE) {
                        foundInfDuration = true;
                    }

                    // If the duration of this new effect is non-zero, tally up the total duration and magnitude.
                    if (effectOfThisType.getValue().duration != 0) {
                        duration += effectOfThisType.getValue().duration;
                        magnitude += effectOfThisType.getValue().magnitude;
                    }
                }
            }
        }

        // If the smallestDuration is still at the max value, or it's at 0 amd there was an effect duration found that
        // was infinite, set the smallestDuration to infinite.
        if (smallestDuration == Integer.MAX_VALUE || (smallestDuration == 0 && foundInfDuration)) {
            smallestDuration = AlterationEffects.DURATION_INDEFINITE;
        }

        // Set the important values of the combined EquipmentEffectComponent
        eecCombined.duration = smallestDuration;
        eecCombined.magnitude = magnitude;
        eecCombined.id = eec.id;
        eecCombined.effectID = effectID;
        eecCombined.affectsUser = affectsUser;
        eecCombined.affectsEnemies = affectsEnemies;

        // Return the combined EquipmentEffect component.
        return eecCombined;
    }

    @ReceiveEvent
    public void onUnequip(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
                if (eec != null) {
                    EquipmentEffectsListComponent eqEffectsList = entity.getComponent(EquipmentEffectsListComponent.class);

                    // In case of effects that use IDs, have another check. This so that that stuff like individual ResistEffects
                    // with different types of resists (e.g. Poison vs Fire vs Physical) are distinguished and tallied
                    // correctly.
                    if (eqEffectsList != null) {
                        if (eec.id.equals("")) {
                            if (eqEffectsList.effects.containsKey(entry.getKey().getTypeName())) {
                                eqEffectsList.effects.get(entry.getKey().getTypeName()).remove(eec.effectID);
                            }
                        } else {
                            if (eqEffectsList.effects.containsKey(entry.getKey().getTypeName())) {
                                eqEffectsList.effects.get(entry.getKey().getTypeName() + eec.id).remove(eec.effectID);
                            }
                        }
                    }

                    // If this effect affects the user, remove it from the user.
                    if (eec.affectsUser) {
                        removeEffect(entry.getValue(), eec, entity, entity);
                    }
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
        alterationEffectComponents.put(alterationEffect.getClass().getTypeName(), eec);
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

    @ReceiveEvent
    public void onEquipmentEffectApplied(OnEffectModifyEvent event, EntityRef entity) {
        EquipmentEffectsListComponent eq = entity.getComponent(EquipmentEffectsListComponent.class);

        // If there's no list of equipment effects, or the list is empty, return.
        if (eq == null || eq.effects.size() == 0) {
            return;
        }

        // Get the effect component associated with this alteration effect.
        Class component = alterationEffectComponents.get(event.getAlterationEffect().getClass().getTypeName());

        // If this component doesn't exist, or the given effect is not registered in the effects map, return.
        if (component == null || eq.effects.get(component.getTypeName() + event.getId()) == null) {
            return;
        }

        EquipmentEffectComponent applyThis = null;

        for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                eq.effects.get(component.getTypeName()).entrySet()) {
            applyThis = combineEffectValues(effectOfThisType.getValue(), entity.getComponent(EquipmentEffectsListComponent.class),
                    component, entity);

            event.addDuration(applyThis.duration, applyThis.effectID);
            event.addMagnitude(applyThis.magnitude);

            return;
        }
    }

    @ReceiveEvent
    public void onEffectRemoved(OnEffectRemoveEvent event, EntityRef entity) {
        if (!entity.hasComponent(EquipmentEffectsListComponent.class)) {
            return;
        }

        Class component = alterationEffectComponents.get(event.getAlterationEffect().getClass().getTypeName());

        if (component == null) {
            return;
        }

        EquipmentEffectsListComponent eq = entity.getComponent(EquipmentEffectsListComponent.class);

        if (eq.effects.get(component.getTypeName() + event.getId()) != null) {
            String x = event.getEffectId();
            eq.effects.get(component.getTypeName() + event.getId()).get(event.getEffectId()).duration = 0;
            eq.effects.get(component.getTypeName() + event.getId()).remove(event.getEffectId());
        } else {
            eq.effects.remove(component.getTypeName() + event.getId());
        }
    }
}
