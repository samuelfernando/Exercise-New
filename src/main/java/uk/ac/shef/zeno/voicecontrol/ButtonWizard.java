// =================================================================                                                                   
// Copyright (C) 2011-2013 Pierre Lison (plison@ifi.uio.no)                                                                            
// Permission is hereby granted, free of charge, to any person 
// obtaining a copy of this software and associated documentation 
// files (the "Software"), to deal in the Software without restriction, 
// including without limitation the rights to use, copy, modify, merge, 
// publish, distribute, sublicense, and/or sell copies of the Software, 
// and to permit persons to whom the Software is furnished to do so, 
// subject to the following conditions:
// The above copyright notice and this permission notice shall be 
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
// CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
// TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// =================================================================                                                                   
package uk.ac.shef.zeno.voicecontrol;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.BoxLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import opendial.DialogueSystem;
import opendial.arch.DialException;
import opendial.arch.Logger;
import opendial.datastructs.Assignment;
import opendial.modules.Module;
import opendial.state.DialogueState;

/**
 * Simple example of an asynchronous module for the domain specified in
 * domains/examples/example-step-by-step.xml.
 *
 * <p>
 * The example creates a small control window where the user can click to
 * provide directions to the agent.
 *
 * @author Pierre Lison (plison@ifi.uio.no)
 * @version $Date:: 2014-04-16 17:34:31 #$
 */
class MyButton extends JButton {

    String id;

    MyButton(String id, String text) {
        super(text);
        this.id = id;
    }

    String getId() {
        return id;
    }
}

public class ButtonWizard implements Module {

    // logger
    public static Logger log = new Logger("ModuleExample2", Logger.Level.DEBUG);
    DialogueSystem system;
    JFrame frame;
    boolean paused = true;

    JLabel currentAction;
    JLabel currentJoint;

    /**
     * Creates the example module. The module must have access to the dialogue
     * system since it will periodically write new content to it.
     *
     * @param system the dialogue system
     */
    public ButtonWizard(DialogueSystem system) {
        this.system = system;
        currentAction = new JLabel();
        currentJoint = new JLabel();
    }

    /**
     * Creates a small control window with 4 arrow buttons. When clicked, each
     * button will create a new dialogue act corresponding to the instruction to
     * perform, and add it to the dialogue state.
     */
    @Override
    public void start() throws DialException {
        frame = new JFrame();
        JointActionListener jointListener = new JointActionListener();
        MyActionListener actionListener = new MyActionListener();

        JPanel jointPanel = new JPanel();
        JPanel actionPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        frame.add(jointPanel);
        frame.add(actionPanel);
        frame.add(statusPanel);
        BoxLayout actionPanelLayout = new BoxLayout(actionPanel, BoxLayout.Y_AXIS);
        BoxLayout jointPanelLayout = new BoxLayout(jointPanel, BoxLayout.Y_AXIS);
        BoxLayout statusPanelLayout = new BoxLayout(statusPanel, BoxLayout.Y_AXIS);

        BoxLayout frameLayout = new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS);

        jointPanel.setLayout(jointPanelLayout);
        actionPanel.setLayout(actionPanelLayout);
        statusPanel.setLayout(statusPanelLayout);
        frame.setLayout(frameLayout);

        ArrayList<String> jointNames = readJointNames();
        for (String jointName : jointNames) {
            JButton button = new JButton(jointName);
            button.addActionListener(jointListener);

            jointPanel.add(button);
        }

        String actions[] = {"up", "down", "max", "min", "def", "GO", "USER_READY", "YES", "NO"};
        for (String action : actions) {
            JButton button = new JButton(action);
            button.addActionListener(actionListener);

            actionPanel.add(button);

        }

        currentAction.setText(actions[0]);
        currentJoint.setText(jointNames.get(0));
        statusPanel.add(currentJoint);

        statusPanel.add(currentAction);

        frame.setSize(800, 600);
        // frame.setLocation(600, 600);
        paused = false;
        frame.setVisible(true);
    }

    /**
     * Does nothing.
     */
    @Override
    public void trigger(DialogueState state, Collection<String> updatedVars) {
    }

    /**
     * Pauses the module.
     */
    @Override
    public void pause(boolean toPause) {
        paused = toPause;
    }

    /**
     * Returns true is the module is not paused, and false otherwise.
     */
    @Override
    public boolean isRunning() {
        return !paused;
    }

    private ArrayList<String> readJointNames() {

        ArrayList<String> res = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("joints.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Action listener for the arrow buttons.
     */
    class JointActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (paused) {
                return;
            }
            try {
                JButton source = (JButton) arg0.getSource();
                String text = source.getText();
                currentJoint.setText(text);
                frame.repaint();
                //system.addContent(new Assignment("a_u", "Request(" + text + ")"));

            } catch (Exception e) {
                log.warning("could not send instruction: " + e);
            }
        }
    }

    class MyActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (paused) {
                return;
            }

            JButton source = (JButton) arg0.getSource();
            String text = source.getText();
            if (text.equals("GO")) {
                    system.addContent(new Assignment("a_u", "Request(" + currentJoint.getText() + "," + currentAction.getText() + ")"));
            } else if (text.equals("USER_READY") || text.equals("YES") || text.equals("NO")) {
                    system.addContent(new Assignment("a_u", text));
            } else {
                currentAction.setText(text);
                frame.repaint();
            }

        }
    }

}
