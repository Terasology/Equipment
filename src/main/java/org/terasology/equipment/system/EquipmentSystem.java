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

import com.google.common.collect.Lists;
import org.terasology.audio.AudioManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.equipment.component.EquipmentComponent;
import org.terasology.equipment.component.EquipmentInventoryComponent;
import org.terasology.equipment.component.EquipmentItemComponent;
import org.terasology.equipment.component.EquipmentSlot;
import org.terasology.equipment.event.EquipItemEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.Arrays;

@RegisterSystem
public class EquipmentSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @ReceiveEvent
    public void itemPutIntoEquipmentSlot(BeforeItemPutInInventory event, EntityRef entity,
                                         EquipmentInventoryComponent eqInv, InventoryComponent inventory) {

        if (!event.getItem().hasComponent(EquipmentItemComponent.class)) {
            event.consume();
            return;
        }

        if (!equipItem(event.getInstigator(), event.getItem(), event.getSlot(), entity)) {
            event.consume();
        }

        int slot = event.getSlot();
        boolean hasValidation = false;

        if (hasValidation) {
            // There were validators, but no process has accepted this item
            event.consume();
        }
    }

    @ReceiveEvent
    public void itemRemovedFromEquipmentSlot(BeforeItemRemovedFromInventory event, EntityRef entity,
                                             EquipmentInventoryComponent eqInv, InventoryComponent inventory) {
        if (!event.getItem().hasComponent(EquipmentItemComponent.class)) {
            //event.consume();
            return;
        }

        if (!unequipItem(event.getInstigator(), event.getItem())) {
            event.consume();
        }

        int slot = event.getSlot();
        boolean hasValidation = false;

        if (hasValidation) {
            // There were validators, but no process has accepted this item
            event.consume();
        }
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, EquipmentComponent eq) {
        if (eq.equipmentInventory == EntityRef.NULL) {
            eq.equipmentInventory = entityManager.create("Equipment:EquipmentInventory");
            InventoryComponent inv =  eq.equipmentInventory.getComponent(InventoryComponent.class);

            for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                inv.itemSlots.add(EntityRef.NULL);
            }

            eq.equipmentInventory.saveComponent(inv);
            player.saveComponent(eq);
        }
    }

    @ReceiveEvent
    public void setItemTooltip(GetItemTooltip event, EntityRef item, EquipmentItemComponent eqItem) {
        DisplayNameComponent d = item.getComponent(DisplayNameComponent.class);
        event.getTooltipLines().add(new TooltipLine(d.description));
        event.getTooltipLines().add(new TooltipLine(""));

        if (eqItem.quality == 5) {
            event.getTooltipLines().add(new TooltipLine("Level " + eqItem.level + " Rare " + eqItem.type));
        }
        else {
            event.getTooltipLines().add(new TooltipLine("Level " + eqItem.level + " Common " + eqItem.type));
        }

        event.getTooltipLines().add(new TooltipLine("Equippable on " + eqItem.location + "."));
        event.getTooltipLines().add(new TooltipLine("Physical Attack: " + eqItem.attack));
        event.getTooltipLines().add(new TooltipLine("Physical Defense: " + eqItem.defense));
        event.getTooltipLines().add(new TooltipLine("Speed: " + eqItem.speed));
        event.getTooltipLines().add(new TooltipLine("Weight: " + eqItem.weight));
    }

    @ReceiveEvent
    public void uponReceiveEquipRequest(EquipItemEvent eEvent, EntityRef instigator, EquipmentComponent eqComponent) {
        if (!eEvent.getItem().hasComponent(EquipmentItemComponent.class)) {
            return;
        }

        for (EquipmentSlot eSlot : eqComponent.equipmentSlots) {
            if (eSlot.type.equalsIgnoreCase(eEvent.getSlotName())) {
                if (eSlot.itemRef != EntityRef.NULL) {
                    return;
                }
                else {
                    eSlot.itemRef = eEvent.getItem();
                    return;
                }
            }
        }

        eEvent.getCharacter().saveComponent(eqComponent);
    }

    private boolean equipItem(EntityRef character, EntityRef item, int slotNumber, EntityRef eqInvEntRef) {
        EquipmentComponent eq = character.getComponent(EquipmentComponent.class);

        for (EquipmentSlot eSlot : eq.equipmentSlots) {
            if (eSlot.type.equalsIgnoreCase(item.getComponent(EquipmentItemComponent.class).location)) {
                // If there's already an equipment of the same type in a different slot, swap.
                if (eSlot.itemRef != EntityRef.NULL) {
                    InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

                    int index = 0;
                    boolean found = false;
                    InventoryComponent charInv = character.getComponent(InventoryComponent.class);
                    for (int i = 0; i < charInv.itemSlots.size() && !found; i++) {
                        if (charInv.itemSlots.get(i) == EntityRef.NULL) {
                            index = i;
                            found = true;
                        }
                    }

                    if (found) {
                        /*
                        Replace with
                        EntityRef remItem = EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved, int count);
                        c.movingItem.getComponent(InventoryComponent.class).itemSlots.get(0) = remItem;

                         */
                        inventoryManager.moveItem(eqInvEntRef, eqInvEntRef, InventoryUtils.getSlotWithItem(eqInvEntRef, eSlot.itemRef),
                                character, index, 1);

                        //inventoryManager.switchItem(character, character, index, eqInvEntRef, slotNumber);
                        eSlot.itemRef = item;
                        character.saveComponent(eq);
                        CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash").get(), 1.0f);
                        return true;
                    }
                }
                else {
                    eSlot.itemRef = item;
                    character.saveComponent(eq);
                    CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash").get(), 1.0f);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean unequipItem(EntityRef character, EntityRef item) {
        EquipmentComponent eq = character.getComponent(EquipmentComponent.class);

        for (EquipmentSlot eSlot : eq.equipmentSlots) {
            if (eSlot.type.equalsIgnoreCase(item.getComponent(EquipmentItemComponent.class).location)) {
                eSlot.itemRef = EntityRef.NULL;
                character.saveComponent(eq);
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash-reverse").get(), 1.0f);
                return true;
            }
        }

        return false;
    }

    @ReceiveEvent
    public void doingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        if (event.getInstigator().hasComponent(EquipmentComponent.class)) {

            EquipmentComponent eq = event.getInstigator().getComponent(EquipmentComponent.class);
            int phyAtkTotal = 0;

            for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                if (eq.equipmentSlots.get(i).itemRef != EntityRef.NULL) {
                    phyAtkTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).attack;
                }
            }

            event.add(phyAtkTotal);
        }
    }

    @ReceiveEvent
    public void takingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        if (damageTarget.hasComponent(EquipmentComponent.class)) {

            // TODO: Ascertain why this call sometimes fails when the damage is caused by falling.
            boolean result = damageTarget.hasComponent(EquipmentComponent.class);
            EquipmentComponent eq = damageTarget.getComponent(EquipmentComponent.class);
            int phyDefTotal = 0;

            for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                if (eq.equipmentSlots.get(i).itemRef != EntityRef.NULL) {
                    phyDefTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).defense;
                }
            }

            event.add(-phyDefTotal);
        }
    }

}
