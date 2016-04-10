import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class ClusterList {
	ArrayList<Cluster> clusters;
	public ClusterList(){
		clusters = new ArrayList<Cluster>();
	}
	public ClusterList(Collection<? extends Cluster> c){
		clusters = new ArrayList<Cluster>(c);
	}
	public void insert(Cluster cl, int tolerance){
		int comp;
		if (clusters.isEmpty()){
			clusters.add(cl);
			return;
		}
		for (int i = 0; i < clusters.size(); i++){
			comp = clusters.get(i).compareTo(cl);
			if (Math.abs(comp) <= tolerance){
				clusters.get(i).merge(cl);
				return;
			}
			if (comp > 0){
				clusters.add(i, cl);
				return;
			}
		}
		clusters.add(cl);
	}
	public Cluster aggregate(double tolerance){
		ArrayList<Cluster> tmp;
		Cluster to_ret = null;
		long tol;
		if (clusters.isEmpty()){
		} else if (clusters.size() == 1){
			return clusters.get(0);
		} else {
			tmp = new ArrayList<Cluster>(clusters);
			/* sort by weight in descending order */
			Collections.sort(tmp, new Comparator<Cluster>(){
				@Override
				public int compare(Cluster o1, Cluster o2) {
					return (int)(o2.total_weight - o1.total_weight);
				}});
			to_ret = tmp.get(0);
			tol = (long) (tolerance * to_ret.total_weight);
			/* maybe do some merging / preference to things closer to 120 bpm? */
		}
		return to_ret;
	}
}
