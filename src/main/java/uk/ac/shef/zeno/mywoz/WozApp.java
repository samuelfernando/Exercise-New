/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.zeno.mywoz;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import uk.ac.shef.zeno.utils.Utils;

/**
 *
 * @author samf
 */
public class WozApp {

    WozState state = WozState.NOTHINGNESS;
    ButtonWizard wizard;
    ToRobot robot;
    private HashSet<String> gestures;
    private HashSet<String> foods;

    LinkedList<String> queue;
    boolean speaking = false;

    void start() {
        wizard = new ButtonWizard(this);
        queue = new LinkedList<String>();
        robot = new ToRobot();
        gestures = Utils.readSet("gestures.txt");
        foods = new HashSet<String>();
        wizard.start();

    }

    public void addRequest(String text) {
        queue.add(text);
    }

    public void update() {

        //System.out.println(queue);
        robot.flushQueue();
        if (state == WozState.NOTHINGNESS) {
            if (queue.isEmpty()) {
                return;
            }
            String text = queue.poll();
            if (text.equals("User_Ready")) {
                state = WozState.USER_READY;

            } else {
                System.err.println("Unexpected input");
                System.exit(1);
            }
        }

        if (state == WozState.USER_READY) {
            robot.addToQueue("OK now it's my turn to play Simon Says. But this time you will tell me what to do! Give me instructions like wave, smile and hands up.");
            state = WozState.SIMON_SAYS;
        }

        if (state == WozState.SIMON_SAYS) {
            if (queue.isEmpty()) {
                return;
            }
            String text = queue.poll();
            if (gestures.contains(text)) {
                robot.addToQueue("Doing " + text);
                robot.playAnimation(text);

            } else if (text.equals("Hesitant")) {
                robot.addToQueue("Don't be shy");

            } else if (text.equals("Unrecognised")) {
                robot.addToQueue("I didn't get that");

            } else if (text.equals("Full_Response")) {
                robot.addToQueue("OK enough, let's move on.");

                state = WozState.FOOD_TALK;
            }
        }

        if (state == WozState.FOOD_TALK) {

            robot.addToQueue("Now let's talk about food.");
            state = WozState.FOOD_PROMPT;
        }

        if (state == WozState.FOOD_PROMPT) {
            if (queue.isEmpty()) {
                return;
            }
            String text = queue.poll();

            if (foods.contains(text)) {
                robot.addToQueue("I know about food " + text);

            } else if (text.equals("Hesitant")) {
                robot.addToQueue("Don't be shy");

            } else if (text.equals("Unrecognised")) {
                robot.addToQueue("I don't know about that food. Tell me more.");

            } else if (text.equals("Full_Response")) {
                robot.addToQueue("OK enough. Any questions for me?");

                state = WozState.FINISH;
            }
        }

        /*if (gestures.contains(text)) {
         robot.playAnimation(text);
         } else {
         robot.speak(text);
         }*/
    }

    public static void main(String args[]) {
        WozApp app = new WozApp();
        app.start();
    }

}
