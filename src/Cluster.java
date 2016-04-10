import java.util.ArrayList;
import java.util.Collection;

/**
 * Clustering for pseudo k-means approximation
 * @author bowenzhi
 * could make this much more generalized with generics, but this suffices for now
 * @param <K>
 */
public class Cluster implements Comparable<Cluster> {
	public ArrayList<Long> values;
	public ArrayList<Integer> weights;
	public long aggregate;
	public long total_weight;
	public Cluster(){
		values = new ArrayList<Long>();
		weights = new ArrayList<Integer>();
		aggregate = 0;
		total_weight = 0;
	}
	public Cluster(long init, int weight){
		this();
		this.insert(init, weight);
	}
	public Cluster(Collection<? extends Long> init_list){
		values = new ArrayList<Long>(init_list);
	}
	public void insert(long value, int weight){
		values.add(value);
		weights.add(weight);
		aggregate += value*weight;
		total_weight += weight;
	}
	public void merge(Cluster cl){
		for (int i = 0; i < cl.values.size(); i++){
			this.insert(cl.values.get(i), cl.weights.get(i));
		}
	}
	public long center(){
		return (total_weight > 0)? (aggregate / total_weight) : 0;
	}
	
	@Override
	public int compareTo(Cluster o) {
		return (int)(this.center() - o.center());
	}
	
	/** 
	 * Could implement aggregation functions, but for now I do not think that is necessary
	 **/
}
