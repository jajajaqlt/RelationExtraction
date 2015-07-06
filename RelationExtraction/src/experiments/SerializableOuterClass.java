package experiments;

import gov.nih.nlm.nls.metamap.Result;

import java.io.Serializable;
import java.util.List;

public class SerializableOuterClass implements Serializable {
	public List<Result> results;
}
