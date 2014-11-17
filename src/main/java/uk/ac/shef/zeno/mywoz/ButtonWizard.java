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
package uk.ac.shef.zeno.mywoz;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import uk.ac.shef.zeno.utils.Utils;


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
public class ButtonWizard  {

    // logger
    JFrame frame;
    MyActionListener listener;
    WozApp app;
    boolean shouldRun;
   
    /**
     * Creates the example module. The module must have access to the dialogue
     * system since it will periodically write new content to it.
     *
     * @param system the dialogue system
     */
    
    public ButtonWizard(WozApp app) {
        this.app = app;
    }
  
    public void start() {
        listener = new MyActionListener();
        frame = new JFrame();
        JPanel panel = new JPanel();
        
        
        GridLayout gridLayout = new GridLayout(3, 3);
        panel.setLayout(gridLayout);
        frame.add(panel);
        
        ArrayList<String> options = Utils.readList("resources/options.txt");
        
        addButtons(panel, options);
        
        
        frame.setSize(400, 300);
        // frame.setLocation(600, 600);
        frame.setVisible(true);
        shouldRun = true;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shouldRun = false;
            }
        });
        
        while (shouldRun) {
            try {
                app.update();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        frame.dispose();
        System.exit(0);
    }

    private void addButtons(JPanel panel, Collection<String> set) {
        for (String buttonName : set) {
            JButton button = new JButton(buttonName);
            button.addActionListener(listener);
            panel.add(button);
        }
    }

    private LinkedHashMap<String, String> readMap(String filename) {

        LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                res.put(fields[0], fields[1]);
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
            try {
                JButton source = (JButton) arg0.getSource();
                String text = source.getText();
                app.addRequest(text);
            } catch (Exception e) {
                   e.printStackTrace();
            }
        }
    }

}
