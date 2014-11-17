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

import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import java.util.HashMap;
import java.util.LinkedList;
import org.mechio.api.animation.Animation;

import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.motion.Robot.RobotPositionMap;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.api.speech.utils.DefaultSpeechJob;
import org.mechio.client.basic.MechIO;
import org.mechio.client.basic.UserSettings;
import uk.ac.shef.zeno.utils.Utils;

/**
 * Simple example of a synchronous module for the domain specified in
 * domains/examples/example-step-by-step.xml.
 *
 * <p>
 * The example creates a visual grid of size GRID_SIZE and updates the position
 * of the agent in accordance with the movements.
 *
 * @author Pierre Lison (plison@ifi.uio.no)
 * @version $Date:: 2014-04-16 17:34:31 #$
 */
public class ToRobot {

    // logger
    // start the agent in the middle of the grid
    RemoteSpeechServiceClient mySpeaker;
    RemoteAnimationPlayerClient animPlayer;
    RemoteRobot myRobot;
    RobotPositionMap storedPositions;
    boolean robotActive;
    HashMap<String, Animation> animations;
    DefaultSpeechJob currentSpeechJob = null;
    private boolean robotSpeechPendingComplete;
    private long endOfSpeech;
    LinkedList<String> queue;
    
    public ToRobot() {
        HashMap<String, String> configs = Utils.readConfig();
        animations = new HashMap<>();
        Animation happyAnim = MechIO.loadAnimation("animations/victory.xml");
        Animation sadAnim = MechIO.loadAnimation("animations/disappointed.xml");
        Animation waveAnim = MechIO.loadAnimation("animations/robokind-wave.xml");
        Animation onehandAnim = MechIO.loadAnimation("animations/robokind-onehandwave.xml");
        
        animations.put("Happy", happyAnim);
        animations.put("Sad", sadAnim);
        animations.put("Wave", waveAnim);
        animations.put("Onehand", onehandAnim);
        
        robotActive = Boolean.parseBoolean(configs.get("robot-active"));
        queue = new LinkedList<>();
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
    }
    
    void addToQueue(String text) {
        queue.add(text);
    }
    
    void flushQueue() {
        if (queue.isEmpty()) {
            return;
        }
        if (speechFinished()) {
            String text = queue.poll();
            speak(text);
        }
    }
    
    void speak(String text) {
        if (robotActive) {
            currentSpeechJob = (DefaultSpeechJob) mySpeaker.speak(text);
        } else {
            System.out.println(text);
        }
    }
    
    void playAnimation(String name) {
        if (robotActive) {
            
            if (name.equals("Default")) {
                RobotPositionMap map = myRobot.getDefaultPositions();
                myRobot.move(map, 1000);
            } else {
                Animation anim = animations.get(name);
                animPlayer.playAnimation(anim);
            }
        } else {
            System.out.println("anim " + name);
            
        }
        
    }
    
    boolean hasAnimation(String text) {
        return animations.containsKey(text);
    }
    
    boolean speechFinished() {
        long now = System.currentTimeMillis();
        if (robotActive) {
            if (currentSpeechJob == null) {
                if (robotSpeechPendingComplete) {
                    if (now > endOfSpeech) {
                        robotSpeechPendingComplete = false;
                        return true;
                    } else {
                        
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                boolean ret = (currentSpeechJob.getStatus() == DefaultSpeechJob.COMPLETE);
                if (ret) {
                    this.robotSpeechPendingComplete = true;
                    currentSpeechJob = null;
                    endOfSpeech = now + 500;
                    
                }
                return false;
                
            }
        } else {
            
            return true;
        }
    }
    
}
