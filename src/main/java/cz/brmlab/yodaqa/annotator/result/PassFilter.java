package cz.brmlab.yodaqa.annotator.result;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;

import cz.brmlab.yodaqa.model.SearchResult.Passage;

/**
 * Generate Passages from Sentences that contain some Clue in Question
 * and copy them over to the Passages view.
 *
 * Prospectively, we might want to keep some surrounding sentences,
 * though. */

@SofaCapability(
	inputSofas = { "Passages" },
	outputSofas = { "PickedPassages" }
)


public class PassFilter extends JCasAnnotator_ImplBase {
	/** Number of passages to pick for detailed analysis. */
	public static final String PARAM_NUM_PICKED = "num-picked";
	@ConfigurationParameter(name = PARAM_NUM_PICKED, mandatory = false, defaultValue = "3")
	private int numPicked;

	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
	}

	public void process(JCas jcas) throws AnalysisEngineProcessException {
		JCas passagesView, pickedPassagesView;
		try {
			passagesView = jcas.getView("Passages");
			jcas.createView("PickedPassages");
			pickedPassagesView = jcas.getView("PickedPassages");
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		pickedPassagesView.setDocumentText(passagesView.getDocumentText());
		pickedPassagesView.setDocumentLanguage(passagesView.getDocumentLanguage());

		/* Just copy over the first N ones. */
		FSIndex idx = passagesView.getJFSIndexRepository().getIndex("SortedPassages");
		FSIterator passages = idx.iterator();
		int i = 0;
		CasCopier copier = new CasCopier(passagesView.getCas(), pickedPassagesView.getCas());
		while (passages.hasNext() && i++ < numPicked) {
			Passage passage = (Passage) passages.next();
			Passage p2 = (Passage) copier.copyFs(passage);
			p2.addToIndexes();
		}
	}
}
