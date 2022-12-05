package examples.bookTrading;



import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PatientAgent extends Agent{
    // The type of disease
	private String[] targetSymptom;
	// The list of known doctor agents
	private AID[] doctorAgents;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! Patient-agent "+getAID().getName()+" is ready.");

		// Get the symptom as a start-up argument
        Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetSymptom = (String) args[0];

			System.out.println("Your symptoms are "+targetSymptom);

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("give-diagnosis");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template); 

                System.out.println("Found the following doctor agents:");

                doctorAgents = new AID[result.length];

                for (int i = 0; i < result.length; ++i) {
                    doctorAgents[i] = result[i].getName();
                    System.out.println(doctorAgents[i].getName());
                }
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }
            
            // Perform the request
			myAgent.addBehaviour(new RequestPerformer());
		}
		else {
			// Make the agent terminate
			System.out.println("No symtomp specified");
			doDelete();
		}
    }
    private class RequestPerformer extends Behaviour {
		private AID lastDoctor; // The agent who provides the last offer 
		private int repliesCnt = 0; // The counter of replies from doctor agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all doctors
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < doctoAgents.length; ++i) {
					cfp.addReceiver(doctorAgents[i]);
				} 
				cfp.setContent(targetSymptom);
				cfp.setConversationId("get-diagnosis");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-diagnosis"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					repliesCnt++;
					if (repliesCnt >= doctorAgents.length) {
						// We received all replies
						lastDoctor = reply.getSender();
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(targetSymptom);
				order.setConversationId("get-diagnosis");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-diagnosis"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the diagnosis reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// diagnosis order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// diagnosis successful. We can terminate
						System.out.println(targetBookTitle+" successfully got diagnosis from agent "+reply.getSender().getName());
						
						myAgent.doDelete();
					}
					else {
						System.out.println("Attempt failed: not a diagnosis gor your symptoms.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
            }
        }

		
		public boolean done() {
			if (step == 2 && lastDoctor == null) {
				System.out.println("Attempt failed: "+targetSymptom+" not available for diagnosis");
			}
			return ((step == 2 && lastDoctor == null) || step == 4);
		}
    }    
    
}
