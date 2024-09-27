package de.yard.threed.core.loader;

import de.yard.threed.core.TreeNode;

import java.util.List;
import java.util.ArrayList;

public class PreparedModel {
    //unknown use case
    public String parent;
    public TreeNode<PreparedObject> root;
    public int useCounter;
    private String name;
    // not really a property and no functional use, but useful for tracking/testing

    public PreparedModel(String name) {
        this.name = name;
    }

    public TreeNode<PreparedObject> getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }
}
