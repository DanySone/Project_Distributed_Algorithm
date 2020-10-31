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
	private int localValue=0; //locally stored value, initially 0
	private int localTS=0; //locally stored timestamp, initially 0
	private int r;
	private int t;
	
	public Process(int ID, int nb, int r, int t) {
        N = nb;
        id = ID;
        r=0;
        t=0;
    }
	
	public int getSeqNbr() {
		return this.r;
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
		if (message instanceof Members) {//save the system's info
            Members m = (Members) message;
            processes = m;
            log.info("p" + self().path().name() + " received processes info");
        }
		else if (message instanceof ReadRequestMsg) {
            ReadRequestMsg m = (ReadRequestMsg) message;
            getContext().sender().tell(, getContext().self());
        }
		
	}
}
