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
package uk.ac.shef.zeno.wozdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.ArrayList;
import javax.swing.BoxLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
 * <p>The example creates a small control window where the user can click to
 * provide directions to the agent.
 *
 * @author Pierre Lison (plison@ifi.uio.no)
 * @version $Date:: 2014-04-16 17:34:31 #$
 */
public class ButtonWizard implements Module {

    // logger
    public static Logger log = new Logger("ButtonWizard", Logger.Level.DEBUG);
    DialogueSystem system;
    JFrame frame;
    boolean paused = true;
    MyActionListener listener;
    int rounds = 0;
    /**
     * Creates the example module. The module must have access to the dialogue
     * system since it will periodically write new content to it.
     *
     * @param system the dialogue system
     */
    public ButtonWizard(DialogueSystem system) {
        this.system = system;
        listener = new MyActionListener();
    }

    /**
     * Creates a small control window with 4 arrow buttons. When clicked, each
     * button will create a new dialogue act corresponding to the instruction to
     * perform, and add it to the dialogue state.
     */
    @Override
    public void start() throws DialException {
        frame = new JFrame();

        JPanel exercisePanel = new JPanel();
        JPanel foodPanel = new JPanel();
        JPanel actionPanel = new JPanel();

        frame.add(exercisePanel);
        frame.add(foodPanel);
        frame.add(actionPanel);

        BoxLayout exercisePanelLayout = new BoxLayout(exercisePanel, BoxLayout.Y_AXIS);
        BoxLayout foodPanelLayout = new BoxLayout(foodPanel, BoxLayout.Y_AXIS);
        BoxLayout actionPanelLayout = new BoxLayout(actionPanel, BoxLayout.Y_AXIS);

        BoxLayout frameLayout = new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS);

        exercisePanel.setLayout(exercisePanelLayout);
        foodPanel.setLayout(foodPanelLayout);
        actionPanel.setLayout(actionPanelLayout);
        
        frame.setLayout(frameLayout);

        ArrayList<String> exercises = readArray("exercises.txt");
        addButtons(exercisePanel, exercises, "exercise");
        ArrayList<String> foods = readArray("foods.txt");
        addButtons(foodPanel, foods, "food");
        ArrayList<String> actions = readArray("actions.txt");
        addButtons(actionPanel, actions, "action");


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

    private void addButtons(JPanel panel, ArrayList<String> buttonNames, String prefix) {
        for (String buttonName : buttonNames) {
            MyButton button = new MyButton(prefix+","+buttonName, buttonName);
            button.addActionListener(listener);
            panel.add(button);
        }
    }

    private ArrayList<String> readArray(String filename) {

        ArrayList<String> res = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
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
    class MyActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (paused) {
                return;
            }
            try {
                MyButton source = (MyButton) arg0.getSource();
                String id = source.getId();
                String splits[] = id.split(",");
                String type = splits[0];
                String value = splits[1];
                if (type.equals("exercise")) {
                    if (value.equals("unrecognised")) {
                        system.addContent(new Assignment("a_u", "Unrecognised_Exercise")); 
                    }
                    else {
                        system.addContent(new Assignment("a_u", "Mention_Exercise(" + value + ")"));
                    }
                }
                else if (type.equals("food")) {
                    if (value.equals("unrecognised")) {
                        system.addContent(new Assignment("a_u", "Unrecognised_Food")); 
                    }
                    else {
                        system.addContent(new Assignment("a_u", "Mention_Food(" + value + ")"));
                    }
                }
                else if (type.equals("action")) {
                   if (value.equals("WAVE") || value.equals("JUMP") || value.equals("HANDS_UP")) {
                       ++rounds;
                       log.info("rounds = "+rounds);
                       if (rounds>3) {
                           system.addContent(new Assignment("simon_says_limit", "true"));
                       }
                       else {
                           system.addContent(new Assignment("simon_says_limit", "false"));
                     
                       }
                   }
                   system.addContent(new Assignment("a_u", value));
                    
                }

            } catch (Exception e) {
                log.warning("could not send instruction: " + e);
            }
        }
    }

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
}
