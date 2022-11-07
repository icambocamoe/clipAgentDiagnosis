/**
 * javac -cp lib\jade.jar;lib\CLIPSJNI.jar -d classes src\examples\protocols\*.java
 * java -cp lib\jade.jar;lib\CLIPSJNI.jar;classes jade.Boot -gui -agents r0:examples.protocols.ContractNetResponderAgent;r1:examples.protocols.ContractNetResponderAgent;r2:examples.protocols.ContractNetResponderAgent
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package examples.protocols;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;


import net.sf.clipsrules.jni.*;

/**
   This example shows how to implement the responder role in 
   a FIPA-contract-net interaction protocol. In this case in particular 
   we use a 

   <code>ContractNetResponder</code> to participate into a negotiation 
   
   where an initiator needs to assign a task to an agent among a set of candidates.
   @author Giovanni Caire - TILAB
 */
public class ContractNetResponderAgent extends Agent {
	private Environment clips;

	protected void setup() {
		
        clips = new Environment(); 

		loadKnowledge();

		System.out.println("Agent "+getLocalName()+" waiting for CFP...");
		
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		//this isnt the dispatchers guy of a parallel responder
		addBehaviour(new ContractNetResponder(this, template) {
			
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
				ACLMessage propose = cfp.createReply();
				//so my code do the diagnosis from the content of the proposing call that is the symptom
				try{
					clips.build("(defrule diagnosis (disease (name ?n) (symptom $? "+cfp.getContent()+" $?)) => (assert (foundDisease (foundName ?n) ) ))");
					clips.run();
					FactAddressValue fv = clips.findFact("foundDisease");
					String disease = fv.getSlotValue("foundName").toString();
					System.out.println("Agent "+getLocalName()+": Proposing "+disease);
					
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(disease));//setting the found disease as the proposed diagnosis
					
				}catch (Exception e) {
					e.printStackTrace();
				  }
				return propose;
					

			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				System.out.println("Agent "+getLocalName()+": Proposal accepted");
					//int t; variable dummy
					
					ACLMessage inform = accept.createReply();
				 	System.out.println("Agent "+getLocalName()+": Action successfully performed");
					//this give a treatment from the previous diagnosis, getting the diagnosis from the propose content
					 try{
						//clips.build("(defrule treatment (disease (name  "+propose.getContent()+")(treatment $?b )) => (printout t \"treatment \" ?b crlf))");
						clips.build("(defrule treatment ?f <-(disease (name  "+propose.getContent()+")(treatment $?b )) => (assert (foundTreatment (fact-slot-value ?f treatment  ) )))");
						clips.run();
						//t=1;
						FactAddressValue fv = clips.findFact("foundTreatment");
						String treatment = fv.getSlotValue("implied").toString();//implied see docs basic programming
						System.out.println("Agent "+getLocalName()+": Proposing "+treatment);
						inform.setPerformative(ACLMessage.INFORM);
						inform.setContent(String.valueOf(treatment));//setting found treatment to inform patient
					}catch (Exception e) {
						e.printStackTrace();
				 	}

					return inform;


			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Agent "+getLocalName()+": Proposal rejected");
			}
		} );
	}

/* en realidad deberia poner la propuesta de tratamiento y diagnosis aqui pero no me dio mucho tiempo de probaarlo
	private int evaluateAction() {
		
		return (int) (Math.random() * 10);
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
	}
*/
//metodo para cargar la base de conocimiento
	public void loadKnowledge(){
		System.out.println("loading knowledge base"); 
                try{
                    clips.build("(deftemplate disease(slot name)(multislot symptom)(multislot treatment))");
                    clips.build("(deftemplate foundDisease (slot foundName))");
					
					//clips.build("(deftemplate foundTreatment (multislot foundT))");
                   
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
                } catch (Exception e) {
                  e.printStackTrace();
                }
	}
}

