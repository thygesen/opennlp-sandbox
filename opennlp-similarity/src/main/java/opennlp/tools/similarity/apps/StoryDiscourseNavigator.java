/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.similarity.apps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import opennlp.tools.similarity.apps.utils.StringCleaner;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.textsimilarity.ParseTreeChunk;
import opennlp.tools.textsimilarity.SentencePairMatchResult;
import opennlp.tools.textsimilarity.TextProcessor;
import opennlp.tools.textsimilarity.chunker2matcher.ParserChunker2MatcherProcessor;

public class StoryDiscourseNavigator {
	protected BingQueryRunner yrunner = new BingQueryRunner();
	ParserChunker2MatcherProcessor sm = ParserChunker2MatcherProcessor
			.getInstance();
	private PorterStemmer ps = new PorterStemmer();

	public static final String[] frequentPerformingVerbs = {
		" born raised meet learn ", " graduated enter discover",
		" facts inventions life ", "accomplishments childhood timeline",
		" acquire befriend encounter", " achieve reache describe ",
		" invent innovate improve ", " impress outstanding award",
		" curous sceptical pessimistic", " spend enroll assume point",
		" explain discuss dispute", " learn teach study investigate",
		" propose suggest indicate", " pioneer explorer discoverer ",
		" advance promote lead", " direct control simulate ",
		" guide lead assist ", " inspire first initial",
		" vision predict foresee", " prediction inspiration achievement",
		" approve agree confirm", " deny argue disagree",
		" emotional loud imagination", " release announce celebrate discover",
		"introduce enjoy follow", " open present show",
		"meet enjoy follow create", "discover continue produce"

	};

	public String[] obtainAdditionalKeywordsForAnEntity(String entity){
		List<List<ParseTreeChunk>> matchList = runSearchForTaxonomyPath(
				entity, "", "en", 30);
		Collection<String> keywordsToRemove = TextProcessor.fastTokenize(entity.toLowerCase(), false);
		List<List<String>> resList = getCommonWordsFromList_List_ParseTreeChunk(matchList);
		String[] res = new String[resList.size()];
		int i=0;
		for(List<String> phrase: resList){
			phrase.removeAll(keywordsToRemove);
			String keywords = phrase.toString().replace('[', ' ').replace(']', ' ').replace(',',' ');
			res[i] = keywords;
			i++;
		}
		return res;
	}

	public List<List<ParseTreeChunk>> runSearchForTaxonomyPath(String query,
			String domain, String lang, int numbOfHits) {
		List<List<ParseTreeChunk>> genResult = new ArrayList<List<ParseTreeChunk>>();
		try {
			List<HitBase> resultList = yrunner.runSearch(query, numbOfHits);

			for (int i = 0; i < resultList.size(); i++) {
				{
					for (int j = i + 1; j < resultList.size(); j++) {
						HitBase h1 = resultList.get(i);
						HitBase h2 = resultList.get(j);
						String snapshot1 = StringCleaner.processSnapshotForMatching(h1
								.getTitle() + " . " + h1.getAbstractText());
						String snapshot2 = StringCleaner.processSnapshotForMatching(h2
								.getTitle() + " . " + h2.getAbstractText());
						SentencePairMatchResult matchRes = sm.assessRelevance(snapshot1,
								snapshot2);
						List<List<ParseTreeChunk>> matchResult = matchRes.getMatchResult();
						genResult.addAll(matchResult);
					}
				}
			}

		} catch (Exception e) {
			System.err.print("Problem extracting taxonomy node");
		}

		return genResult;
	}
	private List<List<String>> getCommonWordsFromList_List_ParseTreeChunk(
			List<List<ParseTreeChunk>> matchList) {
		List<List<String>> res = new ArrayList<List<String>>();
		for (List<ParseTreeChunk> chunks : matchList) {
			List<String> wordRes = new ArrayList<String>();
			for (ParseTreeChunk ch : chunks) {
				List<String> lemmas = ch.getLemmas();
				for (int w = 0; w < lemmas.size(); w++)
					if ((!lemmas.get(w).equals("*"))
							&& ((ch.getPOSs().get(w).startsWith("NN") || ch.getPOSs().get(w)
									.startsWith("VB"))) && lemmas.get(w).length() > 2) {
						String formedWord = lemmas.get(w);
						String stemmedFormedWord = ps.stem(formedWord);
						if (!stemmedFormedWord.startsWith("invalid"))
							wordRes.add(formedWord);
					}
			}
			wordRes = new ArrayList<String>(new HashSet<String>(wordRes));	   
			if (wordRes.size() > 0) {
				res.add(wordRes);
			}
		}
		res = new ArrayList<List<String>>(new HashSet<List<String>>(res));
		return res;
	}
	public static void main(String[] args){
		String[] res = new StoryDiscourseNavigator().obtainAdditionalKeywordsForAnEntity("Albert Einstein");
		System.out.println(Arrays.asList(res));
	}
}
