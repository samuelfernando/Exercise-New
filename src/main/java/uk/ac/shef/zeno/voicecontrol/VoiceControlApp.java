/*0
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.zeno.voicecontrol;

import opendial.DialogueSystem;
import opendial.arch.Logger;
import opendial.domains.Domain;
import opendial.readers.XMLDomainReader;



/**
 *
 * @author samf
 */
public class VoiceControlApp {
    DialogueSystem system;
    public static Logger log = new Logger("DialogueSystem", Logger.Level.DEBUG);
    public VoiceControlApp() {
        try {
            system = new DialogueSystem();
              String domainFile = "domains/zeno-experiment/exercise-zeno.xml";
              System.out.println("Domain = "+domainFile);
            system.getSettings().fillSettings(System.getProperties());
            
            Domain domain = XMLDomainReader.extractDomain(domainFile);
            system.changeDomain(domain);
            system.changeSettings(system.getSettings());
            system.startSystem();
            log.info("Dialogue system started!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        VoiceControlApp app = new VoiceControlApp();
        
    }
    
}
