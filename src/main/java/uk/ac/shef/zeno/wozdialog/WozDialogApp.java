/*0
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.zeno.wozdialog;

import opendial.DialogueSystem;
import opendial.arch.Logger;
import opendial.domains.Domain;
import opendial.readers.XMLDomainReader;

/**
 *
 * @author samf
 */
public class WozDialogApp {

    DialogueSystem system;
    public static Logger log = new Logger("WozDialogApp", Logger.Level.DEBUG);

    public WozDialogApp() {
        try {
            system = new DialogueSystem();
            String domainFile = "domains/zeno-experiment/woz-dialog.xml";
            System.out.println("Domain = " + domainFile);
            system.getSettings().fillSettings(System.getProperties());

            Domain domain = XMLDomainReader.extractDomain(domainFile);
            system.changeDomain(domain);
            system.changeSettings(system.getSettings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void run() {
        system.startSystem();
        log.info("Dialogue system started!");

    }

    public static void main(String args[]) {
        WozDialogApp app = new WozDialogApp();
        app.run();
    }

}
