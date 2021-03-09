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

import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.equipment.event.CharacterScreenButton;
import org.terasology.input.ButtonState;

/**
 * Client system that handles how equipment-related information is displayed.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CharacterScreenClientSystem extends BaseComponentSystem {
    private static final String CHARACTER_SCREEN_NAME = "Equipment:BackupScreen";

    @In
    private NUIManager nuiManager;

    /**
     * This method is executed when the player presses the 'character screen' button.
     *
     * @param event           the event corresponding to the button being pressed
     * @param entity          the button being pressed
     * @param clientComponent the client component associated with the player's client
     */
    @ReceiveEvent
    public void craftRequested(CharacterScreenButton event, EntityRef entity, ClientComponent clientComponent) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen(CHARACTER_SCREEN_NAME);
            CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("Equipment:cloth-inventory").get(), 1.0f);
        }
    }
}
