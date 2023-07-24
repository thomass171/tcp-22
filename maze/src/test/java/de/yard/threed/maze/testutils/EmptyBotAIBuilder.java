package de.yard.threed.maze.testutils;

import de.yard.threed.maze.BotAI;
import de.yard.threed.maze.BotAiBuilder;

/**
 * For a non acting bot?
 */
public class EmptyBotAIBuilder implements BotAiBuilder {

    public BotAI build(){
        return null;
    }
}
