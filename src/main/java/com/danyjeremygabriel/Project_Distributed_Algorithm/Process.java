package com.danyjeremygabriel.Project_Distributed_Algorithm;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Process extends UntypedAbstractActor implements Serializable {
	
	
	private static final long serialVersionUID = -3904026307686534496L;
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor
	private final int N; //number of the process
	private final int id; //id of the process
	private Members processes; //other processes' reference (arraylist)
	private int localValue; //locally stored value, initially 0
    private int localTS; //locally stored timestamp, initially 0
    
    private int state; // process state
    private int localseqnbr; // local sequence number: the number of operations performed so far
    
    //process constructor
	public Process(int ID) {
        this.id = ID; //process ID
        this.state = 0; //default value and needed to get changed
        this.localseqnbr = 0; //initial number of operations = 0
        
    }

    //state : 1 - active, 2 - faulty, 3 - waiting
    static public class State {
        public final int state;
        public State(int state) {
            this.state = state;
        }
    }
    // the "read-request" message type with the local seq number
    static public class ReadRequest {
        public int localseqnbr;
        public ReadRequest(int localseqnbr) {
            this.localseqnbr = localseqnbr;
        }
    }



    






















	
	public static Props createActor(int ID, int nb, int r, int t) {
        return Props.create(Process.class, () -> {
            return new Process(ID, nb, r, t);
        });
    }
	
	private void readrequest(ReadRequestMsg rqmsg, ArrayList<ActorRef> references,  ActorRef sender) {
		
        for (ActorRef actor : references) {
            sender.tell(rqmsg, actor);;
        }
		
    }
	
	@Override
	public void onReceive(Object message) throws Throwable {
        if (this.state != 2) { //if not faulty
            if (message instanceof Members) {//save the system's info
                Members m = (Members) message;
                processes = m;
                log.info("p" + self().path().name() + " received processes info");
            }
            else if (message instanceof State) { //crash message
                this.state = ((State) message).state;
                if(this.state == 2) { //means the state of the process is faulty
                    log.info("P"+this.id+" is faulty");
                }
                
            }
        }
	}
}
