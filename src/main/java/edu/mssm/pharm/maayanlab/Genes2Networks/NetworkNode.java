package edu.mssm.pharm.maayanlab.Genes2Networks;

import java.util.HashSet;

public class NetworkNode {
	
	// TODO: deprecate accession ID
	private HashSet<NetworkNode> neighbors = new HashSet<NetworkNode>();
	private String name;
	private String accessionID1, accessionID2;
	private String type;
	private String location;
	
	public NetworkNode(String name, String accessionID1, String accessionID2, String type, String location) {
		this.name = name;
		this.accessionID1 = accessionID1;
		this.accessionID2 = accessionID2;
		this.type = type;
		this.location = location;
	}
	
	public void update(String type, String location) {
		if (this.type.equals("NA"))
			this.type = type;
		
		if (this.location.equals("NA"))
			this.location = location;
	}
	
	public String getName() {
		return name;
	}
	
	public HashSet<NetworkNode> getNeighbors() {
		return neighbors;
	}
	
	public void addNeighbor(NetworkNode neighbor) {
		neighbors.add(neighbor);
	}

	public void removeNeighbor(NetworkNode neighbor) {
		neighbors.remove(neighbor);
	}
	
	public void removeSelf() {
		for (NetworkNode neighbor : neighbors) {
			if (neighbor != this)
				neighbor.removeNeighbor(this);
		}
		this.name = null;
		this.accessionID1 = null;
		this.accessionID2 = null;
		this.type = null;
		this.location = null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkNode other = (NetworkNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return name + " " + accessionID1 + " " + accessionID2 + " " + type + " " + location;
	}
	
	
}
