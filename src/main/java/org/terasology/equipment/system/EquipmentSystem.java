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
import org.terasology.equipment.event.UnequipItemEvent;
import org.terasology.equipment.ui.CharacterScreenWindow;
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
import org.terasology.physicalstats.event.OnPhysicalStatChangedEvent;
import org.terasology.physicalstats.event.OnPhysicalStatsModifierAddedEvent;
import org.terasology.physicalstats.event.OnPhysicalStatsModifierRemovedEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.Arrays;

@RegisterSystem
public class EquipmentSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
    }

    @ReceiveEvent
    public void itemPutIntoEquipmentSlot(BeforeItemPutInInventory event, EntityRef entity,
                                         EquipmentInventoryComponent eqInv, InventoryComponent inventory) {

        // Ensure that this item is actually a piece of equipment. If not, consume the event and return.
        if (!event.getItem().hasComponent(EquipmentItemComponent.class)) {
            event.consume();
            return;
        }

        // If the equip action fails, consume the event.
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

        // Ensure that this item is actually a piece of equipment. If not, return.
        if (!event.getItem().hasComponent(EquipmentItemComponent.class)) {
            //event.consume();
            return;
        }

        // If the unequip action fails, consume the event.
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

    // Instaniate the EquipmentInventory entity companion and store a refernce to it in the player's EquipmentComponent.
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, EquipmentComponent eq) {
        if (eq.equipmentInventory == EntityRef.NULL) {
            eq.equipmentInventory = entityManager.create("Equipment:EquipmentInventory");
            InventoryComponent inv =  eq.equipmentInventory.getComponent(InventoryComponent.class);

            /*
            int sizeOfEQInv = 0;

            for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                sizeOfEQInv += eq.equipmentSlots.get(i).numSlotsOfSameType;
            }
            */

            for (int i = 0; i < eq.numberOfSlots; i++) {
                inv.itemSlots.add(EntityRef.NULL);
            }

            eq.equipmentInventory.saveComponent(inv);
            player.saveComponent(eq);
        }
    }

    // Set the equipment item's tooltip based on the item's stats. 
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
            if (eSlot.type.equalsIgnoreCase(eEvent.getEquipmentSlot().type)) {
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

        // Search through all the equipment slots for a slot that has the same type as the item intended to be equipped.
        for (EquipmentSlot eSlot : eq.equipmentSlots) {
            // If this slot has the same type as the item.
            if (eSlot.type.equalsIgnoreCase(item.getComponent(EquipmentItemComponent.class).location)) {
                // Check to see if this contains an empty slot first.
                boolean isSlotEmpty = false; // Flag indicating if there's an empty slot.
                int atIndex = 0;             // The index of the empty slot.
                for (int i = 0; i < eSlot.itemRefs.size() && !isSlotEmpty; i++) {
                    if (eSlot.itemRefs.get(i) == EntityRef.NULL) {
                        isSlotEmpty = true;
                        atIndex = i;
                    }
                }

                // If all of the slots of this type are already filled, swap the one in the first slot with the new item.
                if (!isSlotEmpty) {
                    InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

                    // Find an empty spot in the character's inventory to move the swapped item out to.
                    int index = 0;         // Index of the empty spot in the player's inventory.
                    boolean found = false; // Flag indicating whether an empty spot was found.
                    InventoryComponent charInv = character.getComponent(InventoryComponent.class);
                    for (int i = 0; i < charInv.itemSlots.size() && !found; i++) {
                        if (charInv.itemSlots.get(i) == EntityRef.NULL) {
                            index = i;
                            found = true;
                        }
                    }

                    // If an empty spot was found in the character's inventory.
                    if (found) {
                        /*
                        Replace with
                        EntityRef remItem = EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved, int count);
                        c.movingItem.getComponent(InventoryComponent.class).itemSlots.get(0) = remItem;

                         */

                        // Move the equipped item in the first available slot of this equipment slot to the character's inventory.
                        inventoryManager.moveItem(eqInvEntRef, eqInvEntRef, InventoryUtils.getSlotWithItem(eqInvEntRef, eSlot.itemRefs.get(0)),
                                character, index, 1);

                        // Unequip the moved item.
                        unequipItem(character, eSlot.itemRefs.get(0));

                        // Equip the desired item in the now free slot.
                        eSlot.itemRefs.set(atIndex, item);
                        character.saveComponent(eq);

                        // Send an EquipItemEvent, play a sound, and return true, indicating that the equip action was successful.
                        character.send(new EquipItemEvent(character, item, eSlot));
                        CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash").get(), 1.0f);
                        return true;
                    }
                }
                // If there's an empty slot available in this equipment slot.
                else {
                    eSlot.itemRef = item;

                    // Equip the desired item in the free slot.
                    eSlot.itemRefs.set(atIndex, item);
                    character.saveComponent(eq);

                    // Send an EquipItemEvent, play a sound, and return true, indicating that the equip action was successful.
                    character.send(new EquipItemEvent(character, item, eSlot));
                    CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash").get(), 1.0f);
                    return true;
                }
            }
        }

        // If the execution reaches here, that means the equip action failed due to either no space left in the character's inventory, or no appropriate
        // slot type.
        return false;
    }

    private boolean unequipItem(EntityRef character, EntityRef item) {
        EquipmentComponent eq = character.getComponent(EquipmentComponent.class);

        // If this entity doesn't have an EquipmentComponent, just return true.
        if (eq == null) {
            return true;
        }

        // Iterate through all of the equipment slots.
        for (EquipmentSlot eSlot : eq.equipmentSlots) {
            // If this slot's accepted type matches the item's location.
            if (eSlot.type.equalsIgnoreCase(item.getComponent(EquipmentItemComponent.class).location)) {
                // Look through all of the item ref slots of this equipment slot.
                for (int i = 0; i < eSlot.itemRefs.size(); i++) {
                    // If the item is found in one of the item ref slots, remove the matching item from the ref slot.
                    if (eSlot.itemRefs.get(i).equals(item))
                    {
                        // Remove the reference for this item.
                        eSlot.itemRefs.set(i, EntityRef.NULL);
                        character.saveComponent(eq);

                        // Send an UnequipItemEvent, play a sound, and return true, indicating that the unequip action was successful.
                        character.send(new UnequipItemEvent(character, item, eSlot));
                        CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash-reverse").get(), 1.0f);
                        return true;
                    }
                }
            }
        }


        // If the execution reaches here, that means the unequip action failed due to the item not being found.
        return false;
    }

    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatChangedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatsModifierAddedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatsModifierRemovedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    @ReceiveEvent
    public void onEquipChanged(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    @ReceiveEvent
    public void onEquipChanged(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
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

            // Physical defense ONLY protects against physical damage. Not direct/pierce or magical damage.
            if (event.getDamageType().getUrn().getResourceName().toString().equalsIgnoreCase("physicalDamage")) {
                for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                    if (eq.equipmentSlots.get(i).itemRef != EntityRef.NULL) {
                        phyDefTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).defense;
                    }
                }
            }

            event.add(-phyDefTotal);
        }
    }
}
