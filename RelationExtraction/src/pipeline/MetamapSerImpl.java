package pipeline;

import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.Candidates;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Map;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MatchMap;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Phrase;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import se.sics.prologbeans.PBTerm;

public class MetamapSerImpl {

	public static Result convertToSerImpl(Result result) throws Exception {

		ResultSerImpl resultSerImpl = new ResultSerImpl();
		resultSerImpl.setInputText(result.getInputText());

		List<Utterance> utteranceSerImplList = new ArrayList<Utterance>();
		resultSerImpl.utteranceList = utteranceSerImplList;
		for (Utterance utterance : result.getUtteranceList()) {

			UtteranceSerImpl utteranceSerImpl = new UtteranceSerImpl();
			utteranceSerImplList.add(utteranceSerImpl);
			utteranceSerImpl.id = utterance.getId();
			utteranceSerImpl.string = utterance.getString();
			Position position = utterance.getPosition();
			PositionSerImpl positionSerImpl = new PositionSerImpl();
			positionSerImpl.x = position.getX();
			positionSerImpl.y = position.getY();
			utteranceSerImpl.position = positionSerImpl;

			List<PCM> pcmSerImplList = new ArrayList<PCM>();
			utteranceSerImpl.pcmList = pcmSerImplList;
			for (PCM pcm : utterance.getPCMList()) {

				PCMSerImpl pcmSerImpl = new PCMSerImpl();
				pcmSerImplList.add(pcmSerImpl);
				Phrase phrase = pcm.getPhrase();
				PhraseSerImpl phraseImplSer = new PhraseSerImpl();
				phraseImplSer.phraseText = phrase.getPhraseText();
				phraseImplSer.mincoManAsString = phrase.getMincoManAsString();
				Position position2 = phrase.getPosition();
				PositionSerImpl positionSerImpl2 = new PositionSerImpl();
				positionSerImpl2.x = position2.getX();
				positionSerImpl2.y = position2.getY();
				phraseImplSer.position = positionSerImpl2;
				pcmSerImpl.phrase = phraseImplSer;

				List<Mapping> mappingSerImplList = new ArrayList<Mapping>();
				pcmSerImpl.mappingList = mappingSerImplList;
				for (Mapping mapping : pcm.getMappingList()) {

					MappingSerImpl mappingSerImpl = new MappingSerImpl();
					mappingSerImplList.add(mappingSerImpl);
					mappingSerImpl.score = mapping.getScore();

					List<Ev> evSerImplList = new ArrayList<Ev>();
					mappingSerImpl.evList = evSerImplList;
					for (Ev ev : mapping.getEvList()) {

						EvSerImpl evSerImpl = new EvSerImpl();
						evSerImplList.add(evSerImpl);
						evSerImpl.conceptId = ev.getConceptId();
						evSerImpl.conceptName = ev.getConceptName();
						List<String> matchedWordsSerImpl = new ArrayList<String>();
						for (String matchedWord : ev.getMatchedWords()) {
							matchedWordsSerImpl.add(matchedWord);
						}
						evSerImpl.matchedWords = matchedWordsSerImpl;
						List<Position> positionalInfoSerImpl = new ArrayList<Position>();
						for (Position position3 : ev.getPositionalInfo()) {
							PositionSerImpl positionSerImpl3 = new PositionSerImpl();
							positionalInfoSerImpl.add(positionSerImpl3);
							positionSerImpl3.x = position3.getX();
							positionSerImpl3.y = position3.getY();
						}
						evSerImpl.positionalInfo = positionalInfoSerImpl;
						evSerImpl.preferredName = ev.getPreferredName();
						evSerImpl.pruningStatus = ev.getPruningStatus();
						evSerImpl.score = ev.getScore();
						List<String> semanticTypesSerImpl = new ArrayList<String>();
						for (String semanticType : ev.getSemanticTypes()) {
							semanticTypesSerImpl.add(semanticType);
						}
						evSerImpl.semanticTypes = semanticTypesSerImpl;
						List<String> sourcesSerImpl = new ArrayList<String>();
						for (String source : ev.getSources()) {
							sourcesSerImpl.add(source);
						}
						evSerImpl.sources = sourcesSerImpl;
						evSerImpl.isHead = ev.isHead();
						evSerImpl.isOverMatch = ev.isOvermatch();
						evSerImpl.negationStatus = ev.getNegationStatus();
					}

				}

			}

		}
		return resultSerImpl;
	}

	public static class EvSerImpl implements Ev {

		public String conceptId;
		public String conceptName;
		public List<String> matchedWords;
		public List<Position> positionalInfo;
		public String preferredName;
		public int pruningStatus;
		public int score;
		public List<String> semanticTypes;
		public List<String> sources;
		public boolean isHead;
		public boolean isOverMatch;
		public int negationStatus;

		@Override
		public int getScore() throws Exception {
			return this.score;
		}

		@Override
		public String getConceptId() throws Exception {
			return this.conceptId;
		}

		@Override
		public String getConceptName() throws Exception {
			return this.conceptName;
		}

		@Override
		public String getPreferredName() throws Exception {
			return this.preferredName;
		}

		@Override
		public List<String> getMatchedWords() throws Exception {
			return this.matchedWords;
		}

		@Override
		public List<String> getSemanticTypes() throws Exception {
			return this.semanticTypes;
		}

		@Override
		public List<Object> getMatchMap() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<MatchMap> getMatchMapList() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isHead() throws Exception {
			return this.isHead;
		}

		@Override
		public boolean isOvermatch() throws Exception {
			return this.isOverMatch;
		}

		@Override
		public List<String> getSources() throws Exception {
			return this.sources;
		}

		@Override
		public List<Position> getPositionalInfo() throws Exception {
			return this.positionalInfo;
		}

		@Override
		public PBTerm getTerm() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getPruningStatus() throws Exception {
			return this.pruningStatus;
		}

		@Override
		public int getNegationStatus() throws Exception {
			return this.negationStatus;
		}

	}

	public static class MappingSerImpl implements Mapping {

		public List<Ev> evList;
		public int score;

		@Override
		public int getScore() throws Exception {
			return this.score;
		}

		@Override
		public List<Ev> getEvList() throws Exception {
			return this.evList;
		}

	}

	public static class PhraseSerImpl implements Phrase {

		public String phraseText;
		public String mincoManAsString;
		public Position position;

		@Override
		public String getPhraseText() throws Exception {
			return this.phraseText;
		}

		@Override
		public String getMincoManAsString() {
			return this.mincoManAsString;
		}

		@Override
		public Position getPosition() throws Exception {
			return this.position;
		}

	}

	public static class PCMSerImpl implements PCM {

		public Phrase phrase;
		public List<Mapping> mappingList;

		@Override
		public Phrase getPhrase() throws Exception {
			return this.phrase;
		}

		@Override
		public Candidates getCandidatesInstance() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Ev> getCandidates() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Ev> getCandidateList() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Map> getMappings() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Mapping> getMappingList() throws Exception {
			return this.mappingList;
		}

	}

	public static class PositionSerImpl implements Position {

		public int x;
		public int y;

		@Override
		public int getX() {
			return this.x;
		}

		@Override
		public int getY() {
			return this.y;
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}

	}

	public static class UtteranceSerImpl implements Utterance {

		public String id;
		public String string;
		public Position position;
		public List<PCM> pcmList;

		// public UtteranceSerImpl(){
		//
		// }
		
		@Override
		public String getId() throws Exception {
			return this.id;
		}

		@Override
		public String getString() throws Exception {
			return this.string;
		}

		@Override
		public Position getPosition() throws Exception {
			return this.position;
		}

		@Override
		public List<PCM> getPCMList() throws Exception {
			return this.pcmList;
		}

	}

	public static class ResultSerImpl implements Result {

		public String inputText;
		public List<Utterance> utteranceList;

		@Override
		public void setInputText(String theInputText) {
			this.inputText = theInputText;
		}

		@Override
		public String getInputText() {
			return this.inputText;
		}

		@Override
		public PBTerm getMMOPBlist() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMachineOutput() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void traverse(PrintStream out) {
			// TODO Auto-generated method stub

		}

		@Override
		public List<AcronymsAbbrevs> getAcronymsAbbrevs() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<AcronymsAbbrevs> getAcronymsAbbrevsList() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Negation> getNegations() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Negation> getNegationList() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		public List<Utterance> getUtteranceList() throws Exception {
			return this.utteranceList;
		}

	}
}