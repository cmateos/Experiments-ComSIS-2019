package weka;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassifierRunner extends Runner {

	private Classifier classifier;
	private Instances train;
	
	public ClassifierRunner(Classifier original, Instances train) throws Exception {
		classifier = AbstractClassifier.makeCopy(original);
		this.train = train;
	}
	
	@Override
	public void run() {
		try {
			classifier.buildClassifier(train);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
