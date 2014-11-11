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

import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import java.util.Collection;
import java.util.HashMap;



import opendial.DialogueSystem;
import opendial.arch.DialException;
import opendial.arch.Logger;
import opendial.modules.Module;
import opendial.state.DialogueState;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.client.basic.MechIO;
import org.mechio.client.basic.UserSettings;
import uk.ac.shef.zeno.utils.Utils;

/**
 * Simple example of a synchronous module for the domain specified in
 * domains/examples/example-step-by-step.xml.
 *
 * <p>The example creates a visual grid of size GRID_SIZE and updates the
 * position of the agent in accordance with the movements.
 *
 * @author Pierre Lison (plison@ifi.uio.no)
 * @version $Date:: 2014-04-16 17:34:31 #$
 */
public class ToRobot implements Module {

    // logger
    public static Logger log = new Logger("ToRobot", Logger.Level.DEBUG);
    boolean paused = true;
    // start the agent in the middle of the grid
    RemoteSpeechServiceClient mySpeaker;
    RemoteAnimationPlayerClient animPlayer;
    DialogueSystem system;
    boolean robotActive = false;
    RemoteRobot myRobot;
    RobotPositionMap storedPositions;

    public ToRobot(DialogueSystem system) {
        this.system = system;
        storedPositions = new RobotPositionHashMap();
    }

    /**
     * Creates a simple visual grid of size GRID_SIZE and puts the agent in the
     * middle of this grid.
     */
    @Override
    public void start() throws DialException {
        HashMap<String, String> configs = Utils.readConfig();
        robotActive = Boolean.parseBoolean(configs.get("robot-active"));
        if (robotActive) {
            String robotID = "myRobot";
            String robotIP = configs.get("ip");
            UserSettings.setSpeechAddress(robotIP);
            UserSettings.setRobotId(robotID);
            UserSettings.setRobotAddress(robotIP);
            UserSettings.setAnimationAddress(robotIP);
            myRobot = MechIO.connectRobot();
            animPlayer = MechIO.connectAnimationPlayer();
            mySpeaker = MechIO.connectSpeechService();


        }
        paused = false;
    }

    /**
     * If the updated variables contain the system action "a_m" and the action
     * is a movement, updates the visual grid in accordance with the movement.
     */
    @Override
    public void trigger(DialogueState state, Collection<String> updatedVars) {
        //log.info("trigger "+updatedVars + state.getChanceNodeIds());
        if (updatedVars.contains("u_m") && state.hasChanceNode("u_m") && !paused) {
            //log.info("a_m trigger");
           // String actionValue = state.queryProb("u_m").toDiscrete().getBest().getValue("u_m").toString();
             String actionValue = state.queryProb("u_m").toDiscrete().getBest().toString();
            
            robotSpeak(actionValue);
        }
    }

    void robotSpeak(String text) {
        if (robotActive) {
            mySpeaker.speak(text);
        } else {
            log.info(text);
        }
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
}
