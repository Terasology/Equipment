// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.equipment.event;

import org.lwjgl.input.Keyboard;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;

/**
 * An event that is sent when the character screen button is pressed.
 */
@RegisterBindButton(id = "characterScreen", description = "Character Screen", category = "interaction")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_C)
public class CharacterScreenButton extends BindButtonEvent {
}
