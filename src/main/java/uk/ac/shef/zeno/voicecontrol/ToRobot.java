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

import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import java.util.Collection;
import java.util.HashMap;



import opendial.DialogueSystem;
import opendial.arch.DialException;
import opendial.arch.Logger;
import opendial.modules.Module;
import opendial.state.DialogueState;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot.JointId;
import org.mechio.api.motion.Robot.RobotPositionHashMap;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.utils.DefaultSpeechJob;
import org.mechio.client.basic.MechIO;
import org.mechio.client.basic.R50RobotJoints;
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
    private JointId left_shoulder_pitch;
    private JointId left_shoulder_roll;
    private JointId left_wrist_yaw;
    private JointId left_grasp;
    private JointId left_elbow_yaw;
    private Animation shoulder_anim;
    private Animation elbow_anim;
   
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
            left_shoulder_pitch = new JointId(myRobot.getRobotId(), new Joint.Id(R50RobotJoints.LEFT_SHOULDER_PITCH));
            left_shoulder_roll = new JointId(myRobot.getRobotId(), new Joint.Id(R50RobotJoints.LEFT_SHOULDER_ROLL));
            left_elbow_yaw = new JointId(myRobot.getRobotId(), new Joint.Id(R50RobotJoints.LEFT_ELBOW_YAW));
            left_wrist_yaw = new JointId(myRobot.getRobotId(), new Joint.Id(R50RobotJoints.LEFT_WRIST_YAW));
            left_grasp = new JointId(myRobot.getRobotId(), new Joint.Id(R50RobotJoints.LEFT_HAND_GRASP));
            shoulder_anim = MechIO.loadAnimation("animations/shoulder_anim.xml");
            elbow_anim = MechIO.loadAnimation("animations/elbow_anim.xml");
   
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
        if (updatedVars.contains("a_m") && state.hasChanceNode("a_m") && !paused) {
            //log.info("a_m trigger");
            String actionValue = state.queryProb("a_m").toDiscrete().getBest().toString();
            if (actionValue.startsWith("Do_Request(")) {
                String request = actionValue.substring(11, actionValue.length() - 1);
                process(request);

            } else if (actionValue.equals("Greet_User")) {
                greet();
            } else if (actionValue.equals("Explain_Session")) {
                explain();
            }
        }
    }

    private void greet() {
        String text = "Hello Dave. My name is Zeeno. I want to learn how to speak with you and how to wave my arms."
                + " Will you help me?";
        robotSpeak(text);
    }

    private void explain() {

        if (robotActive) {
            try {
                String text = "OK, so you can help me learn how to wave my left arm.";
                robotSpeak(text);
                Thread.sleep(3000);
                text = "I can move my shoulder.";
                robotSpeak(text);
                playAnim(shoulder_anim);
                Thread.sleep(4000);
                text = "And I can move my elbow.";
                robotSpeak(text);
                playAnim(elbow_anim);
                Thread.sleep(4000);
                text = "You can give me instructions like move elbow up or move shoulder down. Help me to wave my arm!";
                robotSpeak(text);
                
            } catch (Exception e) {

                e.printStackTrace();
            }
        } else {
            log.info("robot explain.");
        }

    }

    private void process(String request) {
        String[] split = request.split(",");
        String jointReq = split[0];
        String actionReq = split[1];
        if (robotActive) {
            JointId joint = null;
            if (jointReq.equals("left upper")) {
                joint = left_shoulder_pitch;
            } else if (jointReq.equals("left lower")) {
                joint = left_shoulder_roll;
            } else if (jointReq.equals("left elbow")) {
                joint = left_elbow_yaw;
            } else if (jointReq.equals("left wrist")) {
                joint = left_wrist_yaw;
            } else if (jointReq.equals("left grasp")) {
                joint = left_grasp;
            }
            double num = myRobot.getDefaultPositions().get(joint).getValue();
            if (storedPositions.containsKey(joint)) {
                num = storedPositions.get(joint).getValue();
            }
            RobotPositionMap goalPositions = new RobotPositionHashMap();
            if (actionReq.equals("up")) {
                num += 0.05;
            } else if (actionReq.equals("down")) {
                num -= 0.05;
            } else if (actionReq.equals("max")) {
                num = 1;
            } else if (actionReq.equals("min")) {
                num = 0;
            } else if (actionReq.equals("def")) {
                num = 0.5;
            }
            if (num >= 0 && num <= 1) {
                storedPositions.put(joint, new NormalizedDouble(num));
                goalPositions.put(joint, new NormalizedDouble(num));
                myRobot.move(goalPositions, 1000);
            }
            mySpeaker.speak(request);
        } else {
            log.info(request);
        }
        //log.info(request);

    }

    DefaultSpeechJob robotSpeak(String text) {
        DefaultSpeechJob job = null;
        if (robotActive) {
            job = (DefaultSpeechJob) mySpeaker.speak(text);
        } else {
            log.info(text);
        }
        return job;
    }

    AnimationJob playAnim(Animation anim) {
        AnimationJob job = null;
        if (robotActive) {
            job = animPlayer.playAnimation(anim);
        } else {
            log.info("play anim " + anim);
        }
        return job;
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
