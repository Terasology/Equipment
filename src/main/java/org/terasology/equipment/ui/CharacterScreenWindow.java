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
package org.terasology.equipment.ui;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.equipment.component.EquipmentComponent;
import org.terasology.equipment.component.EquipmentItemComponent;
import org.terasology.equipment.component.EquipmentSlot;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.physicalstats.component.PhysicalStatsComponent;
import org.terasology.physicalstats.component.PhysicalStatsModifierComponent;
import org.terasology.physicalstats.component.PhysicalStatsModifiersListComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Character Screen window, a screen that contains information about the character's inventory.
 */
public class CharacterScreenWindow extends BaseInteractionScreen {
    @In
    private EntityManager entityManager;

    private InventoryGrid ingredientsInventory;

    private UILabel strLabel;
    private UILabel dexLabel;
    private UILabel conLabel;
    private UILabel endLabel;
    private UILabel thaLabel;
    private UILabel defLabel;
    private UILabel resLabel;
    private UILabel intLabel;
    private UILabel wisLabel;
    private UILabel wilLabel;
    private UILabel forLabel;
    private UILabel agiLabel;
    private UILabel chaLabel;
    private UILabel lukLabel;

    private ColumnLayout eqSlotLabelsLayout;
    private List<UILabel> eqSlotLabels;

    private UILabel maxHealth;
    private UILabel physicalAttackPower;
    private UILabel physicalDefensePower;
    private UILabel magicalAttackPower;
    private UILabel magicalDefensePower;
    private UILabel speedPower;

    private InventoryGrid playerInventory;
    private InventoryGrid playerEQInventory;

    private EntityRef player;
    private float lastUpdate = 0;

    /**
     * Initializes the character screen
     */
    @Override
    public void initialise() {
        ingredientsInventory = find("ingredientsInventory", InventoryGrid.class);

        strLabel = find("STR", UILabel.class);
        dexLabel = find("DEX", UILabel.class);
        conLabel = find("CON", UILabel.class);
        endLabel = find("END", UILabel.class);
        agiLabel = find("AGI", UILabel.class);
        chaLabel = find("CHA", UILabel.class);
        lukLabel = find("LUK", UILabel.class);

        maxHealth = find("HealthPoints", UILabel.class);
        physicalAttackPower = find("PhyAttackPower", UILabel.class);
        physicalDefensePower = find("PhyDefensePower", UILabel.class);
        magicalAttackPower = find("MagAttackPower", UILabel.class);
        magicalDefensePower = find("MagDefensePower", UILabel.class);
        speedPower = find("SpeedPower", UILabel.class);

        playerInventory = find("playerInventory", InventoryGrid.class);
        playerInventory.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        playerInventory.setCellOffset(0);
        playerInventory.setMaxCellCount(40);

        playerEQInventory = find("playerEQInventory", InventoryGrid.class);


        player = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();

        EquipmentComponent eqC = player.getComponent(EquipmentComponent.class);
        playerEQInventory.setTargetEntity(eqC.equipmentInventory);
        playerEQInventory.setCellOffset(0);
        playerEQInventory.setMaxCellCount(eqC.numberOfSlots);

        // Create list of labels for each equipment slot.
        eqSlotLabels = new ArrayList<UILabel>();
        eqSlotLabelsLayout = find("eqSlotNamesLayout", ColumnLayout.class);

        // Iterate through the list of equipment slots.
        for (EquipmentSlot equipmentSlot : eqC.equipmentSlots) {
            // For each slot present in this type, add a label to the layout and eqSlotLabels list.
            for (int i = 0; i < equipmentSlot.numSlotsOfSameType; i++) {
                UILabel newLabel = new UILabel();

                if (equipmentSlot.numSlotsOfSameType == 1) {
                    newLabel.setText(equipmentSlot.name + ": None");
                }
                else {
                    newLabel.setText(equipmentSlot.name + " #" + (i + 1) + ": None");
                }

                eqSlotLabelsLayout.addWidget(newLabel);
                eqSlotLabels.add(newLabel);
            }
        }
    }

    @Override
    protected void initializeWithInteractionTarget(final EntityRef screen) {
        updateStats();
    }

    /**
     * Updates the character's stats every 30 seconds. Called every time the game updates.
     *
     * @param delta the time since the last update
     */
    @Override
    public void update(float delta) {
        super.update(delta);
        lastUpdate += delta;

        // Only manually update the stats every 30 seconds. Replace with smarter system later.
        if (lastUpdate >= 30.0) {
            updateAllStats();
            lastUpdate = 0f;
        }
    }

    /**
     * Updates all stats of the character.
     */
    public void updateAllStats() {
        updateStats();
    }

    /**
     * Updates the character's stats.
     */
    public void updateStats() {
        if (player.hasComponent(EquipmentComponent.class)) {
            EquipmentComponent eq = player.getComponent(EquipmentComponent.class);
            PhysicalStatsComponent phy = player.getComponent(PhysicalStatsComponent.class);
            PhysicalStatsModifiersListComponent mods = player.getComponent(PhysicalStatsModifiersListComponent.class);

            int strTemp = phy.strength;
            int dexTemp = phy.dexterity;
            int conTemp = phy.constitution;
            int endTemp = phy.endurance;
            int agiTemp = phy.agility;
            int chaTemp = phy.charisma;
            int lukTemp = phy.luck;

            if (mods != null) {
                for (PhysicalStatsModifierComponent mod : mods.modifiers.values()) {
                    strTemp += mod.strength;
                    dexTemp += mod.dexterity;
                    conTemp += mod.constitution;
                    endTemp += mod.endurance;
                    agiTemp += mod.agility;
                    chaTemp += mod.charisma;
                    lukTemp += mod.luck;
                }
            }
            strLabel.setText("Strength: " + strTemp);
            dexLabel.setText("Dexterity: " + dexTemp);
            conLabel.setText("Constitution: " + conTemp);
            endLabel.setText("Endurance: " + endTemp);
            agiLabel.setText("Agility: " + agiTemp);
            chaLabel.setText("Charisma: " + chaTemp);
            lukLabel.setText("Luck: " + lukTemp);

            strLabel.setText(strLabel.getText() + " (" + phy.strength + ")");
            dexLabel.setText(dexLabel.getText() + " (" + phy.dexterity + ")");
            conLabel.setText(conLabel.getText() + " (" + phy.constitution + ")");
            endLabel.setText(endLabel.getText() + " (" + phy.endurance + ")");
            agiLabel.setText(agiLabel.getText() + " (" + phy.agility + ")");
            chaLabel.setText(chaLabel.getText() + " (" + phy.charisma + ")");
            lukLabel.setText(lukLabel.getText() + " (" + phy.luck + ")");

            // Calculating the derived stat values.
            int defense = 0;
            int thaumacity = 0;
            int resistance = 0;

            int phyAtkTotal = 0;
            int phyDefTotal = 0;
            int speedTotal = Math.round(phy.dexterity / 2f);

            maxHealth.setText("Health: " + phy.constitution*10);

            int c = 0; // Counter for storing which label in eqSlotLabels to access.
            // Iterate through the list of equipment slots.
            for (int i = 0; (i < eq.equipmentSlots.size()); i++) {
                // For each slot present in this type.
                for (int j = 0; j < eq.equipmentSlots.get(i).numSlotsOfSameType; j++) {
                    // If nothing's equipped in this slot, update the appropriate label.
                    if (eq.equipmentSlots.get(i).itemRefs.get(j) == EntityRef.NULL) {

                        if (eq.equipmentSlots.get(i).numSlotsOfSameType == 1) {
                            eqSlotLabels.get(c).setText(eq.equipmentSlots.get(i).name + ": None");
                        }
                        else {
                            eqSlotLabels.get(c).setText(eq.equipmentSlots.get(i).name + " #" + (j + 1) + ": None");
                        }
                    }
                    // If something's equipped in this slot, update the appropriate label.
                    else {

                        if (eq.equipmentSlots.get(i).numSlotsOfSameType == 1) {
                            eqSlotLabels.get(c).setText(eq.equipmentSlots.get(i).name + ": " +
                                    eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(DisplayNameComponent.class).name);
                        }
                        else {
                            eqSlotLabels.get(c).setText(eq.equipmentSlots.get(i).name + " #" + (j + 1) + ": " +
                                    eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(DisplayNameComponent.class).name);
                        }

                        // Add the stat bonuses from the equipped item.
                        phyAtkTotal +=
                                eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(EquipmentItemComponent.class).attack;
                        phyDefTotal +=
                                eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(EquipmentItemComponent.class).defense;
                        speedTotal +=
                                eq.equipmentSlots.get(i).itemRefs.get(j).getComponent(EquipmentItemComponent.class).speed;
                    }

                    // As we are done with this equipment slot (and associated label), increment the counter.
                    c++;
                }
            }

            // Update the labels of the derived stats.
            physicalAttackPower.setText("Physical Attack: " + (phyAtkTotal + (strTemp / 2)));
            physicalDefensePower.setText("Physical Defense: " + (phyDefTotal + (defense / 2)));
            magicalAttackPower.setText("Magic Attack: " + (0 + (thaumacity / 2)));
            magicalDefensePower.setText("Magic Defense: " + (0 + (resistance / 2)));
            speedPower.setText("Speed: " + speedTotal);
        }
    }
}
