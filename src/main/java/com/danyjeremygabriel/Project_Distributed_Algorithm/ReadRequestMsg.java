package com.danyjeremygabriel.Project_Distributed_Algorithm;

import java.io.Serializable;
import java.util.ArrayList;

public class ReadRequestMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public final ArrayList<Integer> requestList; //list that contains the receiver and the sequence number -> [?,r]
	public final Process p;
	public int r;
	
	public ReadRequestMsg(ArrayList<Integer> requestList, Process p, int r) {
		
		this.requestList = org.apache.commons.lang3.SerializationUtils.clone(requestList);
		this.p = org.apache.commons.lang3.SerializationUtils.clone(p);
		this.r = r;
		
	}
	
}
