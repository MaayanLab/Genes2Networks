package edu.mssm.pharm.maayanlab.Genes2Networks;

public class Interaction {
	
	private NetworkNode source;
	private NetworkNode target;
	private String effect;
	private String type;
	private String pmid;
	
	public Interaction(NetworkNode source, NetworkNode target, String effect, String type, String pmid) {
		this.source = source;
		this.target = target;
		this.effect = effect;
		this.type = type;
		this.pmid = pmid;
	}

	public NetworkNode getSource() {
		return source;
	}
	
	public NetworkNode getTarget() {
		return target;
	}
	
	public String getPmid() {
		return pmid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((effect == null) ? 0 : effect.hashCode());
		result = prime * result + ((pmid == null) ? 0 : pmid.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interaction other = (Interaction) obj;
		if (effect == null) {
			if (other.effect != null)
				return false;
		} else if (!effect.equals(other.effect))
			return false;
		if (pmid == null) {
			if (other.pmid != null)
				return false;
		} else if (!pmid.equals(other.pmid))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return source.toString() + " " + target.toString() + " " + effect +  " " + type + " " + pmid;
	}
	
}
