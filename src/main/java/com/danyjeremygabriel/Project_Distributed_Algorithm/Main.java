package com.danyjeremygabriel.Project_Distributed_Algorithm;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

//To print time 
import java.text.SimpleDateFormat;
import java.util.Date;
//

import java.util.ArrayList;

import java.util.Collections;
import akka.actor.ActorRef;
public class Main
{
    public static void main( String[] args )
    {
        final int N = 100;  // The system size, we test for N = 3, N = 10 and N = 100
        final ActorSystem system = ActorSystem.create("system"); // create system
        Date now = new Date();
        ArrayList<ActorRef> members = new ArrayList<ActorRef>();

        try {
            //#create-actors    
        for(int x = 0; x <= N-1; x = x + 1) {
             members.add(system.actorOf(Process.props(x), "P"+Integer.toString(x)));
        }    
        System.out.println("Members are : " + members);
    
           SimpleDateFormat dateFormatter = new SimpleDateFormat("E m/d/y h:m:s.SSS z");
           System.out.println("System birth: "+ dateFormatter.format(now));
    
           for(int x = 0; x < N; x = x + 1) {
           members.get(x).tell(new Process.Members(members), ActorRef.noSender());      
        }
    
        // shuffle and choose 1/3 random processes to fail
           
        Collections.shuffle(members);
      
        for(int x = 0; x <2*N/3; x = x + 1) { // first 2N/3+1 processes are active
               members.get(x).tell(new Process.State(1), ActorRef.noSender());      
            } 
            for(int x = 2*N/3; x < N; x = x + 1) { // last N/3-1 processes are faulty 
                members.get(x).tell(new Process.State(2), ActorRef.noSender());
               //System.out.println("Process "+ members.get(x)+" is faulty");
             }
        
            //activate the writer and the reader
            
            members.get(0).tell(new Role(1), ActorRef.noSender());  //writer
            members.get(1).tell(new Role(2), ActorRef.noSender());  //reader
        
            
          //#main-send-messages
    
          System.out.println(">>> Press ENTER to exit <<<");
          System.in.read();
        } catch (IOException ioe) {
        } finally {
          system.terminate();
        }
    }
}
