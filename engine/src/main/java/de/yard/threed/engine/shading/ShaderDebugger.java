package de.yard.threed.engine.shading;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Uniform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.ObjectSelector;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.gui.*;

/**
 * A component that can be added to every scene that has a MenuCycler.
 * Needs to be hooked into update() loop.
 * <p>
 * Dedicated to CustomShaderMaterial. Just a draft for now, not yet tested due to missing model with that shader.
 */
public class ShaderDebugger {
    public Log logger = Platform.getInstance().getLog(ShaderDebugger.class);

    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;
    private MessageBox messageBox = null;
    private ObjectSelector objectSelector;
    private SceneNode selectedObject = null;
    private ControlPanelArea textArea;
    double distanceToNearplane = 0.0;

    public ShaderDebugger(ObjectSelector objectSelector) {
        this.objectSelector = objectSelector;
    }

    public void setMessageBox(MessageBox messageBox) {
        this.messageBox = messageBox;
    }

    public void update() {
        if (objectSelector.update()){
        selectedObject = objectSelector.getSelectedObject();
            if (textArea != null) {
                if (selectedObject == null) {
                    ControlPanelHelper.setText(textArea, " ");}
                else{
                    ControlPanelHelper.setText(textArea, selectedObject.getName());
                }
            }
        }

    }

    public MenuProvider getMenuProvider(Camera camera,     double distanceToNearplane) {
        this.distanceToNearplane=distanceToNearplane;
        return new DefaultMenuProvider(camera, (Camera c) -> {
            //ShaderDebugMenu sdm = new ShaderDebugMenu(this);
            Menu menu = buildMenu(c);
            return menu;
        });
    }

    public ControlPanelMenu buildMenu(Camera camera) {
        Material mat = Material.buildBasicMaterial(Color.DARKGREEN, null);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        int rows = 3;
        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, rows * PropertyControlPanelRowHeight), mat, 0.01);

        textArea = ControlPanelHelper.addText(cp, " ", new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(2, rows, PropertyControlPanelRowHeight)),
                new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight));

        // (un)shaded toggle
        IntHolder shadingSpinnedValue = new IntHolder(1);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(1, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("shading", rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(1, value -> {
                    if (value != null) {
                        shadingSpinnedValue.setValue(value.intValue());
                    }
                    double newval = Double.valueOf(shadingSpinnedValue.getValue());
                    updateShaderDebugMode((int) newval);
                    logger.debug("shadingSpinnedValue="+shadingSpinnedValue.getValue());
                    updateShaderShaded(newval != 0);
                    return newval;
                }, 2, new NumericDisplayFormatter(0)), Color.RED));

        // bottom line. debug mode value spinner, starting at 0
        // for some unknown reason LabeledSpinnerControlPanel desn't display the current value??
        IntHolder debugModeSpinnedValue = new IntHolder(0);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(0, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("debug mode", rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(1, value -> {
                    if (value != null) {
                        debugModeSpinnedValue.setValue(value.intValue());
                    }
                    double newval = Double.valueOf(debugModeSpinnedValue.getValue());
                    updateShaderDebugMode((int) newval);
                    logger.debug("debugModeSpinnedValue="+debugModeSpinnedValue.getValue());
                    return newval;
                }, 5, new NumericDisplayFormatter(0)), Color.RED));

        ControlPanelHelper.positionToNearPlaneByGrid(camera, distanceToNearplane, new Dimension(2, 6),
                new Point(1, 0), cp, new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight * rows));

        return new ControlPanelMenu(cp);
    }

    private void updateShaderDebugMode(int debugMode) {
        NativeUniform u = shaderReady(Uniform.DEBUG_MODE);
        if (u != null) {
            logger.debug("Setting debugMode to " + debugMode);
            u.setValue(debugMode);
        }
    }

    private void updateShaderShaded(boolean enabled) {
        NativeUniform u = shaderReady(Uniform.SHADED);
        if (u != null) {
            logger.debug("Setting shaded to " + enabled);
            u.setValue(enabled);
        }
    }

    private NativeUniform shaderReady(String uniformName) {
        if (selectedObject == null) {
            if (messageBox != null) {
                messageBox.showMessage("no selected object", 3000);
            }
            return null;
        }
        if (selectedObject.getMesh() == null || selectedObject.getMesh().getMaterial() == null) {
            if (messageBox != null) {
                messageBox.showMessage("no mesh/material in select object", 3000);
            }
            return null;
        }
        Material material = selectedObject.getMesh().getMaterial();
        NativeUniform u = material.material.getUniform(uniformName);
        if (u == null) {
            if (messageBox != null) {
                messageBox.showMessage("no uniform '" + uniformName + "' in selected object material", 3000);
            }
            return null;
        }
        return u;

    }
}