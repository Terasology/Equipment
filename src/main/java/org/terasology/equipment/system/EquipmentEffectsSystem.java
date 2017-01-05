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
    private Map<String, Class> alterationEffectComponents = new HashMap<>();
    private Map<Class, Class> equipEffectToAlterationEffectMap = new HashMap<>();

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

        addEquipToAlterationRelation(BoostEffectComponent.class, HealthBoostComponent.class);
        addEquipToAlterationRelation(BreathingEffectComponent.class, WaterBreathingComponent.class);
        addEquipToAlterationRelation(CureDamageOverTimeEffectComponent.class, CureDamageOverTimeEffectComponent.class);
        addEquipToAlterationRelation(DamageOverTimeEffectComponent.class, DamageOverTimeComponent.class);
        addEquipToAlterationRelation(DecoverEffectComponent.class, DecoverComponent.class);
        addEquipToAlterationRelation(RegenEffectComponent.class, RegenerationComponent.class);
        addEquipToAlterationRelation(ResistEffectComponent.class, ResistDamageComponent.class);
        addEquipToAlterationRelation(ItemUseSpeedEffectComponent.class, ItemUseSpeedComponent.class);
        addEquipToAlterationRelation(JumpSpeedEffectComponent.class, JumpSpeedComponent.class);
        addEquipToAlterationRelation(MultiJumpEffectComponent.class, MultiJumpComponent.class);
        addEquipToAlterationRelation(SwimSpeedEffectComponent.class, SwimSpeedComponent.class);
        addEquipToAlterationRelation(StunEffectComponent.class, StunComponent.class);
        addEquipToAlterationRelation(WalkSpeedEffectComponent.class, WalkSpeedComponent.class);
    }

    @ReceiveEvent
    public void onEquip(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        //loops through known EquipmentEffectComponents
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());

                if (eec != null) {
                    // Add the equipment effects list to the character if it doesn't exist.
                    if (entity.getComponent(EquipmentEffectsListComponent.class) == null) {
                        entity.addComponent(new EquipmentEffectsListComponent());
                    }

                    EquipmentEffectsListComponent eqEffectsList = entity.getComponent(EquipmentEffectsListComponent.class);
                    eec.effectID = event.getItem().toFullDescription();

                    // In case of potions that use IDs, have another check. This so that that stuff like individual ResistEffects
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

                    // Now, I need to check to see if a list of this type of eq modifier already exists in the map.
                    // If it does, sum the effects up.
                    // I need a map of maps.
                    // Map<String, Map<String, EquipmentEffectComponent>>

                    //TEST
                    if (eec.affectsUser) {
                        applyEffect(entry.getValue(), eec, entity, entity);
                    }

                    // TODO: Move the following to a function!
                    /*
                    if (eec.affectsUser) {
                        int duration = 0;
                        int magnitude = 0;
                        boolean affectsUser = true; // Assume this is always true for now.
                        boolean affectsEnemies = false; // Assume this is always false for now.
                        String id;

                        EquipmentEffectComponent eecCombined = new EquipmentEffectComponent();

                        // TODO: What happens if we have a poison DOT applied on the user AND enemies?
                        // How will that be managed?
                        // If this equip effect has an ID (like Resist or DOT), treat the looping differently.
                        if (eec.id.equals("")) {
                            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                                    eqEffectsList.effects.get(entry.getKey().getTypeName()).entrySet()) {
                                if (effectOfThisType.getValue().affectsUser) {
                                    duration += effectOfThisType.getValue().duration;
                                    magnitude += effectOfThisType.getValue().magnitude;
                                }
                            }
                        } else {
                            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                                    eqEffectsList.effects.get(entry.getKey().getTypeName()).entrySet()) {
                                if (effectOfThisType.getValue().id.equalsIgnoreCase(eec.id) && effectOfThisType.getValue().affectsUser) {
                                    duration += effectOfThisType.getValue().duration;
                                    magnitude += effectOfThisType.getValue().magnitude;
                                }
                            }
                        }

                        eecCombined.duration = duration;
                        eecCombined.magnitude = magnitude;
                        eecCombined.id = eec.id;
                        eecCombined.affectsUser = affectsUser;
                        eecCombined.affectsEnemies = affectsEnemies;

                        applyEffect(entry.getValue(), eecCombined, entity, entity);
                    }
                    */
                }
            }
        }
    }

    // Tallies up the magnitude and duration of one type of equipment effect and apply it to the given entity.
    private EquipmentEffectComponent combineEffectValues(EquipmentEffectComponent eec, EquipmentEffectsListComponent eqEffectsList,
                                     Class effectClass, EntityRef entity) {
        int duration = 0;
        int magnitude = 0;
        int smallestDuration = Integer.MAX_VALUE;
        boolean affectsUser = true; // Assume this is always true for now.
        boolean affectsEnemies = false; // Assume this is always false for now.
        String effectID = eec.effectID;

        EquipmentEffectComponent eecCombined = new EquipmentEffectComponent();

        // TODO: What happens if we have a poison DOT applied on the user AND enemies?
        // How will that be managed?
        // If this equip effect has an ID (like Resist or DOT), treat the looping differently.
        if (eec.id.equals("")) {
            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                    eqEffectsList.effects.get(effectClass.getTypeName()).entrySet()) {
                if (effectOfThisType.getValue().affectsUser) {
                    if (effectOfThisType.getValue().duration < smallestDuration) {
                        smallestDuration = effectOfThisType.getValue().duration;
                        effectID = effectOfThisType.getKey();
                    }

                    duration += effectOfThisType.getValue().duration;
                    magnitude += effectOfThisType.getValue().magnitude;
                }
            }
        } else {
            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                    eqEffectsList.effects.get(effectClass.getTypeName() + eec.id).entrySet()) {
                if (effectOfThisType.getValue().id.equalsIgnoreCase(eec.id) && effectOfThisType.getValue().affectsUser) {
                    if (effectOfThisType.getValue().duration < smallestDuration) {
                        smallestDuration = effectOfThisType.getValue().duration;
                        effectID = effectOfThisType.getKey();
                    }

                    duration += effectOfThisType.getValue().duration;
                    magnitude += effectOfThisType.getValue().magnitude;
                }
            }
        }

        eecCombined.duration = smallestDuration; //duration;
        eecCombined.magnitude = magnitude;
        eecCombined.id = eec.id;
        eecCombined.effectID = effectID; //eec.id;
        eecCombined.affectsUser = affectsUser;
        eecCombined.affectsEnemies = affectsEnemies;

        return eecCombined;
        /*
        if (duration > 0 && magnitude != 0) {
            applyEffect(entry.getValue(), eecCombined, entity, entity);
        }
        */
    }

    @ReceiveEvent
    public void onUnequip(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            if (event.getItem().hasComponent(entry.getKey())) {
                EquipmentEffectComponent eec = (EquipmentEffectComponent) event.getItem().getComponent(entry.getKey());
                if (eec != null) {
                    EquipmentEffectsListComponent eqEffectsList = entity.getComponent(EquipmentEffectsListComponent.class);

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

                    //TEST
                    if (eec.affectsUser) {
                        removeEffect(entry.getValue(), eec, entity, entity);
                    }

                    /*
                    if (eec.affectsUser) {
                        removeEffect(entry.getValue(), eec, entity, entity);

                        // TODO: Move the following to a function!
                        // ----

                        int duration = 0;
                        int magnitude = 0;
                        boolean affectsUser = true; // Assume this is always true for now.
                        boolean affectsEnemies = false; // Assume this is always false for now.
                        String id;

                        EquipmentEffectComponent eecCombined = new EquipmentEffectComponent();

                        // TODO: What happens if we have a poison DOT applied on the user AND enemies?
                        // How will that be managed?
                        // If this equip effect has an ID (like Resist or DOT), treat the looping differently.
                        if (eec.id.equals("")) {
                            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                                    eqEffectsList.effects.get(entry.getKey().getTypeName()).entrySet()) {
                                if (effectOfThisType.getValue().affectsUser) {
                                    duration += effectOfThisType.getValue().duration;
                                    magnitude += effectOfThisType.getValue().magnitude;
                                }
                            }
                        } else {
                            for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                                    eqEffectsList.effects.get(entry.getKey().getTypeName()).entrySet()) {
                                if (effectOfThisType.getValue().id.equalsIgnoreCase(eec.id) && effectOfThisType.getValue().affectsUser) {
                                    duration += effectOfThisType.getValue().duration;
                                    magnitude += effectOfThisType.getValue().magnitude;
                                }
                            }
                        }

                        eecCombined.duration = duration;
                        eecCombined.magnitude = magnitude;
                        eecCombined.id = eec.id;
                        eecCombined.affectsUser = affectsUser;
                        eecCombined.affectsEnemies = affectsEnemies;

                        if (duration > 0 && magnitude != 0) {
                            //applyEffect(entry.getValue(), eecCombined, entity, entity);
                        }
                    }
                    */
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

    public void addEquipToAlterationRelation(Class eec, Class aec) {
        equipEffectToAlterationEffectMap.put(eec, aec);
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

    //----


    @ReceiveEvent
    public void onEquipmentEffectApplied(OnEffectModifyEvent event, EntityRef entity) {
        EquipmentEffectsListComponent eq = entity.getComponent(EquipmentEffectsListComponent.class);
        if (eq == null || eq.effects.size() == 0) {
            return;
        }

        Class component = alterationEffectComponents.get(event.getAlterationEffect().getClass().getTypeName());
        if (component == null || eq.effects.get(component.getTypeName()) == null) {
            return;
        }

        EquipmentEffectComponent applyThis = null;

        for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                eq.effects.get(component.getTypeName()).entrySet()) {
            applyThis = combineEffectValues(effectOfThisType.getValue(), entity.getComponent(EquipmentEffectsListComponent.class),
                    component, entity);

            if (applyThis.duration <= 0) {
                //return;
            }

            event.addDuration(applyThis.duration, applyThis.effectID);
            event.addMagnitude(applyThis.magnitude);

            return;
        }

        /*
        EquipmentComponent equipmentComponent = entity.getComponent(EquipmentComponent.class);


        if (equipmentComponent != null ) {
            float magnitude = 0;
            long duration = 0;

            for (EquipmentSlot eqSlot : equipmentComponent.equipmentSlots) {
                if (eqSlot.itemRefs.get(0).hasComponent(RegenEffectComponent.class)) {
                    RegenEffectComponent r = eqSlot.itemRefs.get(0).getComponent(RegenEffectComponent.class);

                    magnitude += r.magnitude;
                    duration += r.duration;
                }
            }

            if (duration <= 0) {
                return;
            }

            event.addDuration(duration);
            event.addMagnitude(magnitude);
        }
        */
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
            eq.effects.get(component.getTypeName() + event.getId()).remove(event.getEffectId());
        } else {
            eq.effects.remove(component.getTypeName() + event.getId());
            // TODO: Until I figure out how to remove specific item effects. Perhaps send the item dsecription into the DelayManager ID?
        }
    }
    /*
    @ReceiveEvent
    public void onRegenerationEffectApplied(OnEffectModifyEvent event, EntityRef entity) {
        if (!entity.hasComponent(RegenerationComponent.class)) {
            //return;
        }

        Class component = alterationEffectComponents.get(event.getAlterationEffect().getClass().getTypeName());

        if (component == null) {
            return;
        }

        EquipmentEffectComponent applyThis = null;

        for (Entry<String, EquipmentEffectComponent> effectOfThisType :
                entity.getComponent(EquipmentEffectsListComponent.class).effects.get(component.getTypeName()).entrySet()) {
            applyThis = combineEffectValues(effectOfThisType.getValue(), entity.getComponent(EquipmentEffectsListComponent.class),
                    component, entity); // NEED TO MOVE APPLY EFFECT TO HERE.
            break;
        }

        EquipmentComponent equipmentComponent = entity.getComponent(EquipmentComponent.class);

        if (equipmentComponent != null ) {
            float magnitude = 0;
            long duration = 0;

            for (EquipmentSlot eqSlot : equipmentComponent.equipmentSlots) {
                if (eqSlot.itemRefs.get(0).hasComponent(RegenEffectComponent.class)) {
                    RegenEffectComponent r = eqSlot.itemRefs.get(0).getComponent(RegenEffectComponent.class);

                    magnitude += r.magnitude;
                    duration += r.duration;
                }
            }

            if (duration <= 0) {
                return;
            }

            event.addDuration(duration);
            event.addMagnitude(magnitude);
        }
    }
    */

}
