package cz.brmlab.yodaqa.io.interactive;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import cz.brmlab.yodaqa.flow.dashboard.Question;
import cz.brmlab.yodaqa.flow.dashboard.QuestionDashboard;
import cz.brmlab.yodaqa.model.AnswerHitlist.Answer;
import cz.brmlab.yodaqa.model.Question.QuestionInfo;

/**
 * A trivial consumer that will extract the final answer and print it
 * on the standard output for the user to "officially" see.
 *
 * Pair this with InteractiveQuestionReader.
 */

public class InteractiveAnswerPrinter extends JCasConsumer_ImplBase {

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
	}

	public void process(JCas jcas) throws AnalysisEngineProcessException {
		JCas questionView, answerHitlist;
		try {
			questionView = jcas.getView("Question");
			answerHitlist = jcas.getView("AnswerHitlist");
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		QuestionInfo qi = JCasUtil.selectSingle(questionView, QuestionInfo.class);
		FSIndex idx = answerHitlist.getJFSIndexRepository().getIndex("SortedAnswers");
		FSIterator answers = idx.iterator();
		if (answers.hasNext()) {
			int i = 1;
			while (answers.hasNext()) {
				Answer answer = (Answer) answers.next();
				System.out.println((i++) + ". " + answer.getText() + " (conf. " + answer.getConfidence() + ")");
			}
		} else {
			System.out.println("No answer found.");
		}
		Question q = QuestionDashboard.getInstance().get(Integer.parseInt(qi.getQuestionId()));
		// q.setAnswers(answers); XXX
		QuestionDashboard.getInstance().finishQuestion(q);
	}
}
