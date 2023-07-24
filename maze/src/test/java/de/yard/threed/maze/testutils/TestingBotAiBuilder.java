package de.yard.threed.maze.testutils;

import de.yard.threed.maze.BotAI;
import de.yard.threed.maze.BotAiBuilder;
import de.yard.threed.maze.SimpleBotAI;

import java.util.ArrayList;
import java.util.List;

public class TestingBotAiBuilder implements BotAiBuilder {

    public List<TestingBotAI> ais = new ArrayList<>();

    public BotAI build(){
        TestingBotAI tbai = new TestingBotAI();
        ais.add(tbai);
        return tbai;
    }
}
