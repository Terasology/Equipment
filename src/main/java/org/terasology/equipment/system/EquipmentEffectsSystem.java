/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.collect.Lists;
import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.alterationEffects.AlterationEffects;
import org.terasology.alterationEffects.OnEffectModifyEvent;
import org.terasology.alterationEffects.OnEffectRemoveEvent;
import org.terasology.alterationEffects.boost.HealthBoostAlterationEffect;
import org.terasology.alterationEffects.breath.WaterBreathingAlterationEffect;
import org.terasology.alterationEffects.buff.BuffDamageAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.CureAllDamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.DamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.decover.DecoverAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.resist.ResistDamageAlterationEffect;
import org.terasology.alterationEffects.resist.ResistDamageEffect;
import org.terasology.alterationEffects.speed.ItemUseSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.MultiJumpAlterationEffect;
import org.terasology.alterationEffects.speed.StunAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.climateConditions.alterationEffects.BodyTemperatureAlterationEffect;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.equipment.component.EquipmentComponent;
import org.terasology.equipment.component.EquipmentEffectComponent;
import org.terasology.equipment.component.EquipmentEffectsListComponent;
import org.terasology.equipment.component.effects.BodyTemperatureEffectComponent;
import org.terasology.equipment.component.effects.BoostEffectComponent;
import org.terasology.equipment.component.effects.BreathingEffectComponent;
import org.terasology.equipment.component.effects.BuffEffectComponent;
import org.terasology.equipment.component.effects.CureDamageOverTimeEffectComponent;
import org.terasology.equipment.component.effects.DamageOverTimeEffectComponent;
import org.terasology.equipment.component.effects.DecoverEffectComponent;
import org.terasology.equipment.component.effects.ItemUseSpeedEffectComponent;
import org.terasology.equipment.component.effects.JumpSpeedEffectComponent;
import org.terasology.equipment.component.effects.MultiJumpEffectComponent;
import org.terasology.equipment.component.effects.RegenEffectComponent;
import org.terasology.equipment.component.effects.ResistEffectComponent;
import org.terasology.equipment.component.effects.StunEffectComponent;
import org.terasology.equipment.component.effects.SwimSpeedEffectComponent;
import org.terasology.equipment.component.effects.WalkSpeedEffectComponent;
import org.terasology.equipment.event.EquipItemEvent;
import org.terasology.equipment.event.UnequipItemEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.health.events.BeforeDamagedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This system handles the application and removal of equipment-based effects.
 */
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

    private List<Class> multiDamageEffects = Lists.newArrayList();

    /**
     * Initialize both maps.
     */
    @Override
    public void initialise() {
        addEffect(BodyTemperatureEffectComponent.class, new BodyTemperatureAlterationEffect(context));
        addEffect(BoostEffectComponent.class, new HealthBoostAlterationEffect(context));
        addEffect(BreathingEffectComponent.class, new WaterBreathingAlterationEffect(context));
        addEffect(BuffEffectComponent.class, new BuffDamageAlterationEffect(context));
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

        multiDamageEffects.add(ResistEffectComponent.class);
    }

    /**
     * When an equipment item has been equipped, add all of its equipment effect components into the equipment effect
     * modifiers double map.
     *
     * @param event     Event containing information about the item equipped.
     * @param entity    Entity that equipped this item.
     * @param eq        Reference to the entity's equipment component. Used as a delimiter/filter.
     */
    @ReceiveEvent
    public void onEquip(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        // Loop through known EquipmentEffectComponents.
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            // If the item has one of the equipment effect components.
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
                    } else {
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

    /**
     * Tallies up the magnitude and duration of one type of equipment effect and returns it in one combined
     * EquipmentEffectComponent.
     *
     * @param eec           The base EquipmentEffectComponent to be used. The only thing that'll be used is its type.
     * @param eqEffectsList The list of equipment effects present on the entity.
     * @param effectClass   The base class of effect that's being tallied for.
     * @param entity        The entity that either has this effect or will have this applied soon.
     * @return              A EquipmentEffectComponent with the combination of all magnitudes and durations of the same
     *                      type (and subtype if any) as eec.
     */
    private EquipmentEffectComponent combineEffectValues(EquipmentEffectComponent eec, EquipmentEffectsListComponent eqEffectsList,
                                                         Class effectClass, EntityRef entity) {
        int duration = 0;
        float magnitude = 0;
        int smallestDuration = Integer.MAX_VALUE;
        boolean affectsUser = true; // Assume this is always true for now.
        boolean affectsEnemies = false; // Assume this is always false for now.
        String effectID = eec.effectID;

        // This flag is used for tracking whether an effect with infinite duration has been found.
        boolean foundInfDuration = false;

        // Create a new EquipmentEffectComponent for tallying up the combined values of this effect type (or subtype).
        EquipmentEffectComponent eecCombined = new EquipmentEffectComponent();

        // In case of effects that use IDs, have another check. This so that that stuff like individual ResistEffects
        // with different types of resists (e.g. Poison vs Fire vs Physical) are distinguished and tallied
        // correctly.
        if (multiDamageEffects.contains(eec.getClass())) {
            ResistEffectComponent recCombined = new ResistEffectComponent();
            // Iterate through all effects that are under this particular effect class or type.
            for (Entry<String, EquipmentEffectComponent> effectOfThisType : eqEffectsList.effects.get(effectClass.getTypeName()).entrySet()) {
                if (effectOfThisType.getValue().affectsUser) {
                    ResistEffectComponent resistEffectOfThisType = (ResistEffectComponent) effectOfThisType.getValue();
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
                        // Loop over individual damage resistances in each component.
                        for (Entry<String, ResistDamageEffect> resistDamageType : resistEffectOfThisType.resistances.entrySet()) {
                            ResistDamageEffect rde = recCombined.resistances.get(resistDamageType.getValue().resistType);
                            if (rde == null) {
                                ResistDamageEffect rdeCombined = new ResistDamageEffect();
                                rdeCombined.resistType = resistDamageType.getValue().resistType;
                                rdeCombined.resistAmount = resistDamageType.getValue().resistAmount;
                                recCombined.resistances.put(resistDamageType.getValue().resistType, rdeCombined);
                            } else {
                                rde.resistAmount += resistDamageType.getValue().resistAmount;
                            }
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
            recCombined.duration = smallestDuration;
            recCombined.effectID = effectID;
            recCombined.affectsUser = affectsUser;
            recCombined.affectsEnemies = affectsEnemies;

            // Return the combined EquipmentEffect component.
            return recCombined;
        } else if (eec.id.equals("")) {
            // Iterate through all effects that are under this particular effect class or type.
            for (Entry<String, EquipmentEffectComponent> effectOfThisType : eqEffectsList.effects.get(effectClass.getTypeName()).entrySet()) {
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
            for (Entry<String, EquipmentEffectComponent> effectOfThisType : eqEffectsList.effects.get(effectClass.getTypeName() + eec.id).entrySet()) {
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

    /**
     * When an equipment item has been unequipped, remove all of its equipment effect components from the equipment
     * effect modifiers double map.
     *
     * @param event     Event containing information about the item unequipped.
     * @param entity    The entity that unequipped this item.
     * @param eq        Reference to the entity's equipment component. Used as a delimiter/filter.
     */
    @ReceiveEvent
    public void onUnequip(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        // Loop through known EquipmentEffectComponents.
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            // If the item has one of the equipment effect components.
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

    /**
     * Before the damageTarget takes damage, apply any applicable effects to the target. That is, any effects that
     * affect enemies.
     *
     * @param event         Event containing information about what item caused the damage.
     * @param damageTarget  The entity that will be impacted by damage and any effects present on the item.
     */
    @ReceiveEvent
    public void takingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        // Get the item that will be dealing damage to the damageTarget.
        EntityRef item = event.getDirectCause();

        // Iterate through all possible equipment effect components, to see if any of them are present in the item.
        for (Entry<Class, AlterationEffect> entry: effectComponents.entrySet()) {
            // If an equipment effect was found on this item.
            if (event.getDirectCause().hasComponent(entry.getKey())) {
                // Get the equipment effect from the item.
                EquipmentEffectComponent eec = (EquipmentEffectComponent) item.getComponent(entry.getKey());

                // If the effect exists and it affects enemies, apply it on the damageTarget.
                if (eec != null && eec.affectsEnemies) {
                    applyEffect(entry.getValue(), eec, event.getInstigator(), damageTarget);
                }
            }
        }
    }

    /**
     * Add an element to the two maps.
     *
     * @param eec               The base class of the equipment effect component.
     * @param alterationEffect  The alteration effect associated with that effect type.
     */
    public void addEffect(Class eec, AlterationEffect alterationEffect) {
        effectComponents.put(eec, alterationEffect);
        alterationEffectComponents.put(alterationEffect.getClass().getTypeName(), eec);
    }

    /**
     * Apply the equipment effect on the entity.
     *
     * @param alterationEffect  The alteration effect that will create this effect.
     * @param eec               The equipment effect whose attributes will be used for the magnitude and duration.
     * @param instigator        The instigator of this effect application.
     * @param entity            The entity who will have the eec added to it.
     */
    private void applyEffect(AlterationEffect alterationEffect, EquipmentEffectComponent eec, EntityRef instigator, EntityRef entity) {
        // As long as the alteration effect is not NULL, apply the eec onto the entity.
        if (alterationEffect != null) {
            if (multiDamageEffects.contains(eec.getClass())) {
                ResistEffectComponent rec = (ResistEffectComponent) eec;
                for (Entry<String, ResistDamageEffect> resistance : rec.resistances.entrySet()) {
                    alterationEffect.applyEffect(instigator, entity, resistance.getValue().resistType, resistance.getValue().resistAmount, eec.duration);
                }
            } else if (eec.id != null) {
                alterationEffect.applyEffect(instigator, entity, eec.id, eec.magnitude, eec.duration);
            } else {
                alterationEffect.applyEffect(instigator, entity, eec.magnitude, eec.duration);
            }
        }
    }

    /**
     * Remove the equipment effect from the entity using a workaround by setting the duration to 0. Note that it's only
     * removing this particular equipment effect. So, the base alteration effect may still be applied, but without this
     * particular equipment effect contributing.
     *
     * @param alterationEffect  The alteration effect that created this effect.
     * @param eec               The equipment effect whose attributes will be used for the magnitude and duration.
     * @param instigator        The instigator of this effect removal.
     * @param entity            The entity who will have the eec removed from it.
     */
    //workaround to remove effect by setting duration to 0
    private void removeEffect(AlterationEffect alterationEffect, EquipmentEffectComponent eec, EntityRef instigator, EntityRef entity) {
        // As long as the alteration effect is not NULL, apply the eec with a duration of 0 onto the entity.
        if (alterationEffect != null) {
            if (multiDamageEffects.contains(eec.getClass())) {
                ResistEffectComponent rec = (ResistEffectComponent) eec;
                for (Entry<String, ResistDamageEffect> resistance : rec.resistances.entrySet()) {
                    alterationEffect.applyEffect(instigator, entity, resistance.getValue().resistType, resistance.getValue().resistAmount, 0);
                }
            } else if (eec.id != null) {
                alterationEffect.applyEffect(instigator, entity, eec.id, eec.magnitude, 0);
            } else {
                alterationEffect.applyEffect(instigator, entity, eec.magnitude, 0);
            }
        }
    }

    /**
     * When an alteration effect is going to be applied, add all the equipment effects that can contribute to it as
     * modifiers.
     *
     * @param event     Event containing information on what alteration effect is being applied, as well as a list of
     *                  effect modifiers that can be added to.
     * @param entity    The entity who the effect is being applied on.
     */
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
        if (component == null) {
            return;
        }
        // Check because for MultiDamage EEC id is not used.
        if (multiDamageEffects.contains(component)) {
            if (eq.effects.get(component.getTypeName()) == null) {
                return;
            }
        } else {
            if (eq.effects.get(component.getTypeName() + event.getId()) == null) {
                return;
            }
        }

        EquipmentEffectComponent applyThis = null;

        // This loop is only used to get the first element from the map of applicable equipment effects.
        for (Entry<String, EquipmentEffectComponent> effectOfThisType : eq.effects.get(component.getTypeName()).entrySet()) {
            // Get the combination of all equipment effect values that have the same type (and subtype if applicable)
            // as this one.
            applyThis = combineEffectValues(effectOfThisType.getValue(), entity.getComponent(EquipmentEffectsListComponent.class),
                    component, entity);
            // Now, add the duration, effectID, and magnitude of the combined matching equipment effects into the
            // event's list of effect modifiers.
            event.addDuration(applyThis.duration, applyThis.effectID);
            // Get the specific type damage in case of a multiDamage EEC.
            if (multiDamageEffects.contains(component)) {
                ResistEffectComponent rec = (ResistEffectComponent) applyThis;
                event.addMagnitude(rec.resistances.get(event.getId()).resistAmount);
            } else {
                event.addMagnitude(applyThis.magnitude);
            }

            return;
        }
    }

    /**
     * When an effect modifier is going to be removed, first see if it was added in this system (or module). If so,
     * remove it from the equipment effects map. Otherwise, leave it alone as it was added by another system. Remember,
     * the entire alteration effect may or may not be removed following this. It depends on whether there are any
     * remaining effect modifiers that contribute to the base alteration effect.
     *
     * @param event     Event containing information on what was the alteration effect being applied, and some details
     *                  on the effect modifier expiring.
     * @param entity    The entity who had this effect.
     */
    @ReceiveEvent
    public void onEffectRemoved(OnEffectRemoveEvent event, EntityRef entity) {
        // If there's no list of equipment effects, return.
        if (!entity.hasComponent(EquipmentEffectsListComponent.class)) {
            return;
        }

        // Get the equipment effect component from the alterationEffectComponents map using the type name of the base
        // alteration effect.
        Class component = alterationEffectComponents.get(event.getAlterationEffect().getClass().getTypeName());

        // If effect doesn't exist in the map (not an equipment effect), return.
        if (component == null) {
            return;
        }

        EquipmentEffectsListComponent eq = entity.getComponent(EquipmentEffectsListComponent.class);

        // Set the duration of the old expired effect to be 0, and remove the equipment effect from the effects map. If
        // no items exist under that effect type, remove that first-layer of the map.
        if (eq.effects.get(component.getTypeName() + event.getId()) != null) {
            eq.effects.get(component.getTypeName() + event.getId()).get(event.getEffectId()).duration = 0;
            eq.effects.get(component.getTypeName() + event.getId()).remove(event.getEffectId());
        } else {
            eq.effects.remove(component.getTypeName() + event.getId());
        }
    }
}
