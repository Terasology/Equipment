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
import org.terasology.equipment.event.OnPlayerWithEquipSpawnedEvent;
import org.terasology.equipment.event.UnequipItemEvent;
import org.terasology.equipment.ui.CharacterScreenWindow;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physicalstats.component.PhysicalStatsModifierComponent;
import org.terasology.physicalstats.component.PhysicalStatsModifiersListComponent;
import org.terasology.physicalstats.event.OnPhysicalStatChangedEvent;
import org.terasology.physicalstats.event.OnPhysicalStatsModifierAddedEvent;
import org.terasology.physicalstats.event.OnPhysicalStatsModifierRemovedEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;

/**
 * This system handles all equipment-related operations.
 */
@RegisterSystem
public class EquipmentSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    /**
     * Called on startup for initialization.
     */
    @Override
    public void initialise() {
    }

    /**
     * Method that defines what happens when an item is put into an equipment slot.
     *
     * @param event     the event associated with the insertion of the item into the equipment slot
     * @param entity    the entity who has inserted the item into the slot
     * @param eqInv     the equipment inventory component containing the equipment slot
     * @param inventory the inventory component assiciated with the entity
     */
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

    /**
     * Defines what to do when an item is removed from an equipment slot.
     *
     * @param event     the event corresponding to the removal of the item from the equipment slot
     * @param entity    the entity who has removed the item from the equipment slot
     * @param eqInv     the equipment inventory component containing the equipment slot
     * @param inventory the inventory component assiciated with the entity
     */
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

    /**
     * Initializes equipment and inventory-related components when the player spawns.
     *
     * @param event  the event corresponding to the spawning of the player
     * @param player an EntityRef pointing to the player
     * @param eq     the player's equipment component
     */
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, EquipmentComponent eq) {
        player.send(new OnPlayerWithEquipSpawnedEvent());
        /*
        if (eq.equipmentInventory == EntityRef.NULL) {
            // Instantiate the EquipmentInventory entity companion and store a reference to it in the player's
            // EquipmentComponent.
            eq.equipmentInventory = entityManager.create("Equipment:EquipmentInventory");
            InventoryComponent inv = eq.equipmentInventory.getComponent(InventoryComponent.class);

            for (int i = 0; i < eq.numberOfSlots; i++) {
                inv.itemSlots.add(EntityRef.NULL);
            }

            // Save the equipment components.
            eq.equipmentInventory.saveComponent(inv);
            player.saveComponent(eq);

            // Toggle the CharacterScreen on in case it was NULL.
            nuiManager.toggleScreen("Equipment:BackupScreen");
            CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

            // Reinitialize the CharacterScreen so the labels and references are setup properly.
            if (screen != null) {
                screen.initialise();
                screen.updateStats();
            }

            // Toggle off the CharacterScreen.
            nuiManager.toggleScreen("Equipment:BackupScreen");
        }
        */
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerWithEquipSpawnedEvent event, EntityRef player, EquipmentComponent eq) {
        if (eq.equipmentInventory == EntityRef.NULL) {
            // Instantiate the EquipmentInventory entity companion and store a reference to it in the player's
            // EquipmentComponent.
            eq.equipmentInventory = entityManager.create("Equipment:EquipmentInventory");
            InventoryComponent inv = eq.equipmentInventory.getComponent(InventoryComponent.class);

            for (int i = 0; i < eq.numberOfSlots; i++) {
                inv.itemSlots.add(EntityRef.NULL);
            }

            // Save the equipment components.
            eq.equipmentInventory.saveComponent(inv);
            player.saveComponent(eq);

            if (localPlayer != null) {
                // Toggle the CharacterScreen on in case it was NULL.
                nuiManager.toggleScreen("Equipment:BackupScreen");
                CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

                // Reinitialize the CharacterScreen so the labels and references are setup properly.
                if (screen != null) {
                    //screen.initialise();
                    screen.reInit();
                    screen.updateStats();
                }

                // Toggle off the CharacterScreen.
                nuiManager.toggleScreen("Equipment:BackupScreen");
            }
        }
    }

    /**
     * Drops all equipment to the ground and destroys the related equipment inventory entity.
     *
     * @param event  The event corresponding to the death of the player.
     * @param player An EntityRef pointing to the player.
     * @param eq     The player's equipment component.
     */
    @ReceiveEvent(components = {CharacterComponent.class})
    public void onPlayerDeath(DoDestroyEvent event, EntityRef player, EquipmentComponent eq) {
        if (eq.equipmentInventory != EntityRef.NULL) {
            // Add a CharacterComponent and LocationComponent to the equipment inventory entity so that the items
            // stored in it can be properly dropped onto the world.
            //eq.equipmentInventory.addComponent(localPlayer.getCharacterEntity().getComponent(LocationComponent.class));
            eq.equipmentInventory.addComponent(new LocationComponent());
            eq.equipmentInventory.addComponent(new CharacterComponent());

            InventoryComponent equipmentInv = eq.equipmentInventory.getComponent(InventoryComponent.class);

            // Get the position and direction of the player, and calculate what the new position of the item should be.
            Vector3f position = localPlayer.getViewPosition();
            Vector3f direction = localPlayer.getViewDirection();
            Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                    position.y + direction.y * 1.5f,
                    position.z + direction.z * 1.5f
            );

            // Based on the direction, create the impulse vector.
            Vector3f impulseVector = new Vector3f(direction);

            // Drop every item stored in the equipment inventory entity.
            for (int slot = 0; slot < eq.numberOfSlots; slot++) {
                // Get the item stored in this slot of the equipment inventory, and if it exists, send a request to
                // drop it into the world with the given impulse in a certain position.
                EntityRef equipmentItem = equipmentInv.itemSlots.get(slot);
                if (equipmentItem.exists()) {
                    eq.equipmentInventory.send(new DropItemRequest(equipmentItem, eq.equipmentInventory,
                            impulseVector,
                            newPosition,
                            1));
                }
            }

            // Once all the items are dropped, destroy the equipment inventory entity.
            eq.equipmentInventory.destroy();
        }
    }

    /**
     * Sets an item's tooltip based on its stats.
     *
     * @param event  the event corresponding to a request to get the tooltip for an item
     * @param item   the item who's tooltip is to be set
     * @param eqItem the equipment item component associated with the item
     */
    @ReceiveEvent
    public void setItemTooltip(GetItemTooltip event, EntityRef item, EquipmentItemComponent eqItem) {
        DisplayNameComponent d = item.getComponent(DisplayNameComponent.class);
        event.getTooltipLines().add(new TooltipLine(d.description));
        event.getTooltipLines().add(new TooltipLine(""));

        if (eqItem.quality == 5) {
            event.getTooltipLines().add(new TooltipLine("Level " + eqItem.level + " Rare " + eqItem.type));
        } else {
            event.getTooltipLines().add(new TooltipLine("Level " + eqItem.level + " Common " + eqItem.type));
        }

        event.getTooltipLines().add(new TooltipLine("Equippable on " + eqItem.location + "."));
        event.getTooltipLines().add(new TooltipLine("Physical Attack: " + eqItem.attack));
        event.getTooltipLines().add(new TooltipLine("Physical Defense: " + eqItem.defense));
        event.getTooltipLines().add(new TooltipLine("Speed: " + eqItem.speed));
        event.getTooltipLines().add(new TooltipLine("Weight: " + eqItem.weight));
    }

    /**
     * Defines what to do when a request to equip an item is received.
     *
     * @param eEvent      the event corresponding to the equipment of the item
     * @param instigator  the entity instigating the item's equipment
     * @param eqComponent the equipment component associated with the entity
     */
    @ReceiveEvent
    public void uponReceiveEquipRequest(EquipItemEvent eEvent, EntityRef instigator, EquipmentComponent eqComponent) {
        if (!eEvent.getItem().hasComponent(EquipmentItemComponent.class)) {
            return;
        }

        for (EquipmentSlot eSlot : eqComponent.equipmentSlots) {
            if (eSlot.type.equalsIgnoreCase(eEvent.getEquipmentSlot().type)) {
                if (eSlot.itemRef != EntityRef.NULL) {
                    return;
                } else {
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
                        // Move the equipped item in the first available slot of this equipment slot to the character's inventory.
                        inventoryManager.moveItem(eqInvEntRef, eqInvEntRef, InventoryUtils.getSlotWithItem(eqInvEntRef, eSlot.itemRefs.get(0)),
                                character, index, 1);

                        // Unequip the moved item.
                        unequipItem(character, eSlot.itemRefs.get(0));

                        // Add item's stat modifier (if any) to the character.
                        addModifier(character, item);

                        // Equip the desired item in the now free slot.
                        eSlot.itemRefs.set(atIndex, item);
                        character.saveComponent(eq);

                        // Send an EquipItemEvent, play a sound, and return true, indicating that the equip action was successful.
                        character.send(new EquipItemEvent(character, item, eSlot));
                        CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:metal-clash").get(), 1.0f);
                        return true;
                    }
                } else { // If there's an empty slot available in this equipment slot.
                    eSlot.itemRef = item;

                    // Equip the desired item in the free slot.
                    eSlot.itemRefs.set(atIndex, item);
                    character.saveComponent(eq);

                    // Add item's stat modifier (if any) to the character.
                    addModifier(character, item);

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
                    if (eSlot.itemRefs.get(i).equals(item)) {
                        // Remove the reference for this item.
                        eSlot.itemRefs.set(i, EntityRef.NULL);
                        character.saveComponent(eq);

                        removeModifier(character, item);

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

    /**
     * Adds physical stat modifiers of an item (if any) to a character.
     *
     * @param character the character to whom the stat modifiers are to be applied
     * @param item      the item whose stat modifiers are to be applied
     */
    public void addModifier(EntityRef character, EntityRef item) {
        // If this equipment item has a physical stats modifier.
        if (item.getComponent(PhysicalStatsModifierComponent.class) != null) {
            // Add the physical stats modifier list to the character if it doesn't exist.
            if (character.getComponent(PhysicalStatsModifiersListComponent.class) == null) {
                character.addComponent(new PhysicalStatsModifiersListComponent());
            }

            // Add the item modifier to the character.
            PhysicalStatsModifiersListComponent pStatsMod = character.getComponent(PhysicalStatsModifiersListComponent.class);
            PhysicalStatsModifierComponent eqStatsMod = item.getComponent(PhysicalStatsModifierComponent.class);

            if (eqStatsMod != null) {
                eqStatsMod.id = item.toFullDescription();
                pStatsMod.modifiers.put(eqStatsMod.id, eqStatsMod);
                //pStatsMod.modifiers.put("" + eqStatsMod.hashCode(), eqStatsMod);
            }

            // Save the component.
            character.saveComponent(pStatsMod);
        }
    }

    /**
     * Removes an item's physical stat modifiers (if any) from a character.
     *
     * @param character the character from whom the stat modifiers are to be removed
     * @param item      the item whose stat modifiers are to be removed
     */
    public void removeModifier(EntityRef character, EntityRef item) {
        if (character.getComponent(PhysicalStatsModifiersListComponent.class) != null) {

            PhysicalStatsModifiersListComponent pStatsModList = character.getComponent(PhysicalStatsModifiersListComponent.class);
            PhysicalStatsModifierComponent eqStatsMod = item.getComponent(PhysicalStatsModifierComponent.class);

            if (eqStatsMod != null) {
                pStatsModList.modifiers.remove(eqStatsMod.id);
            }

            character.saveComponent(pStatsModList);
        }
    }

    /**
     * Defines what to do when a stat of an entity is changed.
     *
     * @param event  the event corresponding to the changing of the stat
     * @param entity the entity who's stat has been changed
     * @param eq     the equipment component associated with the entity
     */
    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatChangedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    /**
     * Defines what to do when a physical stat modifier is added to an entity.
     *
     * @param event  the event corresponding to the adding of the physical stat modifier
     * @param entity the entity to whom the stat modifier has been added
     * @param eq     the equipment component associated with the entity
     */
    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatsModifierAddedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    /**
     * Defines what to do when a physical stat modifier is removed from an entity.
     *
     * @param event  the event corresponding to the removal of the physical stat modifier
     * @param entity the entity from whom the stat modifier has been removed
     * @param eq     the equipment component associated with the entity
     */
    @ReceiveEvent
    public void onStatChanged(OnPhysicalStatsModifierRemovedEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    /**
     * Defines what to do when an item is equipped by an entity.
     *
     * @param event  the event corresponding to the equipment of the item
     * @param entity the entity who has equipped the item
     * @param eq     the equipment component associated with the entity
     */
    @ReceiveEvent
    public void onEquipChanged(EquipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    /**
     * Defines what to do when an item is unequipped by an entity.
     *
     * @param event  the event corresponding to the unequipment of the item
     * @param entity the entity who has unequipped the item
     * @param eq     the equipment component associated with the entity
     */
    @ReceiveEvent
    public void onEquipChanged(UnequipItemEvent event, EntityRef entity, EquipmentComponent eq) {
        CharacterScreenWindow screen = (CharacterScreenWindow) nuiManager.getScreen("Equipment:BackupScreen");

        if (screen != null) {
            screen.updateStats();
        }
    }

    /**
     * Applies item stats (attack boosts, for example) while dealing damage.
     *
     * @param event        the event corresponding to the attack on the target
     * @param damageTarget an entity reference to the target of the attack
     */
    @ReceiveEvent
    public void doingDamage(BeforeDamagedEvent event, EntityRef damageTarget) {
        if (event.getInstigator().hasComponent(EquipmentComponent.class)) {

            EquipmentComponent eq = event.getInstigator().getComponent(EquipmentComponent.class);
            int phyAtkTotal = 0;

            for (int i = 0; i < eq.equipmentSlots.size(); i++) {
                for (int j = 0; j < eq.equipmentSlots.get(i).itemRefs.size(); j++) {
                    if (eq.equipmentSlots.get(i).itemRefs.get(j) != EntityRef.NULL) {
                        phyAtkTotal += eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(EquipmentItemComponent.class).attack;
                    }
                }
            }

            event.add(phyAtkTotal);
        }
    }

    /**
     * Applies item stats (defense boosts, for example) while taking damage.
     *
     * @param event        the event corresponding to the entity taking damage
     * @param damageTarget the entity dealing damage
     */
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
