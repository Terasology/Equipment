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
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.UILabel;

public class CharacterScreenWindow extends BaseInteractionScreen {
    @In
    private EntityManager entityManager;

    private InventoryGrid ingredientsInventory;
    private UILabel headEQ;
    private UILabel bodyEQ;
    private UILabel armsEQ;
    private UILabel handsEQ;
    private UILabel legsEQ;
    private UILabel feetEQ;

    private UILabel physicalAttackPower;
    private UILabel physicalDefensePower;
    private UILabel magicalAttackPower;
    private UILabel magicalDefensePower;
    private UILabel speedPower;

    private InventoryGrid playerInventory;
    private InventoryGrid playerEQInventory;

    private EntityRef player;
    private float lastUpdate = 0;

    @Override
    public void initialise() {
        ingredientsInventory = find("ingredientsInventory", InventoryGrid.class);
        headEQ = find("HeadEQ", UILabel.class);
        bodyEQ = find("BodyEQ", UILabel.class);
        armsEQ = find("ArmsEQ", UILabel.class);
        handsEQ = find("HandsEQ", UILabel.class);
        legsEQ = find("LegsEQ", UILabel.class);
        feetEQ = find("FeetEQ", UILabel.class);

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

        // TODO: Only for testing.
        for (EntityRef clientEntity : entityManager.getEntitiesWith(EquipmentComponent.class)) {
            if (clientEntity.hasComponent(CharacterComponent.class)) {

                player = clientEntity;
                break;
            }
        }

        EquipmentComponent eqC = player.getComponent(EquipmentComponent.class);
        playerEQInventory.setTargetEntity(eqC.equipmentInventory);
        playerEQInventory.setCellOffset(0);
        playerEQInventory.setMaxCellCount(6);
    }

    @Override
    protected void initializeWithInteractionTarget(final EntityRef screen) {

        /*
        EquipmentComponent eqC = player.getComponent(EquipmentComponent.class);
        playerEQInventory.setTargetEntity(eqC.equipmentInventory);
        playerEQInventory.setCellOffset(0);
        playerEQInventory.setMaxCellCount(6);
        */

        handsEQ.setText("Hands: Unarmed");

        /*
        if (CoreRegistry.get(LocalPlayer.class).getCharacterEntity().hasComponent(EquipmentComponent.class)) {
            int xd = 0;
        }
        */
        updateStats();
        /*
        if (player.hasComponent(EquipmentComponent.class)) {
            EquipmentComponent eq = player.getComponent(EquipmentComponent.class);

            int phyAtkTotal = 0;
            int phyDefTotal = 0;

            // Temp values until stat system is in place.
            int strength = 10;
            int defense = 10;

            // Hard-coded for humans now.
            String[] names = {"Head", "Body", "Arms", "Hands", "Legs", "Feet"};
            UILabel[] labels = {headEQ, bodyEQ, armsEQ, handsEQ, legsEQ, feetEQ};

            for (int i = 0; (i < names.length) && (i < eq.equipmentSlots.size()); i++) {
                if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                    labels[i].setText(names[i] + ": None");
                } else {
                    labels[i].setText(names[i] + ": " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
                    phyAtkTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).attack;
                    phyDefTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).defense;
                }
            }

            physicalAttackPower.setText("Physical Attack: " + (phyAtkTotal + (strength/2)));
            physicalDefensePower.setText("Physical Defense: " + (phyDefTotal + (defense/2)));


            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Head: None");
            } else {
                headEQ.setText("Head: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

            i++;
            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Body: None");
            } else {
                headEQ.setText("Body: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

            i++;
            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Arms: None");
            } else {
                headEQ.setText("Arms: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

            i++;
            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Hands: None");
            } else {
                headEQ.setText("Hands: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

            i++;
            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Legs: None");
            } else {
                headEQ.setText("Legs: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

            i++;
            if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                headEQ.setText("Feet: None");
            } else {
                headEQ.setText("Feet: " + eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
            }

        }
        */
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        lastUpdate += delta;

        // Only update the stats every second. Replace with smarter system later.
        if (lastUpdate >= 1.0) {
            updateStats();
            lastUpdate = 0f;
        }
    }

    private void updateStats() {
        if (player.hasComponent(EquipmentComponent.class)) {
            EquipmentComponent eq = player.getComponent(EquipmentComponent.class);

            int phyAtkTotal = 0;
            int phyDefTotal = 0;
            int speedTotal = 0;

            // Temp values until stat system is in place.
            int strength = 10;
            int defense = 10;
            int thaumacity = 6;
            int resistance = 4;

            // Hard-coded for humans now.
            String[] names = {"Head", "Body", "Arms", "Hands", "Legs", "Feet"};
            UILabel[] labels = {headEQ, bodyEQ, armsEQ, handsEQ, legsEQ, feetEQ};

            for (int i = 0; (i < names.length) && (i < eq.equipmentSlots.size()); i++) {
                if (eq.equipmentSlots.get(i).itemRef == EntityRef.NULL) {
                    labels[i].setText(eq.equipmentSlots.get(i).name + ": None");
                } else {
                    labels[i].setText(eq.equipmentSlots.get(i).name + ": " +
                            eq.equipmentSlots.get(i).itemRef.getComponent(DisplayNameComponent.class).name);
                    phyAtkTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).attack;
                    phyDefTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).defense;
                    speedTotal += eq.equipmentSlots.get(i).itemRef.getComponent(EquipmentItemComponent.class).speed;
                }
            }

            physicalAttackPower.setText("Physical Attack: " + (phyAtkTotal + (strength / 2)));
            physicalDefensePower.setText("Physical Defense: " + (phyDefTotal + (defense / 2)));
            magicalAttackPower.setText("Magic Attack: " + (0 + (thaumacity / 2)));
            magicalDefensePower.setText("Magic Defense: " + (0 + (resistance / 2)));
            speedPower.setText("Speed: " + speedTotal);
        }
    }
}
