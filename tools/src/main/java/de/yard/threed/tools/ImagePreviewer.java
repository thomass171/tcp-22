package de.yard.threed.tools;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.round;

public class ImagePreviewer extends JPanel {
    ButtonPanel buttonPanel;
    BufferedImage image;
    JLabel imageLabel;
    ControlPanel controlPanel;

    private ImagePreviewer(BufferedImage image) {
        this.image = image;
        setLayout(new BorderLayout());
        buttonPanel = new ButtonPanel(this);
        controlPanel = new ControlPanel(this);
        add("North", buttonPanel);
        add("West", controlPanel);
        imageLabel = new JLabel();
        render();
        add("Center", new JScrollPane(imageLabel));
    }

    void render() {
        BufferedImage img = frame(image, controlPanel.cbo_grid.isSelected());
        img = getScaled(img, buttonPanel.getScale());
        imageLabel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        //jframe.pack();
        imageLabel.setIcon(new ImageIcon(img));
    }

    static void preview(BufferedImage image) {
        // create our jframe as usual
        JFrame jframe = new JFrame();
        jframe.setTitle("" + image.getWidth() + "x" + image.getHeight());
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setLocationRelativeTo(null);
        jframe.setLocation(20,30);
        // set the jframe size and location, and make it visible
        //jframe.setPreferredSize(new Dimension(1024, 1024));
        jframe.setSize(new Dimension(1524, 900));
        ImagePreviewer imagePreviewer = new ImagePreviewer(image);
        jframe.getContentPane().add(imagePreviewer);
        jframe.setVisible(true);
    }

    private static BufferedImage frame(BufferedImage image, boolean withgrid) {
        BufferedImage img = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.drawLine(0, 0, w - 1, 0);
        g2d.drawLine(w - 1, 0, w - 1, h - 1);
        g2d.drawLine(0, h - 1, w - 1, h - 1);
        g2d.drawLine(0, 0, 0, h - 1);
        if (withgrid) {
            int gridsize = 128;
            for (int x = gridsize; x < w; x += gridsize) {
                g2d.drawLine(x, 0, x, h - 1);
            }
            for (int y = gridsize; y < h; y += gridsize) {
                g2d.drawLine(0, y, w - 1, y);
            }
        }
        return img;
    }

    static BufferedImage getScaled(BufferedImage before, double factor) {
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage((int) round(w * factor), (int) round(h * factor), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(factor, factor);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }
}

class ButtonPanel extends JPanel {
    JComboBox cbo_grid;
    JComboBox cbo_source, cbo_subconfig;
    ImagePreviewer imagePreviewer;
    JComboBox cbo_scale;

    ButtonPanel(ImagePreviewer imagePreviewer) {
        this.imagePreviewer = imagePreviewer;
        setLayout(new FlowLayout());

        // 800 und 1600 sind zu gross -> OOM
        cbo_scale = new JComboBox(new String[]{"10 %", "25 %", "50 %", "75 %", "100 %", "150 %", "200 %", "300 %", "400 %", "800 %!", "1600 %!"});
        add(cbo_scale);
        cbo_scale.setSelectedIndex(2);
        cbo_scale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imagePreviewer.render();
            }
        });
        /*JButton btn_dec = new JButton("-");
        add(btn_dec);
        btn_dec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //mainpanel.incscale(0.8f);
            }
        });
        JButton btn_inc = new JButton("+");
        add(btn_inc);
        btn_inc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //mainpanel.incscale(1.2f);
            }
        });*/

    }

    void addButton(String label, ActionListener lis) {
        JButton btn = new JButton(label);
        add(btn);
        btn.addActionListener(lis);
    }

    double getScale() {
        String s = (String) cbo_scale.getSelectedItem();
        float scale = Integer.parseInt(s.substring(0, s.indexOf(" ")));

        return (scale / 100.0);
    }
}

class ControlPanel extends JPanel {
    JCheckBox cbo_grid;
    ImagePreviewer imagePreviewer;

    ControlPanel(final ImagePreviewer imagePreviewer) {
        this.imagePreviewer = imagePreviewer;
        //top = new SceneNodeTreeNode();
        JScrollPane treeView = new JScrollPane(new JLabel("kkk"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        add(new JLabel("Render Options:"));
        cbo_grid = new JCheckBox("grid");
        add(cbo_grid);
        cbo_grid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imagePreviewer.render();
            }
        });
        /*final JCheckBox a = new JCheckBox("AA");
        add(a);
        a.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainpanel.setAA(a.isSelected());
            }
        });
        final JCheckBox merge = new JCheckBox("merge??");
        add(merge);
        merge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainpanel.setMerge(merge.isSelected());
            }
        });
        addBox("Triangulated", e -> mainpanel.setTriangulated(((JCheckBox) e.getSource()).isSelected()), "Triangulated and textured. Show triangles (only in combination with wireframe).");

        addBox("VV", e -> mainpanel.setVV(((JCheckBox) e.getSource()).isSelected()), "Render volumes, eg. bridges, buildings. Only triangulated with wireframe. Option ist aber witzlos wegen 2D Darstellung.");

        addBox("Node Objects", e -> mainpanel.setVisualizeNodes(((JCheckBox) e.getSource()).isSelected()), "Visualize node objects, eg. junctions");
        addBox("Ele Groups", e -> mainpanel.setElegroups(((JCheckBox) e.getSource()).isSelected()), "Render ele connect groups. Gibt es auch, wenn ohne Elevation gebaut wird.");

        // merge.setSelected(true);
        add(new JLabel("Layer Options:"));
        addBox("Grid/Connector", e -> mainpanel.setGrid(((JCheckBox) e.getSource()).isSelected()), null);
        addBox("Poly-Test", e -> mainpanel.setPolytest(((JCheckBox) e.getSource()).isSelected()), null);
        addBox("Triangulator-Test", e -> mainpanel.setTriangulator(((JCheckBox) e.getSource()).isSelected()), null);
        addBox("Indicator", e -> mainpanel.setIndicator(((JCheckBox) e.getSource()).isSelected()), null);*/
    }

    JCheckBox addBox(String label, ActionListener lis, String tooltip) {
        return addBox(this, label, lis, tooltip);
    }

    static JCheckBox addBox(JPanel p, String label, ActionListener lis, String tooltip) {
        JCheckBox b = new JCheckBox(label);
        p.add(b);
        b.addActionListener(lis);
        if (tooltip != null) {
            b.setToolTipText(tooltip);
        }
        return b;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(120, 600);
    }
}

