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
import java.util.HashMap;
import java.util.Random;


public class Process extends UntypedAbstractActor implements Serializable {
	
	
	private static final long serialVersionUID = -3904026307686534496L;
    private static final int M = 3;
    private static volatile HashMap vh = new HashMap<Integer,Integer>();
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor
    

    private Random randomno = new Random();
    private int state; // process state
    private int localseqnbr; // local sequence number: the number of operations performed so far
    private StampedValue val; // response from the other processes (read and write requests)
    private ArrayList msgs = new ArrayList();
    private Members mem;
    private int role;
    public int timestamp;    // number of operations to perform 

    public volatile int test;
    private int id;
	private Object put;
    
    // process constructor
    public Process(int id) {
        this.id = id; // process ID
        this.state = 0; // default value and needed to get changed
        this.localseqnbr = 0; // initial number of operations = 0
        this.val = new StampedValue(0, 0, 0);
        this.role = 0;
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        log = Logging.getLogger(getContext().getSystem(), this);
        
    }

    //state : 1 - active, 2 - faulty, 3 - waiting
    static public class State {
        public final int state;
        public State(int state) {
            this.state = state;
        }
    }
    static public class StampedValue {
        public volatile int value;
        public volatile int seqnbr;
        public int pid;
        public StampedValue(int value, int seqnbr, int pid) {
            this.value = value;
            this.seqnbr = seqnbr;
            this.pid = pid;
        }
    }
    // the "read-request" message type with the local seq number
    static public class ReadRequest {
        public int localseqnbr;
        public ReadRequest(int localseqnbr) {
            this.localseqnbr = localseqnbr;
        }
    }

    static public class WriteRequest {
        public StampedValue val;
        public int localseqnbr;
        public WriteRequest(StampedValue val, int localseqnbr) {
            this.val = val;
            this.localseqnbr = localseqnbr;
        }
    }
    static public class ReadResponse {
        public StampedValue val;
        public int localseqnbr;
        public ReadResponse(StampedValue val, int localseqnbr) {
            this.val = val;
            this.localseqnbr = localseqnbr;
        }
    }
    static public class WriteResponse {
        public int localseqnbr;
        public WriteResponse(int localseqnbr) {
            this.localseqnbr = localseqnbr;
        }
    }

    static public class maxTimestampRequest {
        public int val;
        public int timestamp;
        public maxTimestampRequest(int value, int timestamp) {
            this.val = val;
            this.timestamp = timestamp;
        }
    }
    
    static public class maxTimestampResponse {
        public volatile int val;
        public volatile int timestamp;
        public maxTimestampResponse(int val, int timestamp) {
            this.val = val;
            this.timestamp = timestamp;
        }

    }
    static public class Members {
        public ArrayList<ActorRef> members; //list containing all actors that one actor knows
        public Members(ArrayList<ActorRef> members) {   
            this.members = members;
        }
    }

    public static void put(int key, int val) {
        vh.put(key, val);
    }

    public int get(int key) {
        if (vh.containsKey(key)) {
            return (int) vh.get(key);
        }
        
        return -1;
    }

    static public Props props(int id) {
        return Props.create(Process.class, () -> new Process(id));
    }
	
	@Override
	public void onReceive(Object message) throws Throwable {
        if (this.state != 2) { //if not faulty
            ActorRef sender = getSender();
            if (message instanceof Members) {//save the system's info
                this.mem = (Members) message;
                log.info("p" + self().path().name() + " received processes info");
            }
            else if (message instanceof Role) {
                this.role = ((Role) message).role;
                if (this.role == 1) { //the process is the writer
                    log.info("P"+this.id+" is the writer");
                    this.localseqnbr++; //increments sequence number
                    int ik = randomno.nextInt(3);
                    int iv = randomno.nextInt(50);
                    log.info("P"+this.id+": invokes key "+ ik +" : put("+ iv +")");
                    this.put(ik, iv);
                    this.msgs.clear();
                    for(int x = 0; x < this.mem.members.size(); x = x + 1) { // send the write request to all
                        this.mem.members.get(x).tell(new WriteRequest(new StampedValue(this.localseqnbr,this.localseqnbr,this.id),this.localseqnbr),getSelf()); 
                    }
                    this.state=3; // Enter the waiting state, waiting for the response from the majority of the processes
                }
                else { // the process is the reader
                    log.info("P"+this.id+" is the reader");
                    this.localseqnbr++;
                    int ik = randomno.nextInt(3);
                    if (vh.get(ik) != null) {
                        log.info("P"+this.id+": invokes key "+ ik +" : get() with value = "+ vh.get(ik));
                    }
                    else {
                        log.info("P"+this.id+": invokes key "+ ik +" : get() with value = -1");
                    }
                    this.msgs.clear();
                    for(int x = 0; x < this.mem.members.size(); x = x + 1) { // get the current value/timestamp
                        this.mem.members.get(x).tell(new ReadRequest(localseqnbr),getSelf());
                    }
                    this.state=3; // Enter the waiting state, waiting for the response from the majority of the processes

                }
            }

            else if (message instanceof State) { //crash message
                this.state = ((State) message).state;
                if(this.state == 2) { //means the state of the process is faulty
                    log.info("P"+this.id+" is faulty");
                }
                
            }
            else if (message instanceof ReadRequest) {
                //log.info("P"+this.id+": received a read request "+this.localseqnbr+" from "+sender); 
                sender.tell(new ReadResponse(this.val,((ReadRequest)message).localseqnbr),getSelf()); 
            }
            else if (message instanceof ReadResponse && ((ReadResponse)message).localseqnbr==this.localseqnbr && this.state==3) {
                msgs.add(message);
                // If "enough" responses received:  complete the read operation
                if (msgs.size()>=this.mem.members.size()/2+1) {
                    //log.info("P"+this.id+": received a quorum of read responses "+this.localseqnbr);
                    this.state=1; // not waiting any longer
                    for (int x = 0; x<msgs.size(); x = x+1) {
                        if (((ReadResponse)msgs.get(x)).val.seqnbr>this.val.seqnbr || ((ReadResponse)msgs.get(x)).val.seqnbr==this.val.seqnbr && ((ReadResponse)msgs.get(x)).val.pid>this.val.pid) {
                            this.val = ((ReadResponse)msgs.get(x)).val;
                        }
                    }

                    //log.info("P"+this.id+": new (value,timestamp) = ["+this.val.value+",("+this.val.seqnbr+","+this.val.pid+")]");
                    this.state=1;	// not waiting any longer
                    int ik = randomno.nextInt(3);
                    log.info("P"+this.id+": completes get with key = "+ ik +" with value "+ this.get(ik));
                    if ((this.localseqnbr)<M) {
                        getSelf().tell(new Role(2),ActorRef.noSender()); // invoke a new get operation
                    }
                }

            }
            else if (message instanceof maxTimestampRequest) {
                sender.tell(new maxTimestampResponse(this.val.value, this.val.seqnbr), getSelf());
            }

            else if (message instanceof maxTimestampResponse) {

                for(int x = 0; x < this.mem.members.size(); x = x + 1) { // get the max value with the highest timestamp

                    if (this.val.value < ((maxTimestampResponse) message).val && this.val.seqnbr < ((maxTimestampResponse) message).timestamp) { 
                        this.val.value = ((maxTimestampResponse) message).val;
                        this.val.seqnbr = ((maxTimestampResponse) message).timestamp;
                    }
                    
                }

            }

            else if (message instanceof WriteResponse && ((WriteResponse)message).localseqnbr==this.localseqnbr && this.state==3) {
                msgs.add(message);
                if (msgs.size()>=this.mem.members.size()/2+1) {
                    this.state=1;
                    log.info("P"+this.id+": completes put operation "+this.localseqnbr);
                    if ((this.localseqnbr)<M) {
                        getSelf().tell(new Role(1),ActorRef.noSender()); // invoke a new put operation
                    }
            } 
            else
            unhandled(message);

        }
	}
}}
