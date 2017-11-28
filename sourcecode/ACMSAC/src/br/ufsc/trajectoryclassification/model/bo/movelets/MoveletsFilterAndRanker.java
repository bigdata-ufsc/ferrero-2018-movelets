package br.ufsc.trajectoryclassification.model.bo.movelets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import br.ufsc.trajectoryclassification.model.bo.dmbs.IDistanceMeasureForSubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;

public class MoveletsFilterAndRanker {

	public static boolean areSelfSimilar(ISubtrajectory candidate, ISubtrajectory subtrajectory,
			double selfSimilarityProp) {
		
		// If their tids are different return false
		if (candidate.getTrajectory().getTid() != subtrajectory.getTrajectory().getTid())
			return false;

		else if (candidate.getStart() < subtrajectory.getStart()) {

			if (candidate.getEnd() < subtrajectory.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (candidate.getEnd() - subtrajectory.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		} else {

			if (subtrajectory.getEnd() < candidate.getStart())
				return false;

			if (selfSimilarityProp == 0)
				return true;

			double intersection = (subtrajectory.getEnd() - candidate.getStart())
					/ (double) Math.min(candidate.getSize(), subtrajectory.getSize());

			return intersection >= selfSimilarityProp;

		}

	}

	public static boolean searchIfSelfSimilarity(ISubtrajectory candidate, List<ISubtrajectory> list,
			double selfSimilarityProp) {

		for (ISubtrajectory s : list) {
			if (areSelfSimilar(candidate, s, selfSimilarityProp))
				return true;
		}

		return false;
	}

	public static void pearsonCorrelationFilter(List<ISubtrajectory> shapelets, double cutoff) {
		System.out.print("Correlation Filter: from " + shapelets.size());
		MyCounter.data.put("shapelets", (long) shapelets.size());
		PearsonsCorrelation pc = new PearsonsCorrelation();

		for (int i = 0; i < shapelets.size(); i++) {

			for (int j = i + 1; j < shapelets.size(); j++) {
				double cor = pc.correlation(shapelets.get(i).getDistances(), shapelets.get(j).getDistances());
				if (cor >= cutoff) {
					shapelets.remove(j);
					j--;
				}
			}
		}
		MyCounter.data.put("uncorrelated", (long) shapelets.size());
		System.out.println(" to " + shapelets.size() + " shapelets.");
	}

	public static void spearmanCorrelationFilter(List<ISubtrajectory> shapelets) {

		System.out.print("Spearman Correlation Filter: from " + shapelets.size());

		MyCounter.data.put("shapelets", (long) shapelets.size());
		SpearmansCorrelation pc = new SpearmansCorrelation();

		for (int i = 0; i < shapelets.size(); i++) {

			for (int j = i + 1; j < shapelets.size(); j++) {
				double cor = pc.correlation(shapelets.get(i).getDistances(), shapelets.get(j).getDistances());
				if (cor > 0.95) {
					shapelets.remove(j);
					j--;
				}
			}
		}
		MyCounter.data.put("uncorrelated", (long) shapelets.size());
		System.out.println(" to " + shapelets.size() + " shapelets.");
	}
	
	public static Set<Integer> getIndexesLowerSplitPoint(double[] distances, double splitpoint ){
		Set<Integer> indexes = new HashSet();
		
		for (int i = 0; i < distances.length; i++) {
			if (distances[i] <= splitpoint){
				indexes.add(i);
			}
		}
		return indexes;		
	}
	
	public static List<ISubtrajectory> noveltyFilter(List<ISubtrajectory> shapelets) {


		List<ISubtrajectory> noveltyShapelets = new ArrayList<>();
		Set<Integer> allCovered = new HashSet<Integer>();
		
		for (int i = 0; i < shapelets.size(); i++) {
			double[] distances = shapelets.get(i).getDistances();
			double splitpoint = (Double) shapelets.get(i).getQuality().getData().get("splitpoint");
			Set<Integer> currentCovered = getIndexesLowerSplitPoint(distances, splitpoint);
			
			if ( ! SetUtils.difference(currentCovered, allCovered).isEmpty()){
				noveltyShapelets.add(shapelets.get(i));
				allCovered.addAll(currentCovered);
			}
			
		}
		
		return noveltyShapelets;
	}

	public static List<ISubtrajectory> getShapelets(List<ISubtrajectory> candidates) {

		List<ISubtrajectory> orderedCandidates = rankCandidates(candidates);

		return getBestShapelets(orderedCandidates);
	}

	public static List<ISubtrajectory> rankCandidates(List<ISubtrajectory> candidates) {

		List<ISubtrajectory> orderedCandidates = new ArrayList<>(candidates);

		orderedCandidates.sort(new Comparator<ISubtrajectory>() {
			@Override
			public int compare(ISubtrajectory o1, ISubtrajectory o2) {

				return o1.getQuality().compareTo(o2.getQuality());

			}
		});

		return orderedCandidates;
	}

	private static List<ISubtrajectory> getBestShapelets(List<ISubtrajectory> shapeletsRanked) {

		return getBestShapelets(shapeletsRanked, 0);

	}

	public static List<ISubtrajectory> getBestShapelets(List<ISubtrajectory> rankedCandidates,
			double selfSimilarityProp) {

		// Realiza o loop até que acabem os atributos ou até que atinga o número
		// máximo de nBestShapelets
		// Isso é importante porque vários candidatos bem rankeados podem ser
		// selfsimilares com outros
		// que tiveram melhor score;
		for (int i = 0; (i < rankedCandidates.size()); i++) {

			// Se a shapelet candidata tem score 0 então já termina o processo
			// de busca
			if (rankedCandidates.get(i).getQuality().hasZeroQuality())
				return rankedCandidates.subList(0, i);

			ISubtrajectory candidate = rankedCandidates.get(i);

			// Removing self similar
			if (searchIfSelfSimilarity(candidate, rankedCandidates.subList(0, i), selfSimilarityProp)) {
				rankedCandidates.remove(i);
				i--;
			}

		}

		return rankedCandidates;
	}

}
