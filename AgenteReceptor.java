
//javac -cp CLIPSJNI\CLIPSJNI.jar;lib\jade.jar  -d classes src\examples\messaging\*.java
//java -cp CLIPSJNI\CLIPSJNI.jar;lib\jade.jar;classes jade.Boot -gui -agents receptor:examples.messaging.AgenteReceptor
package examples.messaging;
 
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import net.sf.clipsrules.jni.*;
 
public class AgenteReceptor extends Agent {
    private Environment clips;

    protected void setup() {
        clips = new Environment(); 
        addBehaviour(new ReceptorComportaminento());
    }

   private class ReceptorComportaminento extends SimpleBehaviour {
            private boolean fin = false;
       
            public void action() {
                //Obtiene el primer mensaje de la cola de mensajes
                System.out.println(" Preparandose para recibir");
                ACLMessage mensaje = receive();

                if (mensaje!= null) {
                    
                    System.out.println(getLocalName() + ": acaba de recibir el siguiente mensaje: ");
                    System.out.println(mensaje.getContent());
                    fin = true;
                    System.out.println("loading knowledge base"); 
                    try{
                      //clips.load("examples.messaging.load-diseases.clp");
                      clips.build("(deftemplate disease(slot name)(multislot symptom)(multislot treatment))");
                      clips.build("(defrule diagnosis (disease (name ?n) (symptom $? "+mensaje.getContent()+" $?)) => (printout t \"disease name found:\" ?n  crlf ))");
                      clips.build("(deffacts diseases"
                      +"(disease (name headache) (symptom \"sensitivity to light\" \"loss of appetite\" \"facial pain\" \"facial pressure\" \"dizziness\" \"blurred vision\")"
                      +"(treatment \"resting in a quiet dark room\" \"administering a hot or cold compress\" \"gentle head massages\" \"over-the-counter medication\"))"
                      +"(disease (name cold) (symptom \"runny nose\" \"stuffy nose\" \"sore throat\" \"cough\" \"congestion\")"
                      +"(treatment \"plenty of rest\" \"proper hydration\" \"over-the-counter nasal decongestants\"))"
                      +"(disease (name otitis) (symptom \"redness outer ear\" \"itch in ear\" \"ear pain\" \"pus in ear\")" 
                      +"(treatment \"antibiotics\" \"ENT doctor\"))"
                      +"(disease (name conjunctivitis) (symptom \"red eye\" \"itchi eye\" \"irritation eye\" \"discharge eye\" \"tearing eye\")" 
                      +"(treatment   \"artificial tears\" \"cleaning the eyelids with a wet cloth\" \"applying cold or warm compresses\" \"antihistamine eyedrops\" ))"
                      +"(disease (name pharyngitis) (symptom \"sore throat\" \"dry throat\" \"scratchy throat\") "
                      +"(treatment \"drinking plenty of fluids\" \"gargling with warm salt water\" \"taking throat lozenges\" \"ENT doctor\")))");
                      clips.reset();
                      //clips.eval("(facts)");
              
                      clips.run();
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                }
            }

            public boolean done() {
                return fin;
            }
    }
}