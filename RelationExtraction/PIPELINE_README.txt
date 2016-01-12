Get Candidates(abstract)
	for each abstract	
		- normalizes non-ascii characters
		- transforms into a metamap instance
		for each utterance(sentence)
			- pickes all pairs of phrases
			for both elements in a pair
				- generates a set of precandidates (the lowest level is ev, value is ev's semantic type)
			for all pairs of precandidates
				- according to semantic-net, generates candidates
	- hence, an abstract can have many sentences having candidates, a sentence can have multiple candidates

Get Sentences(candidates)
- instances of the same sentence will all write out parsing result (needs fixing)
- metamap and stanford parser's tokenization results might be different, those two methods have been coordinated, generates consistent sentence parsing results and instances

Write Features
- pad paddings if less than two words are before or after first or last entities, for chunk-level always keep 5 chunks, for phrase-level, padding is a new phrase, consecutive paddings consist of a single phrase, for word-level a padding is a new word, see this example

<sentence>
<stanford-parser-result>
[Aspirin-1, treats-2, heart-3, disease-4, .-5]
[0-7, 8-14, 15-20, 21-28, 28-29]
[NNP-0, VBZ-1, NN-2, NN-3, .-4]
[nsubj(treats-2, Aspirin-1), root(ROOT-0, treats-2), compound(disease-4, heart-3), dobj(treats-2, disease-4)]
</stanford-parser-result>
<phrase-chunking-result>
[Aspirin-NNP-0] [treats-VBZ-1] [heart-NN-2,disease-NN-3] [.-.-4]
</phrase-chunking-result>
<instance>
index: 1
cui1: C0004057
cui1-type: PHOB
cui1-matched-words: Aspirin
cui1-global-phrase-index: 0
cui2: C0018799
cui2-type: PHPR
cui2-matched-words: heart disease
cui2-global-phrase-index: 2
positivity: false
inverse: false
net-relation: treats
meta-relation: null
number-of-sentence-level-features: 9
number-of-chunks: 5
number-of-phrases: 6
number-of-words: 7
sentence: Aspirin treats heart disease.
<sentence-level-features>
feature: inv_f|PHOB|treats|PHPR
feature: inv_f|#PAD#|PHOB|treats|PHPR|.
feature: inv_f|#PAD# #PAD#|PHOB|treats|PHPR|. #PAD#
feature: inv_f|PHOB|VBZ|PHPR
feature: inv_f|#PAD#|PHOB|VBZ|PHPR|.
feature: inv_f|#PAD# #PAD#|PHOB|VBZ|PHPR|. #PAD#
feature: str:PHOB|[nsubj]<-treats[dobj]->|PHPR
feature: dep:PHOB|[nsubj]<-[dobj]->|PHPR
feature: dir:PHOB|<-->|PHPR
bag-of-words-features:
bow-word-feature:
#PAD# #PAD# treats . #PAD# 
bow-tag-feature:
0 0 32 39 0 
bow-dep-feature:
37 23 
bow-dep-word-feature:
treats 
</sentence-level-features>
<chunk-level-features>
word-features:
inv_f|#PAD# #PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|. #PAD#
tag-features:
inv_f|#PAD# #PAD#
bow: 0 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|. #PAD#
bow: 39 0 
</chunk-level-features>
<phrase-level-features>
word-features:
inv_f|#PAD# #PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|.
inv_f|#PAD#
tag-features:
inv_f|#PAD# #PAD#
bow: 0 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|.
bow: 39 
inv_f|#PAD#
bow: 0 
</phrase-level-features>
<word-level-features>
word-features:
inv_f|#PAD#
inv_f|#PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|.
inv_f|#PAD#
tag-features:
inv_f|#PAD#
bow: 0 
inv_f|#PAD#
bow: 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|.
bow: 39 
inv_f|#PAD#
bow: 0 
</word-level-features>
</instance>
</sentence>
<sentence>
<stanford-parser-result>
[Aspirin-1, treats-2, heart-3, disease-4, .-5]
[0-7, 8-14, 15-20, 21-28, 28-29]
[NNP-0, VBZ-1, NN-2, NN-3, .-4]
[nsubj(treats-2, Aspirin-1), root(ROOT-0, treats-2), compound(disease-4, heart-3), dobj(treats-2, disease-4)]
</stanford-parser-result>
<phrase-chunking-result>
[Aspirin-NNP-0] [treats-VBZ-1] [heart-NN-2,disease-NN-3] [.-.-4]
</phrase-chunking-result>
<instance>
index: 2
cui1: C0004057
cui1-type: PHOB
cui1-matched-words: Aspirin
cui1-global-phrase-index: 0
cui2: C0018799
cui2-type: PHPR
cui2-matched-words: heart disease
cui2-global-phrase-index: 2
positivity: false
inverse: false
net-relation: prevents
meta-relation: null
number-of-sentence-level-features: 9
number-of-chunks: 5
number-of-phrases: 6
number-of-words: 7
sentence: Aspirin treats heart disease.
<sentence-level-features>
feature: inv_f|PHOB|treats|PHPR
feature: inv_f|#PAD#|PHOB|treats|PHPR|.
feature: inv_f|#PAD# #PAD#|PHOB|treats|PHPR|. #PAD#
feature: inv_f|PHOB|VBZ|PHPR
feature: inv_f|#PAD#|PHOB|VBZ|PHPR|.
feature: inv_f|#PAD# #PAD#|PHOB|VBZ|PHPR|. #PAD#
feature: str:PHOB|[nsubj]<-treats[dobj]->|PHPR
feature: dep:PHOB|[nsubj]<-[dobj]->|PHPR
feature: dir:PHOB|<-->|PHPR
bag-of-words-features:
bow-word-feature:
#PAD# #PAD# treats . #PAD# 
bow-tag-feature:
0 0 32 39 0 
bow-dep-feature:
37 23 
bow-dep-word-feature:
treats 
</sentence-level-features>
<chunk-level-features>
word-features:
inv_f|#PAD# #PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|. #PAD#
tag-features:
inv_f|#PAD# #PAD#
bow: 0 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|. #PAD#
bow: 39 0 
</chunk-level-features>
<phrase-level-features>
word-features:
inv_f|#PAD# #PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|.
inv_f|#PAD#
tag-features:
inv_f|#PAD# #PAD#
bow: 0 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|.
bow: 39 
inv_f|#PAD#
bow: 0 
</phrase-level-features>
<word-level-features>
word-features:
inv_f|#PAD#
inv_f|#PAD#
inv_f|PHOB
inv_f|treats
inv_f|PHPR
inv_f|.
inv_f|#PAD#
tag-features:
inv_f|#PAD#
bow: 0 
inv_f|#PAD#
bow: 0 
inv_f|PHOB
bow: 
inv_f|VBZ
bow: 32 
inv_f|PHPR
bow: 
inv_f|.
bow: 39 
inv_f|#PAD#
bow: 0 
</word-level-features>
</instance>
</sentence> 